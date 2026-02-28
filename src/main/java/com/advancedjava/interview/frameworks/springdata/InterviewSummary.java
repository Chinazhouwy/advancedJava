package com.advancedjava.interview.frameworks.springdata;

/**
 * JPA 查询投影 DTO 使用 record：
 * 只承载查询结果，不参与实体状态管理。
 */
public record InterviewSummary(
        Long id,
        String candidateName,
        Integer score
) {
}
