package com.dicio.sentences_compiler.compiler;

import com.dicio.sentences_compiler.construct.Section;
import com.dicio.sentences_compiler.parser.Parser;
import com.dicio.sentences_compiler.util.CompilerError;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class CompilerToJava {
    private InputStream inputStream;
    private OutputStreamWriter output;

    public CompilerToJava(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.output = new OutputStreamWriter(outputStream);
    }

    private List<Section> getSections() throws IOException, CompilerError {
        Parser parser = new Parser(inputStream);
        return parser.parse();
    }

    public void compileToVariables(String variablePrefix) throws IOException, CompilerError {
        List<Section> sections = getSections();
        for (Section section : sections) {
            section.compileToJava(output, variablePrefix + section.getSectionId());
        }
        output.flush();
    }

    public void compileToFile(String packageName, String className, String variablePrefix) throws CompilerError, IOException {
        output.write("package ");
        output.write(packageName);
        output.write(";\n" +
                "import com.dicio.input_recognition.standard.Sentence;\n" +
                "import com.dicio.input_recognition.standard.StandardRecognitionUnit;\n" +
                "public class ");
        output.write(className);

        output.write(" {\n");
        compileToVariables(variablePrefix);
        output.write("}\n");
        output.flush();
    }
}
