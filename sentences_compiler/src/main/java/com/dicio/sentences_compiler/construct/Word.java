package com.dicio.sentences_compiler.construct;

import com.dicio.sentences_compiler.parser.UnfoldableConstruct;

import java.util.ArrayList;

public final class Word implements UnfoldableConstruct {
    private String word;

    public Word(String word) {
        this.word = word;
    }

    @Override
    public ArrayList<ArrayList<String>> unfold() {
        return new ArrayList<ArrayList<String>>() {{ add(new ArrayList<String>(){{ add(word); }}); }};
    }

    @Override
    public boolean isOptional() {
        return false;
    }
}
