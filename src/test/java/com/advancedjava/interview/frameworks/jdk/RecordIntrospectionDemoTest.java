package com.advancedjava.interview.frameworks.jdk;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;

public class RecordIntrospectionDemoTest {

    @Test
    public void shouldExposeRecordMetadataByReflection() {
        Class<?> type = RecordIntrospectionDemo.Candidate.class;

        Assert.assertTrue(type.isRecord());
        RecordComponent[] components = type.getRecordComponents();
        Assert.assertEquals(2, components.length);
        Assert.assertEquals("name", components[0].getName());
        Assert.assertEquals("yearsOfExperience", components[1].getName());
    }

    @Test
    public void shouldCreateRecordByCanonicalConstructor() throws Exception {
        Constructor<RecordIntrospectionDemo.Candidate> constructor =
                RecordIntrospectionDemo.Candidate.class.getDeclaredConstructor(String.class, int.class);

        RecordIntrospectionDemo.Candidate candidate = constructor.newInstance("Alice", 6);
        Assert.assertEquals("Alice", candidate.name());
        Assert.assertEquals(6, candidate.yearsOfExperience());
    }
}
