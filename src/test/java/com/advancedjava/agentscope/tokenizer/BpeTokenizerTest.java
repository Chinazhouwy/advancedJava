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

        // 验证：词汇表应包含基础字符
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
