package com.yilail;


import java.io.*;

import static com.yilail.constant.GlobalConstant.*;

public class Main {
    public static void main(String[] args) throws IOException {
        //String originalDocumentPath = args[0];
        //String targetDocumentPath = args[1];
        String originalDocumentPath = "C:\\Users\\86159\\Desktop\\新建 文本文档 (3).txt";
        //"C:\Users\86159\Desktop\test1_Target.docx"
        //test1_Origin.docx
        //新建 文本文档.txt
        //新建 文本文档 (2).txt
        String targetDocumentPath = "C:\\Users\\86159\\Desktop\\新建 文本文档 (2).txt";
        Article article1=new Article(originalDocumentPath);
        Article article2=new Article(targetDocumentPath);
//        for (int i = 0; i < 10000; i++) {
//            Article.compareArticle(article1,article2,ACCURATE_CHUNK_SIZE);
//        }
        System.out.println("相似度："+Article.compareArticle(article1,article2,ACCURATE_CHUNK_SIZE)*100+"%");
    }
}