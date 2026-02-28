package com.advancedjava.interview.records;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CandidateProfileTest {

    @Test
    public void shouldTrimNameAndDefensivelyCopySkills() {
        List<String> sourceSkills = new ArrayList<>(List.of("Java", "SQL"));
        CandidateProfile profile = new CandidateProfile("  Bob  ", 3, sourceSkills);

        sourceSkills.add("Redis");

        Assert.assertEquals("Bob", profile.name());
        Assert.assertEquals(List.of("Java", "SQL"), profile.skills());

        try {
            profile.skills().add("Kotlin");
            Assert.fail("skills should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void shouldUseValueBasedEquality() {
        CandidateProfile left = new CandidateProfile("Alice", 5, List.of("Java"));
        CandidateProfile right = new CandidateProfile("Alice", 5, List.of("Java"));

        Assert.assertEquals(left, right);
        Assert.assertEquals(left.hashCode(), right.hashCode());
    }

    @Test
    public void shouldRejectInvalidArguments() {
        assertThrows(() -> new CandidateProfile(" ", 1, List.of()), IllegalArgumentException.class);
        assertThrows(() -> new CandidateProfile("Alice", -1, List.of()), IllegalArgumentException.class);
    }

    @Test
    public void shouldSupportFactoriesForInterviewResult() {
        CandidateProfile candidate = new CandidateProfile("Alice", 2);

        InterviewResult passResult = InterviewResult.pass(candidate, "  Solid basics  ");
        InterviewResult failResult = InterviewResult.fail(candidate, null);

        Assert.assertTrue(passResult.passed());
        Assert.assertEquals("Solid basics", passResult.feedback());
        Assert.assertFalse(failResult.passed());
        Assert.assertEquals("", failResult.feedback());
    }

    @Test
    public void shouldMatchClassicPojoBehavior() {
        CandidateProfile recordProfile = new CandidateProfile("  Alice  ", 5, List.of("Java", "SQL"));
        CandidateProfileClassic classicProfile = new CandidateProfileClassic("  Alice  ", 5, List.of("Java", "SQL"));

        Assert.assertEquals(classicProfile.getName(), recordProfile.name());
        Assert.assertEquals(classicProfile.getYearsOfExperience(), recordProfile.yearsOfExperience());
        Assert.assertEquals(classicProfile.getSkills(), recordProfile.skills());
        Assert.assertEquals(classicProfile.isSenior(), recordProfile.isSenior());
        Assert.assertEquals(classicProfile.hasSkill("SQL"), recordProfile.hasSkill("SQL"));

        InterviewResult recordResult = InterviewResult.pass(recordProfile, "  Good  ");
        InterviewResultClassic classicResult = InterviewResultClassic.pass(classicProfile, "  Good  ");

        Assert.assertEquals(classicResult.isPassed(), recordResult.passed());
        Assert.assertEquals(classicResult.getFeedback(), recordResult.feedback());
    }

    private static void assertThrows(Runnable runnable, Class<? extends Throwable> expectedType) {
        try {
            runnable.run();
            Assert.fail("expected exception: " + expectedType.getSimpleName());
        } catch (Throwable throwable) {
            Assert.assertTrue("unexpected exception type: " + throwable.getClass(), expectedType.isInstance(throwable));
        }
    }
}
