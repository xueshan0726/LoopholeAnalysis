/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tools.excel;

import com.tools.MyCounter;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelS extends DefaultHandler {

    public static final String ERROR = "ERROR";
    private final LineProcessor processor;
    private final SharedStringsTable sst;
    private boolean valueStart = false;
    private StringBuffer curValue = new StringBuffer();
    private final ArrayList<String> curRow = new ArrayList<>();
    private String curCellNo;
    private String curType;
    private Integer sheetIdx = 0;
    private Integer rowno = 0;

    public ExcelS(String filename, LineProcessor processor) throws Exception {
        this.processor = processor;
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader reader = new XSSFReader(pkg);
//        reader.
        this.sst = reader.getSharedStringsTable();
        XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        parser.setContentHandler(this);
        Iterator<InputStream> sheetsData = reader.getSheetsData();
        while (sheetsData.hasNext()) {
            rowno = 0;
            sheetIdx++;
            InputStream next = sheetsData.next();
            parser.parse(new InputSource(next));
            next.close();
        }
        try {
            pkg.flush();
            pkg.close();
        }catch (Exception e){

        }


    }



        public static int countLine(String f, final int sheet) throws Exception {
        final MyCounter counter = new MyCounter();
        ExcelS s = new ExcelS(f, new LineProcessor() {

            @Override
            public void proccessLine(int sheetIdx, int rowno, ArrayList<String> line) {
                if (sheet == sheetIdx) {
                    if (!line.isEmpty()) {
                        counter.count("count");
                    }
                }
            }
        }
        );

        return counter.getCounts("count");
    }

    public static List<List<String>> readFirstSheet(String path) throws Exception {
        List<List<String>> table = new ArrayList<>();
        new ExcelS(path, (sheetIdx1, rowno1, line) -> table.add(new ArrayList<>(line)));
        return table;
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        valueStart = qName.equals("v") || qName.equals("f");
        if (qName.equals("c")) {//cell
            this.curCellNo = attributes.getValue("r");
            this.curType = attributes.getValue("t");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("row")) {
            rowno++;

            this.processor.proccessLine(sheetIdx, rowno, new ArrayList<String>(curRow));
            this.curRow.clear();

        } else if (valueStart) {
            if (curValue.length() != 0) {
                Integer col = this.getColIndex(curCellNo);

                try {
//                    System.out.println(curType+"\t"+curValue);
                    String tmp = curValue.toString();
                    if (curType != null) {
                        if (curType.equals("n") || curType.equals("str")) {
                            tmp = curValue.toString();
                        }
                        if (curType.equals("s")) {//String
                            tmp = new XSSFRichTextString(sst.getEntryAt(Integer.parseInt(curValue.toString()))).toString();
                        }
                        if (curType.equals("e")) {//ERROR
                            tmp = ERROR;
                        }
                        if (!curType.equals("s") && !curType.equals("n") && !curType.equals("e") && !curType.equals("str")) {//print undiscovered type
                            System.out.println("new type:" + curType + "\t" + curValue.toString());
                        }
                    } else {
                        tmp = curValue.toString();

                    }

                    curRow.add(tmp);
                } catch (NumberFormatException e) {
                    System.out.println("error:" + this.curCellNo + "\t" + curType + "\t" + curValue);
                    curRow.add(ERROR);
                }
                while (curRow.size() < col) {
                    curRow.add(curRow.size() - 1, "");
                }
            } else {
                curRow.add("");
            }
            valueStart = false;

            curValue.setLength(0);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (valueStart) {
            this.curValue.append(ch, start, length);
//            this.curValue = curValue + new String(ch,start,length);
        }
    }

    //get column index from cellNo

    private int getColIndex(String rowStr) {
        rowStr = rowStr.replaceAll("[^A-Z]", "");
        byte[] rowAbc = rowStr.getBytes();
        int len = rowAbc.length;
        float num = 0;
        for (int i = 0; i < len; i++) {
            num += (rowAbc[i] - 'A' + 1) * Math.pow(26, len - i - 1);
        }

        return (int) num;

    }

}
