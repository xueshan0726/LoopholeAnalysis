/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tools.excel;

public class HighLightString {
    private final FontStyle fontStyle;
    private final String content;

    public HighLightString(String content, FontStyle fontStyle) {
        this.content = content;
        this.fontStyle = fontStyle;
    }

    /**
     * @return the fontStyle
     */
    public FontStyle getFontStyle() {
        return fontStyle;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }
}
