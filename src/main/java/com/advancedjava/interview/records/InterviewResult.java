package com.advancedjava.interview.records;

import java.util.Objects;

/**
 * record 组合 record 的示例。
 * CandidateProfile 是一个值对象，InterviewResult 再把它作为组件进行组合。
 */
public record InterviewResult(CandidateProfile candidate, boolean passed, String feedback) {

    public InterviewResult {
        // 传统 class 通常也要在构造器里做同样的非空校验
        candidate = Objects.requireNonNull(candidate, "candidate must not be null");
        // 统一将 null 反馈变成空串，减少调用方判空负担
        feedback = feedback == null ? "" : feedback.trim();
    }

    // 静态工厂：比 new InterviewResult(..., true, ...) 语义更清晰
    public static InterviewResult pass(CandidateProfile candidate, String feedback) {
        return new InterviewResult(candidate, true, feedback);
    }

    // 静态工厂：语义化地表达“未通过”
    public static InterviewResult fail(CandidateProfile candidate, String feedback) {
        return new InterviewResult(candidate, false, feedback);
    }
}
