package com.advancedjava.ai.langchain4j.advanced;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LangChain4j 工具调用演示
 *
 * <p>展示如何定义带有 @Tool 注解的工具类，以及工具调用的基本模式。
 */
public class ToolCallingDemo {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j 工具调用演示 ===\n");

        // 1. 创建工具实例
        System.out.println("1. 创建工具实例...");
        CalculatorTool calculator = new CalculatorTool();
        WeatherTool weather = new WeatherTool();
        DateTimeTool dateTime = new DateTimeTool();
        System.out.println("   ✓ 工具实例已创建\n");

        // 2. 演示直接调用工具方法
        System.out.println("2. 直接调用计算器工具...");
        System.out.println("   10 + 20 = " + calculator.add(10, 20));
        System.out.println("   100 - 30 = " + calculator.subtract(100, 30));
        System.out.println("   5 * 6 = " + calculator.multiply(5, 6));
        System.out.println();

        // 3. 演示天气工具
        System.out.println("3. 调用天气工具...");
        System.out.println("   北京天气: " + weather.getWeather("北京"));
        System.out.println("   上海天气: " + weather.getWeather("上海"));
        System.out.println();

        // 4. 演示日期时间工具
        System.out.println("4. 调用日期时间工具...");
        System.out.println("   当前日期: " + dateTime.getCurrentDate());
        System.out.println("   当前时间: " + dateTime.getCurrentTime());
        System.out.println("   日期时间: " + dateTime.getCurrentDateTime());
        System.out.println();

        // 5. 展示工具规范（ToolSpecification）
        System.out.println("5. 工具规范（ToolSpecification）说明：");
        System.out.println("   在真实场景中，LangChain4j 会自动扫描 @Tool 注解的方法");
        System.out.println("   并生成 ToolSpecification 供 LLM 理解工具的功能和参数。");
        System.out.println();
        System.out.println("   ToolSpecification 包含：");
        System.out.println("   - name: 工具名称（方法名）");
        System.out.println("   - description: 工具描述（来自 @Tool 注解）");
        System.out.println("   - parameters: 参数模式（类型、描述等）");
        System.out.println();

        // 6. 展示工具调用流程
        System.out.println("6. 工具调用典型流程（概念说明）：");
        System.out.println("   步骤1: 用户发送问题（如'北京今天天气怎么样？'）");
        System.out.println("   步骤2: LLM 分析需要调用天气工具");
        System.out.println("   步骤3: LLM 生成工具调用请求（城市=北京）");
        System.out.println("   步骤4: 框架执行工具方法，获取结果");
        System.out.println("   步骤5: 将工具结果返回给 LLM");
        System.out.println("   步骤6: LLM 生成最终回复");
        System.out.println();

        System.out.println("=== 演示结束 ===");
        System.out.println();
        System.out.println("要点回顾：");
        System.out.println("- @Tool 注解用于标记可被 LLM 调用的方法");
        System.out.println("- 工具方法需要有清晰的命名和描述");
        System.out.println("- 参数类型会被自动解析为工具规范");
        System.out.println("- 框架负责处理 LLM 与工具之间的协调");
    }

    /**
     * 计算器工具类
     */
    public static class CalculatorTool {

        @Tool("将两个数字相加")
        public int add(int a, int b) {
            return a + b;
        }

        @Tool("将第一个数字减去第二个数字")
        public int subtract(int a, int b) {
            return a - b;
        }

        @Tool("将两个数字相乘")
        public int multiply(int a, int b) {
            return a * b;
        }

        @Tool("将第一个数字除以第二个数字")
        public double divide(int a, int b) {
            if (b == 0) {
                throw new IllegalArgumentException("除数不能为零");
            }
            return (double) a / b;
        }
    }

    /**
     * 天气查询工具类
     */
    public static class WeatherTool {

        @Tool("查询指定城市的当前天气情况")
        public String getWeather(String city) {
            // 模拟天气查询
            String[] weathers = {"晴朗", "多云", "小雨", "阴天"};
            int index = city.hashCode() % weathers.length;
            if (index < 0) {
                index = -index;
            }
            return weathers[index] + "，温度 20-25°C";
        }

        @Tool("查询指定城市的温度范围")
        public String getTemperatureRange(String city) {
            return "15°C - 28°C";
        }
    }

    /**
     * 日期时间工具类
     */
    public static class DateTimeTool {

        @Tool("获取当前日期（格式：yyyy-MM-dd）")
        public String getCurrentDate() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        @Tool("获取当前时间（格式：HH:mm:ss）")
        public String getCurrentTime() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }

        @Tool("获取当前完整的日期和时间")
        public String getCurrentDateTime() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
