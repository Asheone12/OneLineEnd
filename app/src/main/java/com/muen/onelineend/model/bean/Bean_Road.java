package com.muen.onelineend.model.bean;

import androidx.annotation.NonNull;

import com.muen.onelineend.util.ValueUtil;

import java.util.Arrays;
import java.util.List;

public class Bean_Road {
    private final int rows;
    private final int columns;
    private final List<Integer> roadList;

    public Bean_Road(int rows, int columns, @NonNull List<Integer> road) {
        if(road==null) throw new NullPointerException("List<Integer> road 不能为null");
        this.rows = rows;
        this.columns = columns;
        this.roadList = road;
    }

    public Bean_Road(int rows, int columns, Integer[] roads) {
        this.rows = rows;
        this.columns = columns;
        this.roadList = Arrays.asList(roads);
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public List<Integer> getRoadList() {
        return roadList;
    }

    public String getRoadString(){
        return ValueUtil.getListString(roadList);
    }

    public int getDifficulties(){
        return rows*columns-roadList.size();
    }
}
