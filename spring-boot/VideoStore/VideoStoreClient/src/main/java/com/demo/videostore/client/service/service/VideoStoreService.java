package com.demo.videostore.client.service.service;

import com.demo.videostore.client.model.model.Metadata;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.UUID;

public interface VideoStoreService {

    public static enum VideoType {
        MP4,
        WEBM
    };

    @GET("/v1/files")
    Call<List<Metadata>> getFileList();

    @GET("/v1/files/{id}")
    Call<ResponseBody> getFile(@Path("id") String id);

    @DELETE("/v1/files/{id}")
    Call<ResponseBody> deleteFile(@Path("id") String id);

    @Multipart
    @POST("/v1/files/")
    Call<Metadata> uploadFile(@Part MultipartBody.Part file);

    @POST("/v1/files/convert/{videoType}/{id}/{guid}")
    Call<Metadata> convertFile(@Path("videoType") VideoType videoType, @Path("id") String id, @Path("guid") UUID guid);

    @GET("/v1/files/search/filename/{filename}")
    Call<List<Metadata>> searchByFileName(@Path("filename") String filename);

    @GET("/v1/files/search/duration/{from}-{to}")
    Call<List<Metadata>> searchByDuration(@Path("from") int from, @Path("to") int to);
}
