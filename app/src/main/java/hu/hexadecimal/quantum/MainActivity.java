package hu.hexadecimal.quantum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hu.hexadecimal.quantum.graphics.BlochSphereView;
import hu.hexadecimal.quantum.graphics.QuantumView;

public class MainActivity extends Activity {

    BlochSphereView glSurfaceView;
    QuantumView qv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout relativeLayout  = findViewById(R.id.relative);
        qv = new QuantumView(this);
        relativeLayout.addView(qv);
        TextView tv = (TextView) findViewById(R.id.sample_text);

        glSurfaceView = new BlochSphereView(this);

        Qubit q = new Qubit();
        Qubit c = new Qubit();
        q.applyOperator(LinearOperator.HADAMARD);

        double value = 0;

        for (int i = 0; i < 10000; i++) {
            q.prepare(false);
            q.applyOperator(LinearOperator.SQRT_NOT);
            //Qubit[] qs = MultiQubitOperator.CNOT.operateOn(new Qubit[]{q, c});
            value += q.measureZ() ? 1 : 0;
        }
        tv.setText("" + value / 10000);

        View.OnTouchListener click = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
                float x = event.getX();
                float y = event.getY();
                Toast.makeText(MainActivity.this, "x: " + x + " y: " + y, Toast.LENGTH_SHORT).show();
                showAddGateDialog();
                return true;
            }
        };
        qv.setOnTouchListener(click);

        qv.addGate(0, LinearOperator.HADAMARD);
        qv.addGate(0, LinearOperator.PAULI_X);
        qv.addGate(0, LinearOperator.T_GATE);
        qv.addMultiQubitGate(new int[]{0,1},MultiQubitOperator.CNOT);
        qv.addMultiQubitGate(new int[]{0,1},MultiQubitOperator.SWAP);
        qv.addGate(4, LinearOperator.PAULI_Y);
    }

    public void displayBlochSphere(Qubit q) {
        Qubit q2 = q.copy();
        q2.applyOperator(LinearOperator.SQRT_NOT);
        glSurfaceView.setQBit(q2);
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.bloch_sphere);
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                glSurfaceView = new BlochSphereView(MainActivity.this);
            }
        });
        adb.setCancelable(false);
        adb.setView(glSurfaceView);
        adb.show();
    }

    public void showAddGateDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle(R.string.select_gate_type)
                .setPositiveButton(R.string.single_gate, null)
                .setNegativeButton(R.string.multi_gate, null)
                .setNeutralButton(R.string.cancel, null)
                .setCancelable(false);

        final AlertDialog d = adb.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button single = d.getButton(AlertDialog.BUTTON_POSITIVE);
                final Button multi = d.getButton(AlertDialog.BUTTON_NEGATIVE);
                single.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.setTitle(getString(R.string.select_gate));
                        single.setText("OK");
                        multi.setEnabled(false);
                        final View view = MainActivity.this.getLayoutInflater().inflate(R.layout.single_gate_selector, null);
                        final Spinner sp = view.findViewById(R.id.gate_spinner);
                        final Spinner qsp = view.findViewById(R.id.qubit_spinner);
                        List<String> qs = new ArrayList<>();
                        for (int i = 0; i < qv.getDisplayedQubits(); i++) {
                            qs.add("q" + (i + 1));
                        }
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, qs);
                        ArrayAdapter<String> gadapter =
                                new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, LinearOperator.getPredefinedGateNames());

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        qsp.setAdapter(adapter);
                        sp.setAdapter(gadapter);
                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                d.cancel();
                            }
                        });
                        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                qv.addGate(qsp.getSelectedItemPosition(),LinearOperator.findGateByName((String)sp.getSelectedItem()));
                                d.cancel();
                            }
                        });
                        d.setContentView(view);
                    }
                });
                multi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.setTitle(getString(R.string.select_gate));
                        multi.setText("OK");
                        single.setEnabled(false);
                    }
                });
            }
        });
        d.show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.prefs:
                Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
                startActivity(intent);
                break;
            case R.id.bloch:
                displayBlochSphere(qv.qbits.get(0));
                break;
            default:

        }
        return super.onOptionsItemSelected(item);
    }
}
