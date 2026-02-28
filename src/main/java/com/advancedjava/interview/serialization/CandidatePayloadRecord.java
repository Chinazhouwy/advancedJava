package com.advancedjava.interview.serialization;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * record 版本：
 * 1. 演示 Jackson/Fastjson2 对 record 的序列化与反序列化
 * 2. 通过 alias 支持旧字段名（years/workYears）到新字段名（yearsOfExperience）的兼容
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CandidatePayloadRecord(
        String name,
        @JsonAlias({"years", "workYears"})
        @JSONField(name = "yearsOfExperience", alternateNames = {"years", "workYears"})
        int yearsOfExperience,
        List<String> skills
) {

    public CandidatePayloadRecord {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        name = name.trim();
        skills = skills == null ? List.of() : List.copyOf(skills);
    }
}
