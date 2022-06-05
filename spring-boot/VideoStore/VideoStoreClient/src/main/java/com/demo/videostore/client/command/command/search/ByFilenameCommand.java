package com.demo.videostore.client.command.command.search;

import com.demo.videostore.client.model.model.Metadata;
import com.demo.videostore.client.service.service.VideoStoreService;
import com.demo.videostore.client.service.service.VideoStoreServiceGenerator;
import picocli.CommandLine;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@CommandLine.Command(
        name = "filename",
        description = "Search by file name"
)
public class ByFilenameCommand implements Runnable {

    @CommandLine.Parameters
    private String filename;

    @Override
    public void run() {
        VideoStoreService service = VideoStoreServiceGenerator.createService(VideoStoreService.class);
        Call<List<Metadata>> callSync = service.searchByFileName(filename);

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
            System.out.println("SearchByFilenameCommand exception: " + e.getMessage());
        }
    }
}
