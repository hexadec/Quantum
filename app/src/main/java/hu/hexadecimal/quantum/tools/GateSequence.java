package hu.hexadecimal.quantum.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;

import hu.hexadecimal.quantum.math.VisualOperator;

/**
 * Class used to save and load a quantum circuit to and from storage
 * @param <T> ALWAYS has to be a VisualOperator
 */
public class GateSequence<T> extends LinkedList<T> {

    final String name;

    public GateSequence(String name) {
        super();
        this.name = name;
    }

    public GateSequence(Collection<VisualOperator> c, String name) {
        super((Collection<? extends T>) c);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i) instanceof VisualOperator) {
                jsonArray.put(((VisualOperator) this.get(i)).toJSON());
            }
        }
        jsonObject.put("array", jsonArray);
        jsonObject.put("name", name);
        return jsonObject;
    }

    public static GateSequence<VisualOperator> fromJSON(JSONObject jsonObject) {
        try {
            String name = jsonObject.getString("name");
            LinkedList<VisualOperator> visualOperators = new LinkedList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("array");
            for (int i = 0; i < jsonArray.length(); i++) {
                visualOperators.addLast(VisualOperator.fromJSON(jsonArray.getJSONObject(i)));
            }
            return new GateSequence<>(visualOperators, name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
