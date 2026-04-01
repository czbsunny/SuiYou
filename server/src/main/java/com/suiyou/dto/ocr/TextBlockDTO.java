package com.suiyou.dto.ocr;
import lombok.Data;
/**
 * OCR识别文本块DTO类
 * 用于API响应中传输文本块信息
 */
@Data
public class TextBlockDTO {
    
    // 文本内容
    private String text;
    
    // 文本块的位置坐标（x, y, width, height）
    private int x;
    private int y;
    private int width;
    private int height;
    
    // 文本块的置信度
    private double confidence;

    /**
     * 检查当前文本块与另一个文本块的y区间是否相交
     * 
     * @param other 另一个文本块
     * @return 如果y区间相交返回true，否则返回false
     */
    public boolean hasIntersectingYRange(TextBlockDTO other) {
        int thisTop = this.y;
        int thisBottom = thisTop + this.height;
        int otherTop = other.y;
        int otherBottom = otherTop + other.height;
        
        // 检查两个区间是否相交
        // 区间1的开始小于等于区间2的结束，且区间2的开始小于等于区间1的结束
        return thisTop <= otherBottom && otherTop <= thisBottom;
    }
    
    /**
     * 检查当前文本块与另一个文本块的x区间是否相交
     * 
     * @param other 另一个文本块
     * @return 如果x区间相交返回true，否则返回false
     */
    public boolean hasIntersectingXRange(TextBlockDTO other) {
        int thisLeft = this.x;
        int thisRight = thisLeft + this.width;
        int otherLeft = other.x;
        int otherRight = otherLeft + other.width;
        
        // 检查两个区间是否相交
        // 区间1的开始小于等于区间2的结束，且区间2的开始小于等于区间1的结束
        return thisLeft <= otherRight && otherLeft <= thisRight;
    }

    // 重写toString方法
    @Override
    public String toString() {
        return "TextBlockDTO{" +
                "text='" + text + "'" +
                ", position=(" + x + ", " + y + ")" +
                ", size=(" + width + ", " + height + ")" +
                ", confidence=" + confidence +
                '}';
    }
}