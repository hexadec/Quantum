package hu.hexadecimal.quantum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
            Qubit[] qs = MultiQubitOperator.CNOT.operateOn(new Qubit[]{q, c});
            value += qs[1].measureZ() ? 1 : 0;
        }
        tv.setText("" + value / 10000);

        qv.addGate(0, LinearOperator.HADAMARD);
        qv.addGate(0, LinearOperator.PAULI_X);
        qv.addGate(0, LinearOperator.T_GATE);
        qv.addGate(0, LinearOperator.PAULI_Z);
        qv.addGate(0, LinearOperator.S_GATE);
        qv.addGate(2, LinearOperator.T_GATE);
        qv.addGate(4, LinearOperator.PAULI_Y);
    }

    public void displayBlochSphere(Qubit q) {
        Qubit q2 = q.copy();
        q2.applyOperator(LinearOperator.HADAMARD);
        q2.applyOperator(LinearOperator.PAULI_X);
        q2.applyOperator(LinearOperator.T_GATE);
        q2.applyOperator(LinearOperator.PAULI_Z);
        q2.applyOperator(LinearOperator.S_GATE);
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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bloch:
                displayBlochSphere(qv.qbits[0]);
                break;
            default:

        }
        return super.onOptionsItemSelected(item);
    }
}
