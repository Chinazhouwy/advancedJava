package com.advancedjava.interview.records;

import java.util.ArrayList;
import java.util.List;

public class RecordDemoApp {

    public static void main(String[] args) {
        // ==================== record 写法 ====================
        // 外部传入可变 List
        List<String> sourceSkills = new ArrayList<>(List.of("Java", "Spring Boot"));
        CandidateProfile alice = new CandidateProfile("  Alice  ", 4, sourceSkills);

        // 修改原始 List，不会影响 record 内部值（构造器里做了 List.copyOf）
        sourceSkills.add("Redis");

        CandidateProfile aliceCopy = new CandidateProfile("Alice", 4, List.of("Java", "Spring Boot"));
        InterviewResult result = InterviewResult.pass(alice, "  Good communication and clean code  ");

        System.out.println("=== Record ===");
        // record 访问器是 name()，不是 getName()
        System.out.println("Accessor name(): " + alice.name());
        System.out.println("Accessor skills(): " + alice.skills());
        System.out.println("Value equality: " + alice.equals(aliceCopy));
        System.out.println("hasSkill(\"Java\"): " + alice.hasSkill("Java"));
        System.out.println("isSenior(): " + alice.isSenior());
        System.out.println("toString(): " + result);

        // ==================== 传统 class 写法 ====================
        CandidateProfileClassic bob = new CandidateProfileClassic("  Bob  ", 4, sourceSkills);
        CandidateProfileClassic bobCopy = new CandidateProfileClassic("Bob", 4, List.of("Java", "Spring Boot", "Redis"));
        InterviewResultClassic classicResult = InterviewResultClassic.pass(bob, "  Good communication and clean code  ");

        System.out.println("=== Classic Class ===");
        // 传统 JavaBean 风格通常使用 getXxx()
        System.out.println("Getter getName(): " + bob.getName());
        System.out.println("Getter getSkills(): " + bob.getSkills());
        System.out.println("Value equality: " + bob.equals(bobCopy));
        System.out.println("hasSkill(\"Java\"): " + bob.hasSkill("Java"));
        System.out.println("isSenior(): " + bob.isSenior());
        System.out.println("toString(): " + classicResult);
    }
}
