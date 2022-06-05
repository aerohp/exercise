package com.demo.videostore.service.util;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;

public class VideoConverter {

    private static final Logger logger = LoggerFactory.getLogger(VideoConverter.class);

    public static enum VideoType {
        MP4,
        WEBM
    };

    public static boolean convert(VideoType videoType, InputStream is, OutputStream os, SseEmitter sseEmitter) {
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(is);
        Frame captured_frame = null;
        FFmpegFrameRecorder recorder = null;
        try {
            frameGrabber.start();
            recorder = new FFmpegFrameRecorder(os, frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), frameGrabber.getAudioChannels());

            if(videoType == VideoType.MP4) {
                recorder.setVideoCodecName("libopenh264");
                recorder.setFormat("mp4");
            } else if(videoType == VideoType.WEBM) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_VP8);
                recorder.setFormat("webm");
            } else return false;

            recorder.setFrameRate(frameGrabber.getFrameRate());
            recorder.setSampleRate(frameGrabber.getSampleRate());
            recorder.setAudioChannels(frameGrabber.getAudioChannels());
            recorder.setFrameRate(frameGrabber.getFrameRate());
            recorder.start();

            int lengthInFrames = frameGrabber.getLengthInFrames();
            int progress = 0;
            int iframe = 0;
            int framePerProgress = lengthInFrames / 100;
            while ((captured_frame = frameGrabber.grabFrame()) != null) {
                try {
                    if(++iframe / framePerProgress > progress) {
                        progress = iframe / framePerProgress;
                        sseEmitter.send(SseEmitter.event().name("PROGRESS").data(progress));
                    }
                    recorder.record(captured_frame);
                } catch (Exception e) {
                    //logger.error("recording for " + videoType.name() + " exception", e.getMessage());
                }
            }
            recorder.stop();
            recorder.release();
            frameGrabber.stop();
            return true;
        } catch (Exception e) {
            logger.error("convert file to " + videoType.name() + " failed.", e.getMessage());
            return false;
        }
    }
}
