/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tools;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author 
 */
public class FileUtil {
    public static final String[] SUFFIX_EXCEL = new String[]{"xls", "xlsx"};
    public static final String[] SUFFIX_CSV = new String[]{"csv"};
    public static final String[] SUFFIX_PIC = {"jpg", "png", "bmp", "jpeg"};
    public static final String[] SUFFIX_TEXT = new String[]{"xls", "xlsx", "csv"};

    public static ArrayList<String> getFilePaths(String path, FileFilter ff, boolean traverse) {
        ArrayList<String> res = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return res;
        } else if (!file.isDirectory()) {
            res.add(file.getAbsolutePath());
            return res;
        } else {
            for (File f : file.listFiles(ff)) {
                if (!f.isDirectory() || traverse) {
                    res.addAll(getFilePaths(f.getAbsolutePath(), ff, traverse));
                }
            }
            return res;
        }
    }

    public static String removeSuffix(String fileName) {
        String name = new File(fileName).getName();
        String path = new File(fileName).getParent();
        if (name.contains(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }
        return path+"/"+name;
    }

    public static String addStamp(String fileName,String stamp){
        return removeSuffix(fileName)+stamp+getSuffix(fileName);
    }

    public static ArrayList<String> getFilePaths(String path, boolean traverse) {
        FileFilter ff = null;
        return getFilePaths(path, ff, traverse);
    }

    public static int countLine(String file) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        int count = 0;
        while (br.readLine() != null) {
            count++;
        }
        return count;
    }


    public static HashSet<String> getSuffix(Collection<String> path) {
        HashSet<String> suffixes = new HashSet<>();
        for (String p : path) {
            suffixes.add(getSuffix(p));
        }
        return suffixes;
    }

    public static String getSuffix(String path) {
        String name = new File(path).getName();
        if (name.contains(".")) {
            return name.substring(name.lastIndexOf("."), name.length());
        } else {
            return "";
        }


    }

    public static ArrayList<String> getFilePaths(String path, String[] suffix, boolean traverse) {
        FileFilter ff = getFilter(suffix);
        return getFilePaths(path, ff, traverse);
    }

    public static ArrayList<String> getDirectories(String path, boolean traverse) {
        ArrayList<String> res = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return res;
        } else if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    res.add(f.getAbsolutePath());
                    if (traverse) {
                        res.addAll(getDirectories(f.getAbsolutePath(), traverse));
                    }
                }
            }

        }
        return res;
    }

    public static boolean rename(String from, String to) {
        File fr = new File(from);
        File t = new File(to);
//        if(t.exists()){
//            t.delete();
//        }
        return fr.renameTo(t);
    }

    public static FileFilter getFilter(final String[] suffix) {
        return new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith(".")) {//hidden file is rejected
                    return false;
                }
                if (pathname.isDirectory()) {//directory is accepted
                    return true;
                }
                for (int i = 0; i < suffix.length; i++) {//files has specific suffixes are accepted
                    if (pathname.getName().endsWith(suffix[i])) {
                        return true;
                    }
                }

                return false;//reject others
            }
        };
    }

    public static void delete(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            if (file.listFiles() == null) {
                return;
            }
            for (File f : file.listFiles()) {
                delete(f.getAbsolutePath());
            }
            file.delete();
        } else {
            file.delete();
        }

    }

    public static void move(String from, String to) throws FileNotFoundException, IOException {
        if (new File(from).getAbsoluteFile().equals(new File(to).getAbsoluteFile())) {
            return;
        }
        copy(from, to);
        delete(from);
    }

    public static void copy(String from, String to) throws FileNotFoundException, IOException {
        if (new File(from).getAbsoluteFile().equals(new File(to).getAbsoluteFile())) {
            return;
        }
        BufferedInputStream fr = new BufferedInputStream(new FileInputStream(from));
        BufferedOutputStream fw = new BufferedOutputStream(new FileOutputStream(to));
        byte[] buf = new byte[4096];
        int len = -1;
        while ((len = fr.read(buf)) != -1) {
            fw.write(buf, 0, len);
        }
        fw.close();
        fr.close();
    }

    public static void changeEncode(String fromFile, String toFile, String fromEncode, String toEncode) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fromFile), fromEncode));
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(toFile), toEncode);

        String line = null;
        while ((line = br.readLine()) != null) {
            fw.append(line).append("\n");
        }
        fw.close();

    }

    public static void moveAll(String fromDir, String toDir) throws IOException {
        File from = new File(fromDir);
        File to = new File(toDir);
        if (from.isDirectory() && to.isDirectory()) {
            for (File f : from.listFiles()) {
                if (f.isDirectory()) {
                    createPath(to.getAbsolutePath() + "/" + f.getName());
                    moveAll(f.getAbsolutePath(), to.getAbsolutePath() + "/" + f.getName());
                } else {
                    move(f.getAbsolutePath(), to.getAbsolutePath() + "/" + f.getName());
                }
            }
        }
    }

    public static void merge(File f, File f1, File out) throws IOException {
        FileWriter fw = new FileWriter(out);
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = null;
        while ((line = br.readLine()) != null) {
            fw.append(line).append("\n");
        }
        br = new BufferedReader(new FileReader(f1));
        line = null;
        while ((line = br.readLine()) != null) {
            fw.append(line).append("\n");
        }
        fw.close();
    }

    public static void splitWithHead(File f, Charset charset, int length) throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
        String line = null;
        String head = "";
        String o = f.getAbsolutePath();
        String outfile = o.substring(0, o.lastIndexOf("."));
        String suffix = o.substring(o.lastIndexOf("."));
        int seqNo = 0;
        int lineNo = 0;
        FileWriter out = null;
        seqNo++;
        while ((line = in.readLine()) != null) {
            if (lineNo % length != 0) {
                out.append(line).append("\n");
                lineNo++;
            } else {
                if (out != null) {
                    out.close();
                } else {
                    head = line;
                }
                System.out.println("creating file:" + outfile + "_" + seqNo + suffix);
                out = new FileWriter(outfile + "_" + seqNo + suffix);
                out.append(head).append("\n");
                if (!head.equals(line)) {
                    out.append(line).append("\n");
                    lineNo++;
                    seqNo++;
                }

            }

        }
        if (lineNo % length != 0) {
            out.close();
        }
    }


    public static void insertHead(File f, String content) throws IOException {
        FileWriter out = new FileWriter(f.getParent() + "/" + f.getName() + "_modified");
        BufferedReader in = new BufferedReader(new FileReader(f));
        String line = null;
        out.append(content).append("\n");
        while ((line = in.readLine()) != null) {
            out.append(line).append("\n");
        }
        in.close();
        out.close();
    }


    public static void createPath(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }

    }

}
