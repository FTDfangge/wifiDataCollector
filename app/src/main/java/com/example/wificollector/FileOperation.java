package com.example.wificollector;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by xiao on 2018/5/7.
 */

class FileOperation {

    private String filePath;
    private String fileName;

    FileOperation() {
    }

    FileOperation(String filePath, String fileName) throws Exception{
        this.filePath = filePath;
        this.fileName = fileName;
        makeFilePath(filePath, fileName);
    }

    void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    void write(String content) {
        writeTxtToFile(content, filePath, fileName);
    }

    void write(String content, String filePath, String fileName) {
        writeTxtToFile(content, filePath, fileName);
    }

    void writeCsv(String content) throws IOException {
        writeCsv(content, filePath + fileName);
    }

    void writeCsv(String content, String filePath) throws IOException {

            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true));
            // 添加头部名称
            bw.write(content);
            bw.newLine();
            bw.close();

    }

    // 将字符串写入到文本文件中
    private void writeTxtToFile(String strcontent, String filePath, String fileName) {
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            //生成文件夹之后，再生成文件，不然会出错
            File file = makeFilePath(filePath, fileName);
            if (!file.exists()) {
                Log.d(TAG, "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }
    }

    // 生成文件
    static File makeFilePath(String filePath, String fileName) throws Exception{
        File file = null;
        makeRootDirectory(filePath);

        file = new File(filePath + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }

    // 生成文件夹
    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            Log.i(TAG, e + "");
        }
    }
}
