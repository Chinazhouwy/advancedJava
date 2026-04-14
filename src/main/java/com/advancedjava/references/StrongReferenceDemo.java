package com.advancedjava.references;

/**
 * 强引用(Strong Reference)示例
 * 
 * 强引用是最常见的引用类型，当一个对象被强引用指向时，垃圾回收器不会回收它，即使内存不足也不会。
 * 只有当强引用被显式置为null时，对象才可能被回收。
 */
public class StrongReferenceDemo {
    
    public static void main(String[] args) {
        // 创建一个强引用
        Object strongRef = new Object();
        System.out.println("创建强引用: " + strongRef);
        
        // 对象可以正常访问
        System.out.println("强引用指向的对象: " + strongRef);
        
        // 显式置为null，断开强引用
        strongRef = null;
        System.out.println("强引用置为null: " + strongRef);
        
        // 提示垃圾回收
        System.gc();
        System.out.println("执行垃圾回收");
        
        // 此时对象已经没有强引用指向，可能被垃圾回收
        System.out.println("对象可能已经被回收");
    }
    
    /**
     * 演示强引用导致内存溢出的情况
     * 注意：此方法会尝试分配大量内存，可能导致程序崩溃
     * 运行时需要设置JVM参数：-Xmx100m
     */
    public static void demonstrateOutOfMemory() {
        try {
            // 创建大量强引用对象，导致内存溢出
            int size = 1000000;
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = new byte[1024 * 1024]; // 每个对象1MB
            }
        } catch (OutOfMemoryError e) {
            System.out.println("发生内存溢出: " + e.getMessage());
        }
    }
}