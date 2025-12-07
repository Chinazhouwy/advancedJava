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
│                   └── demo/                      # 示例程序
│                       ├── AgentTest.java         # Agent 测试类
│                       └── AttachMain.java        # Attach 示例类
```

## 核心功能

### 1. Java Agent 技术

项目演示了Java Agent的两种使用方式：

#### 静态加载（Premain）
- 在JVM启动时通过命令行参数加载Agent
- [MethodAgentMain.java](src/main/java/com/zhouwy/advancedjava/agent/MethodAgentMain.java) 实现了premain方法
- 可以在类加载时对字节码进行修改

#### 动态加载（Agentmain）
- 在JVM运行时动态加载Agent
- [MethodAgentMain.java](src/main/java/com/zhouwy/advancedjava/agent/MethodAgentMain.java) 实现了agentmain方法
- 使用[Javassist](https://www.javassist.org/)库对目标类的方法进行增强

### 2. 多线程编程示例

包含了多个关于Java并发编程的示例：
- [ConditionTest.java](src/main/java/com/zhouwy/advancedjava/concurrency/lock/ConditionTest.java) - Condition条件变量使用示例
- [RenentrantLockTest.java](src/main/java/com/zhouwy/advancedjava/concurrency/lock/RenentrantLockTest.java) - 可重入锁使用示例
- [ThreadsInterrupt.java](src/main/java/com/zhouwy/advancedjava/concurrency/thread/ThreadsInterrupt.java) - 线程中断处理示例
- [forkJoin.java](src/main/java/com/zhouwy/advancedjava/concurrency/forkjoin/forkJoin.java) - Fork/Join框架使用示例

### 3. IO操作示例

提供了各种IO操作的示例代码：
- [CharByte.java](src/main/java/com/zhouwy/advancedjava/io/file/CharByte.java) - 字符与字节流操作示例
- [FileUtils.java](src/main/java/com/zhouwy/advancedjava/io/file/FileUtils.java) - 文件工具类示例
- [RandAccessFileTests.java](src/main/java/com/zhouwy/advancedjava/io/file/RandAccessFileTests.java) - 随机访问文件示例
- [IOTests.java](src/main/java/com/zhouwy/advancedjava/io/IOTests.java) - 基础IO操作示例

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
java -cp target/advancedJava-1.0-SNAPSHOT.jar com.zhouwy.advancedjava.demo.AgentTest
```

然后在另一个终端中运行Attach程序来动态加载Agent：
```bash
java -cp target/advancedJava-1.0-SNAPSHOT-jar-with-dependencies.jar com.zhouwy.advancedjava.demo.AttachMain
```

#### 静态加载Agent方式

```bash
java -javaagent:target/advancedJava-1.0-SNAPSHOT-jar-with-dependencies.jar -cp target/advancedJava-1.0-SNAPSHOT.jar com.zhouwy.advancedjava.demo.AgentTest
```

## 依赖项

- [Javassist 3.25.0-GA](https://www.javassist.org/) - 字节码操作库
- JUnit 4.13.1 - 单元测试框架
- JDK Tools - JVM工具类（用于动态attach到JVM进程）

## 注意事项

1. 项目使用Java 17编译，需要相应版本的JDK才能正常运行
2. 动态Attach功能需要相应的权限设置
3. 在不同操作系统上运行时可能需要调整部分系统相关的代码