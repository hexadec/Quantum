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
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import hu.hexadecimal.quantum.graphics.BlochSphereView;
import hu.hexadecimal.quantum.graphics.QuantumView;

public class MainActivity extends AppCompatActivity {

    BlochSphereView glSurfaceView;
    QuantumView qv;
    private ProgressDialog progressDialog;
    private Menu menu;
    private boolean probabilityMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        probabilityMode = false;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        LinearLayout relativeLayout = findViewById(R.id.linearLayout);
        qv = new QuantumView(this);
        relativeLayout.addView(qv);
        qv.setBackgroundColor(0xffeeeeee);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        qv.setLayoutParams(new LinearLayout.LayoutParams(displayMetrics.widthPixels * 2, ViewGroup.LayoutParams.MATCH_PARENT));

        glSurfaceView = new BlochSphereView(this);

        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent e) {
                onSingleTapUp(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public boolean onDown(MotionEvent event) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                float x = e.getX();
                float y = e.getY();
                showAddGateDialog(x, y);
                return true;
            }
        });

        qv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, R.string.choose_experiment, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 42);
                return;
            }
        });
    }

    public void displayBlochSphere() {
        Qubit q2 = new Qubit();
        for (VisualOperator v : qv.getOperators()) {
            for (int q : v.getQubitIDs())
                if (q == 0 && !v.isMultiQubit()) {
                    q2 = v.operateOn(new Qubit[]{q2})[0];
                    break;
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
        final VisualOperator v;
        if ((v = qv.whichGate(posx, posy)) != null) {
            adb.setTitle(R.string.select_action);
            adb.setNegativeButton(R.string.delete_gate, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    qv.deleteGateAt(posx, posy);
                }
            });
            adb.setPositiveButton(R.string.edit_gate, null);
            adb.setNeutralButton(R.string.cancel, null);
        }
        adb.setCancelable(true);

        final AlertDialog d = adb.create();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                final LinkedList<VisualOperator> operators = new LinkedList<>();
                final LinkedList<String> operatorNames = new LinkedList<>();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
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
                                        ObjectInputStream oi = new ObjectInputStream(getContentResolver().openInputStream(file.getUri()));
                                        VisualOperator m = (VisualOperator) oi.readObject();
                                        oi.close();
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
                    }
                }).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        final View mainView = MainActivity.this.getLayoutInflater().inflate(R.layout.gate_selector, null);
                        final Spinner gateType = mainView.findViewById(R.id.type_spinner);
                        List<String> qs = new ArrayList<>();
                        for (int i = 0; i < qv.getDisplayedQubits(); i++) {
                            qs.add("q" + (i + 1));
                        }
                        final Spinner filter = mainView.findViewById(R.id.filter_spinner);
                        final Spinner gateName = mainView.findViewById(R.id.gate_name_spinner);
                        final Spinner[] qX = new Spinner[]{
                                mainView.findViewById(R.id.order_first),
                                mainView.findViewById(R.id.order_second),
                                mainView.findViewById(R.id.order_third),
                                mainView.findViewById(R.id.order_fourth)};
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, qs);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        final LinkedList<String> mGates = VisualOperator.getPredefinedGateNames();
                        Collections.sort(mGates);
                        ArrayAdapter<String> gadapter =
                                new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                        gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < qX.length; i++) {
                                    qX[i].setAdapter(adapter);
                                }
                                if (v != null) {
                                    for (int i = 0; i < v.getQubitIDs().length; i++) {
                                        qX[i].setSelection(v.getQubitIDs()[i]);
                                    }
                                } else {
                                    qX[0].setSelection(qv.whichQubit(posy));
                                }
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
                                                LinkedList<String> mGates = VisualOperator.getPredefinedGateNames(filter.getSelectedItemPosition() == 1);
                                                Collections.sort(mGates);
                                                ArrayAdapter<String> gadapter =
                                                        new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                                                gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                gateName.setAdapter(gadapter);
                                            }
                                        } else {
                                            if (operators.size() > 0) {
                                                filter.setEnabled(false);
                                                filter.setSelection(0);
                                                ArrayAdapter<String> gadapter =
                                                        new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, operatorNames);
                                                gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                gateName.setAdapter(gadapter);
                                            } else {
                                                gateType.setSelection(0);
                                                Toast t = Toast.makeText(MainActivity.this, R.string.no_user_gates, Toast.LENGTH_SHORT);
                                                t.setGravity(Gravity.CENTER, 0, 0);
                                                t.show();
                                            }
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
                                                LinkedList<String> mGates = VisualOperator.getPredefinedGateNames(i == 1);
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
                                        LinkedList<String> gates = VisualOperator.getPredefinedGateNames(filter.getSelectedItemPosition() == 1);
                                        Collections.sort(gates);
                                        ArrayAdapter<String> adapter =
                                                new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, filter.getSelectedItemPosition() == 0 ? mGates : gates);

                                        int qbits = gateType.getSelectedItemPosition() == 0 ? VisualOperator.findGateByName(adapter.getItem(i)).getQubits() : operators.get(i).getQubits();
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
                                                qX[1].setVisibility(View.INVISIBLE);
                                            case 2:
                                                qX[2].setVisibility(View.INVISIBLE);
                                            case 3:
                                                qX[3].setVisibility(View.GONE);
                                            default:
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapterView) {
                                    }
                                });
                                mainView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        d.cancel();
                                    }
                                });
                                mainView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (operators.size() == 0 && gateType.getSelectedItemPosition() != 0) {
                                            return;
                                        }
                                        LinkedList<String> gates = VisualOperator.getPredefinedGateNames(filter.getSelectedItemPosition() == 1);
                                        Collections.sort(gates);
                                        ArrayAdapter<String> adapter =
                                                new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, filter.getSelectedItemPosition() == 0 ? mGates : gates);

                                        int qbits = gateType.getSelectedItemPosition() == 0
                                                ? VisualOperator.findGateByName(adapter.getItem(gateName.getSelectedItemPosition())).getQubits()
                                                : operators.get(gateName.getSelectedItemPosition()).getQubits();
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
                                        VisualOperator gate = gateType.getSelectedItemPosition() == 0
                                                ? VisualOperator.findGateByName((String) gateName.getSelectedItem()).copy()
                                                : operators.get(gateName.getSelectedItemPosition()).copy();
                                        if (((CheckBox) mainView.findViewById(R.id.hermitianConjugate)).isChecked()) gate.hermitianConjugate();
                                        if (v == null)
                                            qv.addGate(qids, gate);
                                        else
                                            qv.replaceGateAt(qids, gate, posx, posy);
                                        d.cancel();
                                    }
                                });
                                d.setTitle(R.string.select_gate);
                                try {
                                    d.setView(mainView);
                                    d.setContentView(mainView);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }).start();

            }
        };

        if (v != null) {
            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    final Button multi = d.getButton(AlertDialog.BUTTON_POSITIVE);
                    multi.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(r);
                        }
                    });
                }
            });
        } else {
            runOnUiThread(r);
        }
        d.show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        this.menu = menu;
        return true;
    }

    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.probability:
                menu.getItem(5).setIcon(ContextCompat.getDrawable(this, probabilityMode ? R.drawable.alpha_p_circle_outline : R.drawable.alpha_p_circle));
                probabilityMode = !probabilityMode;
                break;
            case R.id.save:
                if (qv.getOperators().size() < 1) {
                    Snackbar.make(findViewById(R.id.parent2), getString(R.string.no_gates), Snackbar.LENGTH_LONG).show();
                    return true;
                }
                try {
                    Uri uri = getContentResolver().getPersistedUriPermissions().get(0).getUri();
                    DocumentFile pickedDir = DocumentFile.fromTreeUri(MainActivity.this, uri);
                    if (!pickedDir.exists()) {
                        getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        pickedDir = null;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("'experiment'_yyyy-MM-dd_HH-mm-ss'" + QuantumView.FILE_EXTENSION + "'");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String filename = sdf.format(new Date());
                    DocumentFile newFile = pickedDir.createFile("application/octet-stream", filename);
                    OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
                    out.write(qv.exportGates());
                    out.flush();
                    out.close();
                    Snackbar.make(findViewById(R.id.parent2), getString(R.string.experiment_saved) + " \n" + filename, Snackbar.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(findViewById(R.id.parent2), getString(R.string.unknown_error), Snackbar.LENGTH_LONG).show();
                }
                break;
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
                try {
                    progressDialog = ProgressDialog.show(
                            MainActivity.this, "", MainActivity.this.getString(R.string.wait), true);
                } catch (Exception e) {
                    e.printStackTrace();
                    progressDialog = null;
                }
                new Thread() {
                    public void run() {
                        ExperimentRunner experimentRunner = new ExperimentRunner(qv.getOperators());
                        long startTime = System.currentTimeMillis();
                        float[] probs = experimentRunner.runExperiment(shots, threads, handler, probabilityMode);
                        long time = System.currentTimeMillis() - startTime;
                        String t = time / 1000 + "s " + time % 1000 + "ms";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                                adb.setCancelable(false);
                                adb.setPositiveButton("OK", null);
                                adb.setNeutralButton(R.string.export_csv, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try {
                                            Uri uri = getContentResolver().getPersistedUriPermissions().get(0).getUri();
                                            DocumentFile pickedDir = DocumentFile.fromTreeUri(MainActivity.this, uri);
                                            if (!pickedDir.exists()) {
                                                getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                                pickedDir = null;
                                            }
                                            StringBuilder sb = new StringBuilder();
                                            for (int k = 0; k < probs.length; k++) {
                                                if (k != 0) sb.append("\r\n");
                                                sb.append(k);
                                                sb.append(',');
                                                sb.append(String.format("%" + QuantumView.MAX_QUBITS + "s", Integer.toBinaryString(k)).replace(' ', '0'));
                                                sb.append(',');
                                                sb.append(probs[k]);
                                            }
                                            SimpleDateFormat sdf = new SimpleDateFormat("'results'_yyyy-MM-dd_HH-mm-ss'.csv'");
                                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                            String filename = sdf.format(new Date());
                                            DocumentFile newFile = pickedDir.createFile("text/csv", filename);
                                            OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
                                            out.write(sb.toString().getBytes());
                                            out.close();
                                            Snackbar.make(findViewById(R.id.parent2), filename + " \n" + getString(R.string.successfully_exported), Snackbar.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(MainActivity.this, R.string.choose_save_location_settings, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                                adb.setTitle(getString(R.string.results) + ": \t" + t);
                                ScrollView scrollView = new ScrollView(MainActivity.this);
                                TextView textView = new TextView(MainActivity.this);
                                textView.setTypeface(Typeface.MONOSPACE);
                                byte[] measuredQubits = qv.getMeasuredQubits();
                                outerfor:
                                for (int i = 0; i < probs.length; i++) {
                                    for (int j = 0; j < measuredQubits.length; j++) {
                                        if (measuredQubits[j] < 1 && (i >> j) % 2 == 1) {
                                            continue outerfor;
                                        }
                                    }
                                    textView.setText(textView.getText() + "\n\t\t" + String.format("%" + QuantumView.MAX_QUBITS + "s", Integer.toBinaryString(i)).replace(' ', '0') + ": " + probs[i]);
                                }
                                scrollView.addView(textView);
                                adb.setView(scrollView);
                                adb.show();
                                try {
                                    progressDialog.cancel();
                                } catch (Exception e) {
                                    progressDialog = null;
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }.start();
                break;
            default:

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK && requestCode == 42) {
            Uri uri = resultData.getData();
            DocumentFile pickedFile = DocumentFile.fromSingleUri(this, uri);
            try {
                if (pickedFile.getName().endsWith(QuantumView.FILE_EXTENSION)) {
                    Object obj = new ObjectInputStream(getContentResolver().openInputStream(pickedFile.getUri())).readObject();
                    if (!qv.importGates(obj)) {
                        throw new Exception("Maybe empty gate sequence?");
                    } else {
                        Snackbar.make(findViewById(R.id.parent2), R.string.successfully_imported, Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.parent2), R.string.invalid_file, Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(findViewById(R.id.parent2), getString(R.string.unknown_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
