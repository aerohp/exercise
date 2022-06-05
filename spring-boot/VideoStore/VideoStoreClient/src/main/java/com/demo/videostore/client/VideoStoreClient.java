package com.demo.videostore.client;

import com.demo.videostore.client.command.command.MainCommand;
import picocli.CommandLine;

public class VideoStoreClient {

    public static void main(String[] args) {
        new CommandLine(new MainCommand()).execute(args);
    }
}
