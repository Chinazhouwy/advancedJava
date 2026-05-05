package com.advancedjava.ai.agentscope.advanced;

import io.agentscope.core.agent.AgentBase;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * AgentScope ReAct模式演示
 * ReAct (Reasoning + Acting) 是一种让Agent通过"思考-行动-观察"循环解决问题的模式
 */
public class ReActAgentDemo {

    public static void main(String[] args) {
        // 创建ReAct Agent实例
        ReActAgent agent = new ReActAgent("react-agent", "具备ReAct推理能力的Agent");

        System.out.println("=== AgentScope ReAct模式演示 ===");
        System.out.println("Agent ID: " + agent.getAgentId());
        System.out.println("Agent名称: " + agent.getName());
        System.out.println();

        // 测试ReAct推理：数学问题
        String question1 = "如果一本书有240页，每天读30页，需要几天读完？";
        testReAct(agent, question1);

        // 测试ReAct推理：多步骤问题
        String question2 = "从北京到上海距离约1200公里，高铁速度300公里/小时，需要多长时间？";
        testReAct(agent, question2);
    }

    /**
     * 测试ReAct Agent对问题的推理过程
     */
    private static void testReAct(ReActAgent agent, String question) {
        Msg userMessage = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(question).build())
                .build();

        System.out.println("用户问题: " + question);
        System.out.println("=".repeat(60));

        Msg response = agent.call(List.of(userMessage)).block();
        System.out.println("\n最终答案: " + response.getTextContent());
        System.out.println("\n" + "#".repeat(60) + "\n");
    }

    /**
     * ReAct Agent实现
     * 模拟Thought-Action-Observation循环
     */
    static class ReActAgent extends AgentBase {
        // 记录推理步骤的最大数量
        private static final int MAX_STEPS = 3;

        public ReActAgent(String name, String description) {
            super(name, description);
        }

        @Override
        protected Mono<Msg> doCall(List<Msg> msgs) {
            String question = extractQuestion(msgs);
            List<String> reasoningTrace = new ArrayList<>();

            System.out.println("【开始ReAct推理循环】");

            // ReAct循环：Thought -> Action -> Observation
            String currentThought = "";
            String finalAnswer = "";

            for (int step = 1; step <= MAX_STEPS; step++) {
                System.out.println("\n--- 推理步骤 " + step + " ---");

                // Step 1: Thought（思考）
                currentThought = generateThought(question, reasoningTrace, step);
                System.out.println("[Thought] " + currentThought);
                reasoningTrace.add("Step " + step + " Thought: " + currentThought);

                // Step 2: Action（行动）
                Action action = determineAction(currentThought);
                System.out.println("[Action] " + action.type + ": " + action.content);
                reasoningTrace.add("Step " + step + " Action: " + action.type);

                // Step 3: Observation（观察）
                String observation = executeAction(action, question);
                System.out.println("[Observation] " + observation);
                reasoningTrace.add("Step " + step + " Observation: " + observation);

                // 检查是否已经得到答案
                if (action.type == ActionType.FINAL_ANSWER) {
                    finalAnswer = action.content;
                    break;
                }

                // 模拟计算或查找的结果
                if (step == MAX_STEPS) {
                    finalAnswer = generateFinalAnswer(question, reasoningTrace);
                }
            }

            System.out.println("\n【ReAct推理循环结束】");

            Msg response = Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text(finalAnswer).build())
                    .build();

            return Mono.just(response);
        }

        /**
         * 从消息中提取问题文本
         */
        private String extractQuestion(List<Msg> msgs) {
            if (msgs.isEmpty()) {
                return "";
            }
            return msgs.get(msgs.size() - 1).getTextContent();
        }

        /**
         * 生成当前步骤的思考内容
         */
        private String generateThought(String question, List<String> trace, int step) {
            // 模拟LLM的思考过程
            if (step == 1) {
                return "我需要分析这个问题，找出关键数字和运算关系。";
            } else if (step == 2) {
                return "让我进行具体的计算来解决这个问题。";
            } else {
                return "基于前面的计算，我现在可以得出最终答案了。";
            }
        }

        /**
         * 根据思考内容决定下一步行动
         */
        private Action determineAction(String thought) {
            if (thought.contains("最终答案") || thought.contains("得出")) {
                return new Action(ActionType.FINAL_ANSWER, "根据计算得出结果");
            } else if (thought.contains("计算")) {
                return new Action(ActionType.CALCULATE, "执行数学运算");
            } else {
                return new Action(ActionType.ANALYZE, "分析问题结构");
            }
        }

        /**
         * 执行行动并返回观察结果
         */
        private String executeAction(Action action, String question) {
            return switch (action.type) {
                case ANALYZE -> "从问题中提取关键信息：数字和运算关系";
                case CALCULATE -> {
                    // 模拟计算结果
                    if (question.contains("240") && question.contains("30")) {
                        yield "计算: 240 ÷ 30 = 8";
                    } else if (question.contains("1200") && question.contains("300")) {
                        yield "计算: 1200 ÷ 300 = 4";
                    } else {
                        yield "执行了相关计算";
                    }
                }
                case FINAL_ANSWER -> "准备输出最终答案";
            };
        }

        /**
         * 生成最终答案
         */
        private String generateFinalAnswer(String question, List<String> trace) {
            if (question.contains("240") && question.contains("30")) {
                return "需要8天读完这本书。计算过程：240页 ÷ 30页/天 = 8天";
            } else if (question.contains("1200") && question.contains("300")) {
                return "需要4小时。计算过程：1200公里 ÷ 300公里/小时 = 4小时";
            } else {
                return "基于推理过程得出的答案";
            }
        }

        @Override
        protected Mono<Msg> handleInterrupt(io.agentscope.core.interruption.InterruptContext context, Msg... msgs) {
            Msg interruptResponse = Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text("ReAct推理被中断").build())
                    .build();
            return Mono.just(interruptResponse);
        }
    }

    /**
     * 行动类型枚举
     */
    enum ActionType {
        ANALYZE,
        CALCULATE,
        FINAL_ANSWER
    }

    /**
     * 行动记录
     */
    static class Action {
        final ActionType type;
        final String content;

        Action(ActionType type, String content) {
            this.type = type;
            this.content = content;
        }
    }
}
