package com.demo.videostore.service.util;

import com.coremedia.iso.IsoFile;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class VideoUtil {

    private static final Logger logger = LoggerFactory.getLogger(VideoUtil.class);

    public static long getMp4Duration(String videoPath) throws IOException {
        IsoFile isoFile = new IsoFile(videoPath);
        long lengthInSeconds = isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                               isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        return lengthInSeconds;
    }

    public static long getDuration(String filePath) throws IOException {
        String mediaType = getMediaType(filePath);
        long result = 0;
        switch (mediaType) {
            case "audio/wav":
                result = AudioUtil.getDuration(filePath).intValue();
                break;
            case "audio/mpeg":
                result = AudioUtil.getMp3Duration(filePath).intValue();
                break;
            case "audio/m4a":
            case "video/quicktime":
            case "video/mp4":
                result = VideoUtil.getMp4Duration(filePath);
                break;
        }
        return result;
    }

    public static String getMediaType(String filePath) {
        String mediaType = null;
        try {
            File file = new File(filePath);
            Tika tika = new Tika();
            mediaType = tika.detect(file);
        } catch (IOException e) {
            logger.info("Get media type failed.");
        }
        return mediaType;
    }
}
