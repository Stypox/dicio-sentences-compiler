package com.dicio.sentences_compiler.compiler;

import com.dicio.sentences_compiler.util.CompilerError;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public interface CompilerBase {
    void addInputStream(InputStreamReader input, String inputStreamName) throws IOException, CompilerError;
    void compile(OutputStreamWriter output) throws IOException, CompilerError;
}
