package com.stypox.sentences_compiler.parser;

import com.stypox.sentences_compiler.lexer.Token;
import com.stypox.sentences_compiler.parser.construct.BaseSentenceConstruct;
import com.stypox.sentences_compiler.parser.construct.CapturingGroup;
import com.stypox.sentences_compiler.parser.construct.ConstructOptional;
import com.stypox.sentences_compiler.parser.construct.OrList;
import com.stypox.sentences_compiler.parser.construct.Sentence;
import com.stypox.sentences_compiler.parser.construct.SentenceConstructList;
import com.stypox.sentences_compiler.parser.construct.Word;
import com.stypox.sentences_compiler.util.CompilerError;
import com.stypox.sentences_compiler.lexer.TokenStream;
import com.stypox.sentences_compiler.lexer.Tokenizer;
import com.stypox.sentences_compiler.parser.construct.Section;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Parser {
    private TokenStream ts;

    public Parser(InputStream inputStream) throws IOException, CompilerError {
        Tokenizer tokenizer = new Tokenizer(inputStream);
        this.ts = tokenizer.tokenize();
    }

    public ArrayList<Section> parse() throws CompilerError {
        ArrayList<Section> sections = new ArrayList<>();
        while (true) {
            Section section = readSection();
            if (section == null) {
                if (!ts.isEmpty()) {
                    throw new CompilerError(CompilerError.Type.expectedSectionOrEndOfFile, ts.get(0), "");
                }
                break;
            }
            
            sections.add(section);
        }
        return sections;
    }
    
    private Section readSection() throws CompilerError {
        Section section = new Section();

        String sectionId = readSectionId();
        if (sectionId == null) {
            return null;
        }
        section.setSectionId(sectionId);

        boolean foundSentences = false;
        while (true) {
            Sentence sentence = readSentence();
            if (sentence == null) {
                break;
            }

            foundSentences = true;
            section.addSentence(sentence);
        }

        if (!foundSentences) {
            throw new CompilerError(CompilerError.Type.expectedSentence, ts.get(0), "");
        }
        return section;
    }

    private String readSectionId() throws CompilerError {
        if (ts.get(0).isType(Token.Type.lettersPlusOther)) {
            if (ts.get(1).equals(Token.Type.grammar, ":")) {
                String sectionId = ts.get(0).getValue();
                ts.movePositionForwardBy(2);
                return sectionId;
            } else {
                throw new CompilerError(CompilerError.Type.invalidToken, ts.get(1), "Expected \":\" after section id");
            }
        } else {
            return null;
        }
    }

    private Sentence readSentence() throws CompilerError {
        Sentence sentence = new Sentence();

        String sentenceId = readSentenceId();
        boolean foundId = (sentenceId != null);
        sentence.setSentenceId(foundId ? sentenceId : "");

        SentenceConstructList sentenceContent = readSentenceContent();
        if (sentenceContent == null) {
            if (foundId) {
                throw new CompilerError(CompilerError.Type.expectedSentenceContent, ts.get(0), "");
            } else {
                return null;
            }
        }

        if (ts.get(0).equals(Token.Type.grammar, ";")) {
            sentence.setSentenceConstructs(sentenceContent);
            ts.movePositionForwardBy(1);
            return sentence;
        } else {
            throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0), "Expected \";\" at the end of sentence");
        }
    }

    private String readSentenceId() throws CompilerError {
        if (ts.get(0).equals(Token.Type.grammar, "[")) {
            if (ts.get(1).isType(Token.Type.lettersPlusOther)) {
                if (ts.get(2).equals(Token.Type.grammar, "]")) {
                    String sentenceId = ts.get(1).getValue();
                    ts.movePositionForwardBy(3);
                    return sentenceId;
                } else {
                    throw new CompilerError(CompilerError.Type.invalidToken, ts.get(2), "Expected \"]\" after sentence id");
                }
            } else {
                throw new CompilerError(CompilerError.Type.invalidToken, ts.get(1), "Expected sentence id after token \"[\"");
            }
        } else {
            return null;
        }
    }

    private SentenceConstructList readSentenceContent() throws CompilerError {
        if (ts.get(0).isType(Token.Type.lettersPlusOther) && ts.get(1).equals(Token.Type.grammar, ":")) {
            // found section id, skip to next section
            return null;
        }

        return readSentenceConstructList(true);
    }

    private SentenceConstructList readSentenceConstructList(boolean capturingGroupsAllowed) throws CompilerError {
        SentenceConstructList sentenceConstructList = new SentenceConstructList();

        boolean foundSentenceConstruct = false;
        while (true) {
            BaseSentenceConstruct sentenceConstruct = null;
            OrList orList = readOrList();
            if (orList == null) {
                if (capturingGroupsAllowed) {
                    sentenceConstruct = readCapturingGroup();
                }
            } else {
                sentenceConstruct = orList.shrink();
            }

            if (sentenceConstruct == null) {
                break;
            } else {
                foundSentenceConstruct = true;
                sentenceConstructList.addConstruct(sentenceConstruct);
            }
        }

        if (foundSentenceConstruct) {
            return sentenceConstructList;
        } else {
            return null;
        }
    }

    private OrList readOrList() throws CompilerError {
        OrList orList = new OrList();

        boolean foundSentenceConstruct = false;
        while (true) {
            BaseSentenceConstruct sentenceConstruct;
            sentenceConstruct = readWord();
            if (sentenceConstruct == null) {
                sentenceConstruct = readSentenceConstructListInsideParenthesis();
            }

            if (sentenceConstruct == null) {
                if (foundSentenceConstruct) { // there is a | at the end of the OrList
                    if (ts.get(0).equals(Token.Type.grammar, ".")) {
                        throw new CompilerError(CompilerError.Type.optionalCapturingGroup, ts.get(0), "");
                    } else {
                        throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0), "\"|\" must be followed by a sentence construct");
                    }
                } else {
                    break;
                }
            } else {
                foundSentenceConstruct = true;
                orList.addConstruct(sentenceConstruct);
            }

            if (ts.get(0).equals(Token.Type.grammar, "|")) {
                ts.movePositionForwardBy(1);
            } else if (ts.get(0).equals(Token.Type.grammar, "?")) {
                ts.movePositionForwardBy(1);
                orList.addConstruct(new ConstructOptional());
                break;
            } else {
                break;
            }
        }

        if (foundSentenceConstruct) {
            return orList;
        } else {
            return null;
        }
    }

    private Word readWord() {
        if (ts.get(0).isType(Token.Type.letters)) {
            Word word = new Word(ts.get(0).getValue());
            ts.movePositionForwardBy(1);
            return word;
        } else {
            return null;
        }
    }

    private SentenceConstructList readSentenceConstructListInsideParenthesis() throws CompilerError {
        if (ts.get(0).equals(Token.Type.grammar, "(")) {
            ts.movePositionForwardBy(1);

            SentenceConstructList sentenceConstructList = readSentenceConstructList(false);
            if (sentenceConstructList == null) {
                if (ts.get(0).equals(Token.Type.grammar, ".")) {
                    throw new CompilerError(CompilerError.Type.capturingGroupInsideParenthesis, ts.get(0), "");
                } else {
                    throw new CompilerError(CompilerError.Type.expectedSentenceConstructList, ts.get(0), "");
                }
            } else {
                if (ts.get(0).equals(Token.Type.grammar, ")")) {
                    ts.movePositionForwardBy(1);
                    return sentenceConstructList;
                } else {
                    throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0), "Expected \")\" after list of sentence constructs");
                }
            }
        } else {
            return null;
        }
    }

    private CapturingGroup readCapturingGroup() throws CompilerError {
        if (ts.get(0).equals(Token.Type.grammar, ".")) {
            ts.movePositionForwardBy(1);
            if (ts.get(0).equals(Token.Type.grammar, ".")) {
                ts.movePositionForwardBy(1);
                if (ts.get(0).equals(Token.Type.grammar, ".")) {
                    throw new CompilerError(CompilerError.Type.capturingGroupInvalidLength, ts.get(0), "Found more than two points \".\"");
                } else if (ts.get(0).equals(Token.Type.grammar, "?") || ts.get(0).equals(Token.Type.grammar, "|")) {
                    throw new CompilerError(CompilerError.Type.optionalCapturingGroup, ts.get(0), "");
                }
                return new CapturingGroup();
            } else {
                throw new CompilerError(CompilerError.Type.capturingGroupInvalidLength, ts.get(0), "Found only one point \".\"");
            }
        } else {
            return null;
        }
    }
}
