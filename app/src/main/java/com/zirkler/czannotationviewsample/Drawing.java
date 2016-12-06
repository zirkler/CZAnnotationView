package com.zirkler.czannotationviewsample;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

@Table(name = "Drawing")
public class Drawing extends Model implements Serializable {
    @Column private String drawingTitle;

    public String getDrawingTitle() {
        return drawingTitle;
    }

    public void setDrawingTitle(String drawingTitle) {
        this.drawingTitle = drawingTitle;
    }
}
