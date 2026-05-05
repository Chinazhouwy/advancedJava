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
        if (corpus == null || corpus.isEmpty()) {
            throw new IllegalArgumentException("语料库不能为空");
        }
        for (String word : corpus) {
            if (word == null) {
                throw new IllegalArgumentException("语料库不能包含null值");
            }
        }

        Map<String, Integer> wordFreqs = corpus.stream()
                .collect(Collectors.toMap(
                        w -> w,
                        w -> 1,
                        Integer::sum
                ));

        Set<String> initialVocab = buildInitialVocabulary(wordFreqs);
        wordProbabilities.clear();

        double initialProb = 1.0 / initialVocab.size();
        for (String word : initialVocab) {
            wordProbabilities.put(word, initialProb);
        }

        for (int iter = 0; iter < maxIterations; iter++) {
            Map<String, Double> contributions = computeContributions(wordFreqs);
            normalizeProbabilities(contributions);

            if (wordProbabilities.size() > vocabSize) {
                pruneVocabulary();
            }
        }
    }

    private Set<String> buildInitialVocabulary(Map<String, Integer> wordFreqs) {
        Set<String> vocab = new HashSet<>();
        Set<String> baseChars = new HashSet<>();
        for (String word : wordFreqs.keySet()) {
            for (char c : word.toCharArray()) {
                baseChars.add(String.valueOf(c));
            }
            for (int i = 0; i < word.length(); i++) {
                for (int j = i + 2; j <= Math.min(i + 8, word.length()); j++) {
                    vocab.add(word.substring(i, j));
                }
            }
        }
        vocab.addAll(baseChars);
        return vocab;
    }

    private Map<String, Double> computeContributions(Map<String, Integer> wordFreqs) {
        Map<String, Double> contributions = new HashMap<>();
        for (String word : wordFreqs.keySet()) {
            int freq = wordFreqs.get(word);
            List<String> bestPath = findBestPath(word);
            for (String token : bestPath) {
                contributions.merge(token, (double) freq, Double::sum);
            }
        }
        return contributions;
    }

    private void normalizeProbabilities(Map<String, Double> contributions) {
        double total = contributions.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0) {
            for (Map.Entry<String, Double> entry : contributions.entrySet()) {
                wordProbabilities.put(entry.getKey(), entry.getValue() / total);
            }
        }
    }

    private void pruneVocabulary() {
        Set<String> baseChars = wordProbabilities.keySet().stream()
                .filter(s -> s.length() == 1)
                .collect(Collectors.toSet());

        List<Map.Entry<String, Double>> sorted = wordProbabilities.entrySet().stream()
                .filter(e -> e.getKey().length() > 1)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        Map<String, Double> newProbabilities = new HashMap<>();
        int nonCharSlots = vocabSize - baseChars.size();
        int keepCount = Math.min(nonCharSlots, sorted.size());
        for (int i = 0; i < keepCount; i++) {
            newProbabilities.put(sorted.get(i).getKey(), sorted.get(i).getValue());
        }

        for (String c : baseChars) {
            newProbabilities.put(c, wordProbabilities.get(c));
        }

        wordProbabilities.clear();
        wordProbabilities.putAll(newProbabilities);

        double total = wordProbabilities.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0) {
            for (Map.Entry<String, Double> entry : wordProbabilities.entrySet()) {
                entry.setValue(entry.getValue() / total);
            }
        }
    }

    private List<String> findBestPath(String text) {
        int n = text.length();
        double[] dp = new double[n + 1];
        int[] parent = new int[n + 1];
        Arrays.fill(dp, Double.NEGATIVE_INFINITY);
        dp[0] = 0;

        for (int i = 0; i < n; i++) {
            if (dp[i] == Double.NEGATIVE_INFINITY) {
                continue;
            }
            for (int j = i + 1; j <= Math.min(i + getMaxWordLength(), n); j++) {
                String sub = text.substring(i, j);
                Double prob = wordProbabilities.get(sub);
                if (prob != null) {
                    double logProb = Math.log(prob);
                    if (dp[i] + logProb > dp[j]) {
                        dp[j] = dp[i] + logProb;
                        parent[j] = i;
                    }
                }
            }
        }

        List<String> path = new ArrayList<>();
        int pos = n;
        while (pos > 0) {
            int prev = parent[pos];
            path.add(text.substring(prev, pos));
            pos = prev;
        }
        Collections.reverse(path);
        return path.isEmpty() ? Collections.singletonList(text) : path;
    }

    private int getMaxWordLength() {
        return wordProbabilities.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(1);
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
        if (text == null) {
            throw new IllegalArgumentException("分词文本不能为null");
        }
        if (wordProbabilities.isEmpty()) {
            throw new IllegalStateException("模型未训练，请先调用train()方法");
        }
        return findBestPath(text);
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