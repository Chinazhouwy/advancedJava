package com.advancedjava.ai.langgraph4j.basic;

import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * LangGraph4j 状态管理演示
 *
 * 本示例重点展示 AgentState 的使用方法：
 * 1. value(key) 返回 Optional<T>，安全地获取值
 * 2. value(key, defaultValue) 带默认值的获取
 * 3. data() 获取完整的原始 Map
 * 4. 状态如何在节点间传递和累积
 */
public class StateGraphDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LangGraph4j 状态管理演示 ===\n");

        StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);

        AsyncNodeAction<AgentState> initAction = state -> {
            System.out.println("[initialize] 初始化状态");

            Map<String, Object> updates = new HashMap<>();
            updates.put("counter", 0);
            updates.put("items", new ArrayList<String>());
            updates.put("user", Map.of("name", "Alice", "role", "admin"));

            System.out.println("  初始化 counter: 0");
            System.out.println("  初始化 items: []");
            System.out.println("  初始化 user: {name=Alice, role=admin}");
            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("initialize", initAction);

        AsyncNodeAction<AgentState> process1Action = state -> {
            System.out.println("\n[process1] 第一种处理方式");

            System.out.println("  当前状态数据: " + state.data());

            Integer counter = state.value("counter", 0);
            System.out.println("  value(\"counter\") = " + counter);

            @SuppressWarnings("unchecked")
            List<String> items = (List<String>) state.value("items", new ArrayList<String>());
            System.out.println("  value(\"items\") = " + items);

            System.out.println("  data().get(\"user\") = " + state.data().get("user"));

            Map<String, Object> updates = new HashMap<>();
            updates.put("counter", counter + 10);
            items.add("process1_result");
            updates.put("items", items);
            updates.put("process1_completed", true);

            System.out.println("  更新后 counter: " + (counter + 10));
            System.out.println("  添加 items: process1_result");
            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("process1", process1Action);

        AsyncNodeAction<AgentState> process2Action = state -> {
            System.out.println("\n[process2] 第二种处理方式");

            System.out.println("  当前状态数据大小: " + state.data().size() + " 个键");
            System.out.println("  所有键: " + state.data().keySet());

            Integer counter = state.value("counter", 0);
            System.out.println("  value(\"counter\", 0) = " + counter);

            boolean completed = state.value("process1_completed", false);
            System.out.println("  process1_completed = " + completed);

            String nonExistent = state.value("non_existent", "默认值");
            System.out.println("  value(\"non_existent\") 使用默认值: " + nonExistent);

            Map<String, Object> updates = new HashMap<>();
            updates.put("counter", counter * 2);
            updates.put("process2_completed", true);
            updates.put("status", "processed");

            System.out.println("  counter * 2 = " + (counter * 2));
            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("process2", process2Action);

        AsyncNodeAction<AgentState> finalizeAction = state -> {
            System.out.println("\n[finalize] 最终处理");

            System.out.println("  --- 完整状态检查 ---");
            System.out.println("  data() 返回的完整 Map:");
            state.data().forEach((key, value) -> {
                System.out.println("    " + key + " = " + value + " (类型: " + value.getClass().getSimpleName() + ")");
            });

            System.out.println("\n  --- 使用 value() 读取特定值 ---");
            System.out.println("  counter: " + state.value("counter", 0));
            System.out.println("  status: " + state.value("status", "N/A"));
            System.out.println("  process1_completed: " + state.value("process1_completed", false));
            System.out.println("  process2_completed: " + state.value("process2_completed", false));

            List<String> emptyList = List.of();
            List<String> items = state.value("items", emptyList);
            System.out.println("  items 列表: " + items);

            Map<String, Object> updates = new HashMap<>();
            updates.put("final_message", "所有处理完成！");
            updates.put("total_steps", 3);
            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("finalize", finalizeAction);

        graph.addEdge(GraphDefinition.START, "initialize");
        graph.addEdge("initialize", "process1");
        graph.addEdge("process1", "process2");
        graph.addEdge("process2", "finalize");
        graph.addEdge("finalize", GraphDefinition.END);

        CompiledGraph<AgentState> compiledGraph = graph.compile();
        System.out.println("\n--- 编译成功，开始执行 ---\n");

        Map<String, Object> initialState = Map.of("session_id", "demo_001");
        Optional<AgentState> result = compiledGraph.invoke(initialState);

        System.out.println("\n=== 最终结果 ===");
        if (result.isPresent()) {
            AgentState finalState = result.get();
            System.out.println("\n最终状态 data():");
            System.out.println("  " + finalState.data());

            System.out.println("\n使用 value() 读取各值:");
            System.out.println("  session_id: " + finalState.value("session_id", "N/A"));
            System.out.println("  counter: " + finalState.value("counter", 0));
            System.out.println("  status: " + finalState.value("status", "N/A"));
            System.out.println("  final_message: " + finalState.value("final_message", "N/A"));
            System.out.println("  total_steps: " + finalState.value("total_steps", 0));
        }

        System.out.println("\n=== 演示完成 ===");
    }
}
