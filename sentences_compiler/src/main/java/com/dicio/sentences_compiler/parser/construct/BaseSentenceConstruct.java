package com.dicio.sentences_compiler.parser.construct;

import java.util.ArrayList;

public interface BaseSentenceConstruct {
    ArrayList<ArrayList<String>> unfold();
    boolean isOptional();
}
