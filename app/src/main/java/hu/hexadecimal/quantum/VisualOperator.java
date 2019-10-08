package hu.hexadecimal.quantum;

import android.graphics.Rect;
import android.support.annotation.NonNull;

public class VisualOperator {

    protected int color = 0xff000000;
    protected Rect rectangle;

    VisualOperator() {

    }

    public void setColor(int color1) {
        color = color1;
    }

    public int getColor() {
        return color;
    }

    public void setRect(@NonNull Rect rect) {
        rectangle = rect;
    }

    public Rect getRect() {
        return rectangle;
    }

}
