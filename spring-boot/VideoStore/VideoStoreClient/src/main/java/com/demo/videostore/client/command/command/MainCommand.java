package com.demo.videostore.client.command.command;

import com.demo.videostore.client.command.command.search.SearchCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "main",
        subcommands = {
                UploadCommand.class,
                DeleteCommand.class,
                DownloadCommand.class,
                ListCommand.class,
                SearchCommand.class,
                ConvertFileCommand.class
        }
)
public class MainCommand implements Runnable {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}
