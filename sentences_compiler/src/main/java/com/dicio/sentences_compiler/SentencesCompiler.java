package com.dicio.sentences_compiler;

import com.dicio.sentences_compiler.compiler.CompilerToJava;
import com.dicio.sentences_compiler.util.CompilerError;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.List;

public class SentencesCompiler {
    public static final boolean DEBUG = false;

    public static void main(String[] args) throws IOException {
        parseGlobalArgs(Arrays.asList(args));
    }

    private static void parseGlobalArgs(List<String> args) {
        if (args.size() < 2) {
            if (args.size() == 1 && args.get(0).toLowerCase().equals("--help")) {
                printGlobalHelp(System.out);
            } else {
                printGlobalHelp(System.err);
            }
            return;
        } else if (args.get(0).toLowerCase().equals("--help")) {
            printGlobalHelp(System.out);
            return;
        }

        List<String> nextArgs = args.subList(2, args.size());
        Charset charset;
        try {
            charset = Charset.forName(args.get(0));
        } catch (IllegalCharsetNameException e) {
            System.err.println("Invalid java charset name: " + args.get(0));
            if (DEBUG) e.printStackTrace(System.err);
            return;
        } catch (UnsupportedCharsetException e) {
            System.err.println("Unsupported charset: " + args.get(0));
            if (DEBUG) e.printStackTrace(System.err);
            return;
        }


        switch (args.get(1).toLowerCase()) {
            case "java":
                parseJavaArgs(nextArgs, charset);
                break;
            default:
                System.err.println("Unsupported language: " + args.get(1));
                break;
        }
    }



    private static void parseJavaArgs(List<String> args, Charset charset) {
        if (args.size() != 1 && args.size() != 3) {
            printJavaHelp(System.err);
            return;
        } else if (args.get(0).toLowerCase().equals("--help")) {
            printJavaHelp(System.out);
            return;
        }

        CompilerToJava compilerToJava = new CompilerToJava(System.in, System.out, charset);

        try {
            if (args.size() == 1) {
                compilerToJava.compileToVariables(args.get(0));
            } else {
                compilerToJava.compileToFile(args.get(0), args.get(1), args.get(2));
            }
        } catch (CompilerError e) {
            System.err.println(e.getMessage());
            if (DEBUG) e.printStackTrace(System.err);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            if (DEBUG) e.printStackTrace(System.err);
        }
    }


    private static void printGlobalHelp(PrintStream output) {
        output.print("Sentences compiler for Dicio assistant\n" +
                "Usage: sentences_compiler.jar CHARSET LANGUAGE [language_options]\n" +
                "\n" +
                "CHARSET encoding for stdin and stdout. Must be a valid java charset name (e.g. default, UTF-8, UTF-16)\n" +
                "LANGUAGE can be:\n" +
                "    java    -> Java language, StandardRecognizer\n" +
                "\n" +
                "If --help is provided as the first argument, prints this help screen and exits\n" +
                "For language specific help use: SentencesCompiler CHARSET LANGUAGE --help\n" +
                "\n" +
                "Input is read from stdin using the provided CHARSET." +
                " It should be Dicio-sentences-language code." +
                " Compiled output is written to stdout using the provided CHARSET.\n");
    }

    private static void printJavaHelp(PrintStream output) {
        output.print("Compiler to Java: compiles every section to a com.dicio.component.input.standard.StandardRecognizer" +
                " (from library dicio-assistance-component)\n" +
                "Usage: sentences_compiler.jar CHARSET java VARIABLE_PREFIX [PACKAGE_NAME CLASS_NAME]\n" +
                "\n" +
                "VARIABLE_PREFIX is the prefix for the name of every StandardRecognizer," +
                " that will be followed by the name of the corresponding section\n" +
                "PACKAGE_NAME is the name of the Java package\n" +
                "CLASS_NAME is the name of the Java class\n" +
                "\n" +
                "If --help is provided in the place of VARIABLE_PREFIX, prints this help screen and exits\n" +
                "\n" +
                "When VARIABLE_PREFIX and PACKAGE_NAME are provided an entire valid Java file is generated," +
                " otherwise only the StandardRecognitionUnit variables are included.\n");
    }
}
