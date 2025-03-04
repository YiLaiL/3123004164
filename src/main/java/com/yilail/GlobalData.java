package com.yilail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static com.yilail.constant.GlobalConstant.*;
import static com.yilail.constant.GlobalConstant.STOP_WORDS_FILE_4;

/**
 * @author: YiLaiL
 * @date: 2025/03/04
 * @description: 全局数据
 */
public class GlobalData {
    private static Set<String> stopWords;

    static {
        stopWords = new HashSet<>();
        List<String> stopWordFilePaths = new ArrayList<>();
        stopWordFilePaths.add(STOP_WORDS_FILE_1);
        stopWordFilePaths.add(STOP_WORDS_FILE_2);
        stopWordFilePaths.add(STOP_WORDS_FILE_3);
        stopWordFilePaths.add(STOP_WORDS_FILE_4);
        for (String filePath : stopWordFilePaths) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stopWords.add(line.trim());
                }
            } catch (IOException e) {
                System.err.println("Error loading stop words: " + e.getMessage());
            }
        }
        stopWords=Collections.unmodifiableSet(stopWords);
    }

   public static Set<String> getStopWords() {
        return stopWords;
    }
}
