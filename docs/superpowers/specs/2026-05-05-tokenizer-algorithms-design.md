# BPE & Unigram Tokenizer 设计文档

**日期**: 2026-05-05  
**用途**: 教学演示 - BPE和Unigram子词分词算法的Java实现与对比

---

## 1. 架构设计

```
com.advancedjava.agentscope.tokenizer/
├── BpeTokenizer.java          # BPE算法完整实现（训练+分词）
├── UnigramTokenizer.java      # Unigram算法完整实现（训练+分词）
└── TokenizerDemo.java         # 独立Demo类，对比演示两种算法
```

---

## 2. BPE（Byte-Pair Encoding）模块

### 训练阶段
1. **字符拆分** - 所有训练语料按字符拆分为最小单元，记录词频
2. **迭代合并** - 每次迭代找到最高频相邻符号对，合并为新符号
3. **终止条件** - 词汇表达到指定大小或没有可合并的符号对
4. **输出** - 合并规则列表（符号对→新符号）

### 分词阶段
1. 输入文本按字符拆分
2. 按学习到的合并规则依次应用合并操作
3. 输出最终分词结果

### 核心数据结构
```java
private List<Pair<String, String>> mergeRules;  // 合并规则表
private Map<String, Integer> wordFreqs;          // 词频Map
private Set<String> vocabulary;                  // 词汇表
private int vocabSize;                           // 目标词汇表大小
```

---

## 3. Unigram模块

### 训练阶段
1. **初始词汇** - 收集语料中所有子串构建初始词汇表
2. **EM优化** - 使用期望最大化算法迭代优化子词概率分布
3. **词汇裁剪** - 根据损失贡献裁剪低价值词汇
4. **收缩词表** - 直到达到目标大小

### 分词阶段
1. 使用Viterbi算法寻找最优分词路径
2. 基于对数概率最大化
3. 输出最优子词序列

### 核心数据结构
```java
private Map<String, Double> wordProbabilities;   // 子词概率分布
private int vocabSize;                           // 目标词汇表大小
private int maxIter;                             // EM算法最大迭代次数
```

---

## 4. 对比演示模块

### TokenizerDemo.java 结构
- **main方法** - 统一调度训练、分词、对比输出
- **演示流程**:
  1. 创建相同训练语料（中英文混合句子集）
  2. 初始化BPE和Unigram训练器
  3. 分别训练两个模型（输出训练过程日志）
  4. 对相同测试文本进行分词
  5. 控制台打印对比输出

### 对比输出内容
```
==============================================
BPE vs Unigram - 子词分词算法对比
==============================================

--- 训练语料 ---
["hello world", "hello friends", "world peace"]

--- BPE模型 ---
训练后词汇表大小: XX
分词结果: [he, ll, o, wor, ld]

--- Unigram模型 ---
训练后词汇表大小: XX  
分词结果: [hello, wo, rld]

--- 差异分析 ---
BPE倾向合并高频相邻字符对，产生较长的子词片段
Unigram基于概率选择最优路径，更灵活但计算复杂度较高
```

---

## 5. 教学友好设计

### 注释标准
- 每个方法都有详细的JavaDoc（包括@param, @return, 复杂度分析）
- 关键算法步骤添加行内注释（如`// 步骤1: 统计词频`）
- 特殊逻辑添加解释性注释（如`// 为什么使用对数概率：避免浮点数下溢`）

### 输出友好
- 打印清晰的中文提示
- 训练过程中间状态可选输出
- 分词结果以易读格式展示

---

## 6. 质量要求

- 遵循项目代码风格（Java 17, 4空格缩进，大括号不省略）
- 使用records数据结构（如`record MergePair(String a, String b)`）
- 参数校验清晰
- 无外部依赖，纯Java实现
- 编译可通过
