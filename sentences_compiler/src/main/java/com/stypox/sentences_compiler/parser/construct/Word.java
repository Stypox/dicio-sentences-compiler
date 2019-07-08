package com.stypox.sentences_compiler.parser.construct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public final class Word implements BaseConstruct {
    private String word;

    Word(String word) {
        this.word = word;
    }

    @Override
    public ArrayList<ArrayList<String>> unfold() {
        return new ArrayList<ArrayList<String>>() {{ add(new ArrayList<String>(){{ add(word); }}); }};
    }
}
