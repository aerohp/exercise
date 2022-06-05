package com.demo.videostore.service.integration;

import com.demo.videostore.service.model.Metadata;
import com.demo.videostore.service.repository.MetadataRepository;
import io.minio.*;
import io.minio.messages.Item;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VideoStoreControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private MinioClient minioClient;

    private static final int METADATA_SIZE = 2;
    private List<Metadata> metadataList;

    @Value("${minio.video.bucket.name}")
    private String bucketName;

    private HttpHeaders httpHeaders;

    private MockMultipartFile getMockMultipartFile(String num) {
        return new MockMultipartFile(
                "file",
                "hello_" + num,
                "video/mp4",
                "Hello, World!".getBytes());
    }

    private Metadata createMetadata(JSONObject json) throws JSONException {
        Metadata metadata = new Metadata();
        metadata.setId(json.getString("id"));
        metadata.setFilename(json.getString("filename"));
        return metadata;
    }

    private void deleteBucket() throws Exception {
        if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            Iterator<Result<Item>> iterator1 = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build()).iterator();
            while (iterator1.hasNext()) {
                Result<Item> el = iterator1.next();
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(el.get().objectName()).build());
            }
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        }
    }

    private JSONArray getFileList() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/files").headers(httpHeaders)).andReturn();
        String content = result.getResponse().getContentAsString();
        return new JSONArray(content);
    }

    @Before
    public void init() {
        try {
            httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            metadataRepository.deleteAll();
            deleteBucket();

            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            metadataList = new ArrayList<>();
            for(int i = 0; i < METADATA_SIZE; ++i) {
                MvcResult result = mockMvc.perform(multipart("/v1/files/").file(getMockMultipartFile(Integer.toString(i)))).andReturn();
                String content = result.getResponse().getContentAsString();
                metadataList.add(createMetadata(new JSONObject(content)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void clearDB() {
        try {
            metadataRepository.deleteAll();
            deleteBucket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetFileList() throws Exception {
        JSONArray jsonArray = getFileList();
        Assert.assertEquals(METADATA_SIZE, jsonArray.length());
        for(int i = 0; i < METADATA_SIZE; ++i) {
            JSONObject object = jsonArray.getJSONObject(i);
            Assert.assertEquals(object.getString("id"), metadataList.get(i).getId());
            Assert.assertEquals(object.getString("filename"), metadataList.get(i).getFilename());
        }
    }

    @Test
    public void testGetFile() throws Exception {
        for(Metadata metadata : metadataList) {
            MvcResult result = mockMvc.perform(get("/v1/files/" + metadata.getId()).headers(httpHeaders)).andReturn();
            String content = result.getResponse().getContentAsString();
            Assert.assertEquals(content, "Hello, World!");
        }
    }

    @Test
    public void testGetFileReturnNotFound() throws Exception {
        mockMvc.perform(get("/v1/files/123").headers(httpHeaders))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRemoveFile() throws Exception {
        for(Metadata metadata : metadataList) {
            MvcResult result = mockMvc.perform(delete("/v1/files/" + metadata.getId()).headers(httpHeaders)).andReturn();
            Assert.assertEquals(result.getResponse().getStatus(), HttpStatus.NO_CONTENT.value());
        }
    }

    @Test
    public void testRemoveFileReturnNotFount() throws Exception {
        mockMvc.perform(delete("/v1/files/123").headers(httpHeaders))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUploadFile() throws Exception {
        mockMvc.perform(multipart("/v1/files/").file(getMockMultipartFile("100"))).andExpect(status().isOk());

        List<String> filenames = new ArrayList<>();
        JSONArray jsonArray = getFileList();
        for(int i = 0; i < jsonArray.length(); ++i) {
            JSONObject object = jsonArray.getJSONObject(i);
            filenames.add(object.getString("filename"));
        }

        Assert.assertTrue(filenames.contains("hello_100"));
    }
}
