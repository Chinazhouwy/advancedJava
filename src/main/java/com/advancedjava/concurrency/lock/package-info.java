/**
 * 显式锁与条件变量示例包。
 *
 * <p>这里主要使用 {@code ReentrantLock} 与 {@code Condition} 对比无锁并发下的问题，
 * 帮助理解：
 * 1. 共享状态在并发更新时为何会出错。
 * 2. 显式加锁如何建立互斥。
 * 3. 条件等待/通知如何实现更细粒度的线程协作。
 */
package com.advancedjava.concurrency.lock;
