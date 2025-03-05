package com.yilail;

import com.yilail.util.FileUtil;
import com.yilail.util.SimHashUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;

import static com.yilail.constant.GlobalConstant.HASH_BITS;

/**
 * @author: YiLaiL
 * @date: 2025/03/05
 * @description: 文章信息
 */
public class Article {
   private Charset charset;
   private final String filePath;
   private FileInputStream fis;

    public Article(String filePath) {
        this.filePath = filePath;
        try {
            this.fis = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }


    public FileInputStream getFis() {
        try {
            if(!fis.getFD().valid()){
                fis = new FileInputStream(filePath);
            }
            return fis;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 比较两篇文章
     * @param originArticle 原始文章
     * @param targetArticle 目标文章
     * @param chunkSize     分块大小
     */
    public static double compareArticle(Article originArticle, Article targetArticle, int chunkSize) throws IOException {
        List<BigInteger> fingerprints1 = FileUtil.readAndHandleFile(originArticle, chunkSize);
        List<BigInteger> fingerprints2= FileUtil.readAndHandleFile(targetArticle, chunkSize);
        BigInteger simhash1 = SimHashUtil.mergeSimHashes(fingerprints1);
        BigInteger simhash2 = SimHashUtil.mergeSimHashes(fingerprints2);
        if (simhash1 == null || simhash2 == null) {
            return 0.0;
        }
        int hammingDistance = SimHashUtil.hammingDistance(simhash1, simhash2);
        return 1 - (double) hammingDistance / HASH_BITS;
    }

}
