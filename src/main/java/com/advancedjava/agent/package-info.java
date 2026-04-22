/**
 * Java Agent 示例包。
 *
 * <p>本包聚焦 Java Instrumentation 与 Javassist 的基础用法，演示：
 * 1. premain 方式在目标程序启动前织入字节码。
 * 2. agentmain 方式在目标程序运行中动态 attach 并增强类。
 * 3. 如何在方法前后插入日志，理解“方法增强”的最小闭环。
 */
package com.advancedjava.agent;
