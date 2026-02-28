# Advanced Java 示例项目

本项目包含了一系列Java高级特性的示例代码，主要涉及Java Agent技术、多线程编程、IO操作等。

## 项目结构

```
src/
├── main/
│   └── java/
│       └── com/
│           └── zhouwy/
│               └── advancedjava/
│                   ├── agent/                     # Java Agent 相关类
│                   │   └── MethodAgentMain.java   # Agent 主类
│                   ├── concurrency/               # 并发编程相关类
│                   │   ├── lock/
│                   │   │   ├── ConditionTest.java
│                   │   │   └── RenentrantLockTest.java
│                   │   ├── forkjoin/
│                   │   │   └── forkJoin.java
│                   │   └── thread/
│                   │       ├── ThreadApp.java
│                   │       └── ThreadsInterrupt.java
│                   ├── io/                        # IO操作相关类
│                   │   ├── file/
│                   │   │   ├── CharByte.java
│                   │   │   ├── FileUtils.java
│                   │   │   └── RandAccessFileTests.java
│                   │   └── IOTests.java
│                   ├── interview/                 # 面试题示例
│                   │   ├── records/
│                   │   │   ├── CandidateProfile.java
│                   │   │   ├── CandidateProfileClassic.java
│                   │   │   ├── InterviewResult.java
│                   │   │   ├── InterviewResultClassic.java
│                   │   │   └── RecordDemoApp.java
│                   │   ├── serialization/
│                   │   │   ├── CandidatePayloadRecord.java
│                   │   │   ├── CandidatePayloadClassic.java
│                   │   │   └── JsonSerializationDemoApp.java
│                   │   └── frameworks/
│                   │       ├── spring/
│                   │       │   ├── InterviewAiProperties.java
│                   │       │   ├── InterviewAiConfig.java
│                   │       │   └── InterviewRecordController.java
│                   │       ├── springdata/
│                   │       │   ├── InterviewResultEntity.java
│                   │       │   ├── InterviewSummary.java
│                   │       │   └── InterviewResultRepository.java
│                   │       └── jdk/
│                   │           └── RecordIntrospectionDemo.java
│                   └── demo/                      # 示例程序
│                       ├── AgentTest.java         # Agent 测试类
│                       └── AttachMain.java        # Attach 示例类
```

## 核心功能

### 1. Java Agent 技术

项目演示了Java Agent的两种使用方式：

#### 静态加载（Premain）
- 在JVM启动时通过命令行参数加载Agent
- [MethodAgentMain.java](src/main/java/com/advancedjava/agent/MethodAgentMain.java) 实现了premain方法
- 可以在类加载时对字节码进行修改

