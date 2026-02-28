package com.advancedjava.interview.records;

import java.util.List;
import java.util.Objects;

/**
 * 传统写法（不使用 record）的等价实现。
 *
 * 对比 CandidateProfile(record) 可看到：
 * 1. 字段、构造器、getter、equals/hashCode/toString 都需要手写。
 * 2. 业务逻辑不复杂时，样板代码占比很高。
 */
public final class CandidateProfileClassic {

    private final String name;
    private final int yearsOfExperience;
    private final List<String> skills;

    public CandidateProfileClassic(String name, int yearsOfExperience, List<String> skills) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (yearsOfExperience < 0) {
            throw new IllegalArgumentException("yearsOfExperience must be >= 0");
        }
        this.name = name.trim();
        this.yearsOfExperience = yearsOfExperience;
        this.skills = skills == null ? List.of() : List.copyOf(skills);
    }

    public CandidateProfileClassic(String name, int yearsOfExperience) {
        this(name, yearsOfExperience, List.of());
    }

    public String getName() {
        return name;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public List<String> getSkills() {
        return skills;
    }

    public boolean hasSkill(String skill) {
        return skill != null && skills.contains(skill);
    }

    public boolean isSenior() {
        return yearsOfExperience >= 5;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CandidateProfileClassic that)) {
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
        return "CandidateProfileClassic{"
                + "name='" + name + '\''
                + ", yearsOfExperience=" + yearsOfExperience
                + ", skills=" + skills
                + '}';
    }
}
