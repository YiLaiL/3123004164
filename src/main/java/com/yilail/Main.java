package com.yilail;

import com.yilail.util.FileUtil;

import java.io.*;

import static com.yilail.constant.GlobalConstant.*;

public class Main {
    public static void main(String[] args) throws IOException {
        //String originalDocumentPath = args[0];
        //String targetDocumentPath = args[1];
        String originalDocumentPath = "C:\\Users\\86159\\Desktop\\新建 文本文档 (5).txt";
        //"C:\Users\86159\Desktop\test1_Target.docx"
        //test1_Origin.docx
        //新建 文本文档.txt
        //新建 文本文档 (2).txt
        String targetDocumentPath = "C:\\Users\\86159\\Desktop\\新建 文本文档 (6).txt";
        System.out.println("相似度："+FileUtil.compareArticle(originalDocumentPath,targetDocumentPath,ACCURATE_CHUNK_SIZE)*100+"%");
    }
}