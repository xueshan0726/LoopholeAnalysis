/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tools;

import java.io.*;
import java.nio.charset.Charset;
import java.util.function.Consumer;


public class StreamUtil {
    public static final int BUF_SIZE = 1024*1024;

    public static void trans(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        BufferedOutputStream bos = new BufferedOutputStream(out);
        byte[] buf = new byte[BUF_SIZE];
        int len = 0;
        while ((len = bis.read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        in.close();
        bos.close();
    }

    public static void append(InputStream input, OutputStream output, long numOfBytes) throws IOException {
        BufferedInputStream in = new BufferedInputStream(input);
        BufferedOutputStream out = new BufferedOutputStream(output);
        byte[] buf = new byte[BUF_SIZE];
        int len = 0;
        long curLen = 0;
        long remain = numOfBytes - curLen;
        int quota = (int) ((remain / BUF_SIZE) == 0 ? remain : BUF_SIZE);
        while ((len = in.read(buf, 0, quota)) != -1) {
            out.write(buf, 0, len);
            curLen = curLen + len;
            if (curLen == numOfBytes) {
                break;
            }
            remain = numOfBytes - curLen;
            quota = (int) ((remain / BUF_SIZE) == 0 ? remain : BUF_SIZE);
        }
    }

    public static void append(InputStream input, OutputStream output) throws IOException {
        BufferedInputStream in = new BufferedInputStream(input);
        BufferedOutputStream out = new BufferedOutputStream(output);
        byte[] buf = new byte[BUF_SIZE];
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

    public static InputStream string2InputStream(String s) {
        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
        return bais;
    }

    public static String inputStream2String(InputStream input) throws IOException {
        BufferedInputStream s = new BufferedInputStream(input);
        byte[] buffer = new byte[BUF_SIZE];
        StringBuilder sb = new StringBuilder();
        int n = 0;
        while ((n = s.read(buffer, 0, buffer.length)) != -1) {
            sb.append(new String(buffer, 0, n));
        }
        s.close();
        return sb.toString();

    }

    public static void fileInputStream(String file, Consumer<BufferedInputStream> process) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        process.accept(bis);
        bis.close();
    }

    public static void fileOutputStream(String file,Consumer<BufferedOutputStream> process) throws IOException{
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        process.accept(bos);
        bos.close();
    }

    public static void fileWriter(String file,Consumer<BufferedWriter> process) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        process.accept(bw);
        bw.close();
    }
    public static void fileReader(String file,Consumer<BufferedReader> process) throws IOException {
        BufferedReader bw = new BufferedReader(new FileReader(file));
        process.accept(bw);
        bw.close();
    }
    public static void fileWriter(String file, Charset charset, Consumer<BufferedWriter> process) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),charset));
        try {
            process.accept(bw);
        }finally {
            bw.close();
        }
    }
    public static void fileReader(String file,Charset charset,Consumer<BufferedReader> process) throws IOException {
        BufferedReader bw = new BufferedReader(new InputStreamReader(new FileInputStream(file),charset));
        try {
            process.accept(bw);
        }finally {
            bw.close();

        }
    }
}
