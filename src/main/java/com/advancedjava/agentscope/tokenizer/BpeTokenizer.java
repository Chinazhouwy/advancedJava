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
        if (corpus == null || corpus.isEmpty()) {
            throw new IllegalArgumentException("语料库不能为空");
        }
        for (String word : corpus) {
            if (word == null) {
                throw new IllegalArgumentException("语料库不能包含null值");
            }
        }

        // 统计单词频率
        Map<String, Integer> wordFreqs = corpus.stream()
                .collect(Collectors.toMap(
                        w -> w,
                        w -> 1,
                        Integer::sum
                ));

        // 初始化词汇表：从所有单词的字符开始
        for (String word : wordFreqs.keySet()) {
            for (char c : word.toCharArray()) {
                vocabulary.add(String.valueOf(c));
            }
        }

        // 将每个单词拆分为字符列表
        Map<String, List<String>> wordSplits = new HashMap<>();
        for (String word : wordFreqs.keySet()) {
            List<String> chars = new ArrayList<>();
            for (char c : word.toCharArray()) {
                chars.add(String.valueOf(c));
            }
            wordSplits.put(word, chars);
        }

        // 迭代合并，直到达到词汇表大小
        int numMerges = vocabSize - vocabulary.size();
        for (int i = 0; i < numMerges; i++) {
            Map<StringPair, Integer> pairFreqs = countPairFreqs(wordSplits, wordFreqs);
            if (pairFreqs.isEmpty()) {
                break;
            }

            StringPair maxPair = findMaxFreqPair(pairFreqs);
            String merged = maxPair.first() + maxPair.second();
            mergeRules.add(new MergeRule(maxPair.first(), maxPair.second(), i));
            vocabulary.add(merged);

            // 应用合并到所有单词
            for (Map.Entry<String, List<String>> entry : wordSplits.entrySet()) {
                wordSplits.put(entry.getKey(), mergePair(entry.getValue(), maxPair));
            }
        }
    }

    /**
     * 统计所有相邻字符对的频率。
     */
    private Map<StringPair, Integer> countPairFreqs(
            Map<String, List<String>> wordSplits,
            Map<String, Integer> wordFreqs) {
        Map<StringPair, Integer> pairFreqs = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : wordSplits.entrySet()) {
            String word = entry.getKey();
            List<String> splits = entry.getValue();
            int freq = wordFreqs.get(word);

            for (int i = 0; i < splits.size() - 1; i++) {
                StringPair pair = new StringPair(splits.get(i), splits.get(i + 1));
                pairFreqs.merge(pair, freq, Integer::sum);
            }
        }

        return pairFreqs;
    }

    /**
     * 找到频率最高的字符对。
     */
    private StringPair findMaxFreqPair(Map<StringPair, Integer> pairFreqs) {
        StringPair maxPair = null;
        int maxFreq = -1;

        for (Map.Entry<StringPair, Integer> entry : pairFreqs.entrySet()) {
            if (entry.getValue() > maxFreq) {
                maxFreq = entry.getValue();
                maxPair = entry.getKey();
            }
        }

        return maxPair;
    }

    /**
     * 在单词的字符列表中应用合并操作。
     */
    private List<String> mergePair(List<String> splits, StringPair pair) {
        if (splits.size() < 2) {
            return new ArrayList<>(splits);
        }

        List<String> newSplits = new ArrayList<>();
        int i = 0;
        while (i < splits.size()) {
            if (i < splits.size() - 1
                    && splits.get(i).equals(pair.first())
                    && splits.get(i + 1).equals(pair.second())) {
                newSplits.add(pair.first() + pair.second());
                i += 2;
            } else {
                newSplits.add(splits.get(i));
                i++;
            }
        }

        return newSplits;
    }

    /**
     * 字符对记录（用于BPE合并）。
     */
    public record StringPair(String first, String second) {}
    
    /**
     * 使用训练好的模型对文本进行分词。
     *
     * @param text 待分词文本
     * @return 分词结果列表
     * @throws IllegalStateException 如果模型未训练
     * @throws IllegalArgumentException 如果text为空
     */
    public List<String> tokenize(String text) {
        if (text == null) {
            throw new IllegalArgumentException("分词文本不能为null");
        }
        if (vocabulary.isEmpty()) {
            throw new IllegalStateException("模型未训练，请先调用train()方法");
        }

        List<String> splits = new ArrayList<>();
        for (char c : text.toCharArray()) {
            splits.add(String.valueOf(c));
        }

        for (MergeRule rule : mergeRules) {
            splits = applyMerge(splits, rule);
        }

        return splits;
    }

    private List<String> applyMerge(List<String> splits, MergeRule rule) {
        if (splits.size() < 2) {
            return new ArrayList<>(splits);
        }

        List<String> result = new ArrayList<>();
        int i = 0;
        while (i < splits.size()) {
            if (i < splits.size() - 1
                    && splits.get(i).equals(rule.first())
                    && splits.get(i + 1).equals(rule.second())) {
                result.add(rule.merged());
                i += 2;
            } else {
                result.add(splits.get(i));
                i++;
            }
        }

        return result;
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
     * 获取词汇表大小。
     *
     * @return 词汇表大小
     */
    public int getVocabularySize() {
        return vocabulary.size();
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