package hu.hexadecimal.quantum.tools;

import java.util.LinkedList;

import hu.hexadecimal.quantum.math.Complex;
import hu.hexadecimal.quantum.math.VisualOperator;

/**
 * Class used to hold the changing properties of a QuantumView while a configuration change is occurring
 * TODO use it to store these data at all times
 */
public class QuantumViewData {

    public LinkedList<Doable> undoList;
    public LinkedList<Doable> redoList;
    public GateSequence<VisualOperator> operators;
    public LinkedList<Complex> statevector;

    public QuantumViewData(LinkedList<Doable> undo, LinkedList<Doable> redo, GateSequence<VisualOperator> ops, LinkedList<Complex> statevector) {
        undoList = undo;
        redoList = redo;
        operators = ops;
        this.statevector = statevector;
    }

    public QuantumViewData() {
        undoList = new LinkedList<>();
        redoList = new LinkedList<>();
        operators = new GateSequence<VisualOperator>("");
        statevector = new LinkedList<>();
    }
}
