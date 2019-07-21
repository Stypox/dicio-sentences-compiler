package com.dicio.sentences_compiler.parser.construct;

import com.dicio.sentences_compiler.util.CompilerError;

import java.util.ArrayList;
import java.util.HashMap;

public class Section {
    private String sectionId;
    private int line;
    private ArrayList<Sentence> sentences;

    public Section() {
        sentences = new ArrayList<>();
    }

    public void setSectionId(String sectionId, int line) {
        this.sectionId = sectionId;
        this.line = line;
    }
    public void addSentence(Sentence sentences) {
        this.sentences.add(sentences);
    }

    public void validate() throws CompilerError {
        HashMap<String, Integer> sentenceCapturingGroups = new HashMap<>();
        for (Sentence sentence : sentences) {
            sentence.validate();
            int capturingGroups = sentence.numberOfCapturingGroups();
            String sentenceId = sentence.getSentenceId();

            if (sentenceCapturingGroups.containsKey(sentenceId)) {
                if (sentenceCapturingGroups.get(sentenceId) != capturingGroups) {
                    throw new CompilerError(CompilerError.Type.differentNrOfCapturingGroups, sentenceId, sentence.getLine(), "");
                }
            } else {
                sentenceCapturingGroups.put(sentenceId, capturingGroups);
            }
        }
    }

    public String getSectionId() {
        return sectionId;
    }
    public int getLine() {
        return line;
    }
    public ArrayList<Sentence> getSentences() {
        return sentences;
    }
}
