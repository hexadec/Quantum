package hu.hexadecimal.quantum.tools;

import java.util.LinkedList;

import hu.hexadecimal.quantum.math.VisualOperator;

public class QuantumViewData {

    public LinkedList<Doable> undoList;
    public LinkedList<Doable> redoList;
    public GateSequence<VisualOperator> operators;

    public QuantumViewData(LinkedList<Doable> undo, LinkedList<Doable> redo, GateSequence<VisualOperator> ops) {
        undoList = undo;
        redoList = redo;
        operators = ops;
    }

    public QuantumViewData() {
        undoList = new LinkedList<>();
        redoList = new LinkedList<>();
        operators = new GateSequence<VisualOperator>("");
    }
}
