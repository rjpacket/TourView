package com.rjp.tourview;

import android.graphics.Bitmap;

/**
 * author : Gimpo create on 2018/2/6 15:42
 * email  : jimbo922@163.com
 */

public class TourModel {
    private Bitmap bitmap;
    private int type;

    private int bitmapLeft;
    private int bitmapTop;

    private int holeLeft;
    private int holeTop;
    private int holeRight;
    private int holeBottom;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBitmapLeft() {
        return bitmapLeft;
    }

    public void setBitmapLeft(int bitmapLeft) {
        this.bitmapLeft = bitmapLeft;
    }

    public int getBitmapTop() {
        return bitmapTop;
    }

    public void setBitmapTop(int bitmapTop) {
        this.bitmapTop = bitmapTop;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getHoleLeft() {
        return holeLeft;
    }

    public void setHoleLeft(int holeLeft) {
        this.holeLeft = holeLeft;
    }

    public int getHoleTop() {
        return holeTop;
    }

    public void setHoleTop(int holeTop) {
        this.holeTop = holeTop;
    }

    public int getHoleRight() {
        return holeRight;
    }

    public void setHoleRight(int holeRight) {
        this.holeRight = holeRight;
    }

    public int getHoleBottom() {
        return holeBottom;
    }

    public void setHoleBottom(int holeBottom) {
        this.holeBottom = holeBottom;
    }

    /**
     * 设置高亮洞的rect坐标
     * @param holeLeft 洞左
     * @param holeTop 洞上
     * @param holeRight 洞右
     * @param holeBottom 洞下
     */
    public void setHole(int holeLeft, int holeTop, int holeRight, int holeBottom) {
        this.holeLeft = holeLeft;
        this.holeTop = holeTop;
        this.holeRight = holeRight;
        this.holeBottom = holeBottom;
    }

    /**
     * 设置引导图片的位置
     * @param bitmapLeft 图片居左
     * @param bitmapTop 图片居上
     */
    public void setBitmapLocation(int bitmapLeft, int bitmapTop) {
        this.bitmapLeft = bitmapLeft;
        this.bitmapTop = bitmapTop;
    }
}
