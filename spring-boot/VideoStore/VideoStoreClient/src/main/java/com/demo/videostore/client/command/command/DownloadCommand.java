package com.demo.videostore.client.command.command;

import com.demo.videostore.client.service.service.VideoStoreService;
import com.demo.videostore.client.service.service.VideoStoreServiceGenerator;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import picocli.CommandLine;
import retrofit2.Call;
import retrofit2.Response;

import java.io.*;

@CommandLine.Command(
        name = "download",
        description = "Download video command"
)
public class DownloadCommand implements Runnable {

    @CommandLine.Parameters
    private String fileId;

    @Override
    public void run() {
        try {
            VideoStoreService service = VideoStoreServiceGenerator.createService(VideoStoreService.class);
            Call<ResponseBody> call = service.getFile(fileId);
            Response<ResponseBody> videoBody = call.execute();
            if(videoBody.isSuccessful()) {
                Headers headers = videoBody.headers();
                String filename = headers.get("filename");
                writeVideoToDisk(videoBody.body(), filename);
                System.out.println(filename + " file download successfully.");
            } else {
                System.out.println("Download file failed.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean writeVideoToDisk(ResponseBody body, String filename) {
        try {

            File futureStudioIconFile = new File(System.getProperty("user.dir") + File.separator + filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
