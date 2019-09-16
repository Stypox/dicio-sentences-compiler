package com.dicio.sentences_compiler.lexer;

import org.junit.Test;

import static org.junit.Assert.*;

public class TokenTest {

    @Test
    public void testEmptyToken() {
        assertTrue(Token.emptyToken().isEmpty());
    }

    @Test
    public void testLettersType() {
        Token token1 = new Token(Token.Type.letters, "", "", 0, 0);
        assertTrue(token1.isType(Token.Type.letters));
        assertTrue("letters type is not also considered as lettersPlusOther", token1.isType(Token.Type.lettersPlusOther));

        Token token2 = new Token(Token.Type.lettersPlusOther, "", "", 0, 0);
        assertFalse("lettersPlusOther type is considered as letters", token2.isType(Token.Type.letters));
        assertTrue(token2.isType(Token.Type.lettersPlusOther));
    }
}