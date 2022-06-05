package com.demo.videostore.client.command.command;

import com.demo.videostore.client.model.model.Metadata;
import com.demo.videostore.client.service.service.VideoStoreService;
import com.demo.videostore.client.service.service.VideoStoreServiceGenerator;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.tika.Tika;
import picocli.CommandLine;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;

@CommandLine.Command(
        name = "upload",
        description = "Upload video command"
)
public class UploadCommand implements Runnable {

    @CommandLine.Parameters
    private String filePath;

    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    public void run() {
        VideoStoreService service = VideoStoreServiceGenerator.createService(VideoStoreService.class);
        try {
            uploadFile(service, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uploadFile(VideoStoreService service, String path) throws Exception {
        File file = new File(path);

        Tika tika = new Tika();
        String mediaType = tika.detect(file);

        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(mediaType),
                        file
                );

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Call<Metadata> call = service.uploadFile(body);

        try {
            Response<Metadata> result = call.execute();
            if(result.isSuccessful()) {
                Metadata metadata = result.body();
                StringBuilder sb = new StringBuilder();
                sb.append("Upload file successfully."); sb.append(LINE_SEPARATOR);
                sb.append("File id: " + metadata.getId()); sb.append(LINE_SEPARATOR);
                sb.append("File name: " + metadata.getFilename()); sb.append(LINE_SEPARATOR);
                sb.append("File Url: " + metadata.getUrl()); sb.append(LINE_SEPARATOR);
                System.out.println(sb);
            } else {
                System.out.println("Upload file failed, error code: " + result.code());
            }

        } catch (Exception ex) {
            System.out.println("execute uploadFile failed: " + ex.toString());
        }
    }
}
