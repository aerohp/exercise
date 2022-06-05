package com.demo.videostore.client.command.command;

import com.demo.videostore.client.model.model.Metadata;
import com.demo.videostore.client.service.service.VideoStoreService;
import com.demo.videostore.client.service.service.VideoStoreServiceGenerator;
import picocli.CommandLine;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@CommandLine.Command(
        name = "list",
        description = "List video command"
)
public class ListCommand implements Runnable {

    @Override
    public void run() {
        VideoStoreService service = VideoStoreServiceGenerator.createService(VideoStoreService.class);
        Call<List<Metadata>> callSync = service.getFileList();

        try {
            Response<List<Metadata>> response = callSync.execute();
            if(response.isSuccessful()) {
                List<Metadata> metadataList = response.body();
                if(metadataList.size() != 0) {
                    for (Metadata metadata : response.body()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("file id: ");
                        sb.append(metadata.getId());
                        sb.append(", file name: ");
                        sb.append(metadata.getFilename());

                        System.out.println(sb);
                    }
                } else {
                    System.out.println("There is no file.");
                }
            } else {
                System.out.println("Get file list failed.");
            }

        } catch (IOException e) {
            System.out.println("ListCommand exception: " + e.getMessage());
        }
    }
}
