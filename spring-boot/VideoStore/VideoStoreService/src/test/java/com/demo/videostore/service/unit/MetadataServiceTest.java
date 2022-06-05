package com.demo.videostore.service.unit;

import com.demo.videostore.service.model.Metadata;
import com.demo.videostore.service.repository.MetadataRepository;
import com.demo.videostore.service.service.MetadataService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MetadataServiceTest {

    private static final int METADATA_SIZE = 2;

    @Mock
    private MetadataRepository metadataRepository;

    @InjectMocks
    private MetadataService metadataService;

    private List<Metadata> createMetadataList() {
        List<Metadata> metadataList = new ArrayList<>();
        for(int i = 1; i <= METADATA_SIZE; ++i) {
            Metadata metadata = new Metadata();
            metadata.setId(String.valueOf(i));
            metadata.setFilename("filename_" + i);
            metadataList.add(metadata);
        }
        return metadataList;
    }

    private Metadata createMetadata(String id) {
        Metadata metadata = new Metadata();
        metadata.setId(id);
        metadata.setFilename("filename_" + id);
        return metadata;
    }

    @Test
    public void testGetFileList() {
        List<Metadata> metadataList = createMetadataList();
        when(metadataRepository.findAll()).thenReturn(metadataList);
        List<Metadata> resultMetadataList = metadataService.getFileList();
        for(int i = 0; i < METADATA_SIZE; ++i) {
            Assert.assertEquals(metadataList.get(i).getId(), resultMetadataList.get(i).getId());
            Assert.assertEquals(metadataList.get(i).getFilename(), resultMetadataList.get(i).getFilename());
        }
    }

    @Test
    public void testGetMetadata() {
        Metadata metadata = createMetadata("1");
        when(metadataRepository.findById(any())).thenReturn(Optional.of(metadata));
        Metadata result = metadataService.getMetadata("1");
        Assert.assertEquals(metadata.getId(), result.getId());
        Assert.assertEquals(metadata.getFilename(), result.getFilename());
    }

    @Test(expected = ResponseStatusException.class)
    public void testGetMetadataNotFound() {
        String id = "123";
        when(metadataRepository.findById(id)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        metadataService.getMetadata(id);
    }

    @Test
    public void testCreateUploadedFile() {
        Metadata metadata = createMetadata("1");
        when(metadataRepository.insert(any(Metadata.class))).thenReturn(metadata);
        Metadata result = metadataService.createUploadedFile(metadata);
        Assert.assertEquals(metadata.getId(), result.getId());
        Assert.assertEquals(metadata.getFilename(), result.getFilename());
    }

    @Test(expected = ResponseStatusException.class)
    public void testCreateUploadedFileConflict() {
        when(metadataRepository.insert(any(Metadata.class))).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));
        metadataService.createUploadedFile(createMetadata("1"));
    }

    @Test
    public void testReplaceUploadedFile() {
        Metadata metadata = createMetadata("1");
        when(metadataRepository.save(any(Metadata.class))).thenReturn(metadata);
        Metadata result = metadataService.replaceUploadedFile(metadata);
        Assert.assertEquals(metadata.getId(), result.getId());
        Assert.assertEquals(metadata.getFilename(), result.getFilename());
    }

    @Test(expected = ResponseStatusException.class)
    public void testReplaceUploadedFileNotFound() {
        when(metadataRepository.save(any(Metadata.class))).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        metadataService.replaceUploadedFile(createMetadata("1"));
    }

    @Test
    public void testRemoveMetadata() {
        doNothing().when(metadataRepository).deleteById(any());
        metadataService.removeMetadata("1");
        verify(metadataRepository, times(1)).deleteById("1");
    }

    @Test(expected = ResponseStatusException.class)
    public void testRemoveMetadataNotFound() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(metadataRepository).deleteById(any());
        metadataService.removeMetadata("1");
    }
}
