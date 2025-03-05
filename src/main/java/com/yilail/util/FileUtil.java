package com.yilail.util;

import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.dictionary.common.CommonSynonymDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.yilail.Article;
import com.yilail.GlobalData;
import org.apache.tika.parser.txt.CharsetDetector;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.yilail.constant.GlobalConstant.*;

/**
 * @author: YiLaiL
 * @date: 2025/03/03
 * @description: 文件工具类
 */
public class FileUtil {
    /**
     * 分段读取文件并处理
     *
     * @param article   文章对象
     * @param chunkSize 分块大小
     * @return 指纹
     */
    public static List<BigInteger> readAndHandleFile(Article article, int chunkSize) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        List<BigInteger> fingerprints = new ArrayList<>();
        ByteArrayOutputStream carryOver = new ByteArrayOutputStream();
        // 检测文件编码
        FileInputStream in = article.getFis();
        BufferedInputStream bis = new BufferedInputStream(in);
        try {
            article.setCharset(detectCharset(bis));
            if (article.getCharset() != null && !CHARSET.equals(article.getCharset().toString())) {
                System.out.println("文件编码为：" + article.getCharset() + "要求文件为utf-8编码");
                return null;
            }
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                // 合并遗留字节与新读取的字节
                byte[] combined = combineBytes(carryOver.toByteArray(), buffer, bytesRead);
                carryOver.reset();
                // 寻找最后一个完整的UTF-8字符边界
                int splitPos = findLastValidUtf8Boundary(combined);
                if (splitPos < combined.length) {
                    carryOver.write(combined, splitPos, combined.length - splitPos);
                }
                // 处理有效字节段
                if (splitPos > 0) {
                    String chunk = new String(combined, 0, splitPos, StandardCharsets.UTF_8);
                    fingerprints.add(SimHashUtil.simHash(hanlpWordSegmentation(chunk, chunkSize)));
                }
            }
            // 处理剩余字节
            if (carryOver.size() > 0) {
                throw new IOException("文件包含不完整的UTF-8字符序列");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            article.getFis().close();
            bis.close();
        }
        return fingerprints;
    }

    /**
     * 合并字节数组
     *
     * @param carryOver 遗留字节
     * @param buffer    新读取的字节
     * @param bytesRead 新读取的字节数
     * @return 合并后的字节数组
     */
    private static byte[] combineBytes(byte[] carryOver, byte[] buffer, int bytesRead) {
        byte[] combined = new byte[carryOver.length + bytesRead];
        System.arraycopy(carryOver, 0, combined, 0, carryOver.length);
        System.arraycopy(buffer, 0, combined, carryOver.length, bytesRead);
        return combined;
    }

    /**
     * 查找最后一个完整的UTF-8字符边界
     * @param bytes 字节数组
     * @return 最后一个完整的UTF-8字符边界的位置
     */
    private static int findLastValidUtf8Boundary(byte[] bytes) {
        int pos = bytes.length - 1;
        while (pos >= 0) {
            // 判断当前字节是否为UTF-8字符的起始字节
            if ((bytes[pos] & 0xC0) != 0x80) {
                int expectedLength;
                byte b = bytes[pos];
                if ((b & 0x80) == 0x00) {
                    expectedLength = 1;
                } else if ((b & 0xE0) == 0xC0) {
                    expectedLength = 2;
                } else if ((b & 0xF0) == 0xE0) {
                    expectedLength = 3;
                } else if ((b & 0xF8) == 0xF0) {
                    expectedLength = 4;
                } else {
                    return pos;
                }
                // 检查后续字节是否足够
                if (pos + expectedLength <= bytes.length) {
                    return pos + expectedLength;
                }
            }
            pos--;
        }
        return 0;
    }

    /**
     * 检测文件编码
     *
     * @param bis 输入流
     * @return 编码
     */
    private static Charset detectCharset(BufferedInputStream bis) {
        CharsetDetector detector = new CharsetDetector();
        try {
            detector.setText(bis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        org.apache.tika.parser.txt.CharsetMatch match = detector.detect();
        if (match != null) {
            return Charset.forName(match.getName());
        }
        return null;
    }


    /**
     * 将词语列表分割成若干个chunk
     *
     * @param words     词语列表
     * @param chunkSize 分块大小
     * @return 分块列表
     */
    private static List<List<String>> chunkWords(List<String> words, int chunkSize) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < words.size(); i += chunkSize) {
            int end = Math.min(words.size(), i + chunkSize);
            chunks.add(words.subList(i, end));
        }
        return chunks;
    }

    /**
     * 对文章进行分词
     *
     * @param article 文章
     */
    private static List<List<String>> hanlpWordSegmentation(String article, int chunkSize) {
        article = article.replaceAll("\r\n", "");
        // 分词
        List<Term> segmentedWords = StandardTokenizer.segment(article);
        // 去除停用词
        segmentedWords.removeIf(term -> GlobalData.getStopWords().contains(term.word));
        // 替换同义词
        List<String> replacedText = new ArrayList<>();
        for (Term segmentedWord : segmentedWords) {
            String word = segmentedWord.word;
            CommonSynonymDictionary.SynonymItem synonymItem = CoreSynonymDictionary.get(word);
            replacedText.add(synonymItem != null && !word.equals(synonymItem.synonymList.get(0).realWord) ?
                    synonymItem.synonymList.get(0).realWord :
                    word);
        }
        return chunkWords(replacedText, chunkSize);
    }
}
