package com.advancedjava.references;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

/**
 * 虚引用(Phantom Reference)示例
 * 
 * 虚引用的特点：
 * 1. 虚引用是Java中最弱的引用类型
 * 2. 虚引用不能通过get()方法获取对象，总是返回null
 * 3. 虚引用必须与ReferenceQueue一起使用
 * 4. 当对象被垃圾回收时，虚引用会被加入到ReferenceQueue中
 * 5. 虚引用主要用于跟踪对象的垃圾回收状态，以及在对象被回收前执行一些清理操作
 */
public class PhantomReferenceDemo {
    
    public static void main(String[] args) {
        // 创建引用队列
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        
        // 创建一个对象
        Object obj = new Object();
        System.out.println("创建对象: " + obj);
        
        // 创建虚引用指向该对象，并关联引用队列
        PhantomReference<Object> phantomRef = new PhantomReference<>(obj, queue);
        System.out.println("创建虚引用: " + phantomRef);
        
        // 尝试通过虚引用获取对象（总是返回null）
        Object retrievedObj = phantomRef.get();
        System.out.println("通过虚引用获取对象: " + retrievedObj);
        
        // 断开强引用
        obj = null;
        System.out.println("断开强引用后，obj = " + obj);
        
        // 提示垃圾回收
        System.gc();
        System.out.println("执行垃圾回收");
        
        // 检查引用队列中是否有虚引用
        Reference<?> refFromQueue = queue.poll();
        if (refFromQueue != null) {
            System.out.println("引用队列中获取到虚引用: " + refFromQueue);
            // 执行清理操作
            System.out.println("执行清理操作");
        } else {
            System.out.println("引用队列中没有虚引用");
        }
        
        // 再次尝试通过虚引用获取对象
        retrievedObj = phantomRef.get();
        System.out.println("垃圾回收后通过虚引用获取对象: " + retrievedObj);
        
        // 演示虚引用的清理机制
        demonstrateCleanupMechanism();
    }
    
    /**
     * 演示虚引用的清理机制
     * 虚引用可以用于在对象被回收前执行一些清理操作
     */
    public static void demonstrateCleanupMechanism() {
        // 创建引用队列
        ReferenceQueue<Resource> queue = new ReferenceQueue<>();
        
        // 创建资源对象
        Resource resource = new Resource();
        System.out.println("创建资源对象: " + resource);
        
        // 创建虚引用指向资源对象
        PhantomReference<Resource> phantomRef = new PhantomReference<>(resource, queue);
        
        // 断开强引用
        resource = null;
        System.out.println("断开资源对象的强引用");
        
        // 提示垃圾回收
        System.gc();
        System.out.println("执行垃圾回收");
        
        // 检查引用队列
        Reference<? extends Resource> ref;
        while ((ref = queue.poll()) != null) {
            System.out.println("从引用队列中获取到虚引用: " + ref);
            // 执行清理操作
            System.out.println("执行资源清理操作");
            // 可以在这里释放一些非堆内存资源，如文件句柄、网络连接等
        }
    }
    
    /**
     * 模拟需要清理的资源类
     */
    static class Resource {
        // 模拟资源
        private byte[] data = new byte[1024];
        
        @Override
        protected void finalize() throws Throwable {
            try {
                System.out.println("Resource.finalize() 被调用");
                // 传统的finalize方法也可以用于清理资源，但虚引用提供了更灵活的清理机制
            } finally {
                super.finalize();
            }
        }
    }
}