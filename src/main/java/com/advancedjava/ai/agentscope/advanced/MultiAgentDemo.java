package com.advancedjava.ai.agentscope.advanced;

import io.agentscope.core.agent.AgentBase;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.pipeline.SequentialPipeline;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * AgentScope 多Agent管道演示
 * 展示如何使用SequentialPipeline将多个Agent串联起来，形成处理流水线
 */
public class MultiAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== AgentScope 多Agent管道演示 ===");
        System.out.println();

        // 创建三个不同的Agent组成处理管道
        TextPreprocessorAgent preprocessor = new TextPreprocessorAgent("preprocessor", "文本预处理Agent");
        AnalyzerAgent analyzer = new AnalyzerAgent("analyzer", "内容分析Agent");
        SummarizerAgent summarizer = new SummarizerAgent("summarizer", "摘要生成Agent");

        // 方式1: 使用SequentialPipeline顺序执行
        demoSequentialPipeline(preprocessor, analyzer, summarizer);

        // 方式2: 手动串联Agent（更灵活的方式）
        demoManualChaining(preprocessor, analyzer, summarizer);

        // 方式3: 展示每个Agent的独立执行
        demoIndividualAgents(preprocessor, analyzer, summarizer);
    }

    /**
     * 演示使用SequentialPipeline顺序执行多个Agent
     */
    private static void demoSequentialPipeline(TextPreprocessorAgent preprocessor,
                                                AnalyzerAgent analyzer,
                                                SummarizerAgent summarizer) {
        System.out.println("=".repeat(60));
        System.out.println("方式1: SequentialPipeline 顺序执行");
        System.out.println("=".repeat(60));

        // 构建Pipeline：预处理 -> 分析 -> 摘要
        SequentialPipeline pipeline = SequentialPipeline.builder()
                .addAgent(preprocessor)
                .addAgent(analyzer)
                .addAgent(summarizer)
                .build();

        // 创建输入消息
        String originalText = "  Java是一门面向对象编程语言，具有跨平台特性。 " +
                "它由Sun Microsystems公司于1995年发布。 " +
                "Java具有简单性、面向对象、分布式、健壮性、安全性等特点。  ";

        Msg inputMessage = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(originalText).build())
                .build();

        System.out.println("原始输入: " + originalText);
        System.out.println();

        // 执行Pipeline（同步阻塞调用）
        Msg result = pipeline.execute(inputMessage).block();
        System.out.println("\nPipeline最终结果: " + result.getTextContent());
        System.out.println();
    }

    /**
     * 演示手动串联Agent的方式
     */
    private static void demoManualChaining(TextPreprocessorAgent preprocessor,
                                            AnalyzerAgent analyzer,
                                            SummarizerAgent summarizer) {
        System.out.println("=".repeat(60));
        System.out.println("方式2: 手动串联Agent");
        System.out.println("=".repeat(60));

        String text = "Python和Java都是流行的编程语言。Python以简洁著称，Java以稳定闻名。";
        Msg input = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(text).build())
                .build();

        System.out.println("原始输入: " + text);
        System.out.println();

        // 手动串联：将前一个Agent的输出作为后一个Agent的输入
        Msg step1Result = preprocessor.call(List.of(input)).block();
        System.out.println("步骤1 (预处理): " + step1Result.getTextContent());

        Msg step2Result = analyzer.call(List.of(step1Result)).block();
        System.out.println("步骤2 (分析): " + step2Result.getTextContent());

        Msg step3Result = summarizer.call(List.of(step2Result)).block();
        System.out.println("步骤3 (摘要): " + step3Result.getTextContent());
        System.out.println();
    }

    /**
     * 演示每个Agent的独立执行
     */
    private static void demoIndividualAgents(TextPreprocessorAgent preprocessor,
                                              AnalyzerAgent analyzer,
                                              SummarizerAgent summarizer) {
        System.out.println("=".repeat(60));
        System.out.println("方式3: 独立Agent执行");
        System.out.println("=".repeat(60));

        String text = "  多线程编程是Java的高级特性之一。   ";
        Msg input = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(text).build())
                .build();

        System.out.println("测试文本: [" + text + "]");
        System.out.println();

        // 测试预处理Agent
        Msg preprocessed = preprocessor.call(List.of(input)).block();
        System.out.println("预处理Agent输出: " + preprocessed.getTextContent());

        // 测试分析Agent
        Msg analyzed = analyzer.call(List.of(input)).block();
        System.out.println("分析Agent输出: " + analyzed.getTextContent());

        // 测试摘要Agent
        Msg summarized = summarizer.call(List.of(input)).block();
        System.out.println("摘要Agent输出: " + summarized.getTextContent());
        System.out.println();

        // 显示Pipeline信息
        System.out.println("Pipeline描述: 文本预处理Agent -> 内容分析Agent -> 摘要生成Agent");
        System.out.println("每个Agent独立处理并传递消息，形成完整的处理链条");
    }

    /**
     * 文本预处理Agent：清理和规范化输入文本
     */
    static class TextPreprocessorAgent extends AgentBase {

        public TextPreprocessorAgent(String name, String description) {
            super(name, description);
        }

        @Override
        protected Mono<Msg> doCall(List<Msg> msgs) {
            String input = msgs.get(msgs.size() - 1).getTextContent();

            // 预处理：去除多余空格、统一格式
            String processed = input.trim()
                    .replaceAll("\\s+", " ");

            String result = String.format("[预处理] 原文长度:%d -> 处理后:%d | 内容:%s",
                    input.length(), processed.length(), processed);

            Msg response = Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text(result).build())
                    .build();

            return Mono.just(response);
        }

        @Override
        protected Mono<Msg> handleInterrupt(io.agentscope.core.interruption.InterruptContext context, Msg... msgs) {
            return Mono.just(Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text("预处理被中断").build())
                    .build());
        }
    }

    /**
     * 内容分析Agent：分析文本的关键信息
     */
    static class AnalyzerAgent extends AgentBase {

        public AnalyzerAgent(String name, String description) {
            super(name, description);
        }

        @Override
        protected Mono<Msg> doCall(List<Msg> msgs) {
            String input = msgs.get(msgs.size() - 1).getTextContent();

            // 分析：统计字数、句子数等
            int charCount = input.length();
            int sentenceCount = input.split("[。！？]").length;

            String result = String.format("[分析] 字符数:%d | 句子数:%d | 关键词:编程,语言,特性",
                    charCount, sentenceCount);

            Msg response = Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text(result).build())
                    .build();

            return Mono.just(response);
        }

        @Override
        protected Mono<Msg> handleInterrupt(io.agentscope.core.interruption.InterruptContext context, Msg... msgs) {
            return Mono.just(Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text("分析被中断").build())
                    .build());
        }
    }

    /**
     * 摘要生成Agent：生成文本摘要
     */
    static class SummarizerAgent extends AgentBase {

        public SummarizerAgent(String name, String description) {
            super(name, description);
        }

        @Override
        protected Mono<Msg> doCall(List<Msg> msgs) {
            String input = msgs.get(msgs.size() - 1).getTextContent();

            // 生成摘要（简化版本）
            String summary = "本文介绍了编程语言的主要特性和发展历史。";

            String result = String.format("[摘要] %s (基于输入:%s)", summary, input.substring(0, Math.min(50, input.length())));

            Msg response = Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text(result).build())
                    .build();

            return Mono.just(response);
        }

        @Override
        protected Mono<Msg> handleInterrupt(io.agentscope.core.interruption.InterruptContext context, Msg... msgs) {
            return Mono.just(Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text("摘要生成被中断").build())
                    .build());
        }
    }
}
