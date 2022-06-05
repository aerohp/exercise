package com.demo.videostore.client.command.command;

import com.demo.videostore.client.service.service.VideoStoreService;
import com.demo.videostore.client.service.service.VideoStoreServiceGenerator;
import okhttp3.ResponseBody;
import picocli.CommandLine;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete video command"
)
public class DeleteCommand implements Runnable {

    @CommandLine.Parameters
    private String fileId;

    @Override
    public void run() {
        try {
            VideoStoreService service = VideoStoreServiceGenerator.createService(VideoStoreService.class);
            Call<ResponseBody> call = service.deleteFile(fileId);
            Response<ResponseBody> deleteResponse = call.execute();
            if(deleteResponse.isSuccessful()) {
                System.out.println("Delete " + fileId + " successfully.");
            } else {
                System.out.println("Delete " + fileId + " failed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
