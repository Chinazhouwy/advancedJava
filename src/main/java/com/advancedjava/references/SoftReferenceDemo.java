package com.advancedjava.references;

import java.lang.ref.SoftReference;

/**
 * 软引用(Soft Reference)示例
 * 
 * 软引用的特点：
 * 1. 当内存充足时，垃圾回收器不会回收被软引用指向的对象
 * 2. 当内存不足时，垃圾回收器会回收被软引用指向的对象
 * 3. 软引用通常用于实现缓存，当内存充足时可以保持缓存，当内存不足时自动释放缓存
 */
public class SoftReferenceDemo {
    
    public static void main(String[] args) {
        // 创建一个对象
        Object obj = new Object();
        System.out.println("创建对象: " + obj);
        
        // 创建软引用指向该对象
        SoftReference<Object> softRef = new SoftReference<>(obj);
        System.out.println("创建软引用: " + softRef);
        
        // 断开强引用
        obj = null;
        System.out.println("断开强引用后，obj = " + obj);
        
        // 通过软引用获取对象
        Object retrievedObj = softRef.get();
        System.out.println("通过软引用获取对象: " + retrievedObj);
        
        // 提示垃圾回收
        System.gc();
        System.out.println("执行垃圾回收");
        
        // 再次通过软引用获取对象
        retrievedObj = softRef.get();
        System.out.println("垃圾回收后通过软引用获取对象: " + retrievedObj);
        
        // 内存不足时的行为演示
        demonstrateMemoryShortage();
    }
    
    /**
     * 演示内存不足时软引用的行为
     * 注意：此方法会尝试分配大量内存，可能导致程序崩溃
     * 运行时需要设置JVM参数：-Xmx50m
     */
    public static void demonstrateMemoryShortage() {
        // 创建一个大对象
        byte[] bigObject = new byte[1024 * 1024 * 10]; // 10MB
        SoftReference<byte[]> softRef = new SoftReference<>(bigObject);
        System.out.println("创建大对象并通过软引用指向: " + softRef.get());
        
        // 断开强引用
        bigObject = null;
        System.out.println("断开强引用后，bigObject = " + bigObject);
        
        // 尝试分配更多内存，导致内存不足
        try {
            System.out.println("尝试分配大量内存...");
            byte[][] array = new byte[10][1024 * 1024 * 4]; // 尝试分配40MB
            System.out.println("内存分配成功");
        } catch (OutOfMemoryError e) {
            System.out.println("发生内存溢出: " + e.getMessage());
        }
        
        // 检查软引用是否被回收
        byte[] retrieved = softRef.get();
        if (retrieved == null) {
            System.out.println("软引用指向的对象已被回收");
        } else {
            System.out.println("软引用指向的对象仍然存在: " + retrieved);
        }
    }
}