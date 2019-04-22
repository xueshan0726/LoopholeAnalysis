/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tools.excel;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

public interface FontProcess {
    public Font applyFont(Font f);

    public static final FontProcess BOLD_RED = new FontProcess() {

        @Override
        public Font applyFont(Font f) {
            f.setColor(IndexedColors.RED.getIndex());
            f.setBoldweight(Font.BOLDWEIGHT_NORMAL);
            return f;
        }
    };
    public static final FontProcess STRIKE_OUT = new FontProcess() {

        @Override
        public Font applyFont(Font f) {
            f.setColor(IndexedColors.ROSE.getIndex());
            f.setBoldweight(Font.BOLDWEIGHT_NORMAL);
            f.setStrikeout(true);
            return f;
        }
    };
    public static final FontProcess NORMAL = new FontProcess() {

        @Override
        public Font applyFont(Font f) {
            return f;
        }
    };
}
