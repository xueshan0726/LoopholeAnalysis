package com.tools;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FileTools {
    public static void parseline(String path,Consumer<String> line){
        BufferedReader reader=null;
        try {
            reader=new BufferedReader(new FileReader(new File(path)));
            reader.lines().forEach(l->line.accept(l));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeline(String path,Consumer<BufferedWriter> write){
        BufferedWriter writer=null;
        try {
            writer=new BufferedWriter(new FileWriter(new File(path),true));
            write.accept(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
