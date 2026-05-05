# BPE & Unigram Tokenizer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现BPE和Unigram子词分词算法的教学演示版本，包含完整训练+分词流程，并提供对比演示。

**Architecture:** 三个独立Java类：BpeTokenizer（BPE算法）、UnigramTokenizer（Unigram算法）、TokenizerDemo（对比演示）。每个算法类包含train()和tokenize()两个核心方法，使用records作为辅助数据结构。

**Tech Stack:** Java 17, Maven, 纯Java实现无外部依赖，遵循项目代码风格

---

## File Structure

```
src/main/java/com/advancedjava/agentscope/tokenizer/
├── BpeTokenizer.java          # BPE算法实现（~250行）
├── UnigramTokenizer.java      # Unigram算法实现（~300行）
└── TokenizerDemo.java         # 对比演示类（~100行）

src/test/java/com/advancedjava/agentscope/tokenizer/
├── BpeTokenizerTest.java      # BPE单元测试（~150行）
└── UnigramTokenizerTest.java  # Unigram单元测试（~150行）
```

---

## Task 1: 创建包结构和基础工具类

**Files:**
- Create: `src/main/java/com/advancedjava/agentscope/tokenizer/BpeTokenizer.java`（骨架）
- Create: `src/main/java/com/advancedjava/agentscope/tokenizer/UnigramTokenizer.java`（骨架）

- [ ] **Step 1: 创建BpeTokenizer骨架类**

```java
package com.advancedjava.agentscope.tokenizer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BPE（Byte-Pair Encoding）子词分词算法实现。
 *
 * <p>BPE是一种基于统计的子词分词算法，通过迭代合并最高频的相邻字符对来构建词汇表。
 * 该实现提供完整的训练和分词功能，适合教学演示。
 *
 * <p>算法原理：
 * 1. 训练阶段：从字符级别开始，每次合并最高频的相邻符号对
 * 2. 分词阶段：应用学到的合并规则对文本进行分词
 *
 * <p>时间复杂度：
 * - 训练：O(V * N)，其中V为目标词汇表大小，N为语料长度
 * - 分词：O(M * R)，其中M为文本长度，R为合并规则数量
 *
 * @see <a href="https://arxiv.org/abs/1508.07909">BPE原始论文</a>
 */
public class BpeTokenizer {
    
    /** 合并规则：记录每次合并的字符对及其优先级 */
    private final List<MergeRule> mergeRules;
    
    /** 词汇表：包含所有基础字符和合并生成的子词 */
    private final Set<String> vocabulary;
    
    /** 目标词汇表大小 */
    private final int vocabSize;
    
    /**
     * 构造BPE分词器。
     *
     * @param vocabSize 目标词汇表大小（必须大于基础字符集大小）
     * @throws IllegalArgumentException 如果vocabSize小于2
     */
    public BpeTokenizer(int vocabSize) {
        if (vocabSize < 2) {
            throw new IllegalArgumentException("词汇表大小必须至少为2");
        }
        this.vocabSize = vocabSize;
        this.mergeRules = new ArrayList<>();
        this.vocabulary = new HashSet<>();
    }
    
    /**
     * 训练BPE模型。
     *
     * @param corpus 训练语料（字符串列表）
     * @throws IllegalArgumentException 如果corpus为空或包含空字符串
     */
    public void train(List<String> corpus) {
        // TODO: 待实现
    }
    
    /**
     * 使用训练好的模型对文本进行分词。
     *
     * @param text 待分词文本
     * @return 分词结果列表
     * @throws IllegalStateException 如果模型未训练
     * @throws IllegalArgumentException 如果text为空
     */
    public List<String> tokenize(String text) {
        // TODO: 待实现
        return Collections.emptyList();
    }
    
    /**
     * 获取词汇表内容。
     *
     * @return 词汇表集合（只读视图）
     */
    public Set<String> getVocabulary() {
        return Collections.unmodifiableSet(vocabulary);
    }
    
    /**
     * 获取合并规则数量。
     *
     * @return 合并规则数量
     */
    public int getMergeRuleCount() {
        return mergeRules.size();
    }
    
    /**
     * 合并规则记录。
     * 
     * <p>记录合并的两个子词及其优先级（训练时学到的顺序）。
     */
    public record MergeRule(String first, String second, int priority) {
        
        /**
         * 获取合并后的结果。
         *
         * @return 合并后的子词
         */
        public String merged() {
            return first + second;
        }
        
        @Override
        public String toString() {
            return String.format("规则#%d: %s + %s → %s", priority, first, second, merged());
        }
    }
}
```

- [ ] **Step 2: 创建UnigramTokenizer骨架类**

