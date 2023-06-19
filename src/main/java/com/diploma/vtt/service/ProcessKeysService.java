package com.diploma.vtt.service;

import com.github.demidko.aot.WordformMeaning;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.github.demidko.aot.WordformMeaning.lookupForMeanings;

@Log4j2
@Service
public class ProcessKeysService {

    private String[] keys = new String[]{};

    public String[] getKeys() {
        return keys;
    }

    private final Map<String, Double> lemmaToScore = new HashMap<>();
    private final Map<String, Double> lemmaToAmount = new HashMap<>();


    public void processKeys() {

        log.info("keys are processing");
        List<String> keyLines = new ArrayList<>();
        try {
            Thread.sleep(2000);
            keyLines = Files.readAllLines(Path.of("keys.txt"), Charset.forName("cp1251"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!keyLines.isEmpty()) {
            List<String> keyPairs = new ArrayList<>();
            for (String keyLine : keyLines) {
                keyLine = keyLine.replaceAll("\\[", "").replaceAll("]", "");
                keyPairs = Arrays.stream(keyLine.split("\\), \\(")).toList();
                for (String keyPair : keyPairs) {
                    keyPair = keyPair.replaceAll("\\(", "").replaceAll("\\)", "");
                    double score = Double.parseDouble(keyPair.substring(keyPair.indexOf(",") + 2));
                    String key = keyPair.substring(0, keyPair.indexOf(",")).replaceAll("\\'", "");
                    List<WordformMeaning> morph = lookupForMeanings(key);
                    key = morph.size() != 0 ? morph.get(0).getLemma().toString() : key;
                    lemmaToScore.put(key, Math.max(lemmaToScore.getOrDefault(key, 0.), score));
                    lemmaToAmount.put(key, lemmaToAmount.getOrDefault(key, 0.) + 0.01);
                }
            }
            List<String> keyList = new ArrayList<>();
            lemmaToScore.entrySet().stream()
                    .peek(entry -> entry.setValue(lemmaToAmount.get(entry.getKey()) + entry.getValue()))
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(15)
                    .forEach(e -> keyList.add(e.getKey()));
            keys = keyList.toArray(new String[0]);
        }
    }

    public void extractKeys() {
        ProcessBuilder pb = new ProcessBuilder()
                .command("python", "scripts\\extract_key_words_script.py", "divided_text.txt")
                .redirectOutput(new File("keys.txt"));

        Process p;
        try {
            p = pb.start();
            log.info("key extraction process started");
            p.getErrorStream().transferTo(System.out);
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        log.info("key extraction process ended");
    }
}
