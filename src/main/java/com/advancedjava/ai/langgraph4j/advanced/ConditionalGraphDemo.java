package com.advancedjava.ai.langgraph4j.advanced;

import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * LangGraph4j 条件分支图演示
 *
 * 本示例展示如何使用 addConditionalEdges 实现条件路由：
 * 1. 根据状态内容决定下一个执行节点
 * 2. 使用 AsyncEdgeAction 返回目标节点名称
 * 3. 示例：根据文档质量分数决定路由路径
 */
public class ConditionalGraphDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LangGraph4j 条件分支图演示 ===\n");

        StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);

        AsyncNodeAction<AgentState> receiveAction = state -> {
            System.out.println("[receive_document] 接收文档");

            String documentId = state.value("document_id", "doc_001");
            String content = state.value("content", "Sample document content for testing.");

            System.out.println("  文档ID: " + documentId);
            System.out.println("  内容长度: " + content.length());

            Map<String, Object> updates = new HashMap<>();
            updates.put("received_at", System.currentTimeMillis());
            updates.put("word_count", content.split("\\s+").length);

            System.out.println("  记录接收时间，计算词数");
            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("receive_document", receiveAction);

        AsyncNodeAction<AgentState> analyzeAction = state -> {
            System.out.println("\n[analyze_quality] 分析文档质量");

            String content = state.value("content", "");
            int wordCount = state.value("word_count", 0);

            double qualityScore = calculateQualityScore(content, wordCount);

            System.out.println("  内容质量分析完成");
            System.out.println("  词数: " + wordCount);
            System.out.println("  质量分数: " + String.format("%.2f", qualityScore));

            Map<String, Object> updates = new HashMap<>();
            updates.put("quality_score", qualityScore);
            updates.put("analysis_complete", true);

            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("analyze_quality", analyzeAction);

        AsyncNodeAction<AgentState> highQualityAction = state -> {
            System.out.println("\n[high_quality_processing] 高质量文档处理");

            double score = state.value("quality_score", 0.0);
            System.out.println("  质量分数: " + String.format("%.2f", score));
            System.out.println("  -> 执行高质量处理流程");
            System.out.println("    - 提取关键信息");
            System.out.println("    - 生成摘要");
            System.out.println("    - 建立索引");

            Map<String, Object> updates = new HashMap<>();
            updates.put("processing_type", "high_quality");
            updates.put("extracted_keywords", Map.of("topic", "demo", "category", "test"));
            updates.put("summary", "高质量文档摘要");

            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("high_quality_processing", highQualityAction);

        AsyncNodeAction<AgentState> mediumQualityAction = state -> {
            System.out.println("\n[medium_quality_processing] 中等质量文档处理");

            double score = state.value("quality_score", 0.0);
            System.out.println("  质量分数: " + String.format("%.2f", score));
            System.out.println("  -> 执行标准处理流程");
            System.out.println("    - 基础分析");
            System.out.println("    - 简单标记");

            Map<String, Object> updates = new HashMap<>();
            updates.put("processing_type", "standard");
            updates.put("flagged_for_review", true);

            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("medium_quality_processing", mediumQualityAction);

        AsyncNodeAction<AgentState> lowQualityAction = state -> {
            System.out.println("\n[low_quality_processing] 低质量文档处理");

            double score = state.value("quality_score", 0.0);
            System.out.println("  质量分数: " + String.format("%.2f", score));
            System.out.println("  -> 执行低质量处理流程");
            System.out.println("    - 记录日志");
            System.out.println("    - 标记为需要人工审核");
            System.out.println("    - 发送通知");

            Map<String, Object> updates = new HashMap<>();
            updates.put("processing_type", "rejected");
            updates.put("requires_manual_review", true);
            updates.put("rejection_reason", "质量分数过低");

            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("low_quality_processing", lowQualityAction);

        AsyncNodeAction<AgentState> finalizeAction = state -> {
            System.out.println("\n[finalize] 最终处理");

            String processingType = state.value("processing_type", "unknown");
            System.out.println("  处理类型: " + processingType);

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "completed");
            updates.put("finalized_at", System.currentTimeMillis());

            System.out.println("  处理完成，状态: completed");
            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("finalize", finalizeAction);

        graph.addEdge(GraphDefinition.START, "receive_document");
        graph.addEdge("receive_document", "analyze_quality");

        Map<String, String> qualityRoutes = Map.of(
            "high", "high_quality_processing",
            "medium", "medium_quality_processing",
            "low", "low_quality_processing"
        );

        AsyncEdgeAction<AgentState> qualityRouter = state -> {
            double score = state.value("quality_score", 0.0);

            String route;
            if (score >= 0.7) {
                route = "high";
            } else if (score >= 0.4) {
                route = "medium";
            } else {
                route = "low";
            }

            System.out.println("\n[条件路由] 质量分数: " + String.format("%.2f", score) + " -> 路由到: " + route);
            return CompletableFuture.completedFuture(route);
        };

        graph.addConditionalEdges("analyze_quality", qualityRouter, qualityRoutes);

        graph.addEdge("high_quality_processing", "finalize");
        graph.addEdge("medium_quality_processing", "finalize");
        graph.addEdge("low_quality_processing", "finalize");
        graph.addEdge("finalize", GraphDefinition.END);

        CompiledGraph<AgentState> compiledGraph = graph.compile();
        System.out.println("\n--- 图编译成功 ---\n");

        System.out.println("=== 测试场景1: 高质量文档 ===");
        Map<String, Object> input1 = Map.of(
            "document_id", "doc_high_001",
            "content", "This is a comprehensive and well-written document with detailed analysis and clear structure. It contains multiple paragraphs with substantial content and demonstrates high quality writing standards."
        );
        runScenario(compiledGraph, input1);

        System.out.println("\n\n=== 测试场景2: 中等质量文档 ===");
        Map<String, Object> input2 = Map.of(
            "document_id", "doc_medium_001",
            "content", "Average content here. Some points made."
        );
        runScenario(compiledGraph, input2);

        System.out.println("\n\n=== 测试场景3: 低质量文档 ===");
        Map<String, Object> input3 = Map.of(
            "document_id", "doc_low_001",
            "content", "Hi"
        );
        runScenario(compiledGraph, input3);

        System.out.println("\n=== 演示完成 ===");
    }

    private static void runScenario(CompiledGraph<AgentState> compiledGraph, Map<String, Object> input) throws Exception {
        System.out.println("\n初始输入: " + input);
        Optional<AgentState> result = compiledGraph.invoke(input);

        if (result.isPresent()) {
            AgentState state = result.get();
            System.out.println("\n最终状态:");
            System.out.println("  quality_score: " + String.format("%.2f", state.value("quality_score").orElse(0.0)));
            System.out.println("  processing_type: " + state.value("processing_type").orElse("N/A"));
            System.out.println("  status: " + state.value("status").orElse("N/A"));
        }
    }

    private static double calculateQualityScore(String content, int wordCount) {
        double score = 0.0;

        if (wordCount >= 50) {
            score += 0.4;
        } else if (wordCount >= 20) {
            score += 0.2;
        }

        if (content.contains(".")) {
            score += 0.1;
        }

        if (content.length() > 100) {
            score += 0.3;
        } else if (content.length() > 50) {
            score += 0.1;
        }

        return Math.min(score, 1.0);
    }
}