```java
package com.advancedjava.agentscope.tokenizer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Unigram子词分词算法实现。
 *
 * <p>Unigram是一种基于概率的子词分词算法，使用EM算法迭代优化子词概率分布。
 * 该实现提供完整的训练和分词功能，适合教学演示。
 *
 * <p>算法原理：
 * 1. 训练阶段：构建初始词汇表，使用EM算法优化概率，裁剪词汇表
 * 2. 分词阶段：使用Viterbi算法寻找最优分词路径
 *
 * <p>时间复杂度：
 * - 训练：O(V² * I)，其中V为词汇表大小，I为EM迭代次数
 * - 分词：O(N * V)，其中N为文本长度，V为词汇表大小（使用Trie可优化）
 *
 * @see <a href="https://arxiv.org/abs/1804.10959">Unigram原始论文</a>
 */
public class UnigramTokenizer {
    
    /** 子词概率分布 */
    private final Map<String, Double> wordProbabilities;
    
    /** 目标词汇表大小 */
    private final int vocabSize;
    
    /** EM算法最大迭代次数 */
    private final int maxIterations;
    
    /**
     * 构造Unigram分词器。
     *
     * @param vocabSize 目标词汇表大小（必须大于基础字符集大小）
     * @param maxIterations EM算法最大迭代次数（建议10-50）
     * @throws IllegalArgumentException 如果参数不合法
     */
    public UnigramTokenizer(int vocabSize, int maxIterations) {
        if (vocabSize < 2) {
            throw new IllegalArgumentException("词汇表大小必须至少为2");
        }
        if (maxIterations < 1) {
            throw new IllegalArgumentException("迭代次数必须至少为1");
        }
        this.vocabSize = vocabSize;
        this.maxIterations = maxIterations;
        this.wordProbabilities = new HashMap<>();
    }
    
    /**
     * 训练Unigram模型。
     *
     * @param corpus 训练语料（字符串列表）
     * @throws IllegalArgumentException 如果corpus为空或包含空字符串
     */
    public void train(List<String> corpus) {
        // TODO: 待实现
    }
    
    /**
     * 使用训练好的模型对文本进行分词。
     *
     * @param text 待分词文本
     * @return 分词结果列表
     * @throws IllegalStateException 如果模型未训练
     * @throws IllegalArgumentException 如果text为空
     */
    public List<String> tokenize(String text) {
        // TODO: 待实现
        return Collections.emptyList();
    }
    
    /**
     * 获取词汇表大小。
     *
     * @return 当前词汇表大小
     */
    public int getVocabularySize() {
        return wordProbabilities.size();
    }
    
    /**
     * 获取词汇表内容。
     *
     * @return 词汇表集合（只读视图）
     */
    public Set<String> getVocabulary() {
        return Collections.unmodifiableSet(wordProbabilities.keySet());
    }
}
```

- [ ] **Step 3: 编译验证骨架**

Run: `mvn clean compile`
Expected: 编译成功，无错误

- [ ] **Step 4: Commit骨架**

```bash
git add src/main/java/com/advancedjava/agentscope/tokenizer/
git commit -m "feat(tokenizer): 添加BPE和Unigram tokenizer骨架类"
```

---

## Task 2: 实现BPE训练功能

**Files:**
- Modify: `src/main/java/com/advancedjava/agentscope/tokenizer/BpeTokenizer.java:train()方法`
- Create: `src/test/java/com/advancedjava/agentscope/tokenizer/BpeTokenizerTest.java`

- [ ] **Step 1: 写BPE训练测试**

```java
package com.advancedjava.agentscope.tokenizer;

import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * BPE分词器单元测试。
 */
public class BpeTokenizerTest {
    
    @Test
    public void shouldTrainOnSimpleCorpus() {
        // 准备简单训练语料
        List<String> corpus = Arrays.asList("hello", "hello", "world");
        
        // 创建BPE分词器（词汇表大小10）
        BpeTokenizer tokenizer = new BpeTokenizer(10);
        
        // 训练模型
        tokenizer.train(corpus);
        
        // 验证：词汇表应包含基础字符和至少一个合并结果
        assertThat(tokenizer.getVocabulary(), hasItem("h"));
        assertThat(tokenizer.getVocabulary(), hasItem("e"));
        assertThat(tokenizer.getVocabulary(), hasItem("l"));
        assertThat(tokenizer.getVocabulary(), hasItem("o"));
        
        // 验证：应有至少一个合并规则（因为"ll"在"hello"中出现两次）
        assertThat(tokenizer.getMergeRuleCount(), greaterThan(0));
        
        // 验证：词汇表大小不应超过目标大小
        assertThat(tokenizer.getVocabulary().size(), lessThanOrEqualTo(10));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectEmptyCorpus() {
        BpeTokenizer tokenizer = new BpeTokenizer(10);
        tokenizer.train(Arrays.asList());  // 应抛异常
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullInCorpus() {
        BpeTokenizer tokenizer = new BpeTokenizer(10);
        tokenizer.train(Arrays.asList("hello", null));  // 应抛异常
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=BpeTokenizerTest`
Expected: FAIL（train()方法未实现，TODO待填充）

