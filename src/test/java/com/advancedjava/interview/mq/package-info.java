/**
 * MQ 可靠性面试示例的测试包。
 *
 * <p>用断言锁死三段演示的关键行为：ACK 丢失后的重投与幂等拦截、
 * 业务键哈希路由带来的队列内有序、事务消息的半可见性与回查补投。
 */
package com.advancedjava.interview.mq;
