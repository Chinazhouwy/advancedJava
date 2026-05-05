package com.advancedjava.agentscope.tokenizer;

import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

public class UnigramTokenizerTest {

    @Test
    public void shouldTrainOnSimpleCorpus() {
        List<String> corpus = Arrays.asList("hello", "hello", "world", "word");
        UnigramTokenizer tokenizer = new UnigramTokenizer(20, 10);
        tokenizer.train(corpus);

        assertThat(tokenizer.getVocabulary(), hasItem("h"));
        assertThat(tokenizer.getVocabulary(), hasItem("e"));
        assertThat(tokenizer.getVocabulary(), hasItem("l"));

        assertThat(tokenizer.getVocabularySize(), greaterThan(5));
        assertThat(tokenizer.getVocabularySize(), lessThanOrEqualTo(20));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectEmptyCorpus() {
        UnigramTokenizer tokenizer = new UnigramTokenizer(10, 5);
        tokenizer.train(Arrays.asList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullInCorpus() {
        UnigramTokenizer tokenizer = new UnigramTokenizer(10, 5);
        tokenizer.train(Arrays.asList("hello", null));
    }

    @Test
    public void shouldTokenizeAfterTraining() {
        List<String> corpus = Arrays.asList("hello", "hello", "world");
        UnigramTokenizer tokenizer = new UnigramTokenizer(15, 10);
        tokenizer.train(corpus);

        List<String> result = tokenizer.tokenize("hello");

        assertThat(result.size(), greaterThan(0));

        for (String token : result) {
            assertThat(tokenizer.getVocabulary(), hasItem(token));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldRejectTokenizeBeforeTraining() {
        UnigramTokenizer tokenizer = new UnigramTokenizer(10, 5);
        tokenizer.tokenize("hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullText() {
        UnigramTokenizer tokenizer = new UnigramTokenizer(10, 5);
        tokenizer.train(Arrays.asList("hello"));
        tokenizer.tokenize(null);
    }
}
