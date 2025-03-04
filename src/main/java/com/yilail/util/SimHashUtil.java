package com.yilail.util;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yilail.constant.GlobalConstant.HASH_BITS;

/**
 * @author: YiLaiL
 * @date: 2025/03/03
 * @description: simhash算法实现
 */
public class SimHashUtil {

    /**
     * 计算simhash值生成指纹
     * @param text 文本内容
     * @param chunkSize 词语分块大小
     * @return 指纹
     */
    public static BigInteger simHash(String text, int chunkSize) {
        List<List<String>> wordLists = FileUtil.hanlpWordSegmentation(text, chunkSize);
        int[] hashBits = new int[HASH_BITS];
        wordLists.forEach(words -> {
            // 计算词频权重
            Map<String, Integer> weights = new HashMap<>();
            for (String word : words) {
                // 计算词频
                weights.put(word, weights.getOrDefault(word, 0) + 1);
            }
            // 生成哈希并加权
            for (String word : weights.keySet()) {
                BigInteger hash = hashWord(word);
                for (int i = 0; i < HASH_BITS; i++) {
                    if (hash.testBit(i)) {
                        hashBits[i] += weights.get(word);
                    } else {
                        hashBits[i] -= weights.get(word);
                    }
                }
            }
        });

        // 生成最终指纹
        BigInteger fingerprint = BigInteger.ZERO;
        for (int i = 0; i < HASH_BITS; i++) {
            if (hashBits[i] > 0) {
                // 将大于0的位设置为1
                fingerprint = fingerprint.setBit(i);
            }
        }
        return fingerprint;
    }

   /**
    * 对词语进行哈希
    * @param word 词语
    * @return 哈希值
    */
    private static BigInteger hashWord(String word) {
        try {
            byte[] bytes = java.security.MessageDigest.getInstance("MD5")
                    .digest(word.getBytes());
            //将字节数组转换为BigInteger
            return new BigInteger(1, bytes);
        } catch (Exception e) {
            return BigInteger.ZERO;
        }
    }

    /**
     * 计算海明距离
     * @param hash1 文章1的simhash值
     * @param hash2 文章2的simhash值
     * @return 不同的位数，海明距离
     */
    public static int hammingDistance(BigInteger hash1, BigInteger hash2) {
    // 按位异或获取不同的位数
    return hash1.xor(hash2).bitCount();
    }

    /**
     * 合并多个simhash值
     * @param simHashes simhash值列表
     * @return 合并后的simhash值
     */
    public static BigInteger mergeSimHashes(List<BigInteger> simHashes) {
        // 检查输入是否为空
        if (simHashes == null || simHashes.isEmpty()) {
            return BigInteger.ZERO;
        }
        // 初始化一个长度为HASH_BITS的数组
        int[] bitSum = new int[HASH_BITS];
        // 遍历每个simhash值，统计每个位上的1和-1的数量
        for (BigInteger simHash : simHashes) {
            for (int i = 0; i < HASH_BITS; i++) {
                if (simHash.testBit(i)) {
                    bitSum[i]++;
                } else {
                    bitSum[i]--;
                }
            }
        }
        // 生成最终的simhash值
        BigInteger fingerprint = BigInteger.ZERO;
        for (int i = 0; i < HASH_BITS; i++) {
            if (bitSum[i] > 0) {
                fingerprint = fingerprint.setBit(i);
            }
        }
        return fingerprint;
    }
}
