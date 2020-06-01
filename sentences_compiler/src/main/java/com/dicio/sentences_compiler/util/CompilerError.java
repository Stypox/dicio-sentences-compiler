package com.dicio.sentences_compiler.util;

import com.dicio.sentences_compiler.lexer.Token;

public class CompilerError extends Exception {
    public enum Type {
        invalidCharacter("Invalid character error"),
        expectedSectionOrEndOfFile("Expected section or end of file"),
        invalidToken("Invalid token"),
        invalidSectionId("The section id has to be a valid java variable name"),
        invalidSpecificity("Invalid specificity"),
        expectedSentence("Expected sentence after specificity"),
        expectedSentenceContent("Expected sentence content after sentence id"),
        expectedSentenceConstructList("Expected list of sentence constructs"),
        expectedCapturingGroupName("Expected capturing group name after point \".\""),
        expectedPoint("Expected point \".\" after capturing group name"),
        sentenceCanBeEmpty("Sentence can be unfolded to an empty sentence (possibly with capturing groups)"),
        duplicateSectionId("Duplicate section id");

        String errorString;
        Type(String errorString) {
            this.errorString = errorString;
        }

        @Override
        public String toString() {
            return errorString;
        }
    }

    private Type type;
    private String tokenValue;
    private String inputStreamName;
    private int line, column;
    private String message;


    public CompilerError(Type type, String tokenValue, String inputStreamName, int line, int column, String message) {
        this.type = type;
        this.tokenValue = tokenValue;
        this.inputStreamName = inputStreamName;
        this.line = line;
        this.column = column;
        this.message = message;
    }
    public CompilerError(Type type, Token token, String message) {
        this(type, token.getValue(), token.getInputStreamName(), token.getLine(), token.getColumn(), message);
    }
    public CompilerError(Type type, String sentenceId, String inputStreamName, int line, String message) {
        this(type, sentenceId, inputStreamName, line, -1, message);
    }

    @Override
    public String getMessage() {
        StringBuilder str = new StringBuilder();
        str.append(inputStreamName);

        if (line == -1 && column == -1) { // empty token
            str.append(":UNKNOWN POSITION");
        } else if (column == -1) { // validation error
            str.append(":");

            if (!tokenValue.isEmpty()) {
                str.append("id=");
                str.append(tokenValue);
                str.append(" on ");
            }

            str.append("line ");
            str.append(line);
        } else {
            str.append(":");
            str.append(line);
            str.append(":");
            str.append(column);

            if (!tokenValue.isEmpty()) {
                str.append(" ");
                str.append(tokenValue);
            }
        }

        str.append(": ");
        str.append(type.toString());

        if (!message.isEmpty()) {
            str.append(": ");
            str.append(message);
        }

        return str.toString();
    }
}
