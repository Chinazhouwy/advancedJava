package com.advancedjava.interview.records;

import java.util.Objects;

/**
 * 传统 class 版本的 InterviewResult，对照 record 版本使用。
 */
public final class InterviewResultClassic {

    private final CandidateProfileClassic candidate;
    private final boolean passed;
    private final String feedback;

    public InterviewResultClassic(CandidateProfileClassic candidate, boolean passed, String feedback) {
        this.candidate = Objects.requireNonNull(candidate, "candidate must not be null");
        this.passed = passed;
        this.feedback = feedback == null ? "" : feedback.trim();
    }

    public static InterviewResultClassic pass(CandidateProfileClassic candidate, String feedback) {
        return new InterviewResultClassic(candidate, true, feedback);
    }

    public static InterviewResultClassic fail(CandidateProfileClassic candidate, String feedback) {
        return new InterviewResultClassic(candidate, false, feedback);
    }

    public CandidateProfileClassic getCandidate() {
        return candidate;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getFeedback() {
        return feedback;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof InterviewResultClassic that)) {
            return false;
        }
        return passed == that.passed
                && Objects.equals(candidate, that.candidate)
                && Objects.equals(feedback, that.feedback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidate, passed, feedback);
    }

    @Override
    public String toString() {
        return "InterviewResultClassic{"
                + "candidate=" + candidate
                + ", passed=" + passed
                + ", feedback='" + feedback + '\''
                + '}';
    }
}
