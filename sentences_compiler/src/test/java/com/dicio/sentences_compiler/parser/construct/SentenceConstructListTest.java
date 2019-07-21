package com.stypox.sentences_compiler.parser.construct;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class SentenceConstructListTest {
    private void assertArrayContains(ArrayList<ArrayList<String>> arr, String[] strings) {
        for (ArrayList<String> stringsInArr : arr) {
            if (Arrays.equals(stringsInArr.toArray(), strings)) {
                return;
            }
        }
        fail();
    }

    @Test
    public void testUnfold() {
        SentenceConstructList sentenceConstructList = new SentenceConstructList();

        OrList c1 = new OrList();
        c1.addConstruct(new Word("hi"));
        c1.addConstruct(new Word("hello"));
        sentenceConstructList.addConstruct(c1);

        Word c2 = new Word("there");
        sentenceConstructList.addConstruct(c2);

        ArrayList<ArrayList<String>> combinations = sentenceConstructList.unfold();
        assertSame(combinations.size(), 2);
        assertArrayContains(combinations, new String[]{"hi", "there"});
        assertArrayContains(combinations, new String[]{"hello", "there"});
    }
}