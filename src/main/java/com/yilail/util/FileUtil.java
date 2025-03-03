package com.yilail.util;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
   public static BigInteger readAndHandleFile(String filePath,int chunkSize) throws IOException {
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      char[] buffer = new char[BUFFER_SIZE];
      List<BigInteger> fingerprints = new ArrayList<>();
      int bytesRead;
      while ((bytesRead = reader.read(buffer, 0, BUFFER_SIZE)) != -1) {
         String chunk = new String(buffer, 0, bytesRead);
         // 处理当前分块
         SimHashUtil.simHash(chunk,chunkSize);
      }
      return SimHashUtil.mergeSimHashes(fingerprints);
   }

   public static double compareArticle(String originArticle, String targetArticle,int chunkSize) throws IOException {
      BigInteger simhash1=readAndHandleFile(originArticle,chunkSize);
      BigInteger simhash2=readAndHandleFile(targetArticle,chunkSize);
      int hammingDistance = SimHashUtil.hammingDistance(simhash1, simhash2);
      return 1 - (double) hammingDistance / HASH_BITS;
   }

   /**
    * 将词语列表分割成若干个chunk
    * @param words 词语列表
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
   public static List<List<String>> jiebaWordSegmentation(String article,int chunkSize) {
      JiebaSegmenter segmenter = new JiebaSegmenter();
      List<String> words = segmenter.sentenceProcess(article);
      return chunkWords(words,chunkSize);
   }

}
