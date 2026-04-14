# Java引用类型详解：从源码到实战

## 目录

1. [引言](#引言)
2. [先搞清楚一个前提：可达性分析](#先搞清楚一个前提：可达性分析)
3. [强引用（Strong Reference）](#强引用（Strong-Reference）)
4. [软引用（Soft Reference）](#软引用（Soft-Reference）)
   - [基本用法](#基本用法)
   - [源码级分析：MyBatis的SoftCache](#源码级分析：MyBatis的SoftCache)
   - [实际开发：用软引用做本地缓存](#实际开发：用软引用做本地缓存)
5. [弱引用（Weak Reference）](#弱引用（Weak-Reference）)
   - [基本用法](#基本用法-1)
   - [源码级分析：ThreadLocal为什么用弱引用](#源码级分析：ThreadLocal为什么用弱引用)
   - [源码级分析：WeakHashMap的工作原理](#源码级分析：WeakHashMap的工作原理)
   - [实际开发：用WeakHashMap避免监听器泄漏](#实际开发：用WeakHashMap避免监听器泄漏)
   - [实际开发：Tomcat的ConcurrentCache](#实际开发：Tomcat的ConcurrentCache)
6. [虚引用（Phantom Reference）](#虚引用（Phantom-Reference）)
   - [基本用法](#基本用法-2)
   - [虚引用 vs finalize()：为什么要用虚引用](#虚引用-vs-finalize()：为什么要用虚引用)
   - [源码级分析：JDK的Cleaner机制](#源码级分析：JDK的Cleaner机制)
   - [源码级分析：DirectByteBuffer如何释放堆外内存](#源码级分析：DirectByteBuffer如何释放堆外内存)
   - [实际开发：用虚引用监控资源泄漏](#实际开发：用虚引用监控资源泄漏)
7. [ReferenceQueue：引用回收的通知机制](#ReferenceQueue：引用回收的通知机制)
   - [核心机制](#核心机制)
   - [源码级分析：ReferenceHandler线程](#源码级分析：ReferenceHandler线程)
   - [实际开发：用ReferenceQueue实现自动清理的缓存](#实际开发：用ReferenceQueue实现自动清理的缓存)
8. [一张图总结](#一张图总结)
9. [常见问题与解答](#常见问题与解答)
   - [Q1：ThreadLocal用弱引用为什么还会内存泄漏？](#Q1：ThreadLocal用弱引用为什么还会内存泄漏？)
   - [Q2：软引用和弱引用做缓存有什么区别？](#Q2：软引用和弱引用做缓存有什么区别？)
   - [Q3：虚引用的get()为什么永远返回null？](#Q3：虚引用的get()为什么永远返回null？)
   - [Q4：ReferenceQueue的poll和remove有什么区别？](#Q4：ReferenceQueue的poll和remove有什么区别？)
   - [Q5：什么时候该用WeakHashMap？](#Q5：什么时候该用WeakHashMap？)

## 引言

提到Java的引用类型，很多人脑子里只有一句"强软弱虚"，但真到面试或者写代码的时候，往往一问就懵。这篇文章我打算换个思路——不搞概念罗列，直接从JDK源码和实际开发场景出发，把每种引用类型到底解决了什么问题、怎么用的，掰开了讲清楚。

## 先搞清楚一个前提：可达性分析

在讲引用类型之前，得先明白JVM是怎么判断一个对象该不该回收的。JVM使用的是**可达性分析算法**：从GC Roots出发，沿着引用链往下找，如果某个对象到GC Roots没有任何引用链可达，那这个对象就是可回收的。

四种引用类型的区别，本质上就是：**GC Roots到对象的这条引用链，有多"硬"**。

- 强引用：铁链子，GC死也不断
- 软引用：橡皮筋，内存不够就断
- 弱引用：纸糊的，GC一来就断
- 虚引用：根本不算链子，只是个事后通知

## 强引用（Strong Reference）

强引用没什么好说的，就是平时写的 `Object obj = new Object()`。只要引用还在，GC宁可抛OOM也不会回收这个对象。

但强引用有个容易踩的坑——**长生命周期对象持有短生命周期对象的引用**。比如：

```java
public class UserController {
    // 这是一个长生命周期的缓存，value是强引用
    private static final Map<Long, User> userCache = new HashMap<>();
    
    public User getUser(Long userId) {
        return userCache.computeIfAbsent(userId, id -> loadFromDb(id));
    }
}
```

这个 `userCache` 是 static 的，生命周期跟类一样长。放进去的 User 对象永远不会被回收，哪怕已经没有任何业务代码在使用它了。这就是典型的**内存泄漏**——对象已经没用了，但因为强引用还在，GC回收不了。

## 软引用（Soft Reference）

### 基本用法

软引用通过 `java.lang.ref.SoftReference` 实现，核心逻辑就一句话：**内存不够了才回收**。

```java
Object obj = new Object();
SoftReference<Object> softRef = new SoftReference<>(obj);

// 断开强引用后，对象只被软引用指向
obj = null;

// 内存充足时，get()能拿到对象
System.out.println(softRef.get()); // java.lang.Object@xxx

// 内存不足时，GC会回收这个对象，get()返回null
```

### 源码级分析：MyBatis的SoftCache

MyBatis中有一个 `SoftCache`，就是用软引用实现的缓存。来看它的核心源码（`org.apache.ibatis.cache.impl.SoftCache`）：

```java
public class SoftCache implements Cache {
    // 引用队列，被GC回收的SoftEntry会被放入这个队列
    private final ReferenceQueue<Object> queueOfGarbageCollectedEntries;
    // 实际存储缓存项的LinkedList
    private final LinkedList<Object> hardLinksToAvoidGarbageCollection;
    // 底层委托的缓存
    private final Cache delegate;
    // 强引用的数量上限
    private int numberOfHardLinks;

    public SoftCache(Cache delegate) {
        this.delegate = delegate;
        this.numberOfHardLinks = 256;
        this.hardLinksToAvoidGarbageCollection = new LinkedList<>();
        this.queueOfGarbageCollectedEntries = new ReferenceQueue<>();
    }

    @Override
    public void putObject(Object key, Object value) {
        // 清理已经被GC回收的缓存项
        removeGarbageCollectedItems();
        // 用SoftEntry包装value，SoftEntry继承SoftReference
        delegate.putObject(key, new SoftEntry(key, value, queueOfGarbageCollectedEntries));
    }

    @Override
    public Object getObject(Object key) {
        Object result = null;
        SoftReference<Object> softReference = (SoftReference<Object>) delegate.getObject(key);
        if (softReference != null) {
            result = softReference.get();
            if (result == null) {
                // 软引用被回收了，从缓存中移除这个key
                delegate.removeObject(key);
            } else {
                // 命中了，把对象加入强引用列表，防止被GC回收
                // 这就是hardLinksToAvoidGarbageCollection的作用——最近访问的数据用强引用兜底
                synchronized (hardLinksToAvoidGarbageCollection) {
                    hardLinksToAvoidGarbageCollection.addFirst(result);
                    if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
                        hardLinksToAvoidGarbageCollection.removeLast();
                    }
                }
            }
        }
        return result;
    }

    // 从ReferenceQueue中取出被GC回收的SoftEntry，清理对应的缓存项
    private void removeGarbageCollectedItems() {
        SoftEntry sv;
        while ((sv = (SoftEntry) queueOfGarbageCollectedEntries.poll()) != null) {
            delegate.removeObject(sv.key);
        }
    }

    // SoftEntry是SoftReference的子类，额外保存了key信息
    private static final class SoftEntry extends SoftReference<Object> {
        private final Object key;
        SoftEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
            super(value, garbageCollectionQueue);
            this.key = key;
        }
    }
}
```

这段源码有几个非常值得学习的点：

1. **SoftEntry继承SoftReference，额外保存了key**——这样当value被GC回收后，通过ReferenceQueue拿到SoftEntry时，还能知道是哪个key对应的value被回收了，从而从缓存中移除整个entry
2. **hardLinksToAvoidGarbageCollection**——最近访问的256个对象用强引用兜底，避免频繁被GC回收导致缓存命中率下降。这是一个很巧妙的"强引用+软引用"混合策略
3. **removeGarbageCollectedItems()**——每次put和get时都会清理已被GC回收的缓存项，保证缓存不会积累无效entry

### 实际开发：用软引用做本地缓存

```java
public class LocalCache<V> {
    private final Map<String, SoftReference<V>> cache = new ConcurrentHashMap<>();
    private final ReferenceQueue<V> refQueue = new ReferenceQueue<>();

    public void put(String key, V value) {
        cleanUp();
        cache.put(key, new SoftReference<>(value, refQueue));
    }

    public V get(String key) {
        cleanUp();
        SoftReference<V> ref = cache.get(key);
        return ref == null ? null : ref.get();
    }

    // 关键：利用ReferenceQueue清理已被GC回收的无效entry
    // 如果不做这一步，cache本身会越来越大（key还在，只是value变成null了）
    private void cleanUp() {
        Reference<? extends V> ref;
        while ((ref = refQueue.poll()) != null) {
            // 注意：这里有个问题——从ReferenceQueue拿到的ref，我们无法知道它对应的key是什么
            // 所以实际使用中，需要像MyBatis那样自定义SoftReference子类来保存key
            // 这里只是演示思路
        }
    }
}
```

> **注意**：上面这个简化版有个坑——从ReferenceQueue拿到的Reference对象，你不知道它对应cache里的哪个key。所以实际使用中，一定要像MyBatis那样自定义一个子类，把key信息保存下来。

## 弱引用（Weak Reference）

### 基本用法

弱引用通过 `java.lang.ref.WeakReference` 实现，核心逻辑：**GC一来就回收，不管内存够不够**。

```java
Object obj = new Object();
WeakReference<Object> weakRef = new WeakReference<>(obj);

obj = null; // 断开强引用
System.gc(); // GC后，weakRef.get()返回null
```

### 源码级分析：ThreadLocal为什么用弱引用

这是面试高频题。先看ThreadLocal的内部结构：

```
Thread
  └── ThreadLocalMap (每个线程一个)
        └── Entry[] table
              └── Entry extends WeakReference<ThreadLocal<?>>
                    ├── referent: ThreadLocal对象（弱引用）
                    └── value: 存储的值（强引用）
```

JDK源码（`java.lang.ThreadLocal.ThreadLocalMap.Entry`）：

```java
static class ThreadLocalMap {
    static class Entry extends WeakReference<ThreadLocal<?>> {
        Object value;
        Entry(ThreadLocal<?> k, Object v) {
            super(k);  // key是弱引用
            value = v;  // value是强引用！
        }
    }
}
```

**为什么要用弱引用？** 假设Entry的key是强引用：

```java
// 业务代码
ThreadLocal<User> tl = new ThreadLocal<>();
tl.set(currentUser);

// 业务用完了，把ThreadLocal变量置空
tl = null;

// 问题来了：如果Entry的key是强引用，那么ThreadLocal对象无法被回收
// 因为 Thread -> ThreadLocalMap -> Entry -> ThreadLocal 这条链路还在
// 这就导致ThreadLocal对象和它关联的value都泄漏了
```

用了弱引用后，当 `tl = null` 时，ThreadLocal对象只被Entry的弱引用指向，GC时会被回收。Entry的key变成null。

**但是！value还是强引用，还是会泄漏！** 所以ThreadLocalMap在每次get/set/remove时，都会主动清理key为null的entry：

```java
// ThreadLocalMap的expungeStaleEntry方法
private int expungeStaleEntry(int staleSlot) {
    Entry[] tab = table;
    int len = tab.length;
    
    // 清理当前slot
    tab[staleSlot].value = null;  // 把value也置空
    tab[staleSlot] = null;
    size--;
    
    // 顺便清理其他key为null的entry
    Entry e;
    int i;
    for (i = nextIndex(staleSlot, len); (e = tab[i]) != null; i = nextIndex(i, len)) {
        ThreadLocal<?> k = e.get();
        if (k == null) {
            e.value = null;  // key为null，value也置空
            tab[i] = null;
            size--;
        } else {
            // rehash
            int h = k.threadLocalHashCode & (len - 1);
            if (h != i) {
                tab[i] = null;
                while (tab[h] != null) h = nextIndex(h, len);
                tab[h] = e;
            }
        }
    }
    return i;
}
```

**总结**：ThreadLocal用弱引用只是减轻了泄漏的程度，并没有完全解决。真正避免泄漏的方式是**用完一定调remove()**。

### 源码级分析：WeakHashMap的工作原理

WeakHashMap的key是弱引用，来看它的核心实现：

```java
public class WeakHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    // Entry继承WeakReference
    private static class Entry<K, V> extends WeakReference<Object> implements Map.Entry<K, V> {
        V value;
        int hash;
        Entry<K, V> next;

        Entry(Object key, V value, ReferenceQueue<Object> queue, int hash, Entry<K, V> next) {
            super(key, queue);  // key是弱引用，关联到引用队列
            this.value = value;
            this.hash = hash;
            this.next = next;
        }
    }

    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    // 每次操作前都会清理被GC回收的entry
    private void expungeStaleEntries() {
        for (Object x; (x = queue.poll()) != null; ) {
            synchronized (queue) {
                Entry<K, V> e = (Entry<K, V>) x;
                int i = e.hash % table.length;
                // 从链表中移除这个entry
                Entry<K, V> prev = table[i];
                Entry<K, V> p = prev;
                while (p != null) {
                    Entry<K, V> next = p.next;
                    if (p == e) {
                        if (prev == e) table[i] = next;
                        else prev.next = next;
                        e.value = null; // Help GC
                        size--;
                        break;
                    }
                    prev = p;
                    p = next;
                }
            }
        }
    }
}
```

关键流程：
1. Entry的key用WeakReference包装，关联到ReferenceQueue
2. 当key被GC回收后，对应的Entry会被放入ReferenceQueue
3. 每次调用size()、getTable()、put()、get()等操作前，都会先调用expungeStaleEntries()清理无效entry

### 实际开发：用WeakHashMap避免监听器泄漏

```java
// 错误做法：用HashMap存储监听器，容易泄漏
public class EventBus {
    private Map<String, EventListener> listeners = new HashMap<>();
    
    public void register(String name, EventListener listener) {
        listeners.put(name, listener);
    }
    // 如果listener不再使用但忘记unregister，就泄漏了
}

// 正确做法：用WeakHashMap，当listener不再被外部引用时自动清理
public class EventBus {
    private Map<EventListener, Object> listeners = new WeakHashMap<>();
    
    public void register(EventListener listener) {
        listeners.put(listener, Boolean.TRUE);
    }
    // listener不再被外部强引用后，GC时自动从map中移除
}
```

### 实际开发：Tomcat的ConcurrentCache

Tomcat中有一个 `ConcurrentCache` 的实现，用WeakHashMap做年轻代缓存，HashMap做老年代缓存：

```java
public final class ConcurrentCache<K, V> {
    // 年轻代：用WeakHashMap，GC时自动回收不常用的
    private final Map<K, V> eden;
    // 老年代：用HashMap，长期持有
    private final Map<K, V> longterm;

    public ConcurrentCache(int size) {
        this.eden = new ConcurrentHashMap<>(size);
        this.longterm = new WeakHashMap<>(size);
    }

    public V get(K k) {
        V v = eden.get(k);
        if (v == null) {
            synchronized (longterm) {
                v = longterm.get(k);
            }
            if (v != null) {
                eden.put(k, v);  // 从老年代提升到年轻代
            }
        }
        return v;
    }

    public void put(K k, V v) {
        if (eden.size() >= size) {
            // 年轻代满了，把所有数据转移到老年代（WeakHashMap）
            synchronized (longterm) {
                longterm.putAll(eden);
            }
            eden.clear();
        }
        eden.put(k, v);
    }
}
```

这个设计很巧妙：eden用ConcurrentHashMap保证并发安全，longterm用WeakHashMap让不常用的数据可以被GC回收。当eden满了就把数据转移到longterm，新数据进eden。访问longterm中的数据时，会把它提升回eden。

## 虚引用（Phantom Reference）

### 基本用法

虚引用是最"虚"的引用，`get()` 永远返回null，必须配合ReferenceQueue使用。它的唯一作用是：**对象被回收后，收到一个通知**。

```java
Object obj = new Object();
ReferenceQueue<Object> queue = new ReferenceQueue<>();
PhantomReference<Object> phantomRef = new PhantomReference<>(obj, queue);

System.out.println(phantomRef.get()); // 永远是null

obj = null;
System.gc();

// GC后，phantomRef会被放入queue
Reference<?> ref = queue.poll();
if (ref != null) {
    System.out.println("对象已被回收，可以执行清理操作");
}
```

### 虚引用 vs finalize()：为什么要用虚引用

很多人会问，finalize()也能在对象被回收前做清理，为什么要用虚引用？

| 对比项 | finalize() | PhantomReference |
|--------|-----------|-----------------|
| 是否能获取对象 | 能（this就是） | 不能（get()返回null） |
| 对象复活 | 可能（finalize中把this赋给静态变量） | 不可能 |
| 执行时机 | 不确定，可能延迟很久 | 对象被回收后，更及时 |
| 性能影响 | 大（GC需要额外处理finalize链） | 小 |
| 是否推荐使用 | 已废弃（Java 9标记@Deprecated） | 推荐 |

finalize()最大的问题是**对象复活**——在finalize()中可以把this赋给一个静态变量，导致对象"死而复生"，这让GC的行为变得不可预测。虚引用彻底杜绝了这种可能，因为get()永远返回null，你根本拿不到对象。

### 源码级分析：JDK的Cleaner机制

JDK 9引入的 `java.lang.ref.Cleaner` 是虚引用最经典的应用。来看它的核心源码：

```java
public final class Cleaner implements java.lang.Runnable {
    // Cleaner内部维护了一个双向链表，所有Cleaner实例都串在一起
    private static Cleaner first = null;
    private Cleaner next = null;
    private Cleaner prev = null;

    // 虚引用 + 引用队列
    private final PhantomReference<Object> phantomReference;
    private final Runnable thunk;  // 清理动作

    private Cleaner(Object referent, Runnable thunk) {
        super();
        this.thunk = thunk;
        // 创建虚引用，关联到自身（Cleaner本身继承PhantomReference）
        // 注意：Cleaner继承了PhantomReference
        // new Cleaner()时，referent被PhantomReference引用
        // 当referent被GC回收后，这个Cleaner会被加入ReferenceQueue
    }

    public static Cleaner create(Object referent, Runnable thunk) {
        if (thunk == null) return null;
        Cleaner cleaner = new Cleaner(referent, thunk);
        // 加入双向链表
        add(cleaner);
        return cleaner;
    }

    @Override
    public void run() {
        // 从链表中移除自己
        remove(this);
        // 执行清理动作
        thunk.run();
    }
}
```

**Cleaner的工作流程**：
1. 调用 `Cleaner.create(referent, thunk)` 注册一个清理动作
2. 当referent被GC回收后，Cleaner（作为PhantomReference）被加入ReferenceQueue
3. ReferenceHandler线程从ReferenceQueue中取出Cleaner，调用其run()方法
4. run()方法执行thunk（即我们注册的清理动作）

### 源码级分析：DirectByteBuffer如何释放堆外内存

这是虚引用最硬核的应用场景。`DirectByteBuffer`分配的是堆外内存，不受GC管理，必须手动释放。JDK是怎么做的呢？

```java
// JDK源码：java.nio.DirectByteBuffer
class DirectByteBuffer extends MappedByteBuffer implements DirectBuffer {
    // Cleaner就是用来释放堆外内存的
    private final Cleaner cleaner;

    DirectByteBuffer(int cap) {
        // ...
        // 分配堆外内存
        base = UNSAFE.allocateMemory(size);
        // 创建Cleaner，当this被GC回收后，执行Deallocator.run()释放堆外内存
        cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
    }

    public Cleaner cleaner() { return cleaner; }

    // Deallocator就是清理动作
    private static class Deallocator implements Runnable {
        private long address;
        private long size;
        private int cap;

        public void run() {
            if (address == 0) return;
            // 释放堆外内存
            UNSAFE.freeMemory(address);
            address = 0;
        }
    }
}
```

整个流程：
1. 创建DirectByteBuffer时，通过UNSAFE分配堆外内存，同时注册一个Cleaner
2. 当DirectByteBuffer对象在堆上被GC回收后，Cleaner被触发
3. Cleaner调用Deallocator.run()，通过UNSAFE.freeMemory释放堆外内存

**这就是为什么DirectByteBuffer不需要我们手动free的原因**——虚引用+Cleaner帮我们做了。

### 实际开发：用虚引用监控资源泄漏

```java
public class ResourceLeakDetector {
    private final ReferenceQueue<Object> leakQueue = new ReferenceQueue<>();
    private final Map<PhantomReference<Object>, String> resourceMap = new ConcurrentHashMap<>();
    private final Thread detectorThread;

    public ResourceLeakDetector() {
        // 后台线程持续监控
        detectorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Reference<? extends Object> ref = leakQueue.remove(1000);
                    if (ref != null) {
                        String resourceInfo = resourceMap.remove(ref);
                        // 如果到这里，说明对象被GC回收了，但可能没有正确关闭资源
                        System.err.println("可能的资源泄漏: " + resourceInfo);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "ResourceLeakDetector");
        detectorThread.setDaemon(true);
        detectorThread.start();
    }

    // 注册需要监控的资源
    public void track(Object resource, String description) {
        PhantomReference<Object> ref = new PhantomReference<>(resource, leakQueue);
        resourceMap.put(ref, description + " [" + Thread.currentThread().getStackTrace() + "]");
    }
}

// 使用示例
public class FileProcessor {
    private static final ResourceLeakDetector leakDetector = new ResourceLeakDetector();

    public void process(String path) {
        InputStream is = new FileInputStream(path);
        // 注册到泄漏检测器，如果is被GC回收时没有close，就会报警
        leakDetector.track(is, "未关闭的文件流: " + path);
        // ... 处理逻辑
        is.close(); // 正确关闭后，就不会被检测为泄漏
    }
}
```

Netty中就有一个类似的 `ResourceLeakDetector`，用来检测ByteBuf是否被正确释放，原理一模一样。

## ReferenceQueue：引用回收的通知机制

### 核心机制

ReferenceQueue的核心就三个方法：

```java
// 非阻塞地取出一个Reference，没有就返回null
Reference<? extends T> poll();

// 阻塞地取出一个Reference
Reference<? extends T> remove() throws InterruptedException;

// 带超时的阻塞取出
Reference<? extends T> remove(long timeout) throws InterruptedException, IllegalArgumentException;
```

**什么时候Reference会被放入Queue？** 当一个SoftReference/WeakReference/PhantomReference指向的对象被GC回收后，JVM会自动把这个Reference对象放入它关联的ReferenceQueue中。

注意时序：
- **SoftReference/WeakReference**：对象被GC回收**之前**，Reference被放入Queue（此时get()可能还能返回非null）
- **PhantomReference**：对象被GC回收**之后**，Reference被放入Queue（此时get()一定是null）

### 源码级分析：ReferenceHandler线程

JDK内部有一个ReferenceHandler线程，负责处理被GC回收的引用：

```java
// java.lang.Reference.ReferenceHandler
private static class ReferenceHandler extends Thread {
    public void run() {
        while (true) {
            try {
                Reference<Object> ref;
                synchronized (lock) {
                    // pending是JVM设置的，当对象被GC回收后，对应的Reference会被加入pending链表
                    if ((ref = pending) != null) {
                        pending = ref.discovered;
                    } else {
                        // 等待JVM通知
                        lock.wait();
                        continue;
                    }
                }
                // 将Reference加入ReferenceQueue
                ReferenceQueue<Object> q = ref.queue;
                if (q == ReferenceQueue.NULL) continue;
                if (q != null) {
                    q.enqueue(ref);
                }
                // 如果是Cleaner，直接调用clean()
                if (ref instanceof Cleaner) {
                    ((Cleaner) ref).clean();
                }
            } catch (Throwable t) {
                // ...
            }
        }
    }
}
```

**关键流程**：
1. JVM在GC时，把被回收的Reference加入pending链表
2. ReferenceHandler线程从pending链表取出Reference
3. 如果Reference关联了ReferenceQueue，就把它入队
4. 如果是Cleaner类型，直接调用clean()方法

### 实际开发：用ReferenceQueue实现自动清理的缓存

```java
public class AutoCleanupCache<K, V> {
    private final ConcurrentHashMap<K, SoftEntry<K, V>> cache = new ConcurrentHashMap<>();
    private final ReferenceQueue<V> refQueue = new ReferenceQueue<>();

    // 自定义SoftReference子类，保存key，这样从ReferenceQueue取出时能知道对应的key
    private static class SoftEntry<K, V> extends SoftReference<V> {
        final K key;

        SoftEntry(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this.key = key;
        }
    }

    public void put(K key, V value) {
        cleanUp();
        cache.put(key, new SoftEntry<>(key, value, refQueue));
    }

    public V get(K key) {
        cleanUp();
        SoftEntry<K, V> entry = cache.get(key);
        return entry == null ? null : entry.get();
    }

    // 核心清理逻辑：从ReferenceQueue取出被GC回收的entry，移除对应的key
    private void cleanUp() {
        SoftEntry<K, V> entry;
        while ((entry = (SoftEntry<K, V>) refQueue.poll()) != null) {
            cache.remove(entry.key);
        }
    }
}
```

这个模式在Guava Cache、MyBatis SoftCache中都有用到，是生产级别的缓存实现思路。

## 一张图总结

```
引用强度：  强引用 > 软引用 > 弱引用 > 虚引用
回收时机：  不回收   内存不足时  GC时立即  GC后通知
get()：    正常获取  能获取     能获取    返回null
用途：      日常开发  缓存      避免泄漏  资源清理

JDK/框架应用：
  强引用 → HashMap（日常开发）
  软引用 → MyBatis SoftCache、Guava Cache.softValues()
  弱引用 → ThreadLocal.Entry、WeakHashMap、Tomcat ConcurrentCache
  虚引用 → Cleaner、DirectByteBuffer堆外内存释放、Netty ResourceLeakDetector
```

## 常见问题与解答

### Q1：ThreadLocal用弱引用为什么还会内存泄漏？

**A**：Entry的key是弱引用，value是强引用。key被GC回收后变成null，但value还在。ThreadLocalMap在get/set/remove时会顺带清理key为null的entry，但如果你再也没调用过这些方法，value就泄漏了。所以**用完ThreadLocal一定要调remove()**。

### Q2：软引用和弱引用做缓存有什么区别？

**A**：
- 软引用缓存：内存不够了才清，适合"宁可慢点也不能丢数据"的场景，比如MyBatis的查询缓存
- 弱引用缓存：GC一来就清，适合"丢了就重新加载"的场景，比如Tomcat ConcurrentCache的年轻代

### Q3：虚引用的get()为什么永远返回null？

**A**：这是设计上的安全考虑。如果虚引用能获取到对象，那你就可以在对象"应该被回收"的时候把它复活，这违背了GC的可达性分析结果。虚引用的设计目标就是"事后通知"，不是"临终抢救"。

### Q4：ReferenceQueue的poll和remove有什么区别？

**A**：
- `poll()`：非阻塞，没有就返回null
- `remove()`：阻塞，会一直等到有Reference入队
- `remove(long timeout)`：带超时的阻塞

实际开发中，poll()用得更多，一般在每次缓存操作前调用cleanUp()。remove()一般用在专门的后台监控线程中。

### Q5：什么时候该用WeakHashMap？

**A**：当你需要一个Map，且希望"key不再被外部使用时，entry自动移除"的时候。典型场景：
- 监听器/回调注册表
- 对象到元数据的映射（如对象→配置信息）
- 缓存（配合Tomcat ConcurrentCache的分代策略）

注意：WeakHashMap不是线程安全的，多线程环境下要用Collections.synchronizedMap包装，或者自己实现ConcurrentWeakHashMap。