- [ ] **Step 3: 实现BPE训练核心逻辑**

```java
// 在BpeTokenizer.java的train()方法中实现：

/**
 * 训练BPE模型。
 *
 * <p>训练流程：
 * 1. 初始化：将所有单词拆分为字符，统计词频
 * 2. 迭代：每次找最高频相邻符号对，合并并加入词汇表
 * 3. 终止：词汇表达到目标大小或无更多可合并对
 *
 * @param corpus 训练语料（字符串列表）
 * @throws IllegalArgumentException 如果corpus为空或包含空字符串
 */
public void train(List<String> corpus) {
    // 参数校验
    if (corpus == null || corpus.isEmpty()) {
        throw new IllegalArgumentException("训练语料不能为空");
    }
    if (corpus.stream().anyMatch(s -> s == null || s.isEmpty())) {
        throw new IllegalArgumentException("语料中不能包含空字符串");
    }
    
    // 步骤1: 统计单词频次，并将每个单词拆分为字符序列
    // 使用List<String>表示单词的字符序列，便于后续合并操作
    Map<List<String>, Integer> wordFreqs = new HashMap<>();
    
    for (String word : corpus) {
        // 拆分为字符序列：例如 "hello" → ["h", "e", "l", "l", "o"]
        List<String> symbols = word.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.toList());
        
        // 统计频次
        wordFreqs.merge(symbols, 1, Integer::sum);
    }
    
    // 步骤2: 初始化词汇表（所有基础字符）
    wordFreqs.keySet().stream()
            .flatMap(List::stream)
            .forEach(vocabulary::add);
    
    // 步骤3: 迭代合并最高频相邻符号对
    int priority = 0;  // 合并优先级（训练顺序）
    
    while (vocabulary.size() < vocabSize) {
        // 统计所有相邻符号对的频次
        Map<StringPair, Integer> pairFreqs = countPairFreqs(wordFreqs);
        
        if (pairFreqs.isEmpty()) {
            // 无更多可合并对，提前终止
            break;
        }
        
        // 找到最高频符号对
        StringPair bestPair = findMaxFreqPair(pairFreqs);
        
        // 生成新符号并加入词汇表
        String newSymbol = bestPair.first + bestPair.second;
        vocabulary.add(newSymbol);
        
        // 记录合并规则
        mergeRules.add(new MergeRule(bestPair.first, bestPair.second, priority++));
        
        // 应用合并：更新所有包含该符号对的单词
        wordFreqs = mergePair(wordFreqs, bestPair);
    }
}

/**
 * 统计相邻符号对的频次。
 *
 * @param wordFreqs 单词频次表（单词已拆分为字符序列）
 * @return 符号对频次表
 */
private Map<StringPair, Integer> countPairFreqs(Map<List<String>, Integer> wordFreqs) {
    Map<StringPair, Integer> pairFreqs = new HashMap<>();
    
    for (Map.Entry<List<String>, Integer> entry : wordFreqs.entrySet()) {
        List<String> symbols = entry.getKey();
        int freq = entry.getValue();
        
        // 遍历相邻符号对
        for (int i = 0; i < symbols.size() - 1; i++) {
            StringPair pair = new StringPair(symbols.get(i), symbols.get(i + 1));
            pairFreqs.merge(pair, freq, Integer::sum);
        }
    }
    
    return pairFreqs;
}

/**
 * 找到频次最高的符号对。
 *
 * @param pairFreqs 符号对频次表
 * @return 最高频符号对
 */
private StringPair findMaxFreqPair(Map<StringPair, Integer> pairFreqs) {
    return pairFreqs.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElseThrow(() -> new IllegalStateException("符号对频次表为空"));
}

/**
 * 应用合并规则：将指定符号对合并为新符号。
 *
 * @param wordFreqs 单词频次表
 * @param pair 要合并的符号对
 * @return 更新后的单词频次表
 */
private Map<List<String>, Integer> mergePair(
        Map<List<String>, Integer> wordFreqs, StringPair pair) {
    
    Map<List<String>, Integer> newWordFreqs = new HashMap<>();
    String merged = pair.first + pair.second;
    
    for (Map.Entry<List<String>, Integer> entry : wordFreqs.entrySet()) {
        List<String> symbols = entry.getKey();
        int freq = entry.getValue();
        
        // 替换所有匹配的符号对
        List<String> newSymbols = new ArrayList<>();
        int i = 0;
        
        while (i < symbols.size()) {
            // 检查当前位置是否匹配要合并的符号对
            if (i < symbols.size() - 1 
                    && symbols.get(i).equals(pair.first) 
                    && symbols.get(i + 1).equals(pair.second)) {
                newSymbols.add(merged);
                i += 2;  // 跳过已合并的两个符号
            } else {
                newSymbols.add(symbols.get(i));
                i += 1;
            }
        }
        
        newWordFreqs.put(newSymbols, freq);
    }
    
    return newWordFreqs;
}

/**
 * 符号对辅助类（用于统计频次）。
 */
private record StringPair(String first, String second) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringPair(String f, String s))) return false;
        return first.equals(f) && second.equals(s);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=BpeTokenizerTest`
