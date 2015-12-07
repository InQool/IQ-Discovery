package com.inqool.dcap.ip.tile;

import java.io.File;

public class Tile {
    private File data;

    private int level;
    private int row;
    private int column;

    public Tile(File data, int level, int row, int column) {
        this.data = data;
        this.level = level;
        this.row = row;
        this.column = column;
    }

    public File getData() {
        return data;
    }

    public void setData(File data) {
        this.data = data;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
