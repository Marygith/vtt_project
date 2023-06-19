package com.diploma.vtt.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.diploma.vtt.service.FIleService.deleteIfPresent;

@Service
@Log4j2
public class ProcessVideoService {

    private boolean done = false;

    private final ProcessTextService processTextService;

    private Double duration = 0.0;

    public ProcessVideoService(ProcessTextService processTextService) {
        this.processTextService = processTextService;
    }

    public boolean isDone() {
        return done;
    }

    private void convertVideoToAudio(String pathToVideoFile) {
        startAnew();
        String pathToAudio = "audio.wav";
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", pathToVideoFile, pathToAudio).inheritIO();
        Process p = null;
        try {
            log.info("ffmpeg process has started at " + Instant.now().toString());
            p = pb.start();
            p.waitFor();
            log.info("ffmpeg process has ended at " + Instant.now().toString());
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private void getAudioDuration() {

        String pathToAudio = "audio.wav";
        ProcessBuilder pb = new ProcessBuilder("ffprobe", "-i", pathToAudio, "-show_entries", "format=duration", "-v", "quiet", "-of", "csv=\"p=0\"");
        Process p = null;
        try {
            log.info("ffprobe process has started at " + Instant.now().toString());
            p = pb.start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error("thread interrupted");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error("thread interrupted");
            }
            duration = Double.valueOf(reader.readLine());
            p.waitFor();
            log.info("ffprobe process has ended at " + Instant.now().toString());
        } catch (Exception e) {
            log.error(e.getMessage());
            getAudioDuration();
        }
    }

    public void passVideoToPython(String pathToVideoFile) {

        convertVideoToAudio(pathToVideoFile);
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            log.error("thread interrupted");
        }
        getAudioDuration();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error("thread interrupted");
        }
        ProcessBuilder pb = new ProcessBuilder("python3", "scripts\\whisper_script.py").redirectOutput(new File("new_scripts.txt"));

        Process p = null;
        try {
            log.info("whisper process has started at " + Instant.now().toString());
            p = pb.start();
            p.getErrorStream().transferTo(System.out);
            p.waitFor();
            log.info("whisper process has ended at " + Instant.now().toString());
            done = true;

        } catch (IOException | InterruptedException e) {
            log.error("whisper process didn't end correctly");
        }
        processTextService.divideTextOnParagraphs();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error("thread interrupted");
        }
        processTextService.getProcessKeysService().extractKeys();
        processTextService.getProcessKeysService().processKeys();
    }

    private void startAnew() {

        deleteIfPresent("audio.wav");
        deleteIfPresent("new_scripts.txt");
        deleteIfPresent("keys.txt");
        deleteIfPresent("script_without_timings.txt");
        deleteIfPresent("divided.txt");
        duration = 0.0;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Double getDuration() {
        return duration;
    }
}