Expected: PASS（所有测试通过）

- [ ] **Step 5: Commit训练功能**

```bash
git add src/main/java/com/advancedjava/agentscope/tokenizer/BpeTokenizer.java \
        src/test/java/com/advancedjava/agentscope/tokenizer/BpeTokenizerTest.java
git commit -m "feat(tokenizer): 实现BPE训练功能并添加单元测试"
```

---

## Task 3: 实现BPE分词功能

**Files:**
- Modify: `src/main/java/com/advancedjava/agentscope/tokenizer/BpeTokenizer.java:tokenize()方法`
- Modify: `src/test/java/com/advancedjava/agentscope/tokenizer/BpeTokenizerTest.java`

- [ ] **Step 1: 添加BPE分词测试**

```java
// 在BpeTokenizerTest.java中添加：

@Test
public void shouldTokenizeAfterTraining() {
    // 使用简单语料训练
    List<String> corpus = Arrays.asList("hello", "hello", "hello", "world");
    BpeTokenizer tokenizer = new BpeTokenizer(15);
    tokenizer.train(corpus);
    
    // 分词测试：对训练过的单词分词
    List<String> result = tokenizer.tokenize("hello");
    
    // 验证：分词结果应包含合并生成的子词
    assertThat(result, hasItem("l"));  // 单个字符
    assertThat(result.size(), greaterThan(0));  // 至少有一个子词
    assertThat(result.size(), lessThan(6));  // 不超过字符数
    
    // 验证：所有子词都在词汇表中
    for (String token : result) {
        assertThat(tokenizer.getVocabulary(), hasItem(token));
    }
}

@Test(expected = IllegalStateException.class)
public void shouldRejectTokenizeBeforeTraining() {
    BpeTokenizer tokenizer = new BpeTokenizer(10);
    tokenizer.tokenize("hello");  // 未训练，应抛异常
}

@Test(expected = IllegalArgumentException.class)
public void shouldRejectNullText() {
    BpeTokenizer tokenizer = new BpeTokenizer(10);
    tokenizer.train(Arrays.asList("hello"));
    tokenizer.tokenize(null);  // 应抛异常
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=BpeTokenizerTest#shouldTokenizeAfterTraining`
Expected: FAIL（tokenize()返回空列表）

- [ ] **Step 3: 实现BPE分词核心逻辑**

```java
// 在BpeTokenizer.java的tokenize()方法中实现：

/**
 * 使用训练好的模型对文本进行分词。
 *
 * <p>分词流程：
 * 1. 将文本拆分为字符序列
 * 2. 按训练学到的合并规则依次合并
 * 3. 返回最终的分词结果
 *
 * @param text 待分词文本
 * @return 分词结果列表
 * @throws IllegalStateException 如果模型未训练
 * @throws IllegalArgumentException 如果text为空
 */
public List<String> tokenize(String text) {
    // 状态检查
    if (mergeRules.isEmpty()) {
        throw new IllegalStateException("模型未训练，请先调用train()方法");
    }
    
    // 参数校验
    if (text == null || text.isEmpty()) {
        throw new IllegalArgumentException("待分词文本不能为空");
    }
    
    // 步骤1: 拆分为字符序列
    List<String> symbols = text.chars()
            .mapToObj(c -> String.valueOf((char) c))
            .collect(Collectors.toList());
    
    // 步骤2: 按优先级应用合并规则
    // 注意：必须按训练时的顺序（优先级）依次应用，保证结果一致
    for (MergeRule rule : mergeRules) {
        symbols = applyMerge(symbols, rule);
    }
    
    // 步骤3: 返回分词结果
    return symbols;
}

/**
 * 应用单个合并规则。
 *
 * <p>将符号序列中所有匹配的相邻符号对替换为合并结果。
 *
 * @param symbols 当前符号序列
 * @param rule 合并规则
 * @return 应用规则后的符号序列
 */
private List<String> applyMerge(List<String> symbols, MergeRule rule) {
    List<String> result = new ArrayList<>();
    int i = 0;
    
    while (i < symbols.size()) {
        // 检查当前位置是否匹配合并规则
        if (i < symbols.size() - 1 
                && symbols.get(i).equals(rule.first())
                && symbols.get(i + 1).equals(rule.second())) {
            // 匹配：合并并跳过下一个符号
            result.add(rule.merged());
            i += 2;
        } else {
            // 不匹配：保持原符号
            result.add(symbols.get(i));
            i += 1;
        }
    }
    
    return result;
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=BpeTokenizerTest`
Expected: PASS（所有测试通过）

