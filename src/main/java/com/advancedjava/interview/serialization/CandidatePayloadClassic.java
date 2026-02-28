package com.advancedjava.interview.serialization;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;

/**
 * 传统 POJO 版本：用于和 record 做序列化写法对照。
 * 保留无参构造 + getter/setter，兼容老框架常见的 JavaBean 约束。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CandidatePayloadClassic {

    private String name;

    @JsonAlias({"years", "workYears"})
    @JSONField(name = "yearsOfExperience", alternateNames = {"years", "workYears"})
    private int yearsOfExperience;

    private List<String> skills = List.of();

    public CandidatePayloadClassic() {
        // 供序列化框架反射创建
    }

    public CandidatePayloadClassic(String name, int yearsOfExperience, List<String> skills) {
        setName(name);
        setYearsOfExperience(yearsOfExperience);
        setSkills(skills);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name.trim();
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        if (yearsOfExperience < 0) {
            throw new IllegalArgumentException("yearsOfExperience must be >= 0");
        }
        this.yearsOfExperience = yearsOfExperience;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills == null ? List.of() : List.copyOf(skills);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CandidatePayloadClassic that)) {
            return false;
        }
        return yearsOfExperience == that.yearsOfExperience
                && Objects.equals(name, that.name)
                && Objects.equals(skills, that.skills);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, yearsOfExperience, skills);
    }

    @Override
    public String toString() {
        return "CandidatePayloadClassic{"
                + "name='" + name + '\''
                + ", yearsOfExperience=" + yearsOfExperience
                + ", skills=" + skills
                + '}';
    }
}
