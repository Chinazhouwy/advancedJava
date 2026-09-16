package com.advancedjava.agentscope.harmess;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QuickstartExample {

    public static void main(String[] args) throws Exception {
        // 1. 准备工作区：第一次运行生成 AGENTS.md，后续运行复用
        Path workspace = Paths.get(".agentscope/workspace");
        initWorkspaceIfAbsent(workspace);

        // 2. 构建模型
        Model model = OpenAIChatModel.builder()
                .baseUrl(envOrDefault("DEMO_AI_BASE_URL", "https://api.deepseek.com"))
                .apiKey(requiredEnv("DEMO_AI_API_KEY"))
                .modelName(envOrDefault("DEMO_AI_MODEL", "deepseek-chat"))
                .stream(true)
                .build();

        // 3. 构建 HarnessAgent：工作区注入、会话持久化、追踪日志默认开启；
        //    这里显式启用对话压缩，当消息数超过30条时触发压缩，保留最近10条关键消息
        HarnessAgent agent = HarnessAgent.builder()
                .name("quickstart-agent")
                .sysPrompt("你是一个帮助用户做笔记的助手。")
                .model(model)
                .workspace(workspace)
                .compaction(CompactionConfig.builder()
                        .triggerMessages(3)
                        .keepMessages(10)
                        .flushBeforeCompact(true)
                        .build())
                .build();

        // 4. 同一个 RuntimeContext 发起两轮对话
        //    sessionId 相同 → 第二轮自动从 Session 恢复第一轮的状态
        // userId 用于标识当前交互的用户身份，便于在多用户场景下隔离会话数据或进行用户维度的统计与追踪
        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId("demo-session")
                .userId("alice")
                .build();

        Msg turn1 = agent.call(
                Msg.builder().role(MsgRole.USER)
                        .textContent("我叫天宇,今天准备一个关于 ReAct 的技术分享。")
                        .build(),
                ctx).block();
        System.out.println("[turn1] " + turn1.getTextContent());

        Msg turn2 = agent.call(
                Msg.builder().role(MsgRole.USER)
                        .textContent("我叫什么?我今天要干什么?")
                        .build(),
                ctx).block();
        System.out.println("[turn2] " + turn2.getTextContent());

        Msg turn3 = agent.call(
                Msg.builder().role(MsgRole.USER)
                        .textContent("我叫什么?今天天气?")
                        .build(),
                ctx).block();
        System.out.println("[turn3] " + turn3.getTextContent());

        Msg turn4 = agent.call(
                Msg.builder().role(MsgRole.USER)
                        .textContent("我叫什么?汽车买什么牌子好?")
                        .build(),
                ctx).block();
        System.out.println("[turn4] " + turn4.getTextContent());
    }

    private static void initWorkspaceIfAbsent(Path workspace) throws Exception {
        Files.createDirectories(workspace);
        Path agentsMd = workspace.resolve("AGENTS.md");
        if (Files.exists(agentsMd)) return;
        Files.writeString(agentsMd, """
                # 笔记助手

                你是一个帮助用户整理笔记和知识的助手。

                ## 行为约定
                - 主动记录用户提到的关键事实(姓名、计划、偏好等)
                - 回答用简洁中文,必要时给出要点列表
                - 对不确定的内容要主动说明,不要臆造
                """);
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "缺少环境变量 " + name + "。请先执行：set -a; source .env; set +a"
            );
        }
        return value;
    }
}
