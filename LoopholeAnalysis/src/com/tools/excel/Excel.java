package com.tools.excel;

import com.tools.MyCounter;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class Excel {

    private Workbook workbook;
    //    private String fileSuffix;
    public static final int READ = 0;
    public static final int WRITE = 1;
    public boolean opened = false;

    public Workbook getWorkbook() {
        return workbook;
    }

    private String filePath;

    public static void main(String[] arg) throws IOException, InvalidFormatException, InterruptedException {
        long start = System.currentTimeMillis();
    }

    public Excel(String filePath, int operation) throws IOException, InvalidFormatException {
        this.filePath = filePath;
        if (operation == READ) {
            FileInputStream fis = new FileInputStream(filePath);
            workbook = WorkbookFactory.create(fis);
            fis.close();
        }
        if (operation == WRITE) {
            if (filePath.endsWith("xls")) {
                workbook = new HSSFWorkbook();
            } else {
                workbook = new XSSFWorkbook();
            }

        }

        workbook.setForceFormulaRecalculation(true);
        opened = true;
    }

    public static void write(String filePath, Consumer<Excel> consumer){
        try {
            Excel x = new Excel(filePath,Excel.WRITE);
            consumer.accept(x);
            x.writeAndClose();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }

    }

    public static void read(String filePath,Consumer<Excel> consumer){
        try {
            Excel x = new Excel(filePath,Excel.READ);
            consumer.accept(x);
            x.writeAndClose();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<ExcelValue> getValueLine(){
        int activeSheetIndex = this.workbook.getActiveSheetIndex();
        String sheet = this.workbook.getSheetName(activeSheetIndex);
        ArrayList<ExcelValue> valueLine = getValueLine(sheet, 1, 1 + counter.getCounts(sheet));
        counter.count(sheet);
        return valueLine;
    }
    public ArrayList<ExcelValue> getValueLine(String sheet){
        ArrayList<ExcelValue> valueLine = getValueLine(sheet, 1, 1 + counter.getCounts(sheet));
        counter.count(sheet);
        return valueLine;
    }

    public void putValueBlock(String sheetName, int blockBeginX, int blockBeginY, ArrayList<ArrayList<ExcelValue>> block) {
        for (int rowId = blockBeginY; rowId < blockBeginY + block.size(); rowId++) {
            for (int colId = blockBeginX; colId < blockBeginX + block.get(rowId - blockBeginY).size(); colId++) {
                putValue(sheetName, colId, rowId, block.get(rowId - blockBeginY).get(colId - blockBeginX));
            }
        }
    }

    public void putValueBlock(String sheetName, int blockBeginX, int blockBeginY, int blockXEnd, int blockYEnd, ExcelValue v) {
        for (int rowId = blockBeginY; rowId <= blockYEnd; rowId++) {
            for (int colId = blockBeginX; colId <= blockXEnd; colId++) {
                putValue(sheetName, colId, rowId, v);
            }
        }
    }

    public void putValueLine(String sheetName, int blockBeginX, int blockBeginY, ArrayList<ExcelValue> values) {
        for (int colId = blockBeginX; colId < blockBeginX + values.size(); colId++) {
            putValue(sheetName, colId, blockBeginY, values.get(colId - blockBeginX));
        }
    }

    private MyCounter<String> counter = new MyCounter<>();
    public void putValueLine(String sheetName,  ArrayList<ExcelValue> values) {
        putValueLine(sheetName,1,1+counter.getCounts(sheetName),values);
        counter.count(sheetName);
    }

    public static ArrayList<ExcelValue> values(Object... ss) {
        ArrayList<ExcelValue> ls = new ArrayList<>();
        for (Object s : ss) {
            ls.add(value(s));
        }
        return ls;
    }

    public static ExcelValue value(Object s) {
        if (s instanceof Integer || s instanceof Long || s instanceof Double) {
            return new ExcelValue(s.toString(), Cell.CELL_TYPE_NUMERIC);
        } else {
            if (s instanceof HighLightString) {
                HighLightString hls = (HighLightString) s;
                return new ExcelValue(hls.getContent(), Cell.CELL_TYPE_STRING, hls.getFontStyle());
            } else {
                return new ExcelValue(s.toString(), Cell.CELL_TYPE_STRING);
            }
        }

    }

    public void putValueCol(String sheetName, int blockBeginX, int blockBeginY, List<ExcelValue> values) {
        for (int rowId = blockBeginY; rowId < blockBeginY + values.size(); rowId++) {
            putValue(sheetName, blockBeginX, rowId, values.get(rowId - blockBeginY));
        }
    }

    public void putValue(String sheetName, int x, int y, ExcelValue v) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }

        Row row = sheet.getRow(y - 1);
        if (row == null) {
            row = sheet.createRow(y - 1);
        }
        Cell cell = row.getCell(x - 1);
        if (cell == null) {
            cell = row.createCell(x - 1);
        }
        if (v != null) {
            int type = v.getType();
            String value = v.getValue();
            try {

                cell.setCellType(type);
                String typeName = null;

                switch (type) {
                    case Cell.CELL_TYPE_BOOLEAN:
                        cell.setCellValue(Boolean.parseBoolean(value));
                        typeName = "CELL_TYPE_BOOLEAN";
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        typeName = "CELL_TYPE_FORMULA";
                        cell.setCellFormula(value);
                        break;
                    case Cell.CELL_TYPE_STRING:
                        typeName = "CELL_TYPE_STRING";
                        if (v.isRichTextString()) {
                            if (workbook instanceof XSSFWorkbook) {
                                cell.setCellValue(applyFont(v, new XSSFRichTextString(value)));
                            } else {
                                cell.setCellValue(applyFont(v, new HSSFRichTextString(value)));
                            }
                        } else {
                            cell.setCellValue(value);
                        }
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        typeName = "CELL_TYPE_NUMERIC";
                        cell.setCellValue(Double.parseDouble(value));
                        break;
                    case Cell.CELL_TYPE_ERROR:
                        typeName = "CELL_TYPE_ERROR";
                    case Cell.CELL_TYPE_BLANK:
                        typeName = "CELL_TYPE_BLANK";
                        break;

                }
            } catch (NumberFormatException | IllegalStateException e) {
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(value);
            }
            if (v.hasHyperlink()) {
                CreationHelper creationHelper = workbook.getCreationHelper();
                Hyperlink link = creationHelper.createHyperlink(v.getLinkType());
                link.setAddress(v.getHyperlink());
                cell.setHyperlink(link);
                CellStyle hlink_style = workbook.createCellStyle();
                Font hlink_font = workbook.createFont();
                hlink_font.setUnderline(Font.U_SINGLE);
                hlink_font.setColor(IndexedColors.BLUE.getIndex());
                hlink_style.setFont(hlink_font);
                cell.setCellStyle(hlink_style);
            }

        }

    }

    private RichTextString applyFont(ExcelValue v, RichTextString text) {
        FontStyle fs = v.getFontStyle();
        for (String mask : fs.keySet()) {
            Font font = workbook.createFont();
            Font applyFont = fs.get(mask).applyFont(font);
            for (int i = 0; i < mask.length(); i++) {
                if (mask.charAt(i) != ' ') {
                    text.applyFont(i, i + 1, applyFont);
                }
            }
        }

        return text;
    }

    public ExcelValue getValue(String sheetName, int x, int y) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            return new ExcelValue("", Cell.CELL_TYPE_BLANK);
        }

        Row row = sheet.getRow(y - 1);
        if (row == null) {
            return new ExcelValue("", Cell.CELL_TYPE_BLANK);
        }
        Cell cell = row.getCell(x - 1);
        if (cell == null) {
            return new ExcelValue("", Cell.CELL_TYPE_BLANK);
        }
        ExcelValue v = new ExcelValue();
        int type = cell.getCellType();

        v.setType(type);
        try {
            switch (type) {
                case Cell.CELL_TYPE_BOOLEAN:
                    v.setValue(Boolean.toString(cell.getBooleanCellValue()));
                    break;
                case Cell.CELL_TYPE_STRING:
                    v.setValue(cell.getStringCellValue());
                    break;
                case Cell.CELL_TYPE_NUMERIC:

                    if (DateUtil.isCellDateFormatted(cell)) {
                        Date date = DateUtil.getJavaDate(cell.getNumericCellValue());
                        v.setValue(dateFormat(date));

                    } else {
                        v.setValue(new BigDecimal(cell.getNumericCellValue()).toPlainString());
                    }
                    break;
                case Cell.CELL_TYPE_FORMULA:

                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
//                    System.out.println(cell.getCellFormula()+"\t"+filePath+"\t"+x+"\t"+y);

                    try {
                        CellValue val = evaluator.evaluate(cell);

                        v.setType(val.getCellType());
                        switch (val.getCellType()) {
                            case Cell.CELL_TYPE_BOOLEAN:
                                v.setValue(Boolean.toString(val.getBooleanValue()));
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                v.setValue(new BigDecimal(cell.getNumericCellValue()).toPlainString());
                                break;
                            case Cell.CELL_TYPE_STRING:
                                v.setValue(val.getStringValue());
                                break;
                            case Cell.CELL_TYPE_BLANK:
                            case Cell.CELL_TYPE_ERROR:
                                v.setValue("");
                                break;
                        }
                    } catch (Exception e) {
                        v.setValue(cell.getCellFormula());
                        v.setType(Cell.CELL_TYPE_STRING);
                    }
                    break;
                case Cell.CELL_TYPE_ERROR:
                    v.setValue("");
                case Cell.CELL_TYPE_BLANK:
                    v.setValue("");
                    break;
            }
        } catch (NumberFormatException e) {
            v.setType(Cell.CELL_TYPE_ERROR);
            v.setValue("");
        }
        return v;

    }

    public static String dateFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(date);
    }

    public static ArrayList<String> toStringArray(ArrayList<ExcelValue> line) {
        ArrayList<String> l = new ArrayList<>();
        for (ExcelValue c : line) {
            l.add(c.getValue());
        }
        return l;
    }

    public static ArrayList<ArrayList<String>> toStringTable(ArrayList<ArrayList<ExcelValue>> line) {
        ArrayList<ArrayList<String>> l = new ArrayList<>();
        for (ArrayList<ExcelValue> c : line) {
            l.add(toStringArray(c));
        }
        return l;
    }

    public ArrayList<ExcelValue> getValueLine(String sheetName, int blockBeginX, int blockBeginY, int length) {
        ArrayList<ExcelValue> line = new ArrayList<ExcelValue>();
        for (int colId = blockBeginX; colId < blockBeginX + length; colId++) {
            line.add(getValue(sheetName, colId, blockBeginY));
        }
        return line;
    }

    public ArrayList<ExcelValue> getValueLine(String sheetName,
                                              int blockBeginX, int blockBeginY) {
        ArrayList<ExcelValue> line = new ArrayList<ExcelValue>();
        int colId = blockBeginX;
        Row row = getSheet(sheetName).getRow(blockBeginY - 1);
        if (row != null) {

            int colEnd = row.getPhysicalNumberOfCells();// getRow中的参数0代表第1行

            for (; colEnd >= blockBeginX; colEnd--) {
                if (!getValue(sheetName, colEnd, blockBeginY).getValue()
                        .equals("")) {
                    break;
                }
            }
            for (int i = colId; i <= colEnd; i++) {
                line.add(getValue(sheetName, i, blockBeginY));
            }
        }
        return line;
    }

    public ArrayList<ExcelValue> getValueCol(String sheetName, int blockBeginX, int blockBeginY, int length) {
        ArrayList<ExcelValue> col = new ArrayList<ExcelValue>();
        for (int rowId = blockBeginY; rowId < blockBeginY + length; rowId++) {
            col.add(getValue(sheetName, blockBeginX, rowId));
        }
        return col;
    }

    public ArrayList<ExcelValue> getValueCol(String sheetName, int blockBeginX, int blockBeginY) {
        ArrayList<ExcelValue> col = new ArrayList<ExcelValue>();
        int rowId = blockBeginY;
        int rowEnd = getSheet(sheetName).getPhysicalNumberOfRows();
        for (; rowEnd >= blockBeginY; rowEnd--) {
            if (!getValue(sheetName, blockBeginX, rowEnd).getValue().equals("")) {
                break;
            }
        }
        for (int i = rowId; i <= rowEnd; i++) {

            col.add(getValue(sheetName, blockBeginX, i));
        }
        return col;
    }

    public ArrayList<ArrayList<ExcelValue>> getValueBlock(String sheetName, int blockBeginX, int blockBeginY, int lengthX, int lengthY) {
        ArrayList<ArrayList<ExcelValue>> block = new ArrayList<ArrayList<ExcelValue>>();
        for (int rowId = blockBeginY; rowId < blockBeginY + lengthY; rowId++) {
            ArrayList<ExcelValue> line = new ArrayList<ExcelValue>();
            for (int colId = blockBeginX; colId < blockBeginX + lengthX; colId++) {
                line.add(getValue(sheetName, colId, rowId));
            }
            block.add(line);
        }
        return block;
    }

    /**
     * 遍历当前数据区域的每一行，寻找最大列数，作为数据块列数
     */
    public int getBlockCol(String sheetName, int blockBeginX, int blockBeginY) {
        int col = 0;
        for (int i = 0; i < getSheet(sheetName).getPhysicalNumberOfRows(); i++) {
            if (getSheet(sheetName).getRow(i) == null) {
                continue;
            }
            int c = getSheet(sheetName).getRow(i).getPhysicalNumberOfCells();
            if (c > col) {
                col = c;
            }
        }
//        System.out.println("col:"+col);
        return col;
    }

    /**
     * 遍历当前数据区域的每一列，寻找最大行数，要求最大行至少又一列不为空。
     */
    public int getBlockRow(String sheetName, int blockBeginX, int blockBeginY) {
        int initRow = blockBeginY - 1;
        int initCol = blockBeginX - 1;
        int col = getBlockCol(sheetName, blockBeginX, blockBeginY);
        int maxRow = 0;
        for (int i = initCol; i < initCol + col; i++) {
            int row = getSheet(sheetName).getPhysicalNumberOfRows();
            for (; row > blockBeginY; row--) {
                if (getSheet(sheetName).getRow(row - 1) != null && !getValue(sheetName, i + 1, row).getValue().trim().equals("")) {
                    break;
                }
            }
            if (row > maxRow) {
                maxRow = row;
            }
        }
        return maxRow;
    }

    public ArrayList<ArrayList<ExcelValue>> getValueBlock(String sheetName, int blockBeginX, int blockBeginY) {
        return getValueBlock(sheetName, blockBeginX, blockBeginY, getBlockCol(sheetName, blockBeginX, blockBeginY), getBlockRow(sheetName, blockBeginX, blockBeginY));
    }

    public static int alphabetToNumber(String aIndex) throws Exception {
        int index = 0;
        aIndex = aIndex.toLowerCase();
        Pattern pt = Pattern.compile("[a-z]*");
        HashMap<String, Integer> aToIndex = new HashMap<String, Integer>();
        char[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        for (int i = 0; i < alphabet.length; i++) {
            aToIndex.put(String.valueOf(alphabet[i]), i + 1);
        }
        if (pt.matcher(aIndex).matches()) {
            char[] ca = aIndex.toCharArray();
            for (int i = 0; i < ca.length; i++) {
                index = aToIndex.get(String.valueOf(ca[i])).intValue() + index * 26;
            }
        } else {
            throw new Exception("alphabetToNumberError" + aIndex);

        }
        return index;
    }

    public void writeAndClose() throws IOException {
        if (opened) {
            FileOutputStream out = new FileOutputStream(filePath);
            workbook.write(out);
            out.close();
            opened = false;
        }
    }

    public Sheet getSheet(int idx) {
        return workbook.getSheetAt(idx);
    }

    public Sheet getActiveSheet() {
        return getSheet(workbook.getActiveSheetIndex());
    }

    public Sheet getSheet(String name) {
        return workbook.getSheet(name);
    }

    public String getCSVContent() {
        StringBuffer sb = new StringBuffer("");
        Sheet s = getSheet(0);
        ArrayList<ExcelValue> header = getValueLine(s.getSheetName(), 1, 1);

        for (int i = 0; i < s.getLastRowNum() + 1; i++) {
            ArrayList<ExcelValue> line = getValueLine(s.getSheetName(), 1, i + 1, header.size());
            sb.append(convertToCSVLine(line));
        }
        return sb.toString();
    }

    public String convertToCSVLine(ArrayList<ExcelValue> line) {
        StringBuffer sb = new StringBuffer("");
        for (ExcelValue i : line) {
            sb.append(i.getValue() + ",");
        }
        sb.append('\n');
        return sb.toString();
    }

    public static ArrayList<ExcelValue> toExcelValueArray(ArrayList<String> line) {
        ArrayList<ExcelValue> r = new ArrayList<>();
        for (String l : line) {
            r.add(new ExcelValue(l, Cell.CELL_TYPE_STRING));
        }
        return r;
    }

    public static ArrayList<ArrayList<ExcelValue>> toExcelValueBlock(ArrayList<ArrayList<String>> line) {
        ArrayList<ArrayList<ExcelValue>> r = new ArrayList<>();
        for (ArrayList<String> l : line) {
            r.add(toExcelValueArray(l));
        }
        return r;
    }
}