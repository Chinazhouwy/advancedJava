/**
 * JSON 序列化面试示例包。
 *
 * <p>本包用 Jackson 与 Fastjson2 对比 record/传统 POJO 的序列化与反序列化行为，
 * 重点观察：
 * 1. record 是否能正确参与 JSON 映射。
 * 2. 旧字段兼容与忽略未知字段的策略。
 * 3. 不同库面对同一模型时的结果是否一致。
 */
package com.advancedjava.interview.serialization;
