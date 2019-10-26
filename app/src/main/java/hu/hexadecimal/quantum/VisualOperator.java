package hu.hexadecimal.quantum;

import android.graphics.Rect;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class VisualOperator {

    protected int color = 0xff000000;
    protected List<Rect> rectangle;
    public final int MATRIX_DIM;
    private int[] qubit_ids;

    VisualOperator(int DIM) {
        int nqubits = 1;
        switch (DIM) {
            case 4:
                nqubits = 2;
                break;
            case 8:
                nqubits = 3;
                break;
            case 16:
                nqubits = 4;
                break;
            case 32:
                nqubits = 5;
                break;
            default:
                nqubits = 1;

        }
        qubit_ids = new int[nqubits];
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
        return MATRIX_DIM != 2;
    }

    public boolean setQubitIDs(int[] qubit_ids) {
        if (qubit_ids.length != this.qubit_ids.length) {
            return false;
        } else {
            this.qubit_ids = qubit_ids;
            return true;
        }
    }

    public int[] getQubitIDs() {
        return qubit_ids;
    }
}
