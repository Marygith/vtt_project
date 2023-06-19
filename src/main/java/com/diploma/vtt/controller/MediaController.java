package com.diploma.vtt.controller;

import com.diploma.vtt.service.ProcessKeysService;
import com.diploma.vtt.service.ProcessTextService;
import com.diploma.vtt.service.ProcessVideoService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Log4j2
@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MediaController {

    private final ProcessVideoService processVideoService;
    private final ProcessTextService processTextService;

    private final ProcessKeysService processKeysService;
    private SseEmitter sseEmitter = new SseEmitter();

    public MediaController(ProcessVideoService processVideoService, ProcessTextService processTextService, ProcessKeysService processKeysService1) {
        this.processVideoService = processVideoService;
        this.processTextService = processTextService;
        this.processKeysService = processKeysService1;
    }

    @GetMapping("/events")
    public SseEmitter getEvents() {
        sseEmitter = new SseEmitter();
        try {
            sseEmitter.send(SseEmitter.event().name("message").data(processTextService.getText()));
            sseEmitter.send(SseEmitter.event().name("duration").data(processVideoService.getDuration()));
            if (processVideoService.getDuration() != 0.0) {
                processVideoService.setDuration(-1.0);
            }
            processTextService.updateText();
            if (processVideoService.isDone()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
                sseEmitter.send(SseEmitter.event().name("message").data(processTextService.getText()));
                processTextService.setText(new String[]{"done"});
                sseEmitter.send(SseEmitter.event().name("message").data(processTextService.getText()));
                processTextService.setText(new String[]{""});
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        sseEmitter.complete();
        return sseEmitter;
    }

    @GetMapping("/new_events")
    public SseEmitter getNewEvents() {
        sseEmitter = new SseEmitter();
        try {
            sseEmitter.send(SseEmitter.event().name("division").data(processTextService.getDividedText()));
            sseEmitter.send(SseEmitter.event().name("keys").data(processKeysService.getKeys()));
            processTextService.updateDividedText();
            if (processTextService.isDivided()) {
                sseEmitter.send(SseEmitter.event().name("division").data(processTextService.getDividedText()));
                processTextService.setDividedText(new String[]{"done"});
                sseEmitter.send(SseEmitter.event().name("division").data(processTextService.getDividedText()));

            }
            if (processKeysService.getKeys().length != 0) {
                sseEmitter.send(SseEmitter.event().name("keys").data(processKeysService.getKeys()));
                sseEmitter.send(SseEmitter.event().name("keys").data(new String[]{"done"}));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sseEmitter.complete();
        return sseEmitter;
    }

    @PostMapping("/upload")
    public ResponseEntity.BodyBuilder uploadImage(@RequestParam("videoFile") MultipartFile file)
            throws IOException {
        try {
            Files.delete(Path.of("video.mp4"));
        } catch (IOException e) {
            log.warn("didn't manage to delete media files");
        }
        log.info("Original Image Byte Size - " + file.getBytes().length);
        FileOutputStream out = new FileOutputStream("video.mp4");
        out.write(file.getBytes());
        out.close();


        log.info("Path to file is video.mp4");
        processVideoService.passVideoToPython("video.mp4");
        return ResponseEntity.status(HttpStatus.OK);
    }

}
