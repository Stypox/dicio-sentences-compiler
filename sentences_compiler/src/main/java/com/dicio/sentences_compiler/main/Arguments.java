package com.dicio.sentences_compiler.main;

import com.beust.jcommander.Parameter;

import java.util.List;

public class Arguments {

    @Parameter(names = {"-h", "--help"}, description = "Shows this screen")
    public boolean help = false;

    @Parameter(names = {"-i", "--input"}, description = "An input file containing dicio-sentences-language code (stdin is accepted), and its charset, formatted like `--input CHARSET:FILE` (`CHARSET:` can be emitted, defaulting to utf8)", required = true)
    public List<String> inputFiles;

    @Parameter(names = {"-o", "--output"}, description = "The output file (stdout is accepted), and its charset, formatted like `--output CHARSET:FILE` (`CHARSET:` can be emitted, defaulting to utf8)", required = true)
    public String outputFile;
}
