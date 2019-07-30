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
        capturingGroupInvalidLength("Capturing groups are made of exactly two points \".\""),
        capturingGroupInsideParenthesis("Capturing groups cannot be nested inside parenthesis"),
        optionalCapturingGroup("Capturing groups cannot be optional"),
        differentNrOfCapturingGroups("Sentences with the same sentence id (possibly empty) must have the same number of capturing groups"),
        tooManyCapturingGroups("Too many capturing groups"),
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
    private int line, column;
    private String message;


    public CompilerError(Type type, String tokenValue, int line, int column, String message) {
        this.type = type;
        this.tokenValue = tokenValue;
        this.line = line;
        this.column = column;
        this.message = message;
    }
    public CompilerError(Type type, Token token, String message) {
        this(type, token.getValue(), token.getLine(), token.getColumn(), message);
    }
    public CompilerError(Type type, String sentenceId, int line, String message) {
        this(type, sentenceId, line, -1, message);
    }

    @Override
    public String getMessage() {
        String str = "";

        if (line == -1 && column == -1) { // empty token
            str += "END OF FILE";
        } else if (column == -1) { // validation error
            if (!tokenValue.isEmpty()) {
                str += "id=";
                str += tokenValue;
                str += " on ";
            }

            str += "line ";
            str += line;
        } else {
            str += line;
            str += ":";
            str += column;

            if (!tokenValue.isEmpty()) {
                str += " ";
                str += tokenValue;
            }
        }

        str += ": ";
        str += type.toString();

        if (!message.isEmpty()) {
            str += ": ";
            str += message;
        }

        return str;
    }
}
