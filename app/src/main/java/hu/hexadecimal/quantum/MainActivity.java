package hu.hexadecimal.quantum;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import hu.hexadecimal.quantum.graphics.BlochSphereView;
import hu.hexadecimal.quantum.graphics.QuantumView;

public class MainActivity extends AppCompatActivity {

    BlochSphereView glSurfaceView;
    QuantumView qv;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout relativeLayout = findViewById(R.id.relative);
        qv = new QuantumView(this);
        relativeLayout.addView(qv);

        glSurfaceView = new BlochSphereView(this);

        View.OnTouchListener click = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
                float x = event.getX();
                float y = event.getY();
                VisualOperator vop = qv.whichGate(x, y);
                String opn = vop == null ? null : vop.getName();
                Toast.makeText(MainActivity.this, "x: " + x + " y: " + y + " OP: " + opn, Toast.LENGTH_SHORT).show();
                showAddGateDialog(x, y);
                return true;
            }
        };
        qv.setOnTouchListener(click);
    }

    public void displayBlochSphere() {
        Qubit q2 = new Qubit();
        for (VisualOperator v : qv.getOperators()) {
            if (v instanceof LinearOperator) {
                for (int q : v.getQubitIDs())
                    if (q == 0) {
                        q2.applyOperator((LinearOperator) v);
                        break;
                    }
            }
        }
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

    public void showAddGateDialog(float posx, float posy) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle(R.string.select_action)
                .setNegativeButton(R.string.delete_gate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        qv.deleteGateAt(posx, posy);
                    }
                })
                .setPositiveButton(R.string.add_gate, null)
                .setNeutralButton(R.string.cancel, null)
                .setCancelable(true);

        final AlertDialog d = adb.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button multi = d.getButton(AlertDialog.BUTTON_POSITIVE);
                d.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(qv.whichGate(posx, posy) != null);
                multi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinkedList<MultiQubitOperator> operators = new LinkedList<>();
                        LinkedList<String> operatorNames = new LinkedList<>();
                        try {
                            Uri uri = getContentResolver().getPersistedUriPermissions().get(0).getUri();
                            DocumentFile pickedDir = DocumentFile.fromTreeUri(MainActivity.this, uri);
                            if (!pickedDir.exists()) {
                                getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                pickedDir = null;
                            }

                            for (DocumentFile file : pickedDir.listFiles()) {
                                try {
                                    if (file.getName().endsWith(VisualOperator.FILE_EXTENSION)) {
                                        MultiQubitOperator m = (MultiQubitOperator) new ObjectInputStream(getContentResolver().openInputStream(file.getUri())).readObject();
                                        operators.add(m);
                                        operatorNames.add(m.getName());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("QubitAdder", "Some error has happened :(");
                            e.printStackTrace();
                        }
                        final View view = MainActivity.this.getLayoutInflater().inflate(R.layout.gate_selector, null);
                        final Spinner gateType = view.findViewById(R.id.type_spinner);
                        final Spinner filter = view.findViewById(R.id.filter_spinner);
                        final Spinner gateName = view.findViewById(R.id.gate_name_spinner);
                        final Spinner[] qX = new Spinner[]{
                                view.findViewById(R.id.order_first),
                                view.findViewById(R.id.order_second),
                                view.findViewById(R.id.order_third),
                                view.findViewById(R.id.order_fourth)};
                        List<String> qs = new ArrayList<>();
                        for (int i = 0; i < qv.getDisplayedQubits(); i++) {
                            qs.add("q" + (i + 1));
                        }
                        if (operators.size() == 0) {
                            gateType.setEnabled(false);
                        }
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, qs);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        qX[0].setAdapter(adapter);
                        qX[1].setAdapter(adapter);
                        qX[2].setAdapter(adapter);
                        qX[3].setAdapter(adapter);
                        final LinkedList<String> mGates = MultiQubitOperator.getPredefinedGateNames();
                        Collections.sort(mGates);
                        ArrayAdapter<String> gadapter =
                                new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                        gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        gateName.setAdapter(gadapter);
                        gateType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                if (i == 0) {
                                    filter.setEnabled(true);
                                    if (filter.getSelectedItemPosition() == 0) {
                                        ArrayAdapter<String> gadapter =
                                                new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                                        gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        gateName.setAdapter(gadapter);
                                    } else {
                                        LinkedList<String> mGates = MultiQubitOperator.getPredefinedGateNames(filter.getSelectedItemPosition() == 1);
                                        Collections.sort(mGates);
                                        ArrayAdapter<String> gadapter =
                                                new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                                        gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        gateName.setAdapter(gadapter);
                                    }
                                } else {
                                    filter.setEnabled(false);
                                    filter.setSelection(0);
                                    ArrayAdapter<String> gadapter =
                                            new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, operatorNames);
                                    gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    gateName.setAdapter(gadapter);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });
                        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                if (gateType.getSelectedItemPosition() == 0) {
                                    if (i == 0) {
                                        ArrayAdapter<String> gadapter =
                                                new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                                        gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        gateName.setAdapter(gadapter);
                                    } else {
                                        LinkedList<String> mGates = MultiQubitOperator.getPredefinedGateNames(i == 1);
                                        Collections.sort(mGates);
                                        ArrayAdapter<String> gadapter =
                                                new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                                        gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        gateName.setAdapter(gadapter);
                                    }
                                } else {

                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                        gateName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                LinkedList<String> gates = MultiQubitOperator.getPredefinedGateNames(filter.getSelectedItemPosition() == 1);
                                Collections.sort(gates);
                                ArrayAdapter<String> adapter =
                                        new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, filter.getSelectedItemPosition() == 0 ? mGates : gates);

                                int qbits = gateType.getSelectedItemPosition() == 0 ? MultiQubitOperator.findGateByName(adapter.getItem(i)).NQBITS : operators.get(i).NQBITS;
                                switch (qbits) {
                                    case 1:
                                        qX[0].setVisibility(View.VISIBLE);
                                    case 2:
                                        qX[1].setVisibility(View.VISIBLE);
                                    case 3:
                                        qX[2].setVisibility(View.VISIBLE);
                                    case 4:
                                        qX[3].setVisibility(View.VISIBLE);
                                    default:
                                }
                                switch (qbits) {
                                    case 1:
                                        qX[1].setVisibility(View.GONE);
                                    case 2:
                                        qX[2].setVisibility(View.GONE);
                                    case 3:
                                        qX[3].setVisibility(View.GONE);
                                    default:
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });
                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                d.cancel();
                            }
                        });
                        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                LinkedList<String> gates = MultiQubitOperator.getPredefinedGateNames(filter.getSelectedItemPosition() == 1);
                                Collections.sort(gates);
                                ArrayAdapter<String> adapter =
                                        new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, filter.getSelectedItemPosition() == 0 ? mGates : gates);

                                int qbits = gateType.getSelectedItemPosition() == 0
                                        ? MultiQubitOperator.findGateByName(adapter.getItem(gateName.getSelectedItemPosition())).NQBITS
                                        : operators.get(gateName.getSelectedItemPosition()).NQBITS;
                                int[] qids = new int[qbits];
                                for (int i = 0; i < qbits; i++) {
                                    qids[i] = qX[i].getSelectedItemPosition();
                                }
                                for (int i = 0; i < qbits; i++) {
                                    for (int j = i + 1; j < qbits; j++) {
                                        if (qids[i] == qids[j]) {
                                            d.cancel();
                                            return;
                                        }
                                    }
                                }
                                qv.addGate(qids, gateType.getSelectedItemPosition() == 0
                                        ? MultiQubitOperator.findGateByName((String) gateName.getSelectedItem())
                                        : operators.get(gateName.getSelectedItemPosition()));
                                d.cancel();
                            }
                        });
                        d.setContentView(view);
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
            case R.id.undo:
                qv.removeLastGate();
                break;
            case R.id.matrix:
                startActivity(new Intent(MainActivity.this, MatrixEditorActivity.class));
                break;
            case R.id.bloch:
                displayBlochSphere();
                break;
            case R.id.run:
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                final int shots = Integer.valueOf(pref.getString("shots", "1024"));
                final int threads = Integer.valueOf(pref.getString("threads", "8"));
                final Handler handler = new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        try {
                            progressDialog.setMessage(MainActivity.this.getString(R.string.wait) + "\n" + message.what + "/" + shots);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                });
                progressDialog = ProgressDialog.show(
                        MainActivity.this, "", MainActivity.this.getString(R.string.wait), true);
                new Thread() {
                    public void run() {
                        ExperimentRunner experimentRunner = new ExperimentRunner(qv.getOperators());
                        long startTime = System.currentTimeMillis();
                        float[] probs = experimentRunner.runExperiment(shots, threads, handler);
                        long time = System.currentTimeMillis() - startTime;
                        String t = new SimpleDateFormat("s's' SSS 'ms'").format(new Date(time));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                                adb.setCancelable(false);
                                adb.setPositiveButton("OK", null);
                                adb.setTitle(getString(R.string.results) + ": \t" + t);
                                ScrollView scrollView = new ScrollView(MainActivity.this);
                                TextView textView = new TextView(MainActivity.this);
                                textView.setTypeface(Typeface.MONOSPACE);
                                for (int i = 0; i < probs.length; i++) {
                                    textView.setText(textView.getText() + "\n" + String.format("%" + qv.MAX_QUBITS + "s", Integer.toBinaryString(i)).replace(' ', '0') + ": " + probs[i]);
                                }
                                scrollView.addView(textView);
                                adb.setView(scrollView);
                                adb.show();
                                progressDialog.cancel();
                            }
                        });
                    }
                }.start();
                break;
            default:

        }
        return super.onOptionsItemSelected(item);
    }
}
