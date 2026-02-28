package com.advancedjava.interview.frameworks.springdata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA 实体通常仍使用传统 class：
 * - 需要无参构造
 * - 可能依赖代理/延迟加载
 * 因此不建议直接把 @Entity 改成 record。
 */
@Entity
@Table(name = "interview_result")
public class InterviewResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String candidateName;

    @Column(nullable = false)
    private Integer score;

    private String interviewer;

    protected InterviewResultEntity() {
    }

    public InterviewResultEntity(String candidateName, Integer score, String interviewer) {
        this.candidateName = candidateName;
        this.score = score;
        this.interviewer = interviewer;
    }

    public Long getId() {
        return id;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }
}
