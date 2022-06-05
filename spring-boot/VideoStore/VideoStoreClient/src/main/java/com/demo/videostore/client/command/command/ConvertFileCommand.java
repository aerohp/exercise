package com.demo.videostore.client.command.command;

import com.demo.videostore.client.model.model.Metadata;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import com.demo.videostore.client.service.service.VideoStoreService;
import com.demo.videostore.client.service.service.VideoStoreServiceGenerator;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URI;
import java.util.UUID;

@CommandLine.Command(
        name = "convert",
        description = "Convert video command"
)
public class ConvertFileCommand implements Runnable {

    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final Logger logger = LoggerFactory.getLogger(ConvertFileCommand.class);

    @CommandLine.Parameters
    private String fileId;

    @CommandLine.Parameters
    private VideoStoreService.VideoType videoType;

    private VideoStoreService service;

    private ProgressBar progressBar;

    private EventSource eventSource;

    private class MyEventHandler implements EventHandler {

        @Override
        public void onOpen() {
        }

        @Override
        public void onClosed() {
            progressBar.stop();
            eventSource.close();
        }

        @Override
        public void onMessage(String s, MessageEvent messageEvent) {
            try {
                if(s.equals("GUI_ID")) {
                    Call<Metadata> call = service.convertFile(videoType, fileId, UUID.fromString(messageEvent.getData()));

                    call.enqueue(new Callback<Metadata>() {
                        @Override
                        public void onResponse(Call<Metadata> call, Response<Metadata> response) {
                            if(response.isSuccessful()) {
                                Metadata metadata = response.body();
                                StringBuilder sb = new StringBuilder();
                                sb.append("Convert file successfully."); sb.append(LINE_SEPARATOR);
                                sb.append("File id: " + metadata.getId()); sb.append(LINE_SEPARATOR);
                                sb.append("File name: " + metadata.getFilename()); sb.append(LINE_SEPARATOR);
                                sb.append("File Url: " + metadata.getUrl()); sb.append(LINE_SEPARATOR);
                                System.out.println(sb);
                            } else {
                                System.out.println("Convert file failed, error code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Metadata> call, Throwable throwable) {
                            System.out.println("onFailure: " + throwable.getLocalizedMessage());
                        }
                    });
                } else if(s.equals("PROGRESS")) {
                    progressBar.stepTo(Integer.parseInt(messageEvent.getData()));
                    if(messageEvent.getData().equals("100")) {
                        progressBar.stop();
                        eventSource.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onComment(String s) {
        }

        @Override
        public void onError(Throwable throwable) {
            progressBar.stop();
            eventSource.close();
        }
    }

    @Override
    public void run() {
        try {
            service = VideoStoreServiceGenerator.createService(VideoStoreService.class);
            progressBar = new ProgressBar("Converting", 100, 250, System.err, ProgressBarStyle.UNICODE_BLOCK);
            eventSource = new EventSource.Builder(new MyEventHandler(), URI.create("http://localhost:8080/v1/files/progress")).logger(new MyLogger()).build();
            eventSource.start();
            progressBar.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MyLogger implements com.launchdarkly.eventsource.Logger {

        @Override
        public void debug(String s, Object o) {

        }

        @Override
        public void debug(String s, Object o, Object o1) {

        }

        @Override
        public void info(String s) {

        }

        @Override
        public void warn(String s) {

        }

        @Override
        public void error(String s) {
            System.out.println("MyLogger error: " + s);
        }
    }
}
