package com.advancedjava.interview.records;

import java.util.List;

/**
 * record 写法：适合“只承载数据 + 少量校验/工具方法”的场景。
 *
 * 对比传统 class（见 CandidateProfileClassic）：
 * 1. 这里仅声明组件（name、yearsOfExperience、skills）。
 * 2. 编译器会自动生成：
 *    - private final 字段
 *    - 访问器 name()/yearsOfExperience()/skills()
 *    - equals()/hashCode()/toString()
 * 3. 传统写法里这些都需要手写，样板代码会明显增多。
 */
public record CandidateProfile(String name, int yearsOfExperience, List<String> skills) {

    /**
     * 紧凑构造器（compact constructor）：
     * - 不需要重复声明参数列表
     * - 参数名与组件名一致，可在这里做校验和归一化
     * - 结束后，参数值会被赋给对应组件
     */
    public CandidateProfile {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (yearsOfExperience < 0) {
            throw new IllegalArgumentException("yearsOfExperience must be >= 0");
        }
        // 数据归一化：去掉前后空格，避免 "Alice" 和 " Alice " 语义冲突
        name = name.trim();
        // 防御性拷贝：阻断外部对原 List 的后续修改
        skills = skills == null ? List.of() : List.copyOf(skills);
    }

    /**
     * 重载构造器：
     * record 也可以有额外构造器，但必须最终委托到“规范构造器”。
     */
    public CandidateProfile(String name, int yearsOfExperience) {
        this(name, yearsOfExperience, List.of());
    }

    /**
     * 业务辅助方法：record 不只是“纯字段”，也可以有行为方法。
     */
    public boolean hasSkill(String skill) {
        return skill != null && skills.contains(skill);
    }

    public boolean isSenior() {
        return yearsOfExperience >= 5;
    }
}
