package com.diploma.vtt.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Log4j2
@Service
public class ProcessTextService {

    private final ProcessKeysService processKeysService;

    ProcessTextService(ProcessKeysService processKeysService) {
        this.processKeysService = processKeysService;
    }

    public ProcessKeysService getProcessKeysService() {
        return processKeysService;
    }

    private String[] text = new String[]{""};

    private boolean isDivided = false;

    private String[] dividedText = new String[]{"text is being divided..."};

    public boolean isDivided() {
        return isDivided;
    }

    public void updateText() {
        String[] text = null;
        try {
            text = Files.readAllLines(Path.of("new_scripts.txt"), Charset.forName("cp1251")).toArray(new String[0]);

            if (text.length > 0) {
                setText(text);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            System.out.println("sending empty text");
        }
    }

    public String[] getText() {


        return text;
    }

    public void setText(String[] text) {
        this.text = text;
    }

    public String divideTextOnParagraphs() {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Path.of("new_scripts.txt"), Charset.forName("cp1251"));
        } catch (IOException e) {
            log.error("error in reading lines from new_script.txt");
        }
        try {
            Files.delete(Path.of("divided_text.txt"));
        } catch (IOException e) {
            log.warn("didn't manage to delete divided_text.txt");
        }
        StringBuilder b = new StringBuilder("");
        int bracketInd;
        if (lines != null) {
            for (String row : lines) {
                bracketInd = row.indexOf("]");
                if (bracketInd != -1) {
                    b.append(row.substring(bracketInd + 2));
                } else {
                    b.append(row);
                }
            }
        }
        try {
            Files.writeString(new File("script_without_timings.txt").toPath(), b.toString());
        } catch (IOException e) {
            log.error("didn't manage to write to script_without_timings.txt");
        }
        ProcessBuilder pb = new ProcessBuilder().command("python", "scripts\\split_script.py", "sscript_without_timings.txt").redirectOutput(new File("divided_text.txt"));


        Process p = null;
        String result = null;
        try {
            p = pb.start();
            System.out.println("division process started");
            p.getErrorStream().transferTo(System.out);
            p.waitFor();
            updateDividedText();
            isDivided = true;
        } catch (IOException | InterruptedException e) {
            log.error("division process didn't end correctly");
        }

        setDividedText(new String[]{result});

        return result;
    }


    public String[] getDividedText() {
        return dividedText;
    }

    public void setDividedText(String[] dividedText) {
        this.dividedText = dividedText;
    }

    public void updateDividedText() {
        String text[] = null;
        try {
            text = Files.readAllLines(Path.of("divided_text.txt"), Charset.forName("cp1251")).toArray(new String[0]);
            if (text.length > 0) {
                setDividedText(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("sending empty text");
        }
    }
}
