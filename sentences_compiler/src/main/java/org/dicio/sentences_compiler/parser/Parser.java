package org.dicio.sentences_compiler.parser;

import org.dicio.sentences_compiler.construct.CapturingGroup;
import org.dicio.sentences_compiler.construct.Construct;
import org.dicio.sentences_compiler.construct.OptionalConstruct;
import org.dicio.sentences_compiler.construct.OrList;
import org.dicio.sentences_compiler.construct.Section;
import org.dicio.sentences_compiler.construct.Sentence;
import org.dicio.sentences_compiler.construct.SentenceConstructList;
import org.dicio.sentences_compiler.construct.Word;
import org.dicio.sentences_compiler.construct.WordBase;
import org.dicio.sentences_compiler.construct.WordWithVariations;
import org.dicio.sentences_compiler.lexer.Token;
import org.dicio.sentences_compiler.lexer.TokenStream;
import org.dicio.sentences_compiler.util.CompilerError;
import org.dicio.sentences_compiler.util.JavaSyntaxCheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Parser {
    private final TokenStream ts;

    public Parser(TokenStream tokenStream) {
        this.ts = tokenStream;
    }

    public ArrayList<Section> parse() throws CompilerError {
        ArrayList<Section> sections = new ArrayList<>();
        while (true) {
            Section section = readSection();
            if (section == null) {
                if (ts.get(0).isType(Token.Type.endOfFile)) {
                    ts.movePositionForwardBy(1);
                } else {
                    throw new CompilerError(CompilerError.Type.expectedSectionOrEndOfFile, ts.get(0), "");
                }

                if (ts.get(0).isEmpty()) {
                    break;
                }
            } else {
                sections.add(section);
            }
        }

        validate(sections);
        return sections;
    }

    public SentenceConstructList parseSentenceConstructList() throws CompilerError {
        final SentenceConstructList result = readSentenceConstructList();
        if (result == null) {
            throw new CompilerError(CompilerError.Type.expectedSentenceConstructList, ts.get(0), "");
        }
        return result;
    }


    private Section readSection() throws CompilerError {
        Section section = new Section();

        String sectionId = readSectionId();
        if (sectionId == null) {
            return null;
        }
        String inputStreamName = ts.get(-2).getInputStreamName();
        int sectionIdLine = ts.get(-2).getLine();

        Section.Specificity specificity = readSpecificity();
        section.setSectionInfo(sectionId, specificity, inputStreamName, sectionIdLine);

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
                final String sectionId = ts.get(0).getValue();
                JavaSyntaxCheck.checkValidJavaVariableName(sectionId, ts.get(0),
                        CompilerError.Type.invalidSectionId);
                ts.movePositionForwardBy(2);
                return sectionId;
            } else {
                throw new CompilerError(CompilerError.Type.invalidToken, ts.get(1), "Expected \":\" after section id");
            }
        } else {
            return null;
        }
    }

    private Section.Specificity readSpecificity() throws CompilerError {
        if (ts.get(0).isType(Token.Type.lettersPlusOther)) {
            String specificityStr = ts.get(0).getValue();
            Section.Specificity specificity;

            switch (specificityStr) {
                case "low":
                    specificity = Section.Specificity.LOW;
                    break;
                case "medium":
                    specificity = Section.Specificity.MEDIUM;
                    break;
                case "high":
                    specificity = Section.Specificity.HIGH;
                    break;
                default:
                    throw new CompilerError(CompilerError.Type.invalidSpecificity, ts.get(0), "Accepted values are \"low\", \"medium\" and \"high\"");
            }

            ts.movePositionForwardBy(1);
            return specificity;
        } else {
            throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0), "Expected specificity after section id");
        }
    }

    private Sentence readSentence() throws CompilerError {
        Sentence sentence = new Sentence();

        final String capturingGroupAltsSentenceId = readCapturingGroupAlternativesSentenceId();
        boolean foundId = capturingGroupAltsSentenceId != null;
        if (foundId) {
            sentence.setSentenceId(capturingGroupAltsSentenceId, true,
                    ts.get(-2).getInputStreamName(), ts.get(-2).getLine());
        } else {
            final String sentenceId = readSentenceId();
            foundId = sentenceId != null;
            if (foundId) {
                sentence.setSentenceId(sentenceId, false,
                        ts.get(-2).getInputStreamName(), ts.get(-2).getLine());
            } else {
                sentence.setSentenceId("", false,
                        ts.get(0).getInputStreamName(), ts.get(0).getLine());
            }
        }

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

    private String readCapturingGroupAlternativesSentenceId() throws CompilerError {
        if (!ts.get(0).equals(Token.Type.grammar, "[") ||
                !ts.get(1).equals(Token.Type.grammar, ".")) {
            return null;
        }
        if (!ts.get(2).isType(Token.Type.lettersPlusOther)) {
            throw new CompilerError(CompilerError.Type.invalidToken, ts.get(1),
                    "Expected capturing group alternatives sentence id after token \"[.\"");
        }
        if (!ts.get(3).equals(Token.Type.grammar, ".") ||
                !ts.get(4).equals(Token.Type.grammar, "]")) {
            throw new CompilerError(CompilerError.Type.invalidToken, ts.get(2),
                    "Expected \".]\" after capturing group alternatives sentence id");
        }

        String sentenceId = ts.get(2).getValue();
        ts.movePositionForwardBy(5);
        return sentenceId;
    }

    private String readSentenceId() throws CompilerError {
        if (!ts.get(0).equals(Token.Type.grammar, "[")) {
            return null;
        }
        if (!ts.get(1).isType(Token.Type.lettersPlusOther)) {
            throw new CompilerError(CompilerError.Type.invalidToken, ts.get(1),
                    "Expected sentence id after token \"[\"");
        }
        if (!ts.get(2).equals(Token.Type.grammar, "]")) {
            throw new CompilerError(CompilerError.Type.invalidToken, ts.get(2),
                    "Expected \"]\" after sentence id");
        }

        String sentenceId = ts.get(1).getValue();
        ts.movePositionForwardBy(3);
        return sentenceId;
    }

    private SentenceConstructList readSentenceContent() throws CompilerError {
        if (ts.get(0).isType(Token.Type.lettersPlusOther) && ts.get(1).equals(Token.Type.grammar, ":")) {
            // found section id, skip to next section
            return null;
        }

        return readSentenceConstructList();
    }

    private SentenceConstructList readSentenceConstructList() throws CompilerError {
        SentenceConstructList sentenceConstructList = new SentenceConstructList();

        boolean foundSentenceConstruct = false;
        while (true) {
            final OrList orList = readOrList();

            if (orList == null) {
                break;
            } else {
                foundSentenceConstruct = true;
                sentenceConstructList.addConstruct(orList.shrink());
            }
        }

        if (foundSentenceConstruct) {
            return sentenceConstructList;
        } else {
            return null;
        }
    }

    private OrList readOrList() throws CompilerError {
        final OrList orList = new OrList();

        boolean foundSentenceConstruct = false;
        while (true) {
            Construct sentenceConstruct;
            sentenceConstruct = readWord();
            if (sentenceConstruct == null) {
                sentenceConstruct = readCapturingGroup();
            }
            if (sentenceConstruct == null) {
                sentenceConstruct = readSentenceConstructListInsideParenthesis();
            }

            if (sentenceConstruct == null) {
                if (foundSentenceConstruct) { // there is a | at the end of the OrList
                    throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0), "Expected sentence construct after \"|\" token");
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
                orList.addConstruct(new OptionalConstruct());
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

    private WordBase readWord() throws CompilerError {
        if (ts.get(0).equals(Token.Type.grammar, "\"")) {
            ts.movePositionForwardBy(1);
            final WordBase word = readWordValue(true);

            if (word == null) {
                throw new CompilerError(CompilerError.Type.expectedWordValue, ts.get(0), "");
            } else if (!ts.get(0).equals(Token.Type.grammar, "\"")) {
                throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0),
                        "Expected closing quotation marks '\"' after diacritics-sensitive word");
            } else {
                ts.movePositionForwardBy(1); // skip closing quotation mark "
                return word;
            }
        } else {
            return readWordValue(false);
        }
    }

    private WordBase readWordValue(final boolean diacriticsSensitive) throws CompilerError {
        final List<List<String>> parts = new ArrayList<>();
        while (true) {
            if (ts.get(0).isType(Token.Type.letters)
                    && (parts.isEmpty() || parts.get(parts.size() - 1).size() > 1)) {
                // only use the letters token at the beginning or after a <> variations group
                parts.add(Collections.singletonList(ts.get(0).getValue().toLowerCase()));
                ts.movePositionForwardBy(1);

            } else if (ts.get(0).isType(Token.Type.grammar)
                    && (ts.get(0).isValue("<") || (ts.get(0).isValue(" <") && parts.isEmpty()))) {
                // "<" means `<` directly after some letters, " <" means `<` after a non-letter
                // character, and if `<` wasn't directly after the previous letters in this word, it
                // means it does not belong to this word, hence the `&& parts.isEmpty()`
                ts.movePositionForwardBy(1);
                final List<String> part = new ArrayList<>();
                final boolean lastPart;

                while (true) {
                    if (ts.get(0).isType(Token.Type.letters)) {
                        part.add(ts.get(0).getValue().toLowerCase());
                        ts.movePositionForwardBy(2);

                        if (ts.get(-1).equals(Token.Type.grammar, "?")) {
                            part.add("");
                            ts.movePositionForwardBy(1);
                        }

                        if (ts.get(-1).equals(Token.Type.grammar, ">")) {
                            lastPart = false;
                            break;
                        } else if (ts.get(-1).equals(Token.Type.grammar, "> ")) {
                            lastPart = true;
                            break;
                        } else if (ts.get(-1).equals(Token.Type.grammar, "|")) {
                            if (part.get(part.size() - 1).isEmpty()) {
                                // if the last part is empty, it means we just encountered a ?
                                throw new CompilerError(CompilerError.Type.invalidVariationsGroup,
                                        ts.get(-1), "Expected closing angle bracket '>' after '?'"
                                        + " token");
                            }
                        } else {
                            throw new CompilerError(CompilerError.Type.invalidVariationsGroup,
                                    ts.get(-1), "Expected closing angle bracket '>', question mark"
                                    + " '?' or variations separator '|' after letters");
                        }

                    } else {
                        throw new CompilerError(CompilerError.Type.invalidVariationsGroup, ts.get(0),
                                "Expected letters after '<' or '|' tokens");
                    }
                }

                if (part.size() <= 1) {
                    throw new CompilerError(CompilerError.Type.invalidVariationsGroup, ts.get(-1),
                            "A variations group should provide more than one alternative");
                }

                parts.add(part);
                if (lastPart) {
                    break;
                }

            } else {
                break;
            }
        }

        if (parts.size() == 0) {
            return null;
        } else if (parts.size() == 1 && parts.get(0).size() == 1) {
            return new Word(parts.get(0).get(0), diacriticsSensitive);
        } else {
            boolean canBeEmpty = true;
            for (final List<String> part : parts) {
                if (!part.get(part.size() - 1).isEmpty()) {
                    canBeEmpty = false;
                    break;
                }
            }

            if (canBeEmpty) {
                throw new CompilerError(CompilerError.Type.invalidVariationsGroup, ts.get(-1),
                        (parts.size() == 1 ? "This variations group" : "These variations groups")
                        + " would match an empty word, which is not allowed");
            }

            return new WordWithVariations(parts, diacriticsSensitive);
        }
    }

    private SentenceConstructList readSentenceConstructListInsideParenthesis() throws CompilerError {
        if (ts.get(0).equals(Token.Type.grammar, "(")) {
            ts.movePositionForwardBy(1);

            SentenceConstructList sentenceConstructList = readSentenceConstructList();
            if (sentenceConstructList == null) {
                throw new CompilerError(CompilerError.Type.expectedSentenceConstructList, ts.get(0), "");
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
            if (ts.get(0).isType(Token.Type.lettersPlusOther)) {
                if (ts.get(1).equals(Token.Type.grammar, ".")) {
                    final String capturingGroupName = ts.get(0).getValue();
                    JavaSyntaxCheck.checkValidJavaVariableName(capturingGroupName, ts.get(0),
                            CompilerError.Type.invalidCapturingGroupName);
                    ts.movePositionForwardBy(2);
                    return new CapturingGroup(capturingGroupName);
                } else {
                    throw new CompilerError(CompilerError.Type.invalidToken, ts.get(1), "Expected point \".\" after capturing group name");
                }
            } else {
                throw new CompilerError(CompilerError.Type.expectedCapturingGroupName, ts.get(0), "");
            }
        } else {
            return null;
        }
    }


    private void validate(ArrayList<Section> sections) throws CompilerError {
        ArrayList<String> sectionIds = new ArrayList<>();
        for(Section section : sections) {
            section.compileSentenceWordLists();

            String sectionId = section.getSectionId();
            if (sectionIds.contains(sectionId)) {
                throw new CompilerError(CompilerError.Type.duplicateSectionId, sectionId, section.getInputStreamName(), section.getLine(), "");
            } else {
                sectionIds.add(sectionId);
            }
        }
    }
}
