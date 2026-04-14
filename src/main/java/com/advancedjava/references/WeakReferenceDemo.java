package com.advancedjava.references;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * 弱引用(Weak Reference)示例
 * 
 * 弱引用的特点：
 * 1. 当垃圾回收器运行时，无论内存是否充足，都会回收被弱引用指向的对象
 * 2. 弱引用的生命周期比软引用更短
 * 3. 弱引用通常用于实现缓存，当对象不再被强引用指向时，自动从缓存中移除
 */
public class WeakReferenceDemo {
    
    public static void main(String[] args) {
        // 创建一个对象
        Object obj = new Object();
        System.out.println("创建对象: " + obj);
        
        // 创建弱引用指向该对象
        WeakReference<Object> weakRef = new WeakReference<>(obj);
        System.out.println("创建弱引用: " + weakRef);
        
        // 断开强引用
        obj = null;
        System.out.println("断开强引用后，obj = " + obj);
        
        // 通过弱引用获取对象
        Object retrievedObj = weakRef.get();
        System.out.println("通过弱引用获取对象: " + retrievedObj);
        
        // 提示垃圾回收
        System.gc();
        System.out.println("执行垃圾回收");
        
        // 再次通过弱引用获取对象
        retrievedObj = weakRef.get();
        System.out.println("垃圾回收后通过弱引用获取对象: " + retrievedObj);
        
        // 演示WeakHashMap的使用
        demonstrateWeakHashMap();
    }
    
    /**
     * 演示WeakHashMap的使用
     * WeakHashMap的键是弱引用，当键不再被强引用指向时，会被自动从Map中移除
     */
    public static void demonstrateWeakHashMap() {
        // 创建WeakHashMap
        WeakHashMap<Key, String> map = new WeakHashMap<>();
        
        // 创建键对象
        Key key1 = new Key("key1");
        Key key2 = new Key("key2");
        
        // 添加键值对
        map.put(key1, "value1");
        map.put(key2, "value2");
        
        System.out.println("WeakHashMap初始状态: " + map);
        System.out.println("key1是否在map中: " + map.containsKey(key1));
        System.out.println("key2是否在map中: " + map.containsKey(key2));
        
        // 断开key1的强引用
        key1 = null;
        System.out.println("断开key1的强引用");
        
        // 提示垃圾回收
        System.gc();
        System.out.println("执行垃圾回收");
        
        // 检查map状态
        System.out.println("垃圾回收后WeakHashMap状态: " + map);
        System.out.println("key1是否在map中: " + map.containsKey(new Key("key1")));
        System.out.println("key2是否在map中: " + map.containsKey(key2));
    }
    
    /**
     * 用于WeakHashMap的键类
     */
    static class Key {
        private String name;
        
        public Key(String name) {
            this.name = name;
        }
        
        @Override
        public int hashCode() {
            return name.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Key key = (Key) obj;
            return name.equals(key.name);
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}