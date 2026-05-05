package com.advancedjava.agentscope.tokenizer;

import java.util.Arrays;
import java.util.List;

public class TokenizerDemo {

    public static void main(String[] args) {
        System.out.println("===== Tokenizer对比演示 =====\n");

        List<String> corpus = Arrays.asList(
                "hello",
                "hello",
                "world",
                "low",
                "lower",
                "newest",
                "widest"
        );

        System.out.println("训练语料: " + corpus);
        System.out.println();

        BpeTokenizer bpe = new BpeTokenizer(15);
        bpe.train(corpus);

        UnigramTokenizer unigram = new UnigramTokenizer(15, 10);
        unigram.train(corpus);

        System.out.println("=== BPE分词器 ===");
        System.out.println("词汇表大小: " + bpe.getVocabularySize());
        System.out.println("词汇表: " + bpe.getVocabulary());
        System.out.println("合并规则数: " + bpe.getMergeRuleCount());
        System.out.println();

        System.out.println("=== Unigram分词器 ===");
        System.out.println("词汇表大小: " + unigram.getVocabularySize());
        System.out.println("词汇表: " + unigram.getVocabulary());
        System.out.println();

        String[] testWords = {"hello", "low", "lower", "newest", "widest"};

        System.out.println("=== 分词对比 ===\n");
        System.out.printf("%-10s %-25s %-25s%n", "单词", "BPE分词", "Unigram分词");
        System.out.println("-".repeat(65));

        for (String word : testWords) {
            List<String> bpeTokens = bpe.tokenize(word);
            List<String> unigramTokens = unigram.tokenize(word);
            System.out.printf("%-10s %-25s %-25s%n",
                    word,
                    bpeTokens.toString(),
                    unigramTokens.toString());
        }

        System.out.println();
        System.out.println("=== 算法差异说明 ===");
        System.out.println("BPE: 基于频率贪婪合并高频相邻字符对");
        System.out.println("Unigram: 基于概率选择最优分词路径");
        System.out.println();
        System.out.println("观察: 同一单词可能得到不同分词结果");
        System.out.println("      BPE倾向于保留高频组合，Unigram倾向于高概率路径");
    }
}