- [ ] **Step 5: Commit分词功能**

```bash
git add src/main/java/com/advancedjava/agentscope/tokenizer/BpeTokenizer.java \
        src/test/java/com/advancedjava/agentscope/tokenizer/BpeTokenizerTest.java
git commit -m "feat(tokenizer): 实现BPE分词功能并添加测试"
```

---

## Task 4: 实现Unigram训练功能

**Files:**
- Modify: `src/main/java/com/advancedjava/agentscope/tokenizer/UnigramTokenizer.java:train()方法`
- Create: `src/test/java/com/advancedjava/agentscope/tokenizer/UnigramTokenizerTest.java`

- [ ] **Step 1: 写Unigram训练测试**

```java
package com.advancedjava.agentscope.tokenizer;

import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Unigram分词器单元测试。
 */
public class UnigramTokenizerTest {
    
    @Test
    public void shouldTrainOnSimpleCorpus() {
        // 准备简单训练语料
        List<String> corpus = Arrays.asList("hello", "hello", "world", "word");
        
        // 创建Unigram分词器（词汇表大小20，迭代10次）
        UnigramTokenizer tokenizer = new UnigramTokenizer(20, 10);
        
        // 训练模型
        tokenizer.train(corpus);
        
        // 验证：词汇表应包含基础字符
        assertThat(tokenizer.getVocabulary(), hasItem("h"));
        assertThat(tokenizer.getVocabulary(), hasItem("e"));
        assertThat(tokenizer.getVocabulary(), hasItem("l"));
        
        // 验证：词汇表大小应在合理范围
        assertThat(tokenizer.getVocabularySize(), greaterThan(5));
        assertThat(tokenizer.getVocabularySize(), lessThanOrEqualTo(20));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectEmptyCorpus() {
        UnigramTokenizer tokenizer = new UnigramTokenizer(10, 5);
        tokenizer.train(Arrays.asList());  // 应抛异常
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullInCorpus() {
        UnigramTokenizer tokenizer = new UnigramTokenizer(10, 5);
        tokenizer.train(Arrays.asList("hello", null));  // 应抛异常
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=UnigramTokenizerTest`
Expected: FAIL（train()方法未实现）

- [ ] **Step 3: 实现Unigram训练核心逻辑**

