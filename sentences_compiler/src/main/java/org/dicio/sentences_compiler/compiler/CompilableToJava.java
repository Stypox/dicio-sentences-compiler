package org.dicio.sentences_compiler.compiler;

import java.io.IOException;
import java.io.OutputStreamWriter;

public interface CompilableToJava {
    void compileToJava(OutputStreamWriter output, String variableName) throws IOException;
}
