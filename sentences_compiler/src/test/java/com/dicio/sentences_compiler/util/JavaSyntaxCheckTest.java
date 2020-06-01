package com.dicio.sentences_compiler.util;

import com.dicio.sentences_compiler.lexer.Token;

import org.junit.Test;

import static com.dicio.sentences_compiler.util.CompilerError.Type.invalidSectionId;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JavaSyntaxCheckTest {

    private void checkValidVariableName(final String name) throws CompilerError {
        JavaSyntaxCheck.checkValidJavaVariableName(name,
                new Token(Token.Type.lettersPlusOther, name, "nAmE", 51, 13));
    }

    private void assertInvalid(final String name, final String errorMustContain) {
        try {
            checkValidVariableName(name);
            fail("No error thrown with invalid java variable name");
        } catch (final CompilerError compilerError) {
            String message = compilerError.getMessage();
            assertTrue("\""+message+"\" is not of type \""+invalidSectionId.toString()+"\"",  message.contains(invalidSectionId.toString()));
            assertTrue("\""+message+"\" does not contain input stream name \"nAmE\"", message.contains("nAmE"));
            assertTrue("\""+message+"\" does not contain line number "+51, message.contains(String.valueOf(51)));
            assertTrue("\""+message+"\" does not contain column number "+13, message.contains(String.valueOf(13)));
            assertTrue("\""+message+"\" does not contain \""+errorMustContain+"\"", message.contains(errorMustContain));
        }
    }

    @Test
    public void invalidNameTest() {
        assertInvalid("7p", "The first character cannot be a digit: 7");
        assertInvalid("5",  "The first character cannot be a digit: 5");

        assertInvalid("aè", "Not in the english alphabet, not a digit and not \"_\": è");
        assertInvalid(";|", "Not in the english alphabet, not a digit and not \"_\": ;");

        assertInvalid("void",     "Java keyword");
        assertInvalid("class",    "Java keyword");
        assertInvalid("volatile", "Java keyword");
        assertInvalid("public",   "Java keyword");
        assertInvalid("false",    "Java keyword");
        assertInvalid("boolean",  "Java keyword");
    }

    @Test
    public void validNameTest() throws CompilerError {
        checkValidVariableName("A");
        checkValidVariableName("this_is_MySectioniD");
        checkValidVariableName("afalse");
        checkValidVariableName("Int");
        checkValidVariableName("privatE");
        checkValidVariableName("interfaceUser");
    }
}
