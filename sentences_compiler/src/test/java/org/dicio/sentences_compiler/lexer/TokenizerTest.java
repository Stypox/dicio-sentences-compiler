package org.dicio.sentences_compiler.lexer;

import org.dicio.sentences_compiler.util.CompilerError;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class TokenizerTest {

    private static TokenStream getTokens(String s, String inputStreamName) throws IOException, CompilerError {
        Charset charset = StandardCharsets.UTF_8;
        InputStream stream = new ByteArrayInputStream(s.getBytes(charset));
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize(new InputStreamReader(stream, charset), inputStreamName);
        return tokenizer.getTokenStream();
    }
    private static TokenStream getTokens(String s) throws IOException, CompilerError {
        return getTokens(s, "");
    }

    private static void assertTokenEqualTo(Token token, Token.Type type, String value, int line, int column) {
        assertTrue(token.isType(type));
        assertTrue(token.isValue(value));
        assertEquals(token.getLine(), line);
        assertEquals(token.getColumn(), column);
    }
    private static void assertInvalid(String input, CompilerError.Type errorType, int errorLine, int errorColumn, String errorMustContain) throws IOException {
        final String inputStreamName = "MyGoodFILE!";
        try {
            getTokens(input, inputStreamName);
            fail("No error thrown with invalid input");
        } catch (CompilerError compilerError) {
            String message = compilerError.getMessage();
            assertTrue("\""+message+"\" is not of type \""+errorType.toString()+"\"", message.contains(errorType.toString()));
            assertTrue("\""+message+"\" does not contain input stream name \""+inputStreamName+"\"", message.contains(inputStreamName));
            assertTrue("\""+message+"\" does not contain line number "+errorLine, message.contains(String.valueOf(errorLine)));
            assertTrue("\""+message+"\" does not contain column number "+errorColumn, message.contains(String.valueOf(errorColumn)));
            assertTrue("\""+message+"\" does not contain \""+errorMustContain+"\"", message.contains(errorMustContain));
        }
    }

    @Test
    public void testEmptyInput() throws IOException, CompilerError {
        assertTrue(getTokens("").get(0).isType(Token.Type.endOfFile));
        assertTrue(getTokens("\n").get(0).isType(Token.Type.endOfFile));
        assertTrue(getTokens("  \t  \n\t\t\n  ").get(0).isType(Token.Type.endOfFile));
        assertTrue(getTokens("    # hello: # ## | | ; world   \n#").get(0).isType(Token.Type.endOfFile));
    }

    @Test
    public void testValidInput() throws IOException, CompilerError {
        TokenStream tokens = getTokens("cat_name1:     # hello world  \n" +
                "  [sent_name1] hello?      hi|(bye bye);       \n" +
                "..;");
        assertTrue(tokens.get(-1).isEmpty());
        assertTokenEqualTo(tokens.get(0),  Token.Type.lettersPlusOther, "cat_name1",  1,  1);
        assertTokenEqualTo(tokens.get(1),  Token.Type.grammar,          ":",          1, 10);
        assertTokenEqualTo(tokens.get(2),  Token.Type.grammar,          "[",          2,  3);
        assertTokenEqualTo(tokens.get(3),  Token.Type.lettersPlusOther, "sent_name1", 2,  4);
        assertTokenEqualTo(tokens.get(4),  Token.Type.grammar,          "]",          2, 14);
        assertTokenEqualTo(tokens.get(5),  Token.Type.letters,          "hello",      2, 16);
        assertTokenEqualTo(tokens.get(6),  Token.Type.grammar,          "?",          2, 21);
        assertTokenEqualTo(tokens.get(7),  Token.Type.letters,          "hi",         2, 28);
        assertTokenEqualTo(tokens.get(8),  Token.Type.grammar,          "|",          2, 30);
        assertTokenEqualTo(tokens.get(9),  Token.Type.grammar,          "(",          2, 31);
        assertTokenEqualTo(tokens.get(10), Token.Type.letters,          "bye",        2, 32);
        assertTokenEqualTo(tokens.get(11), Token.Type.letters,          "bye",        2, 36);
        assertTokenEqualTo(tokens.get(12), Token.Type.grammar,          ")",          2, 39);
        assertTokenEqualTo(tokens.get(13), Token.Type.grammar,          ";",          2, 40);
        assertTokenEqualTo(tokens.get(14), Token.Type.grammar,          ".",          3,  1);
        assertTokenEqualTo(tokens.get(15), Token.Type.grammar,          ".",          3,  2);
        assertTokenEqualTo(tokens.get(16), Token.Type.grammar,          ";",          3,  3);
        assertTrue(tokens.get(17).isType(Token.Type.endOfFile));
        assertTrue(tokens.get(18).isEmpty());
    }

    @Test
    public void testInvalidInput() throws IOException {
        assertInvalid("+",                       CompilerError.Type.invalidCharacter, 1, 1, "+");
        assertInvalid("<",                       CompilerError.Type.invalidCharacter, 1, 1, "<");
        assertInvalid("   \n  *\n\n+",           CompilerError.Type.invalidCharacter, 2, 3, "*");
        assertInvalid("[[][\n\t]](\n(%)):;)):(", CompilerError.Type.invalidCharacter, 3, 2, "%");
    }
}