package com.tools.excel;

import org.apache.poi.ss.usermodel.Cell;

public class ExcelValue {
    private boolean isRichText = false;
    private boolean hasHyperlink = false;
    private FontStyle fontStyle;
    private String hyperlink;
    private int linkType;

    ExcelValue() {

    }

    public ExcelValue(String value, int type) {
        this.value = value;
        this.type = type;
    }

    public ExcelValue(String value, int type, FontStyle fontStyle) {
        this.value = value;
        this.type = type;
        this.isRichText = true;
        this.fontStyle = fontStyle;
    }

    public ExcelValue(String value, int type, String link, int linkType) {
        this.value = value;
        this.type = type;
        this.hasHyperlink = true;
        this.hyperlink = link;
        this.linkType = linkType;
    }

    public boolean hasHyperlink() {
        return this.hasHyperlink;
    }

    public boolean isRichTextString() {
        return isRichText;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean equals(Object e) {
        if (value.equals(((ExcelValue) e).getValue()) && type == ((ExcelValue) e).getType()) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        String s = "";
        s = s + "value:" + value;
        switch (type) {
            case Cell.CELL_TYPE_BOOLEAN:
                s = s + " type:CELL_TYPE_BOOLEAN";
                break;
            case Cell.CELL_TYPE_STRING:
                s = s + " type:CELL_TYPE_STRING";
                break;
            case Cell.CELL_TYPE_NUMERIC:
                s = s + " type:CELL_TYPE_NUMERIC";
                break;
            case Cell.CELL_TYPE_FORMULA:
                s = s + " type:CELL_TYPE_FORMULA";
                break;
            case Cell.CELL_TYPE_ERROR:
                s = s + " type:CELL_TYPE_ERROR";
                break;
            case Cell.CELL_TYPE_BLANK:
                s = s + " type:CELL_TYPE_BLANK";
                break;
        }
        return s;
    }


    String value;
    int type;

    /**
     * @return the fontStyle
     */
    public FontStyle getFontStyle() {

        return fontStyle;
    }

    /**
     * @return the hyperlink
     */
    public String getHyperlink() {
        return hyperlink;
    }

    /**
     * @param hyperlink the hyperlink to set
     */
    public void setHyperlink(String hyperlink) {
        this.hyperlink = hyperlink;
    }

    /**
     * @return the linkType
     */
    public int getLinkType() {
        return linkType;
    }

    /**
     * @param linkType the linkType to set
     */
    public void setLinkType(int linkType) {
        this.linkType = linkType;
    }

}
