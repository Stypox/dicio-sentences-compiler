package com.stypox.sentences_compiler.util;

import com.stypox.sentences_compiler.lexer.Token;

public class CompilerError extends Exception {
    public enum Type {
        invalidCharacter,
        expectedSectionOrEndOfFile,
        invalidToken,
        expectedSentence,
        expectedSentenceContent,
        expectedSentenceConstructList,
        capturingGroupInvalidLength,
        capturingGroupInsideParenthesis,
        optionalCapturingGroup;

        public String toString() {
            switch (this) {
                case invalidCharacter:
                    return "Invalid character error";
                case expectedSectionOrEndOfFile:
                    return "Expected section or end of file";
                case invalidToken:
                    return "Invalid token";
                case expectedSentence:
                    return "Expected sentence after section id";
                case expectedSentenceContent:
                    return "Expected sentence content after sentence id";
                case expectedSentenceConstructList:
                    return "Expected list of sentence constructs";
                case capturingGroupInvalidLength:
                    return "Capturing groups are made of exactly two points \".\"";
                case capturingGroupInsideParenthesis:
                    return "Capturing groups cannot be nested inside parenthesis";
                case optionalCapturingGroup:
                    return "Capturing groups cannot be optional";
                default:
                    return "Unknown error";
            }
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
    public CompilerError(Type type, int line, int column, String message) {
        this(type, "", line, column, message);
    }

    @Override
    public String getMessage() {
        String str = "";

        if (line == -1 && column == -1) {// empty token
            str += "END OF FILE";
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