```java
// 在UnigramTokenizer.java的train()方法中实现：

/**
 * 训练Unigram模型。
 *
 * <p>训练流程（简化版EM算法）：
 * 1. 构建初始词汇表（所有子串）
 * 2. 初始化概率（均匀分布）
 * 3. EM迭代：期望步骤计算子词贡献，最大化步骤更新概率
 * 4. 裁剪：删除低贡献词汇，直到达到目标大小
 *
 * @param corpus 训练语料（字符串列表）
 * @throws IllegalArgumentException 如果corpus为空或包含空字符串
 */
public void train(List<String> corpus) {
    // 参数校验
    if (corpus == null || corpus.isEmpty()) {
        throw new IllegalArgumentException("训练语料不能为空");
    }
    if (corpus.stream().anyMatch(s -> s == null || s.isEmpty())) {
        throw new IllegalArgumentException("语料中不能包含空字符串");
    }
    
    // 步骤1: 构建初始词汇表（所有可能的子串）
    Set<String> initialVocab = buildInitialVocabulary(corpus);
    
    // 步骤2: 初始化概率（均匀分布）
    double initialProb = 1.0 / initialVocab.size();
    for (String word : initialVocab) {
        wordProbabilities.put(word, initialProb);
    }
    
    // 步骤3: EM迭代优化
    for (int iter = 0; iter < maxIterations; iter++) {
        // 期望步骤：计算每个子词的期望贡献
        Map<String, Double> contributions = computeContributions(corpus);
        
        // 最大化步骤：更新概率（归一化）
        double total = contributions.values().stream().mapToDouble(Double::doubleValue).sum();
        for (String word : wordProbabilities.keySet()) {
            double contribution = contributions.getOrDefault(word, 0.0);
            wordProbabilities.put(word, contribution / total);
        }
    }
    
    // 步骤4: 裁剪词汇表（删除低贡献词汇）
    pruneVocabulary(corpus);
}

/**
 * 构建初始词汇表。
 *
 * <p>收集语料中所有可能的子串（简化策略）。
 *
 * @param corpus 训练语料
 * @return 初始词汇表
 */
private Set<String> buildInitialVocabulary(List<String> corpus) {
    Set<String> vocab = new HashSet<>();
    
    for (String word : corpus) {
        // 添加所有单个字符
        for (char c : word.toCharArray()) {
            vocab.add(String.valueOf(c));
        }
        
        // 添加所有可能的子串（简化：长度2-3）
        for (int len = 2; len <= Math.min(3, word.length()); len++) {
            for (int i = 0; i <= word.length() - len; i++) {
                vocab.add(word.substring(i, i + len));
            }
        }
        
        // 添加完整单词（作为最长子词）
        vocab.add(word);
    }
    
    return vocab;
}

/**
 * 计算每个子词的期望贡献（EM算法的E步骤）。
 *
 * <p>对于每个单词，找到最优分词路径，统计每个子词的贡献。
 *
 * @param corpus 训练语料
 * @return 子词贡献表
 */
private Map<String, Double> computeContributions(List<String> corpus) {
    Map<String, Double> contributions = new HashMap<>();
    
    for (String word : corpus) {
        // 使用Viterbi算法找到最优分词路径
        List<String> bestPath = findBestPath(word);
        
        // 每个子词的贡献 = 1/路径长度（简化版本）
        double contribution = 1.0 / bestPath.size();
        for (String token : bestPath) {
            contributions.merge(token, contribution, Double::sum);
        }
    }
    
    return contributions;
}

/**
 * 裁剪词汇表。
 *
 * <p>删除对总损失贡献最小的词汇，直到达到目标大小。
 *
 * @param corpus 训练语料（用于计算损失贡献）
 */
private void pruneVocabulary(List<String> corpus) {
    // 计算当前词汇表对每个单词的损失贡献
    Map<String, Double> lossReductions = computeLossReductions(corpus);
    
    // 按贡献排序，保留高贡献词汇
    List<String> sortedVocab = lossReductions.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(vocabSize)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    
    // 更新词汇表
    Map<String, Double> newProbs = new HashMap<>();
    for (String word : sortedVocab) {
        newProbs.put(word, wordProbabilities.get(word));
    }
    wordProbabilities.clear();
    wordProbabilities.putAll(newProbs);
    
    // 重新归一化概率
    double total = wordProbabilities.values().stream().mapToDouble(Double::doubleValue).sum();
    for (String word : wordProbabilities.keySet()) {
        wordProbabilities.put(word, wordProbabilities.get(word) / total);
    }
}

/**
 * 计算词汇表删除后的损失增加（用于裁剪决策）。
 *
 * <p>简化版本：计算每个词汇的频次贡献。
 *
 * @param corpus 训练语料
 * @return 每个词汇的损失贡献
 */
private Map<String, Double> computeLossReductions(List<String> corpus) {
    Map<String, Double> reductions = new HashMap<>();
    
    // 简化：使用概率作为贡献指标
    for (String word : wordProbabilities.keySet()) {
        reductions.put(word, wordProbabilities.get(word));
    }
    
    // 增加频次权重
    for (String text : corpus) {
        for (String token : wordProbabilities.keySet()) {
            if (text.contains(token)) {
                reductions.merge(token, 1.0, Double::sum);
            }
        }
    }
    
    return reductions;
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=UnigramTokenizerTest`
Expected: PASS（所有测试通过）

- [ ] **Step 5: Commit训练功能**

```bash
git add src/main/java/com/advancedjava/agentscope/tokenizer/UnigramTokenizer.java \
        src/test/java/com/advancedjava/agentscope/tokenizer/UnigramTokenizerTest.java
git commit -m "feat(tokenizer): 实现Unigram训练功能并添加单元测试"
```

---

## Task 5: 实现Unigram分词功能（Viterbi算法）

**Files:**
- Modify: `src/main/java/com/advancedjava/agentscope/tokenizer/UnigramTokenizer.java:tokenize()和findBestPath()`
- Modify: `src/test/java/com/advancedjava/agentscope/tokenizer/UnigramTokenizerTest.java`

- [ ] **Step 1: 添加Unigram分词测试**

```java
// 在UnigramTokenizerTest.java中添加：

@Test
public void shouldTokenizeAfterTraining() {
    // 使用简单语料训练
    List<String> corpus = Arrays.asList("hello", "hello", "world");
    UnigramTokenizer tokenizer = new UnigramTokenizer(15, 10);
    tokenizer.train(corpus);
    
    // 分词测试
    List<String> result = tokenizer.tokenize("hello");
    
    // 验证：分词结果不为空
    assertThat(result, hasItem("l"));  // 至少包含基础字符
    assertThat(result.size(), greaterThan(0));
    
    // 验证：所有子词都在词汇表中
    for (String token : result) {
        assertThat(tokenizer.getVocabulary(), hasItem(token));
    }
}

@Test(expected = IllegalStateException.class)
public void shouldRejectTokenizeBeforeTraining() {
    UnigramTokenizer tokenizer = new UnigramTokenizer(10, 5);
    tokenizer.tokenize("hello");  // 未训练，应抛异常
}

@Test(expected = IllegalArgumentException.class)
public void shouldRejectNullText() {
    UnigramTokenizer tokenizer = new UnigramTokenizer(10, 5);
    tokenizer.train(Arrays.asList("hello"));
    tokenizer.tokenize(null);  // 应抛异常
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=UnigramTokenizerTest#shouldTokenizeAfterTraining`
Expected: FAIL（tokenize()返回空列表）

