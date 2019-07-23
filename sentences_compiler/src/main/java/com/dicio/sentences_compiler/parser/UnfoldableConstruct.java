package com.dicio.sentences_compiler.parser;

import java.util.ArrayList;

public interface UnfoldableConstruct {
    ArrayList<ArrayList<String>> unfold();
    boolean isOptional();
}
