package org.dicio.sentences_compiler.util;

import org.dicio.sentences_compiler.lexer.Token;

import javax.lang.model.SourceVersion;

public class JavaSyntaxCheck {

    private static final int minNum = "0".codePointAt(0);
    private static final int maxNum = "9".codePointAt(0);
    private static final int minLow = "a".codePointAt(0);
    private static final int maxLow = "z".codePointAt(0);
    private static final int minUp = "A".codePointAt(0);
    private static final int maxUp = "Z".codePointAt(0);
    private static final int underscore = "_".codePointAt(0);

    private JavaSyntaxCheck() {
    }

    public static void checkValidJavaVariableName(final String name,
                                                  final Token errorToken,
                                                  final CompilerError.Type type)
            throws CompilerError {

        if (Character.isDigit(name.codePointAt(0))) {
            throw new CompilerError(type, errorToken,
                    "The first character cannot be a digit: " + Character.toChars(name.codePointAt(0))[0]);
        }

        for (int i = 0; i < name.codePointCount(0, name.length()); ++i) {
            final int val = name.codePointAt(i);
            if (!((minNum <= val && val <= maxNum) ||
                    (minLow <= val && val <= maxLow) ||
                    (minUp <= val && val <= maxUp) ||
                    (val == underscore))) {
                throw new CompilerError(type, errorToken,
                        "Not in the english alphabet, not a digit and not \"_\": " + Character.toChars(val)[0]);
            }
        }

        if (SourceVersion.isKeyword(name)) {
            throw new CompilerError(type, errorToken, "Java keyword");
        }
    }
}
