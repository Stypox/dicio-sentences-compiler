package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.compiler.Alternative;
import org.dicio.sentences_compiler.compiler.CompilableToJava;
import org.dicio.sentences_compiler.compiler.RepeatedList;
import org.dicio.sentences_compiler.util.CompilerError;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Section implements CompilableToJava {
    public enum Specificity {
        high,
        medium,
        low,
    }

    private String sectionId;
    private Specificity specificity;
    private final List<Sentence> sentences;
    private final Set<String> capturingGroupNames;

    private String inputStreamName;
    private int line;

    public Section() {
        sentences = new ArrayList<>();
        capturingGroupNames = new HashSet<>();
    }

    public void setSectionInfo(final String sectionId,
                               final Specificity specificity,
                               final String inputStreamName,
                               final int line) {
        this.sectionId = sectionId;
        this.specificity = specificity;
        this.inputStreamName = inputStreamName;
        this.line = line;
    }

    public void addSentence(final Sentence sentence) {
        sentences.add(sentence);
        capturingGroupNames.addAll(sentence.getCapturingGroupNames());
    }

    public void compileSentenceWordLists() throws CompilerError {
        for (final Sentence sentence : sentences) {
            sentence.compileWordList();
        }
    }


    public String getSectionId() {
        return sectionId;
    }

    public Specificity getSpecificity() {
        return specificity;
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public String getInputStreamName() {
        return inputStreamName;
    }

    public int getLine() {
        return line;
    }


    @Override
    public void compileToJava(final OutputStreamWriter output, final String variableName)
            throws IOException {
        if (capturingGroupNames.isEmpty()) {
            output.write("public static final StandardRecognizerData ");
            output.write(variableName);
            output.write("=new StandardRecognizerData");
            javaConstructStandardRecognizerData(output);

        } else {
            final String sectionClassName = "SectionClass_" + variableName;

            output.write("public static final class ");
            output.write(sectionClassName);
            output.write(" extends StandardRecognizerData{");
            output.write(sectionClassName);
            output.write("(){super");
            javaConstructStandardRecognizerData(output);

            output.write(";}public final String ");
            boolean comma=false;
            for (final String capturingGroupName : capturingGroupNames) {
                if (comma) {
                    output.write(",");
                } else {
                    comma=true;
                }

                output.write(capturingGroupName);
                output.write("=\"");
                output.write(capturingGroupName);
                output.write("\"");
            }

            output.write(";}public static final ");
            output.write(sectionClassName);
            output.write(" ");
            output.write(variableName);
            output.write("=new ");
            output.write(sectionClassName);
            output.write("()");
        }
    }

    private void javaConstructStandardRecognizerData(final OutputStreamWriter output)
            throws IOException {
        output.write("(Specificity.");
        switch (specificity) {
            case low:
                output.write("low");
                break;
            case medium:
                output.write("medium");
                break;
            case high:
                output.write("high");
                break;
        }

        for (Sentence sentence : sentences) {
            if (!sentence.isCapturingGroupAlternatives()) {
                output.write(",");
                sentence.compileToJava(output, "");
            }
        }
        output.write(")");
    }

    public Map<String, JSONObject> compileToDataset() {
        final Map<String, RepeatedList> capturesAlternatives = new HashMap<>();
        for (final Sentence sentence : sentences) {
            if (sentence.isCapturingGroupAlternatives()) {
                capturesAlternatives.put(
                        sentence.getSentenceId(),
                        new RepeatedList(sentence.buildAlternatives(capturesAlternatives)
                                .stream()
                                .map(alt -> alt.sentence)
                                .collect(Collectors.toList())));

            }
        }

        final Map<String, JSONObject> result = new HashMap<>();
        for (final Sentence sentence : sentences) {
            if (!sentence.isCapturingGroupAlternatives()) {
                for (final Alternative alternative :
                        sentence.buildAlternatives(capturesAlternatives)) {
                    final JSONObject value = new JSONObject();
                    value.put("skill", sectionId);
                    alternative.capturingGroupValues.forEach(value::putOnce);
                    result.put(alternative.sentence, value);
                }
            }
        }

        return result;
    }
}
