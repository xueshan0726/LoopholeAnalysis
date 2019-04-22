/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tools.excel;

import com.tools.MyCounter;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CheckHeader {
    public static final int TOP_ROW_NUM = 10;

    public static int getHeaderRow(String fileName) throws Exception {
        if (fileName.endsWith("xls")) {
            Excel x = new Excel(fileName, Excel.READ);
            Sheet activeSheet = x.getActiveSheet();
            HashMap<Integer, Integer> rowIndex = new HashMap<>();
            MyCounter<Integer> counter = new MyCounter();
            ArrayList<Integer> lenList = new ArrayList<>();
            for (int i = 0; i < Math.min(activeSheet.getPhysicalNumberOfRows(), TOP_ROW_NUM); i++) {
                ArrayList<ExcelValue> valueLine = x.getValueLine(activeSheet.getSheetName(), 1, i + 1);
                if (!rowIndex.containsKey(valueLine.size())) {
                    rowIndex.put(valueLine.size(), i + 1);
                }
                counter.count(valueLine.size());
                lenList.add(valueLine.size());
            }
            int line = 1;
            for (int i = 0; i < lenList.size(); i++) {
                if (lenList.get(i) >= counter.top(1).get(0)) {
                    line = i + 1;
                    break;
                }
            }
            x.writeAndClose();
            return line;

        } else if (fileName.endsWith("xlsx")) {
            final ArrayList<Integer> lenList = new ArrayList<>();
            final HashMap<Integer, Integer> rowIndex = new HashMap<>();
            final MyCounter<Integer> counter = new MyCounter();
            ExcelS x = new ExcelS(fileName, new LineProcessor() {

                @Override
                public void proccessLine(int sheetIdx, int rowno, ArrayList<String> line) {
                    if (rowno < TOP_ROW_NUM) {
                        if (!rowIndex.containsKey(line.size())) {
                            rowIndex.put(line.size(), rowno);
                        }
                        counter.count(line.size());
                        lenList.add(line.size());
                    }
                }
            });
            List<Integer> top = counter.top(1);
            int line = 1;
            for (int i = 0; i < lenList.size(); i++) {
                if (lenList.get(i) >= counter.top(1).get(0)) {
                    line = i + 1;
                    break;
                }
            }
            return rowIndex.get(top.get(0));
        } else {
            return 1;
        }

    }
}
