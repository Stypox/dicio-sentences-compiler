package com.dicio.sentences_compiler.parser.construct;

import com.dicio.sentences_compiler.util.CompilerError;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Section {
    public enum Specificity {
        high,
        medium,
        low,
    }

    private String sectionId;
    private Specificity specificity;
    private int line;
    private ArrayList<Sentence> sentences;

    public Section() {
        sentences = new ArrayList<>();
    }

    public void setSectionInfo(String sectionId, Specificity specificity, int line) {
        this.sectionId = sectionId;
        this.specificity = specificity;
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
    public Specificity getSpecificity() { return specificity; }
    public int getLine() {
        return line;
    }
    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    public void compileToJava(OutputStreamWriter output) throws IOException {
        output.write("final StandardRecognitionUnit section_");
        output.write(sectionId);
        output.write(" = new StandardRecognitionUnit(\nInputRecognitionUnit.Specificity.");

        switch (specificity) {
            case low:
                output.write("low");
                break;
            case medium:
                output.write("medium");
                break;
            case high:
                output.write("high");
                break;
        }

        output.write(",\nnew Sentence[]{\n");
        for (Sentence sentence : sentences) {
            sentence.compileToJava(output);
        }
        output.write("}\n);");
    }
}
