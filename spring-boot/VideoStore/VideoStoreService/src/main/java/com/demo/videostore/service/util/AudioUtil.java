package com.demo.videostore.service.util;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class AudioUtil {

    public static Float getDuration(String filePath) {
        try{
            File destFile = new File(filePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(destFile);
            AudioFormat format = audioInputStream.getFormat();
            long audioFileLength = destFile.length();
            int frameSize = format.getFrameSize();
            float frameRate = format.getFrameRate();
            float durationInSeconds = (audioFileLength / (frameSize * frameRate));
            return durationInSeconds;

        } catch (Exception e) {
            e.printStackTrace();
            return 0f;
        }

    }

    public static Float getMp3Duration(String filePath) {
        try {
            File mp3File = new File(filePath);
            MP3File f = (MP3File) AudioFileIO.read(mp3File);
            MP3AudioHeader audioHeader = (MP3AudioHeader)f.getAudioHeader();
            return Float.parseFloat(audioHeader.getTrackLength()+"");
        } catch(Exception e) {
            e.printStackTrace();
            return 0f;
        }
    }

    public static Float getMp3Duration(File mp3File) {
        try {
            //File mp3File = new File(filePath);
            MP3File f = (MP3File) AudioFileIO.read(mp3File);
            MP3AudioHeader audioHeader = (MP3AudioHeader)f.getAudioHeader();
            return Float.parseFloat(audioHeader.getTrackLength()+"");
        } catch(Exception e) {
            e.printStackTrace();
            return 0f;
        }
    }

    public static long getPCMDurationMilliSecond(String filePath) {
        File file = new File(filePath);

        long second = file.length() / 32000 ;

        long milliSecond = Math.round((file.length() % 32000)   / 32000.0  * 1000 ) ;

        return second * 1000 + milliSecond;
    }
}