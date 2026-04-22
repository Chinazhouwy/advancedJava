package com.advancedjava.interview.records;

import java.util.ArrayList;
import java.util.List;

/**
 * record 与传统 POJO 对比演示入口。
 *
 * <p>通过打印结果对比两套模型对象的访问器命名、值语义、防御性拷贝和工厂方法表达力，
 * 帮助读者快速感受 Java record 在教学示例中的优势。
 */
public class RecordDemoApp {

    /**
     * 同时打印 record 与传统类的行为，便于直接比较两种建模方式。
     */
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
