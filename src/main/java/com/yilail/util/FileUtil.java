package com.yilail.util;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.yilail.GlobalData;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.txt.CharsetDetector;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.yilail.constant.GlobalConstant.*;

/**
 * @author: YiLaiL
 * @date: 2025/03/03
 * @description: 文件工具类
 */
public class FileUtil {
    /**
     * 分段读取文件并处理
     * @param filePath 文件路径
     */
    public static BigInteger readAndHandleFile(String filePath, int chunkSize) throws IOException {
        char[] buffer = new char[BUFFER_SIZE];
        List<BigInteger> fingerprints = new ArrayList<>();
        StringBuilder carryOver = new StringBuilder();
        // 检测文件编码
        Charset charset = detectCharset(filePath);
        if (charset == null) {
            // 默认使用 UTF-8 编码
            charset = StandardCharsets.UTF_8;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), charset))) {
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                String chunk = carryOver.toString() + new String(buffer, 0, charsRead);
                // 清空 carryOver
                carryOver.setLength(0);
                int lastValidIndex = findLastValidUTF8Boundary(chunk);
                if (lastValidIndex < chunk.length()) {
                    carryOver.append(chunk.substring(lastValidIndex));
                    chunk = chunk.substring(0, lastValidIndex);
                }
                if (!chunk.isEmpty()) {
                    // 使用 charset 将 chunk 转换为 UTF-8
                    String utf8Chunk = new String(chunk.getBytes(charset), StandardCharsets.UTF_8);
                    fingerprints.add(SimHashUtil.simHash(utf8Chunk, chunkSize));
                }
            }
            if (!carryOver.isEmpty()) {
                // 使用 charset 将剩余的 carryOver 转换为 UTF-8
                String utf8CarryOver = new String(carryOver.toString().getBytes(charset), StandardCharsets.UTF_8);
                fingerprints.add(SimHashUtil.simHash(utf8CarryOver, chunkSize));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + e.getMessage(), e);
        }
        return SimHashUtil.mergeSimHashes(fingerprints);
    }

    /**
     * 检测文件编码
     * @param filePath 文件路径
     * @return 编码
     */
    private static Charset detectCharset(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(bis);
            org.apache.tika.parser.txt.CharsetMatch match = detector.detect();
            if (match != null) {
                return Charset.forName(match.getName());
            }
        } catch (Exception e) {
            System.err.println("Error detecting charset: " + e.getMessage());
        }
        return null;
    }

    /**
     * 找到最后一个有效的 UTF-8 边界
     * @param s 字符串
     * @return 边界索引
     */
    private static int findLastValidUTF8Boundary(String s) {
        int len = s.length();
        for (int i = len - 1; i >= 0; i--) {
            char c = s.charAt(i);
            // 代理对（Surrogate Pair）检查
            if ((c & 0xF800) == 0xD800) {
                return i - 1;
            }
            // ASCII范围
            if ((c & 0xFF00) == 0) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * 比较两篇文章
     * @param originArticle 原始文章
     * @param targetArticle 目标文章
     * @param chunkSize 分块大小
     */
    public static double compareArticle(String originArticle, String targetArticle, int chunkSize) throws IOException {
        BigInteger simhash1 = readAndHandleFile(originArticle, chunkSize);
        BigInteger simhash2 = readAndHandleFile(targetArticle, chunkSize);
        int hammingDistance = SimHashUtil.hammingDistance(simhash1, simhash2);
        return 1 - (double) hammingDistance / HASH_BITS;
    }

    /**
     * 将词语列表分割成若干个chunk
     * @param words     词语列表
     * @param chunkSize 分块大小
     * @return 分块列表
     */
    public static List<List<String>> chunkWords(List<String> words, int chunkSize) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < words.size(); i += chunkSize) {
            int end = Math.min(words.size(), i + chunkSize);
            chunks.add(words.subList(i, end));
        }
        return chunks;
    }

    /**
     * 对文章进行分词
     * @param article 文章
     */
    public static List<List<String>> jiebaWordSegmentation(String article, int chunkSize) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        // 分词
        List<String> segmentedWords = segmenter.sentenceProcess(article);
        // 使用 removeIf 和 lambda 表达式去除停用词
        segmentedWords.removeIf(GlobalData.getStopWords()::contains);
        return chunkWords(segmentedWords, chunkSize);
    }
}