- [ ] **Step 3: 实现Viterbi分词算法**

```java
// 在UnigramTokenizer.java中实现findBestPath()和tokenize()：

/**
 * 使用训练好的模型对文本进行分词。
 *
 * <p>分词流程：
 * 使用Viterbi算法找到对数概率最大的分词路径。
 *
 * @param text 待分词文本
 * @return 分词结果列表
 * @throws IllegalStateException 如果模型未训练
 * @throws IllegalArgumentException 如果text为空
 */
public List<String> tokenize(String text) {
    // 状态检查
    if (wordProbabilities.isEmpty()) {
        throw new IllegalStateException("模型未训练，请先调用train()方法");
    }
    
    // 参数校验
    if (text == null || text.isEmpty()) {
        throw new IllegalArgumentException("待分词文本不能为空");
    }
    
    // 使用Viterbi算法找最优路径
    return findBestPath(text);
}

/**
 * 使用Viterbi算法找到最优分词路径。
 *
 * <p>动态规划算法，找到对数概率最大的分词序列。
 * 
 * <p>算法步骤：
 * 1. dp[i]表示文本前i个字符的最大对数概率
 * 2. 对于每个位置i，尝试所有可能的子词结束于i
 * 3. 选择使对数概率最大的分词方案
 *
 * @param text 待分词文本
 * @return 最优分词路径
 */
private List<String> findBestPath(String text) {
    int n = text.length();
    
    // dp[i] = 前i个字符的最大对数概率
    double[] dp = new double[n + 1];
    dp[0] = 0.0;  // 空文本的概率为0（对数概率）
    
    // 对于初始值，使用负无穷表示不可达
    for (int i = 1; i <= n; i++) {
        dp[i] = Double.NEGATIVE_INFINITY;
    }
    
    // lastToken[i] = 前i个字符最优路径的最后一个子词的长度
    int[] lastTokenLength = new int[n + 1];
    
    // 动态规划填表
    for (int i = 1; i <= n; i++) {
        // 尝试所有可能的子词长度（从1到最大词汇长度）
        int maxWordLen = Math.min(i, getMaxWordLength());
        
        for (int len = 1; len <= maxWordLen; len++) {
            int start = i - len;
            String candidate = text.substring(start, i);
            
            // 检查候选子词是否在词汇表中
            if (wordProbabilities.containsKey(candidate)) {
                // 计算使用该子词的对数概率
                double prob = wordProbabilities.get(candidate);
                double logProb = Math.log(prob);  // 使用对数避免浮点下溢
                
                double newScore = dp[start] + logProb;
                
                // 如果找到更优路径，更新
                if (newScore > dp[i]) {
                    dp[i] = newScore;
                    lastTokenLength[i] = len;
                }
            }
        }
    }
    
    // 回溯构造最优路径
    List<String> path = new ArrayList<>();
    int i = n;
    
    while (i > 0) {
        int len = lastTokenLength[i];
        String token = text.substring(i - len, i);
        path.add(token);
        i -= len;
    }
    
    // 反转路径（因为是从后向前回溯）
    Collections.reverse(path);
    
    return path;
}

/**
 * 获取词汇表中的最大子词长度。
 *
 * @return 最大子词长度
 */
private int getMaxWordLength() {
    return wordProbabilities.keySet().stream()
            .mapToInt(String::length)
            .max()
            .orElse(1);
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=UnigramTokenizerTest`
Expected: PASS（所有测试通过）

- [ ] **Step 5: Commit分词功能**

```bash
git add src/main/java/com/advancedjava/agentscope/tokenizer/UnigramTokenizer.java \
        src/test/java/com/advancedjava/agentscope/tokenizer/UnigramTokenizerTest.java
git commit -m "feat(tokenizer): 实现Unigram分词功能（Viterbi算法）"
```

---

## Task 6: 实现TokenizerDemo对比演示

**Files:**
- Create: `src/main/java/com/advancedjava/agentscope/tokenizer/TokenizerDemo.java`

- [ ] **Step 1: 创建TokenizerDemo类**

