package hu.hexadecimal.quantum;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class GateView extends TextView {

    private String gate;

    GateView(Context con) {
        super(con);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {}});
    }

    @Override
    public void setOnClickListener(View.OnClickListener l) {
        super.setOnClickListener(l);
        AlertDialog.Builder gates = new AlertDialog.Builder(super.getContext());
        gates.setIcon(android.R.drawable.ic_dialog_map);
        gates.setTitle("Choose a gate");
        //gates.setSingleChoiceItems()
    }

    static List<String> getPredefinedGateNames() {
        List<String> list = new ArrayList<>();
        LinearOperator linearOperator = new LinearOperator();
        try {
            Field[] fields = linearOperator.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(linearOperator) instanceof LinearOperator) {
                    list.add(((LinearOperator)field.get(linearOperator)).getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }
}
