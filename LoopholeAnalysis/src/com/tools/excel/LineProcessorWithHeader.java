/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tools.excel;

import java.util.ArrayList;
import java.util.HashMap;


public abstract class LineProcessorWithHeader implements LineProcessor {
    String[] needHeader = null;

    public LineProcessorWithHeader(String... t) {
        needHeader = t;
    }

    HashMap<String, Integer> colMap = new HashMap<>();

    @Override
    public void proccessLine(int sheetIdx, int rowno, ArrayList<String> line) {
        if (sheetIdx == 1 && rowno == 1) {
            colMap.clear();
            for (int i = 0; i < line.size(); i++) {
                colMap.put(line.get(i), i);
            }
        }
        if (sheetIdx == 1 && rowno > 1) {
            ArrayList<String> need = new ArrayList<>();
            for (String s : needHeader) {
                need.add(line.get(colMap.get(s)));
            }
            processLine(need);
        }
    }

    public abstract void processLine(ArrayList<String> line);

}
