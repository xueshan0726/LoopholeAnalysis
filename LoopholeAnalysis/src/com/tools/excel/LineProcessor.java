/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tools.excel;

import java.util.ArrayList;

public interface LineProcessor {
    public void proccessLine(int sheetIdx, int rowno, ArrayList<String> line);
}
