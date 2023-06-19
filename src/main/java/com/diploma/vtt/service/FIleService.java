package com.diploma.vtt.service;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Log4j2
public class FIleService {

    public static void deleteIfPresent(String path){
        try {
            Files.delete(Path.of(path));
        } catch (IOException e) {
            log.warn("Didn't manage to delete a file with path: "
            + path);
        }
    }
}