#### 动态加载（Agentmain）
- 在JVM运行时动态加载Agent
- [MethodAgentMain.java](src/main/java/com/advancedjava/agent/MethodAgentMain.java) 实现了agentmain方法
- 使用[Javassist](https://www.javassist.org/)库对目标类的方法进行增强

### 2. 多线程编程示例

包含了多个关于Java并发编程的示例：
- [ConditionTest.java](src/main/java/com/advancedjava/concurrency/lock/ConditionTest.java) - Condition条件变量使用示例
- [RenentrantLockTest.java](src/main/java/com/advancedjava/concurrency/lock/RenentrantLockTest.java) - 可重入锁使用示例
- [ThreadsInterrupt.java](src/main/java/com/advancedjava/concurrency/thread/ThreadsInterrupt.java) - 线程中断处理示例
- [forkJoin.java](src/main/java/com/advancedjava/concurrency/forkjoin/forkJoin.java) - Fork/Join框架使用示例

### 3. IO操作示例

提供了各种IO操作的示例代码：
- [CharByte.java](src/main/java/com/advancedjava/io/file/CharByte.java) - 字符与字节流操作示例
- [FileUtils.java](src/main/java/com/advancedjava/io/file/FileUtils.java) - 文件工具类示例
- [RandAccessFileTests.java](src/main/java/com/advancedjava/io/file/RandAccessFileTests.java) - 随机访问文件示例
- [IOTests.java](src/main/java/com/advancedjava/io/IOTests.java) - 基础IO操作示例

### 4. Java Record 面试示例

新增了 `record` 的面试高频用法示例：
- [CandidateProfile.java](src/main/java/com/advancedjava/interview/records/CandidateProfile.java) - 紧凑构造器校验、重载构造器、防御性拷贝
- [CandidateProfileClassic.java](src/main/java/com/advancedjava/interview/records/CandidateProfileClassic.java) - 传统POJO等价实现（字段/getter/equals/hashCode/toString 手写）
- [InterviewResult.java](src/main/java/com/advancedjava/interview/records/InterviewResult.java) - 组合record、静态工厂方法、参数归一化
- [InterviewResultClassic.java](src/main/java/com/advancedjava/interview/records/InterviewResultClassic.java) - 传统POJO等价实现（便于与record对照）
- [RecordDemoApp.java](src/main/java/com/advancedjava/interview/records/RecordDemoApp.java) - 访问器、值相等、toString 示例
- [CandidateProfileTest.java](src/test/java/com/advancedjava/interview/records/CandidateProfileTest.java) - 单元测试覆盖核心行为

对照重点：
1. record 访问器是 `name()`，传统POJO通常是 `getName()`
2. record 自动生成 `equals/hashCode/toString`，传统POJO需要手写
3. record 同样可以写构造器校验和业务方法，不只是“裸字段”

### 5. Jackson/Fastjson2 序列化示例

新增了 `record + 传统POJO` 在两种 JSON 工具下的对照示例：
- [CandidatePayloadRecord.java](src/main/java/com/advancedjava/interview/serialization/CandidatePayloadRecord.java) - record + 旧字段别名兼容（`years` -> `yearsOfExperience`）
- [CandidatePayloadClassic.java](src/main/java/com/advancedjava/interview/serialization/CandidatePayloadClassic.java) - 传统POJO写法对照
- [JsonSerializationDemoApp.java](src/main/java/com/advancedjava/interview/serialization/JsonSerializationDemoApp.java) - Jackson/Fastjson2 序列化与反序列化演示
- [JsonSerializationExamplesTest.java](src/test/java/com/advancedjava/interview/serialization/JsonSerializationExamplesTest.java) - 兼容性与往返测试

### 6. 主流框架 Record 用法示例

新增了 `Spring Boot / Spring Web / Spring Data JPA / JDK` 的 record 代码示例：
- [InterviewAiProperties.java](src/main/java/com/advancedjava/interview/frameworks/spring/InterviewAiProperties.java) - `@ConfigurationProperties` + record 配置绑定
- [InterviewRecordController.java](src/main/java/com/advancedjava/interview/frameworks/spring/InterviewRecordController.java) - Spring Web 请求/响应 DTO 使用 record
- [InterviewResultRepository.java](src/main/java/com/advancedjava/interview/frameworks/springdata/InterviewResultRepository.java) - JPA 用 record 做查询投影（constructor expression）
- [RecordIntrospectionDemo.java](src/main/java/com/advancedjava/interview/frameworks/jdk/RecordIntrospectionDemo.java) - JDK 反射 API 读取 record 元数据
- [RecordIntrospectionDemoTest.java](src/test/java/com/advancedjava/interview/frameworks/jdk/RecordIntrospectionDemoTest.java) - JDK record 反射测试

## 构建和运行

项目使用Maven进行构建管理，需要Java 17环境。

### 编译打包

```bash
mvn clean package
```

该命令会生成两个jar包：
1. `advancedJava-1.0-SNAPSHOT.jar` - 普通jar包
2. `advancedJava-1.0-SNAPSHOT-jar-with-dependencies.jar` - 包含所有依赖的uber-jar

### 运行示例

#### 运行Agent测试程序

首先运行目标应用程序：
```bash
java -cp target/advancedJava-1.0-SNAPSHOT.jar com.advancedjava.demo.AgentTest
```

然后在另一个终端中运行Attach程序来动态加载Agent：
```bash
java -cp target/advancedJava-1.0-SNAPSHOT-jar-with-dependencies.jar com.advancedjava.demo.AttachMain
```

#### 静态加载Agent方式

```bash
java -javaagent:target/advancedJava-1.0-SNAPSHOT-jar-with-dependencies.jar -cp target/advancedJava-1.0-SNAPSHOT.jar com.advancedjava.demo.AgentTest
```

#### 运行 Record 示例

```bash
mkdir -p /tmp/record-demo-classes
javac -d /tmp/record-demo-classes \
  src/main/java/com/advancedjava/interview/records/CandidateProfile.java \
  src/main/java/com/advancedjava/interview/records/CandidateProfileClassic.java \
  src/main/java/com/advancedjava/interview/records/InterviewResult.java \
  src/main/java/com/advancedjava/interview/records/InterviewResultClassic.java \
  src/main/java/com/advancedjava/interview/records/RecordDemoApp.java
java -cp /tmp/record-demo-classes com.advancedjava.interview.records.RecordDemoApp
```

#### 运行 Record 单元测试

```bash
mkdir -p /tmp/record-test-classes
javac -d /tmp/record-test-classes \
  -cp "$HOME/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar:$HOME/.m2/repository/org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar" \
  src/main/java/com/advancedjava/interview/records/CandidateProfile.java \
  src/main/java/com/advancedjava/interview/records/CandidateProfileClassic.java \
  src/main/java/com/advancedjava/interview/records/InterviewResult.java \
  src/main/java/com/advancedjava/interview/records/InterviewResultClassic.java \
  src/test/java/com/advancedjava/interview/records/CandidateProfileTest.java
java -cp "/tmp/record-test-classes:$HOME/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar:$HOME/.m2/repository/org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar" \
  org.junit.runner.JUnitCore com.advancedjava.interview.records.CandidateProfileTest
```

#### 运行 Jackson/Fastjson2 示例

```bash
mkdir -p /tmp/serialization-demo-classes
javac -d /tmp/serialization-demo-classes \
  -cp "$HOME/.m2/repository/com/alibaba/fastjson2/fastjson2/2.0.60/fastjson2-2.0.60.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.17.1/jackson-databind-2.17.1.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.17.1/jackson-core-2.17.1.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.17.1/jackson-annotations-2.17.1.jar" \
  src/main/java/com/advancedjava/interview/serialization/CandidatePayloadRecord.java \
  src/main/java/com/advancedjava/interview/serialization/CandidatePayloadClassic.java \
  src/main/java/com/advancedjava/interview/serialization/JsonSerializationDemoApp.java
java -cp "/tmp/serialization-demo-classes:$HOME/.m2/repository/com/alibaba/fastjson2/fastjson2/2.0.60/fastjson2-2.0.60.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.17.1/jackson-databind-2.17.1.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.17.1/jackson-core-2.17.1.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.17.1/jackson-annotations-2.17.1.jar" \
  com.advancedjava.interview.serialization.JsonSerializationDemoApp
```

#### 运行 Jackson/Fastjson2 测试

```bash
mkdir -p /tmp/serialization-test-classes
javac -d /tmp/serialization-test-classes \
  -cp "$HOME/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar:$HOME/.m2/repository/org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar:$HOME/.m2/repository/com/alibaba/fastjson2/fastjson2/2.0.60/fastjson2-2.0.60.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.17.1/jackson-databind-2.17.1.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.17.1/jackson-core-2.17.1.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.17.1/jackson-annotations-2.17.1.jar" \
  src/main/java/com/advancedjava/interview/serialization/CandidatePayloadRecord.java \
  src/main/java/com/advancedjava/interview/serialization/CandidatePayloadClassic.java \
  src/test/java/com/advancedjava/interview/serialization/JsonSerializationExamplesTest.java
java -cp "/tmp/serialization-test-classes:$HOME/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar:$HOME/.m2/repository/org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar:$HOME/.m2/repository/com/alibaba/fastjson2/fastjson2/2.0.60/fastjson2-2.0.60.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.17.1/jackson-databind-2.17.1.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.17.1/jackson-core-2.17.1.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.17.1/jackson-annotations-2.17.1.jar" \
  org.junit.runner.JUnitCore com.advancedjava.interview.serialization.JsonSerializationExamplesTest
```

#### 运行 JDK Record 反射示例

```bash
mkdir -p /tmp/framework-jdk-demo-classes
javac -d /tmp/framework-jdk-demo-classes \
  src/main/java/com/advancedjava/interview/frameworks/jdk/RecordIntrospectionDemo.java
java -cp /tmp/framework-jdk-demo-classes \
  com.advancedjava.interview.frameworks.jdk.RecordIntrospectionDemo
```

#### 运行 JDK Record 反射测试

```bash
mkdir -p /tmp/framework-jdk-test-classes
javac -d /tmp/framework-jdk-test-classes \
  -cp "$HOME/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar:$HOME/.m2/repository/org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar" \
  src/main/java/com/advancedjava/interview/frameworks/jdk/RecordIntrospectionDemo.java \
  src/test/java/com/advancedjava/interview/frameworks/jdk/RecordIntrospectionDemoTest.java
java -cp "/tmp/framework-jdk-test-classes:$HOME/.m2/repository/junit/junit/4.13.1/junit-4.13.1.jar:$HOME/.m2/repository/org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar" \
  org.junit.runner.JUnitCore com.advancedjava.interview.frameworks.jdk.RecordIntrospectionDemoTest
```

## 依赖项

- [Javassist 3.25.0-GA](https://www.javassist.org/) - 字节码操作库
- JUnit 4.13.1 - 单元测试框架
- JDK Tools - JVM工具类（用于动态attach到JVM进程）

## 注意事项

1. 项目使用Java 17编译，需要相应版本的JDK才能正常运行
2. 动态Attach功能需要相应的权限设置
3. 在不同操作系统上运行时可能需要调整部分系统相关的代码
