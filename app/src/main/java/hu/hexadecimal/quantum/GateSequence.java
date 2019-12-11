package hu.hexadecimal.quantum;

import java.util.Collection;
import java.util.LinkedList;

public class GateSequence<T> extends LinkedList {

    final String name;
    public static final long serialVersionUID = 1L;

    public GateSequence(String name) {
        super();
        this.name = name;
    }

    public GateSequence(Collection c, String name) {
        super(c);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
