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
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * LangGraph4j Agent 循环模式演示
 *
 * 本示例展示如何实现 Agent 循环（ReAct 模式）：
 * 1. Think（思考） -> Act（行动） -> Observe（观察）循环
 * 2. 使用条件边判断是否继续循环或结束
 * 3. 通过迭代计数控制循环次数
 */
public class AgentLoopDemo {

    private static final int MAX_ITERATIONS = 3;

    public static void main(String[] args) throws Exception {
        System.out.println("=== LangGraph4j Agent 循环模式演示 ===\n");
        System.out.println("模式: Think -> Act -> Observe -> [循环或结束]\n");

        StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);

        AsyncNodeAction<AgentState> thinkAction = state -> {
            System.out.println("[Think] Agent 思考阶段");

            int iteration = state.value("iteration", 0);
            String task = state.value("task", "default_task");
            String previousObservation = state.value("observation", "");

            System.out.println("  当前迭代: " + iteration);
            System.out.println("  任务: " + task);

            if (!previousObservation.isEmpty()) {
                System.out.println("  基于上一次观察: " + previousObservation);
            }

            String thought = generateThought(iteration, task, previousObservation);
            System.out.println("  思考结果: " + thought);

            Map<String, Object> updates = new HashMap<>();
            updates.put("thought", thought);
            updates.put("iteration", iteration);

            List<String> thoughts = state.value("thoughts_history", new ArrayList<>());
            thoughts.add(thought);
            updates.put("thoughts_history", thoughts);

            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("think", thinkAction);

        AsyncNodeAction<AgentState> actAction = state -> {
            System.out.println("\n[Act] Agent 行动阶段");

            String thought = state.value("thought", "");
            String task = state.value("task", "");
            int iteration = state.value("iteration", 0);

            System.out.println("  基于思考: " + thought);

            String action = executeAction(iteration, task, thought);
            System.out.println("  执行行动: " + action);

            Map<String, Object> updates = new HashMap<>();
            updates.put("action", action);
            updates.put("action_executed", true);

            List<String> actions = state.value("actions_history", new ArrayList<>());
            actions.add(action);
            updates.put("actions_history", actions);

            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("act", actAction);

        AsyncNodeAction<AgentState> observeAction = state -> {
            System.out.println("\n[Observe] Agent 观察阶段");

            String action = state.value("action", "");
            int iteration = state.value("iteration", 0);

            System.out.println("  观察行动结果: " + action);

            String observation = generateObservation(iteration, action);
            System.out.println("  观察结果: " + observation);

            int newIteration = iteration + 1;
            System.out.println("  迭代计数: " + iteration + " -> " + newIteration);

            Map<String, Object> updates = new HashMap<>();
            updates.put("observation", observation);
            updates.put("iteration", newIteration);

            List<String> observations = state.value("observations_history", new ArrayList<>());
            observations.add(observation);
            updates.put("observations_history", observations);

            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("observe", observeAction);

        AsyncNodeAction<AgentState> decideAction = state -> {
            System.out.println("\n[Decide] Agent 决策阶段");

            int iteration = state.value("iteration", 0);
            String task = state.value("task", "");
            String observation = state.value("observation", "");

            System.out.println("  当前迭代: " + iteration);
            System.out.println("  最新观察: " + observation);

            boolean taskComplete = checkTaskCompletion(task, observation, iteration);
            System.out.println("  任务是否完成: " + taskComplete);

            Map<String, Object> updates = new HashMap<>();
            updates.put("task_complete", taskComplete);

            if (taskComplete) {
                updates.put("final_answer", "任务完成，答案是: " + observation);
                System.out.println("  决策: 结束循环，返回最终答案");
            } else {
                System.out.println("  决策: 继续下一轮循环");
            }

            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("decide", decideAction);

        AsyncNodeAction<AgentState> finalizeAction = state -> {
            System.out.println("\n[Finalize] 最终处理");

            String finalAnswer = state.value("final_answer", "");
            int totalIterations = state.value("iteration", 0);

            System.out.println("  最终答案: " + finalAnswer);
            System.out.println("  总迭代次数: " + totalIterations);

            List<String> empty = List.of();
            List<String> thoughts = state.value("thoughts_history", empty);
            List<String> actions = state.value("actions_history", empty);
            List<String> observations = state.value("observations_history", empty);

            System.out.println("\n  完整思考历史:");
            for (int i = 0; i < thoughts.size(); i++) {
                System.out.println("    " + (i + 1) + ". Thought: " + thoughts.get(i));
                if (i < actions.size()) {
                    System.out.println("       Action: " + actions.get(i));
                }
                if (i < observations.size()) {
                    System.out.println("       Observation: " + observations.get(i));
                }
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "completed");
            updates.put("summary", "循环完成，共执行 " + totalIterations + " 轮");

            return CompletableFuture.completedFuture(updates);
        };
        graph.addNode("finalize", finalizeAction);

        graph.addEdge(GraphDefinition.START, "think");
        graph.addEdge("think", "act");
        graph.addEdge("act", "observe");
        graph.addEdge("observe", "decide");

        Map<String, String> decisionRoutes = Map.of(
            "continue", "think",
            "end", "finalize"
        );

        AsyncEdgeAction<AgentState> decisionRouter = state -> {
            int iteration = state.value("iteration", 0);
            boolean taskComplete = state.value("task_complete", false);

            System.out.println("\n[条件路由] 检查是否继续循环:");
            System.out.println("  迭代次数: " + iteration + "/" + MAX_ITERATIONS);
            System.out.println("  任务完成: " + taskComplete);

            String route;
            if (taskComplete || iteration >= MAX_ITERATIONS) {
                route = "end";
                System.out.println("  -> 路由到: finalize (结束)");
            } else {
                route = "continue";
                System.out.println("  -> 路由到: think (继续循环)");
            }

            return CompletableFuture.completedFuture(route);
        };

        graph.addConditionalEdges("decide", decisionRouter, decisionRoutes);
        graph.addEdge("finalize", GraphDefinition.END);

        CompiledGraph<AgentState> compiledGraph = graph.compile();
        System.out.println("\n--- 图编译成功 ---\n");

        System.out.println("=== 运行 Agent 循环 ===\n");

        Map<String, Object> initialState = Map.of(
            "task", "计算斐波那契数列的第5个数",
            "iteration", 0
        );

        Optional<AgentState> result = compiledGraph.invoke(initialState);

        System.out.println("\n=== 执行结果 ===");
        if (result.isPresent()) {
            AgentState finalState = result.get();
            System.out.println("\n最终状态摘要:");
            System.out.println("  迭代次数: " + finalState.value("iteration").orElse(0));
            System.out.println("  状态: " + finalState.value("status").orElse("unknown"));
            System.out.println("  摘要: " + finalState.value("summary").orElse("N/A"));
            System.out.println("  最终答案: " + finalState.value("final_answer").orElse("N/A"));
        }

        System.out.println("\n=== 演示完成 ===");
    }

    private static String generateThought(int iteration, String task, String previousObservation) {
        if (iteration == 0) {
            return "开始任务: " + task + "，需要先理解问题";
        } else {
            return "根据观察: " + previousObservation + "，需要进一步推理";
        }
    }

    private static String executeAction(int iteration, String task, String thought) {
        if (task.contains("斐波那契")) {
            if (iteration == 0) {
                return "计算斐波那契数列: F(1)=1, F(2)=1, F(3)=2, F(4)=3, F(5)=5";
            } else {
                return "验证计算: F(5) = F(4) + F(3) = 3 + 2 = 5";
            }
        }
        return "执行通用行动 #" + iteration;
    }

    private static String generateObservation(int iteration, String action) {
        if (action.contains("斐波那契") && action.contains("F(5)=5")) {
            return "得到结果: F(5) = 5";
        }
        return "观察 #" + iteration + ": 行动已执行";
    }

    private static boolean checkTaskCompletion(String task, String observation, int iteration) {
        if (task.contains("斐波那契") && observation.contains("F(5) = 5")) {
            return true;
        }
        return iteration >= MAX_ITERATIONS;
    }
}
