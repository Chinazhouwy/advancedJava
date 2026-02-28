package com.advancedjava.interview.serialization;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JsonSerializationExamplesTest {

    private static final ObjectMapper JACKSON = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    public void shouldRoundTripRecordWithJackson() throws Exception {
        CandidatePayloadRecord source = new CandidatePayloadRecord("Alice", 5, List.of("Java", "SQL"));

        String json = JACKSON.writeValueAsString(source);
        CandidatePayloadRecord target = JACKSON.readValue(json, CandidatePayloadRecord.class);

        Assert.assertEquals(source, target);
    }

    @Test
    public void shouldRoundTripRecordWithFastjson2() {
        CandidatePayloadRecord source = new CandidatePayloadRecord("Alice", 5, List.of("Java", "SQL"));

        String json = JSON.toJSONString(source);
        CandidatePayloadRecord target = JSON.parseObject(json, CandidatePayloadRecord.class);

        Assert.assertEquals(source, target);
    }

    @Test
    public void shouldReadLegacyYearsFieldWithBothLibraries() throws Exception {
        String legacyJson = "{\"name\":\"Alice\",\"years\":7,\"skills\":[\"Java\"],\"legacyField\":\"v1\"}";

        CandidatePayloadRecord jacksonRead = JACKSON.readValue(legacyJson, CandidatePayloadRecord.class);
        CandidatePayloadRecord fastjsonRead = JSON.parseObject(legacyJson, CandidatePayloadRecord.class);

        Assert.assertEquals(7, jacksonRead.yearsOfExperience());
        Assert.assertEquals(7, fastjsonRead.yearsOfExperience());
        Assert.assertEquals("Alice", jacksonRead.name());
        Assert.assertEquals(List.of("Java"), fastjsonRead.skills());
    }

    @Test
    public void shouldRoundTripClassicPojoWithBothLibraries() throws Exception {
        CandidatePayloadClassic source = new CandidatePayloadClassic("Bob", 3, List.of("Spring", "Redis"));

        String jacksonJson = JACKSON.writeValueAsString(source);
        CandidatePayloadClassic jacksonTarget = JACKSON.readValue(jacksonJson, CandidatePayloadClassic.class);

        String fastjsonJson = JSON.toJSONString(source);
        CandidatePayloadClassic fastjsonTarget = JSON.parseObject(fastjsonJson, CandidatePayloadClassic.class);

        Assert.assertEquals(source, jacksonTarget);
        Assert.assertEquals(source, fastjsonTarget);
    }
}
