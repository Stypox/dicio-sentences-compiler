package com.dicio.sentences_compiler.construct;

import com.dicio.sentences_compiler.compiler.CompilableToJava;
import com.dicio.sentences_compiler.parser.UnfoldableConstruct;
import com.dicio.sentences_compiler.util.CompilerError;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Sentence implements CompilableToJava {
    private String sentenceId;
    private SentenceConstructList sentenceConstructs;

    private String inputStreamName;
    private int line;

    public void setSentenceId(String sentenceId, String inputStreamName, int line) {
        this.sentenceId = sentenceId;
        this.inputStreamName = inputStreamName;
        this.line = line;
    }
    public void setSentenceConstructs(SentenceConstructList sentenceConstructs) {
        this.sentenceConstructs = sentenceConstructs;
    }

    public int numberOfCapturingGroups() {
        int count = 0;
        for (UnfoldableConstruct sentenceConstruct : sentenceConstructs.getConstructs()) {
            if (sentenceConstruct instanceof CapturingGroup) {
                ++count;
            }
        }
        return count;
    }
    public void validate() throws CompilerError {
        int capturingGroups = numberOfCapturingGroups();
        if (capturingGroups > 2) {
            throw new CompilerError(CompilerError.Type.tooManyCapturingGroups, sentenceId, inputStreamName, line, "");
        }

        if (sentenceConstructs.isOptional()) {
            throw new CompilerError(CompilerError.Type.sentenceCanBeEmpty, sentenceId, inputStreamName, line, "");
        }
    }

    public String getSentenceId() {
        return sentenceId;
    }
    public SentenceConstructList getSentenceConstructs() {
        return sentenceConstructs;
    }

    public String getInputStreamName() {
        return inputStreamName;
    }
    public int getLine() {
        return line;
    }

    @Override
    public void compileToJava(OutputStreamWriter output, String variableName) throws IOException {
        for (ArrayList<String> unfoldedSentence : sentenceConstructs.unfold()) {
            if (!variableName.isEmpty()) {
                output.write("final Sentence ");
                output.write(variableName);
                output.write(" = ");
            }

            output.write("new Sentence(\"");
            output.write(sentenceId);
            output.write("\", new String[]{");

            for (String word : unfoldedSentence) {
                if (word.equals(".")) {
                    output.write("}, new String[]{");
                } else {
                    output.write("\"");
                    output.write(word);
                    output.write("\",");
                }
            }

            output.write("}),\n");
        }
    }
}
