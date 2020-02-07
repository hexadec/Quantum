package hu.hexadecimal.quantum.tools;

import android.content.Context;

import hu.hexadecimal.quantum.R;
import hu.hexadecimal.quantum.math.VisualOperator;

public class Doable {

    VisualOperator visualOperator;
    public String name;
    DoableType type;
    public int index = -1;
    public int oldIndex = -1;
    VisualOperator oldOp;

    public Doable(VisualOperator visualOperator, DoableType type, Context context) {
        this.visualOperator = visualOperator.copy();
        this.type = type;
        switch (type) {
            case ADD:
                name = context.getString(R.string.doable_add) + " " + visualOperator.getName();
                break;
            default:
                throw new IllegalArgumentException("Only ADD is allowed here");
        }
    }

    public Doable(VisualOperator visualOperator, DoableType type, Context context, int oldIndex, int newIndex) {
        this.visualOperator = visualOperator.copy();
        this.type = type;
        this.index = newIndex;
        this.oldIndex = oldIndex;
        switch (type) {
            case MOVE:
                name = context.getString(R.string.doable_move) + " " + visualOperator.getName();
                break;
            default:
                throw new IllegalArgumentException("Only MOVE is allowed here");
        }
    }

    public Doable(VisualOperator visualOperator, DoableType type, Context context, int index, VisualOperator oldOp) {
        this.visualOperator = visualOperator.copy();
        this.type = type;
        this.index = index;
        try {
            this.oldOp = oldOp.copy();
        } catch (Exception e) {
            this.oldOp = null;
        }
        switch (type) {
            case EDIT:
                name = context.getString(R.string.doable_edit) + " " + visualOperator.getName();
                break;
            case DELETE:
                name = context.getString(R.string.doable_delete) + " " + visualOperator.getName();
                break;
            case ADD:
                name = context.getString(R.string.doable_add) + " " + visualOperator.getName();
                break;
        }
    }

    public DoableType getType() {
        return type;
    }

    public VisualOperator getVisualOperator() {
        return visualOperator;
    }

    public VisualOperator oldOperator() {
        return oldOp;
    }

}
