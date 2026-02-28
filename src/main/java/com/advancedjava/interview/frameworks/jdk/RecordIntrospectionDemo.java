package com.advancedjava.interview.frameworks.jdk;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

/**
 * JDK 原生 record 支持：
 * - Class#isRecord()
 * - Class#getRecordComponents()
 * - 通过规范构造器反射创建实例
 */
public class RecordIntrospectionDemo {

    public static void main(String[] args) throws Exception {
        printRecordMetadata(Candidate.class);

        Constructor<Candidate> constructor = Candidate.class.getDeclaredConstructor(String.class, int.class);
        Candidate createdByReflection = constructor.newInstance("Alice", 6);
        System.out.println("Created by reflection: " + createdByReflection);
    }

    static void printRecordMetadata(Class<?> recordType) {
        System.out.println("Type: " + recordType.getName());
        System.out.println("isRecord: " + recordType.isRecord());
        RecordComponent[] components = recordType.getRecordComponents();
        System.out.println("components: " + Arrays.toString(
                Arrays.stream(components).map(RecordComponent::getName).toArray(String[]::new)
        ));
    }

    public record Candidate(String name, int yearsOfExperience) {
    }
}
