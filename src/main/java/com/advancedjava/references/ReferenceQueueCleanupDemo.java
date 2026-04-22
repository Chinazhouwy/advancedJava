package com.advancedjava.references;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * ReferenceQueue + PhantomReference 资源清理教学示例。
 *
 * <p>目标：演示“对象即将被回收”时，如何通过 ReferenceQueue 收到通知，
 * 从而执行对象外部资源（例如 direct memory/文件句柄/本地资源）清理逻辑。
 */
public class ReferenceQueueCleanupDemo {

    /**
     * 模拟一个占用堆外资源的对象。
     */
    static class NativeLikeResource {
        private final String id;
        private final byte[] payload = new byte[1024 * 256];

        NativeLikeResource(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "NativeLikeResource{" + "id='" + id + '\'' + '}';
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("\n=== ReferenceQueue 清理机制 Demo ===");

        ReferenceQueue<NativeLikeResource> queue = new ReferenceQueue<>();
        List<PhantomReference<NativeLikeResource>> refs = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            NativeLikeResource resource = new NativeLikeResource("res-" + i);

            // PhantomReference.get() 永远返回 null，不能用于取对象。
            // 它只用于“回收通知 + 清理触发”。
            PhantomReference<NativeLikeResource> pr = new PhantomReference<>(resource, queue);
            refs.add(pr);

            // 置空强引用，允许 GC。
            resource = null;
        }

        // 尝试触发回收。
        System.gc();
        Thread.sleep(200);

        int cleaned = 0;
        Reference<? extends NativeLikeResource> ref;

        // poll(): 非阻塞获取队列里的引用对象。
        while ((ref = queue.poll()) != null) {
            cleaned++;
            // 这里是你真实项目里的“外部资源清理动作”。
            System.out.println("[cleanup] reference enqueued: " + ref);
            ref.clear();
        }

        System.out.println("cleanup count = " + cleaned);
        System.out.println("提示：GC 时机不保证，若 count 偏小可多次触发 System.gc() 再观察。");
        System.out.println("=== Demo 结束 ===\n");
    }
}
