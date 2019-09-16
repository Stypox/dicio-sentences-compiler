package com.dicio.sentences_compiler.lexer;

import java.util.ArrayList;

public class TokenStream {
    private ArrayList<Token> tokens = new ArrayList<>();
    private int position = 0;

    public void push(Token token) {
        tokens.add(token);
    }

    public Token get(int aheadBy) {
        int index = position + aheadBy;
        if (index < 0 || index >= tokens.size()) {
            return Token.emptyToken();
        }

        return tokens.get(index);
    }

    public void movePositionForwardBy(int delta) {
        assert(delta >= 0);
        position += delta;
    }
}
