package com.advancedjava.ai.langgraph4j.basic;

import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * LangGraph4j 顺序执行图演示
 *
 * 本示例展示如何创建一个简单的顺序执行图：
 * 1. 创建 StateGraph，每个节点按顺序执行
 * 2. 节点之间通过 addEdge 连接
 * 3. 使用 START 和 END 作为入口和出口
 * 4. 每个节点向状态中写入数据，后续节点可以读取
 */
public class SequentialGraphDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LangGraph4j 顺序执行图演示 ===\n");

        // 1. 创建 StateGraph，使用 AgentState 工厂
        // AgentStateFactory 用于创建初始状态对象
        StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);

        // 2. 添加第一个节点：数据输入
        // 每个节点接收当前状态，返回要更新的键值对
        AsyncNodeAction<AgentState> inputAction = state -> {
            System.out.println("[节点: input] 接收初始输入...");
            Map<String, Object> updates = new HashMap<>();
            updates.put("message", "Hello LangGraph4j");
            updates.put("step", 1);
            System.out.println("  -> 设置 message: " + updates.get("message"));
            System.out.println("  -> 设置 step: " + updates.get("step"));
            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("input", inputAction);

        // 3. 添加第二个节点：数据处理
        // 从状态中读取数据，处理后写入新数据
        AsyncNodeAction<AgentState> processAction = state -> {
            System.out.println("\n[节点: process] 处理数据...");

            // 从状态中读取数据，使用 value() 方法
            String message = state.value("message", "");
            Integer step = state.value("step", 0);

            System.out.println("  <- 读取 message: " + message);
            System.out.println("  <- 读取 step: " + step);

            Map<String, Object> updates = new HashMap<>();
            updates.put("processedMessage", message.toUpperCase());
            updates.put("step", step + 1);

            System.out.println("  -> 设置 processedMessage: " + updates.get("processedMessage"));
            System.out.println("  -> 更新 step: " + updates.get("step"));
            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("process", processAction);

        // 4. 添加第三个节点：数据输出
        AsyncNodeAction<AgentState> outputAction = state -> {
            System.out.println("\n[节点: output] 输出结果...");

            String original = state.value("message", "");
            String processed = state.value("processedMessage", "");
            Integer step = state.value("step", 0);

            System.out.println("  <- 原始消息: " + original);
            System.out.println("  <- 处理后消息: " + processed);
            System.out.println("  <- 当前步骤: " + step);

            Map<String, Object> updates = new HashMap<>();
            updates.put("finalResult", "Result: " + processed);
            updates.put("step", step + 1);
            updates.put("completed", true);

            System.out.println("  -> 设置 finalResult: " + updates.get("finalResult"));
            System.out.println("  -> 设置 completed: " + updates.get("completed"));
            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("output", outputAction);

        // 5. 连接节点：定义执行顺序
        // START -> input -> process -> output -> END
        graph.addEdge(GraphDefinition.START, "input");
        graph.addEdge("input", "process");
        graph.addEdge("process", "output");
        graph.addEdge("output", GraphDefinition.END);

        // 6. 编译图
        System.out.println("\n--- 编译图 ---");
        CompiledGraph<AgentState> compiledGraph = graph.compile();
        System.out.println("图编译成功！\n");

        // 7. 执行图
        System.out.println("--- 开始执行 ---");
        // 传入初始状态（空Map，因为第一个节点会设置初始数据）
        Optional<AgentState> result = compiledGraph.invoke(Map.of());

        // 8. 检查结果
        System.out.println("\n--- 执行完成 ---");
        if (result.isPresent()) {
            AgentState finalState = result.get();
            System.out.println("最终状态数据:");
            System.out.println("  " + finalState.data());

            System.out.println("\n使用 value() 方法读取特定值:");
            System.out.println("  message: " + finalState.value("message", "N/A"));
            System.out.println("  processedMessage: " + finalState.value("processedMessage", "N/A"));
            System.out.println("  finalResult: " + finalState.value("finalResult", "N/A"));
            System.out.println("  completed: " + finalState.value("completed", false));
            System.out.println("  step: " + finalState.value("step", 0));
        } else {
            System.out.println("执行结果为空！");
        }

        // 9. 演示带初始输入的执行
        System.out.println("\n=== 带初始输入的执行 ===\n");
        Map<String, Object> initialInput = Map.of(
            "message", "Custom Input",
            "userId", "user123"
        );

        Optional<AgentState> result2 = compiledGraph.invoke(initialInput);
        if (result2.isPresent()) {
            System.out.println("最终状态数据:");
            System.out.println("  " + result2.get().data());
        }

        System.out.println("\n=== 演示完成 ===");
    }
}
