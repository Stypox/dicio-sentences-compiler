package com.dicio.sentences_compiler.construct;

import com.dicio.sentences_compiler.compiler.CompilableToJava;
import com.dicio.sentences_compiler.util.CompilerError;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Section implements CompilableToJava {
    public enum Specificity {
        high,
        medium,
        low,
    }

    private String sectionId;
    private Specificity specificity;
    private ArrayList<Sentence> sentences;

    private String inputStreamName;
    private int line;

    public Section() {
        sentences = new ArrayList<>();
    }

    public void setSectionInfo(String sectionId, Specificity specificity, String inputStreamName, int line) {
        this.sectionId = sectionId;
        this.specificity = specificity;
        this.inputStreamName = inputStreamName;
        this.line = line;
    }
    public void addSentence(Sentence sentences) {
        this.sentences.add(sentences);
    }

    public void compileSentenceWordLists() throws CompilerError {
        for (Sentence sentence : sentences) {
            sentence.compileWordList();
        }
    }

    public String getSectionId() {
        return sectionId;
    }
    public Specificity getSpecificity() {
        return specificity;
    }
    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    public String getInputStreamName() {
        return inputStreamName;
    }
    public int getLine() {
        return line;
    }

    @Override
    public void compileToJava(OutputStreamWriter output, String variableName) throws IOException {
        if (!variableName.isEmpty()) {
            output.write("public static final StandardRecognizerData ");
            output.write(variableName);
            output.write(" = ");
        }
        output.write("new StandardRecognizerData(\nInputRecognizer.Specificity.");

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
            sentence.compileToJava(output, "");
        }
        output.write("}\n);\n");
    }
}
