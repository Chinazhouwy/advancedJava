package com.advancedjava.references;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * 引用类型比较综合示例
 * 
 * 本示例演示了四种引用类型的行为差异：
 * 1. 强引用(Strong Reference)
 * 2. 软引用(Soft Reference)
 * 3. 弱引用(Weak Reference)
 * 4. 虚引用(Phantom Reference)
 */
public class ReferenceComparisonDemo {
    
    public static void main(String[] args) {
        // 创建引用队列，用于虚引用
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        
        // 创建一个对象
        Object obj = new Object();
        System.out.println("创建对象: " + obj);
        
        // 创建四种引用类型指向同一个对象
        Object strongRef = obj; // 强引用
        SoftReference<Object> softRef = new SoftReference<>(obj); // 软引用
        WeakReference<Object> weakRef = new WeakReference<>(obj); // 弱引用
        PhantomReference<Object> phantomRef = new PhantomReference<>(obj, queue); // 虚引用
        
        System.out.println("创建强引用: " + strongRef);
        System.out.println("创建软引用: " + softRef);
        System.out.println("创建弱引用: " + weakRef);
        System.out.println("创建虚引用: " + phantomRef);
        
        // 测试获取对象
        System.out.println("\n=== 初始状态 ===");
        System.out.println("通过强引用获取对象: " + strongRef);
        System.out.println("通过软引用获取对象: " + softRef.get());
        System.out.println("通过弱引用获取对象: " + weakRef.get());
        System.out.println("通过虚引用获取对象: " + phantomRef.get()); // 总是返回null
        
        // 断开强引用
        strongRef = null;
        System.out.println("\n=== 断开强引用后 ===");
        System.out.println("强引用: " + strongRef);
        System.out.println("通过软引用获取对象: " + softRef.get());
        System.out.println("通过弱引用获取对象: " + weakRef.get());
        System.out.println("通过虚引用获取对象: " + phantomRef.get());
        
        // 提示垃圾回收
        System.gc();
        System.out.println("\n=== 执行垃圾回收后 ===");
        System.out.println("通过软引用获取对象: " + softRef.get()); // 软引用在内存充足时不会被回收
        System.out.println("通过弱引用获取对象: " + weakRef.get()); // 弱引用会被回收
        System.out.println("通过虚引用获取对象: " + phantomRef.get());
        
        // 检查引用队列
        Reference<?> refFromQueue = queue.poll();
        if (refFromQueue != null) {
            System.out.println("引用队列中获取到虚引用: " + refFromQueue);
        } else {
            System.out.println("引用队列中没有虚引用");
        }
        
        // 尝试创建大对象，模拟内存不足
        System.out.println("\n=== 模拟内存不足 ===");
        try {
            // 尝试分配大量内存
            byte[][] array = new byte[10][1024 * 1024 * 5]; // 尝试分配50MB
            System.out.println("内存分配成功");
        } catch (OutOfMemoryError e) {
            System.out.println("发生内存溢出: " + e.getMessage());
        }
        
        // 再次检查引用状态
        System.out.println("\n=== 内存操作后 ===");
        System.out.println("通过软引用获取对象: " + softRef.get()); // 软引用在内存不足时可能被回收
        System.out.println("通过弱引用获取对象: " + weakRef.get());
        System.out.println("通过虚引用获取对象: " + phantomRef.get());
        
        // 再次检查引用队列
        refFromQueue = queue.poll();
        if (refFromQueue != null) {
            System.out.println("引用队列中获取到虚引用: " + refFromQueue);
        } else {
            System.out.println("引用队列中没有虚引用");
        }
        
        // 总结四种引用类型的特点
        System.out.println("\n=== 引用类型总结 ===");
        System.out.println("1. 强引用: 垃圾回收器不会回收，除非显式置为null");
        System.out.println("2. 软引用: 内存充足时不回收，内存不足时回收");
        System.out.println("3. 弱引用: 垃圾回收时立即回收");
        System.out.println("4. 虚引用: 不能获取对象，用于跟踪垃圾回收");
    }
}