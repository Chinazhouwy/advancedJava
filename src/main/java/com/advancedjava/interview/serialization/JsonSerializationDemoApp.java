package com.advancedjava.interview.serialization;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * JSON 序列化演示入口。
 *
 * <p>该示例同时使用 Jackson 与 Fastjson2，对 record 和传统 POJO 做序列化回环，
 * 并额外验证旧字段名兼容与忽略未知字段的行为。
 */
public class JsonSerializationDemoApp {

    private static final ObjectMapper JACKSON = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 运行 Jackson/Fastjson2 的序列化对比，并打印结果。
     */
    public static void main(String[] args) throws Exception {
        CandidatePayloadRecord recordData = new CandidatePayloadRecord("  Alice  ", 6, List.of("Java", "Spring"));
        CandidatePayloadClassic classicData = new CandidatePayloadClassic("  Bob  ", 4, List.of("Java", "Redis"));

        String jacksonRecordJson = JACKSON.writeValueAsString(recordData);
        CandidatePayloadRecord jacksonRecordRead = JACKSON.readValue(jacksonRecordJson, CandidatePayloadRecord.class);

        String fastjsonRecordJson = JSON.toJSONString(recordData);
        CandidatePayloadRecord fastjsonRecordRead = JSON.parseObject(fastjsonRecordJson, CandidatePayloadRecord.class);

        String legacyJson = "{\"name\":\"Alice\",\"years\":7,\"skills\":[\"Java\"],\"legacyField\":\"v1\"}";
        CandidatePayloadRecord jacksonLegacyRead = JACKSON.readValue(legacyJson, CandidatePayloadRecord.class);
        CandidatePayloadRecord fastjsonLegacyRead = JSON.parseObject(legacyJson, CandidatePayloadRecord.class);

        String jacksonClassicJson = JACKSON.writeValueAsString(classicData);
        CandidatePayloadClassic jacksonClassicRead = JACKSON.readValue(jacksonClassicJson, CandidatePayloadClassic.class);

        String fastjsonClassicJson = JSON.toJSONString(classicData);
        CandidatePayloadClassic fastjsonClassicRead = JSON.parseObject(fastjsonClassicJson, CandidatePayloadClassic.class);

        System.out.println("=== Jackson Record Round Trip ===");
        System.out.println(jacksonRecordJson);
        System.out.println(jacksonRecordRead);

        System.out.println("=== Fastjson2 Record Round Trip ===");
        System.out.println(fastjsonRecordJson);
        System.out.println(fastjsonRecordRead);

        System.out.println("=== Legacy Field Compatibility ===");
        System.out.println("Jackson yearsOfExperience: " + jacksonLegacyRead.yearsOfExperience());
        System.out.println("Fastjson2 yearsOfExperience: " + fastjsonLegacyRead.yearsOfExperience());

        System.out.println("=== Jackson Classic Round Trip ===");
        System.out.println(jacksonClassicJson);
        System.out.println(jacksonClassicRead);

        System.out.println("=== Fastjson2 Classic Round Trip ===");
        System.out.println(fastjsonClassicJson);
        System.out.println(fastjsonClassicRead);
    }
}
