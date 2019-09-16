package com.dicio.sentences_compiler.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.dicio.sentences_compiler.compiler.CompilerBase;
import com.dicio.sentences_compiler.compiler.CompilerToJava;
import com.dicio.sentences_compiler.util.CompilerError;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SentencesCompiler {

    public static void main(String[] args) throws IOException, CompilerError {
        Arguments arguments = new Arguments();
        JavaCommand javaCommand = new JavaCommand();
        JCommander argParser = JCommander
                .newBuilder()
                .addObject(arguments)
                .addCommand("java", javaCommand)
                .build();

        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                argParser.usage();
                return;
            }
        }

        try {
            argParser.parse(args);
            if (argParser.getParsedCommand() == null) {
                throw new ParameterException("A command must be supplied (e.g. `java`)");
            }
        } catch (Throwable e) {
            StringBuilder stringBuilder = new StringBuilder();
            argParser.usage(stringBuilder);
            System.err.print(stringBuilder);
            throw e;
        }

        CompilerBase compiler;
        switch (argParser.getParsedCommand()) {
            case "java":
                compiler = new CompilerToJava(javaCommand.variablePrefix, javaCommand.packageName, javaCommand.className);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + argParser.getParsedCommand());
        }

        for (String file : arguments.inputFiles) {
            addInputFromOption(file, compiler);
        }
        compileToOutputFromOption(arguments.outputFile, compiler);
    }

    private static class FileInfo {
        String fileName;
        Charset charset;

        FileInfo(String value) {
            String[] values = value.split(":", 2);
            if (values.length == 1) {
                charset = StandardCharsets.UTF_8;
                fileName = values[0];
            } else {
                try {
                    charset = Charset.forName(values[0]);
                } catch (Exception e) {
                    throw new ParameterException("Unknown charset \"" + values[0] + "\": " + value);
                }
                fileName = values[1];
            }
        }
    }

    private static void addInputFromOption(String value, CompilerBase compiler) throws IOException, CompilerError {
        FileInfo fileInfo = new FileInfo(value);

        if (fileInfo.fileName.equals("stdin")) {
            compiler.addInputStream(new InputStreamReader(System.in, fileInfo.charset), fileInfo.fileName);
        } else {
            try {
                File file = new File(fileInfo.fileName);
                compiler.addInputStream(new InputStreamReader(new FileInputStream(file), fileInfo.charset), file.getName());
            } catch (Exception e) {
                throw new ParameterException("File \"" + fileInfo.fileName + "\" does not exist: " + value);
            }
        }
    }

    private static void compileToOutputFromOption(String value, CompilerBase compiler) throws IOException, CompilerError {
        FileInfo fileInfo = new FileInfo(value);

        if (fileInfo.fileName.equals("stdout")) {
            compiler.compile(new OutputStreamWriter(System.out, fileInfo.charset));
        } else {
            try {
                File file = new File(fileInfo.fileName);
                OutputStream outputStream = new FileOutputStream(file);
                compiler.compile(new OutputStreamWriter(outputStream, fileInfo.charset));
                outputStream.close();
            } catch (Exception e) {
                throw new ParameterException("File \"" + fileInfo.fileName + "\" does not exist: " + value);
            }
        }
    }
}
