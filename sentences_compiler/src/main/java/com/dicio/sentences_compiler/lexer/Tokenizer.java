package com.dicio.sentences_compiler.lexer;

import com.dicio.sentences_compiler.util.CompilerError;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class Tokenizer {
    private TokenStream ts;
    private InputStreamReader input;
    private int line, column;

    public Tokenizer() {
        ts = new TokenStream();
    }

    private void setInputStream(InputStreamReader input) {
        this.input = input;
        line = 1;
        column = 0;
    }
    private String getCh() throws IOException {
        int intCh = input.read();
        if (intCh == -1) {
            return "";
        }

        ++column;
        return new String(Character.toChars(intCh));
    }
    private Token.Type updateWordType(Token.Type currentWordType, String ch) {
        if (currentWordType == Token.Type.lettersPlusOther || isOtherValid(ch)) {
            return Token.Type.lettersPlusOther;
        } else {
            return Token.Type.letters;
        }
    }

    private boolean isSpace(String ch) {
        return Character.isWhitespace(ch.codePointAt(0)) && !ch.equals("\n");
    }
    private boolean isLetter(String ch) {
        return Character.isLetter(ch.codePointAt(0));
    }
    private boolean isOtherValid(String ch) {
        return Character.isDigit(ch.codePointAt(0)) || ch.equals("_");
    }
    private boolean isGrammar(String ch) {
        switch (ch) {
            case ":": case ";": case "|": case "?": case ".": case "(": case ")": case "[": case "]":
                return true;
            default:
                return false;
        }
    }

    public void tokenize(InputStreamReader inputStreamReader, String inputStreamName) throws IOException, CompilerError {
        setInputStream(inputStreamReader);
        String ch = getCh();

        while (!ch.isEmpty()) {
            if (isLetter(ch) || isOtherValid(ch)) {
                StringBuilder word = new StringBuilder(ch);
                Token.Type wordType = Token.Type.letters;
                int firstCharCol = column;

                while (true) {
                    wordType = updateWordType(wordType, ch);
                    ch = getCh();
                    if (!ch.isEmpty() && (isLetter(ch) || isOtherValid(ch))) {
                        word.append(ch);
                    } else {
                        break;
                    }
                }

                ts.push(new Token(wordType, word.toString(), inputStreamName, line, firstCharCol));
            } else if (isGrammar(ch)) {
                ts.push(new Token(Token.Type.grammar, ch, inputStreamName, line, column));
                ch = getCh();
            } else if (ch.equals("\n")) {
                ++line;
                column = 0;
                ch = getCh();
            } else if (ch.equals("#")) {
                while (!ch.equals("\n") && !ch.isEmpty()) {
                    ch = getCh();
                }
            } else if (isSpace(ch)) {
                ch = getCh();
            } else {
                throw new CompilerError(CompilerError.Type.invalidCharacter, ch, inputStreamName, column, line, "");
            }
        }

        ts.push(new Token(Token.Type.endOfFile, "", inputStreamName, line, column));
    }

    public TokenStream getTokenStream() {
        return ts;
    }
}
