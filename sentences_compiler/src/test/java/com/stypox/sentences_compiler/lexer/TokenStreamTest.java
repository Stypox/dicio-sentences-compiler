package com.stypox.sentences_compiler.lexer;

import org.junit.Test;

import static org.junit.Assert.*;

public class TokenStreamTest {
    @Test
    public void testTokenStream() {
        TokenStream ts = new TokenStream();
        assertTrue(ts.get(0).isEmpty());

        ts.push(new Token(Token.Type.grammar,"",0,0));
        assertTrue(ts.get(0).isType(Token.Type.grammar));
        assertTrue(ts.get(-1).isEmpty());
        assertTrue(ts.get(1).isEmpty());

        ts.movePositionForwardBy(1);
        assertTrue(ts.get(0).isEmpty());
        assertTrue(ts.get(-1).isType(Token.Type.grammar));
    }
}