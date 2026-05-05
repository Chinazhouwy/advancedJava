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