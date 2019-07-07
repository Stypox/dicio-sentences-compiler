package com.stypox.sentences_compiler;

import com.stypox.sentences_compiler.lexer.Token;

public class CompilerError extends Exception {
    public enum Type {
        invalidCharacter;

        public String toString() {
            switch (this) {
                case invalidCharacter:
                    return "Invalid character error";
                default:
                    return "";
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
        String str = line + ":" + column;

        if (!this.tokenValue.isEmpty()) {
            str += " ";
            str += tokenValue;
        }

        str += ": ";
        str += type.toString();

        if (!this.message.isEmpty()) {
            str += ": ";
            str += message;
        }

        return str;
    }
}
