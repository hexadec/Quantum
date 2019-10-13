package hu.hexadecimal.quantum;

import android.graphics.Rect;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class VisualOperator {

    protected int color = 0xff000000;
    protected List<Rect> rectangle;
    public final int MATRIX_DIM;

    VisualOperator(int DIM) {
        rectangle = new ArrayList<>();
        MATRIX_DIM = DIM;
    }

    public void setColor(int color1) {
        color = color1;
    }

    public int getColor() {
        return color;
    }

    public void addRect(@NonNull Rect rect) {
        rectangle.add(rect);
    }

    public void resetRect() {
        rectangle.clear();
    }

    public List<Rect> getRect() {
        return rectangle;
    }

    public boolean isMultiQubit() {
        return MATRIX_DIM == 2;
    }

}
