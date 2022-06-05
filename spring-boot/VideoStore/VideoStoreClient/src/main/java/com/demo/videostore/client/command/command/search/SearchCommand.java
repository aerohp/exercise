package com.demo.videostore.client.command.command.search;

import picocli.CommandLine;

@CommandLine.Command(
        name = "search",
        description = "Search command",
        subcommands = {
                ByDurationCommand.class,
                ByFilenameCommand.class
        }
)
public class SearchCommand implements Runnable {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}