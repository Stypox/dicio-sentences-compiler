package org.dicio.sentences_compiler.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import org.dicio.sentences_compiler.compiler.CompilerBase;
import org.dicio.sentences_compiler.compiler.CompilerToJava;
import org.dicio.sentences_compiler.util.CompilerError;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SentencesCompiler {

    private SentencesCompiler() {
    }

    /**
     * The main function, to be used in command line environments. In Java code use
     * {@link #compile(List, OutputStreamWriter, OutputStreamWriter, CompilerBase)} instead.
     */
    public static void main(final String[] args) throws IOException, CompilerError {
        final Arguments arguments = new Arguments();
        final JavaCommand javaCommand = new JavaCommand();
        final JCommander argParser = JCommander
                .newBuilder()
                .addObject(arguments)
                .addCommand("java", javaCommand)
                .build();

        for (final String arg : args) {
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
        } catch (final Throwable e) {
            StringBuilder stringBuilder = new StringBuilder();
            argParser.usage(stringBuilder);
            System.err.print(stringBuilder);
            throw e;
        }


        final CompilerBase compiler;
        switch (argParser.getParsedCommand()) {
            case "java":
                compiler = new CompilerToJava(javaCommand.variablePrefix, javaCommand.packageName,
                        javaCommand.className, javaCommand.sectionMapName);
                break;
            default:
                throw new ParameterException("Unexpected value: " + argParser.getParsedCommand());
        }

        final FileInfo outputFileInfo = new FileInfo(arguments.outputFile);
        final FileInfo sectionIdsFileInfo = new FileInfo(arguments.sectionIdsFile);
        compile(arguments.inputFiles,
                outputFileInfo.openOutputStream(false),
                sectionIdsFileInfo.openOutputStream(true),
                compiler);
        outputFileInfo.closeStream();
        sectionIdsFileInfo.closeStream();
    }


    /**
     * Compiles the dicio-sentences-language code contained in input files to code in the output
     * stream using the provided compiler. Also outputs a space-separated list of section ids to
     * the provided section ids stream.
     * @param inputFiles the input files to read data from (stdin is accepted). Note that this
     *                   should be an array of input streams, too, but in dicio-android it is only
     *                   used with files. So it is kept this way for now.
     * @param outputStream where to output compiled code into
     * @param sectionIdsStream where to output a space-separated list of section ids (null is
     *                         accepted)
     * @param compiler the compiler to use to compile from dicio-sentences-language input to output
     */
    public static void compile(final List<String> inputFiles,
                               final OutputStreamWriter outputStream,
                               final OutputStreamWriter sectionIdsStream,
                               final CompilerBase compiler) throws IOException, CompilerError {

        for (final String fileName : inputFiles) {
            final FileInfo inputFileInfo = new FileInfo(fileName);
            compiler.addInputStream(inputFileInfo.openInputStream(), inputFileInfo.fileName);
            inputFileInfo.closeStream();
        }

        compiler.compile(outputStream, sectionIdsStream);
    }


    private static class FileInfo {
        private final String fileName;
        private final Charset charset;
        private Closeable closeableStream = null;

        FileInfo(final String value) {
            final String[] values = value.split(":", 2);
            if (values.length == 1) {
                charset = StandardCharsets.UTF_8;
                fileName = values[0];
            } else {
                try {
                    charset = Charset.forName(values[0]);
                } catch (final Exception e) {
                    throw new ParameterException(
                            "Unknown charset \"" + values[0] + "\": " + value, e);
                }
                fileName = values[1];
            }
        }

        InputStreamReader openInputStream() throws FileNotFoundException {
            final InputStream inputStream;
            if (fileName.equals("stdin")) {
                inputStream = System.in;
            } else {
                inputStream = new FileInputStream(new File(fileName));
                closeableStream = inputStream;
            }
            return new InputStreamReader(inputStream, charset);
        }

        OutputStreamWriter openOutputStream(boolean ifEmptyThenNull) throws FileNotFoundException {
            if (ifEmptyThenNull && fileName.isEmpty()) {
                return null;
            }

            final OutputStream outputStream;
            if (fileName.equals("stdout")) {
                outputStream = System.out;
            } else {
                outputStream = new FileOutputStream(new File(fileName));
                closeableStream = outputStream;
            }
            return new OutputStreamWriter(outputStream, charset);
        }

        void closeStream() throws IOException {
            if (closeableStream != null) {
                closeableStream.close();
            }
        }
    }
}