```java
package com.advancedjava.agentscope.tokenizer;

import java.util.Arrays;
import java.util.List;

/**
 * BPE与Unigram分词算法对比演示。
 *
 * <p>该类提供独立的演示入口，展示两种算法的训练和分词差异，
 * 适合课堂教学和算法理解。
 *
 * <p>运行方式：直接执行main方法，在控制台查看对比输出。
 */
public class TokenizerDemo {
    
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("BPE vs Unigram - 子词分词算法对比");
        System.out.println("==============================================\n");
        
        // 步骤1: 准备训练语料（中英文混合）
        List<String> corpus = Arrays.asList(
                "hello world",
                "hello friends",
                "world peace",
                "hello hello",
                "programming language"
        );
        
        System.out.println("--- 训练语料 ---");
        corpus.forEach(s -> System.out.println("  \"" + s + "\""));
        System.out.println();
        
        // 步骤2: 训练BPE模型
        System.out.println("--- BPE模型训练 ---");
        BpeTokenizer bpe = new BpeTokenizer(30);
        bpe.train(corpus);
        System.out.println("词汇表大小: " + bpe.getVocabulary().size());
        System.out.println("合并规则数量: " + bpe.getMergeRuleCount());
        System.out.println("词汇表示例: " + 
                bpe.getVocabulary().stream().limit(10).toList());
        System.out.println();
        
        // 步骤3: 训练Unigram模型
        System.out.println("--- Unigram模型训练 ---");
        UnigramTokenizer unigram = new UnigramTokenizer(30, 10);
        unigram.train(corpus);
        System.out.println("词汇表大小: " + unigram.getVocabularySize());
        System.out.println("词汇表示例: " + 
                unigram.getVocabulary().stream().limit(10).toList());
        System.out.println();
        
        // 步骤4: 对比分词结果
        System.out.println("--- 分词对比 ---");
        List<String> testTexts = Arrays.asList(
                "hello",
                "world",
                "hello world",
                "programming"
        );
        
        for (String text : testTexts) {
            System.out.println("\n输入文本: \"" + text + "\"");
            
            // BPE分词
            List<String> bpeResult = bpe.tokenize(text);
            System.out.println("BPE结果:  " + bpeResult);
            
            // Unigram分词
            List<String> unigramResult = unigram.tokenize(text);
            System.out.println("Unigram结果: " + unigramResult);
            
            // 差异分析
            System.out.println("子词数量: BPE=" + bpeResult.size() + 
                    ", Unigram=" + unigramResult.size());
        }
        
        // 步骤5: 算法特点总结
        System.out.println("\n==============================================");
        System.out.println("--- 算法特点对比 ---");
        System.out.println("==============================================");
        System.out.println("BPE特点:");
        System.out.println("  - 基于频次统计，合并高频相邻字符对");
        System.out.println("  - 训练速度快，结果可预测");
        System.out.println("  - 适合处理常见词组和固定搭配");
        System.out.println();
        System.out.println("Unigram特点:");
        System.out.println("  - 基于概率模型，使用EM算法优化");
        System.out.println("  - 分词结果更灵活，使用Viterbi找最优路径");
        System.out.println("  - 适合处理多变文本和未知词");
        System.out.println();
        System.out.println("差异分析:");
        System.out.println("  - BPE倾向于产生固定长度的子词片段");
        System.out.println("  - Unigram根据概率动态选择最优分词");
        System.out.println("  - 计算复杂度：BPE较低，Unigram较高");
    }
}
```

- [ ] **Step 2: 运行Demo验证输出**

Run: `mvn exec:java -Dexec.mainClass="com.advancedjava.agentscope.tokenizer.TokenizerDemo"`
Expected: 控制台输出对比演示结果，无异常

- [ ] **Step 3: 编译验证整体**

Run: `mvn clean compile`
Expected: 编译成功

- [ ] **Step 4: 运行所有测试**

Run: `mvn test -Dtest=*TokenizerTest`
Expected: 所有测试通过

- [ ] **Step 5: CommitDemo类**

```bash
git add src/main/java/com/advancedjava/agentscope/tokenizer/TokenizerDemo.java
git commit -m "feat(tokenizer): 添加BPE与Unigram对比演示类"
```

---

## Self-Review Checklist

完成后检查：

1. **Spec覆盖检查:**
   - ✅ BPE训练+分词 → Task 2, Task 3
   - ✅ Unigram训练+分词 → Task 4, Task 5
   - ✅ 对比演示 → Task 6
   - ✅ 注释详细 → 所有方法均有JavaDoc和行内注释
   - ✅ 无外部依赖 → 纯Java实现
   - ✅ 编译通过 → Task 6验证

2. **占位符扫描:**
   - 无TBD/TODO占位符（骨架阶段已填充完整实现）
   - 无"类似Task X"引用
   - 所有代码完整呈现

3. **类型一致性:**
   - MergeRule record定义一致（Task 1, Task 3）
   - StringPair record定义一致（Task 2内部使用）
   - 方法签名train(List<String>), tokenize(String)一致

---

## 完成状态

所有任务完成后，tokenizer包包含：
- ✅ BpeTokenizer.java（完整训练+分词）
- ✅ UnigramTokenizer.java（完整训练+分词）
- ✅ TokenizerDemo.java（对比演示）
- ✅ BpeTokenizerTest.java（单元测试）
- ✅ UnigramTokenizerTest.java（单元测试）

可直接运行Demo演示或通过测试验证功能。