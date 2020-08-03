package org.dicio.sentences_compiler.lexer;

public class Token {
    public enum Type {
        empty,
        grammar,
        letters, // letters are always lettersPlusOther
        lettersPlusOther,
        endOfFile,
    }

    private Type type;
    private String value;

    private String inputStreamName;
    private int line, column;

    private Token() {
        this(Type.empty, "", "", -1, -1);
    }
    public Token(Type type, String value, String inputStreamName, int line, int column) {
        this.type = type;
        this.value = value;
        this.inputStreamName = inputStreamName;
        this.line = line;
        this.column = column;
    }
    public static Token emptyToken() {
        return new Token();
    }


    public boolean isType(Type type) {
        if (this.type == Type.letters && type == Type.lettersPlusOther) {
            return true; // letters are always lettersPlusOther
        }
        return this.type == type;
    }
    public boolean isValue(String value) {
        return this.value.equals(value);
    }
    public boolean equals(Type type, String value) {
        return isType(type) && isValue(value);
    }
    public boolean isEmpty() {
        return this.type == Type.empty;
    }
    public String getValue() {
        return value;
    }

    public String getInputStreamName() {
        return inputStreamName;
    }
    public int getLine() {
        return line;
    }
    public int getColumn() {
        return column;
    }
}
