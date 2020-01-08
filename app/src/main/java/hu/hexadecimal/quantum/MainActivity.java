package hu.hexadecimal.quantum;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import hu.hexadecimal.quantum.graphics.BlochSphereView;
import hu.hexadecimal.quantum.graphics.ExecutionProgressDialog;
import hu.hexadecimal.quantum.graphics.QuantumView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    QuantumView qv;
    private ExecutionProgressDialog progressDialog;
    private ExperimentRunner experimentRunner;
    private Thread expThread;

    private int probabilityMode;
    private boolean saved;
    private int blochSpherePos = 0;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private double[] importantAngles = new double[]{0, Math.PI / 8, Math.PI / 6, Math.PI / 4, Math.PI / 3, Math.PI / 2, Math.PI / 3 * 2, Math.PI / 4 * 3, Math.PI};
    private String[] importantAngleNames = new String[]{"0", "π/8", "π/6", "π/4", "π/3", "π/2", "2π/3", "3π/4", "π"};

    private double[] importantAngles2PI = new double[]{0, Math.PI / 8, Math.PI / 6, Math.PI / 4, Math.PI / 3, Math.PI / 2, Math.PI / 3 * 2, Math.PI / 4 * 3, Math.PI,
            Math.PI / 5 * 4, Math.PI / 3 * 4, Math.PI / 2 * 3, Math.PI / 3 * 5, Math.PI / 4 * 7};
    private String[] importantAngleNames2PI = new String[]{"0", "π/8", "π/6", "π/4", "π/3", "π/2", "2π/3", "3π/4", "π",
            "5π/4", "4π/3", "3π/2", "5π/3", "7π/4"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saved = true;
        probabilityMode = 0;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#171717")));
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#171717"));

        LinearLayout relativeLayout = findViewById(R.id.linearLayout);
        qv = new QuantumView(this);
        relativeLayout.addView(qv);
        qv.setBackgroundColor(0xffeeeeee);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        qv.setLayoutParams(new LinearLayout.LayoutParams(displayMetrics.widthPixels * 2, ViewGroup.LayoutParams.MATCH_PARENT));

        FloatingActionButton fab = findViewById(R.id.fab_main);
        FloatingActionButton execute = findViewById(R.id.fab_matrix);

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
                float rx = e.getRawX();
                float ry = e.getRawY();
                int[] loc = new int[2];
                execute.getLocationOnScreen(loc);
                if (loc[0] < rx && loc[1] < ry) return false;
                showAddGateDialog(x, y);
                return true;
            }
        });

        qv.setOnTouchListener((View view, MotionEvent motionEvent) -> gestureDetector.onTouchEvent(motionEvent));

        fab.setOnClickListener((View view) -> {
            Toast.makeText(MainActivity.this, R.string.choose_experiment, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 42);
        });


        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        execute.postDelayed(() -> execute.setOnClickListener((View view) -> {
            //To prevent double clicking
            execute.setClickable(false);
            new Handler().postDelayed(() -> execute.setClickable(true), 500);
            final int shots = Integer.valueOf(pref.getString("shots", "4096"));
            final int threads = Integer.valueOf(pref.getString("threads", "8"));
            final String separator = pref.getString("separator", ",");
            final boolean scientific = pref.getBoolean("sci_form", false);
            qv.shouldStop = false;
            final int opLen = qv.getOperators().size();
            final Handler handler = new Handler((Message message) -> {
                try {
                    if (!progressDialog.isShowing()) {
                        qv.shouldStop = true;
                    } else {
                        if (experimentRunner.status != 0) {
                            progressDialog.setProgress(message.what, shots);
                        } else {
                            progressDialog.setSecondaryProgress(message.what + 1, opLen);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            });
            if (pref.getBoolean("optimize", false)) {
                qv.optimizeCircuit();
            }
            final int MAX_QUBITS = qv.getLastUsedQubit() + 1;
            try {
                progressDialog = new ExecutionProgressDialog(MainActivity.this, MainActivity.this.getString(R.string.wait));
                progressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog = null;
            }
            expThread = new Thread(() -> {
                experimentRunner = new ExperimentRunner(qv);
                long startTime = System.currentTimeMillis();
                float[] probabilities = experimentRunner.runExperiment(shots, threads, handler, probabilityMode > 0);
                if (qv.shouldStop) {
                    return;
                }
                Complex[] tempStateVector = null;
                if (shots == 0 || probabilityMode > 0) {
                    tempStateVector = experimentRunner.getStateVector();
                }
                final Complex[] stateVector = tempStateVector;
                long time = System.currentTimeMillis() - startTime;
                String t = time / 1000 + "s " + time % 1000 + "ms";
                runOnUiThread(() -> {
                    AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                    adb.setCancelable(false);
                    adb.setPositiveButton("OK", null);
                    adb.setNeutralButton(R.string.export_csv, (DialogInterface dialogInterface, int i) -> {
                        try {
                            Uri uri = getContentResolver().getPersistedUriPermissions().get(0).getUri();
                            DocumentFile pickedDir = DocumentFile.fromTreeUri(MainActivity.this, uri);
                            if (!pickedDir.exists()) {
                                getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                pickedDir = null;
                            }
                            StringBuilder sb = new StringBuilder();
                            DecimalFormat df = new DecimalFormat(scientific ? "0.########E0" : "0.##########", new DecimalFormatSymbols(Locale.UK));
                            for (int k = 0; k < probabilities.length; k++) {
                                if (k != 0) sb.append("\r\n");
                                sb.append(k);
                                sb.append(separator);
                                sb.append(String.format("%" + MAX_QUBITS + "s", Integer.toBinaryString(k)).replace(' ', '0'));
                                sb.append(separator);
                                sb.append(df.format(probabilities[k]));
                                if (stateVector != null) {
                                    sb.append(separator);
                                    sb.append(stateVector[k].toString(10));
                                }
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat("'results'_yyyy-MM-dd_HH-mm-ss'.csv'", Locale.UK);
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String filename = sdf.format(new Date());
                            DocumentFile newFile = pickedDir.createFile("text/csv", filename);
                            OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
                            out.write(sb.toString().getBytes());
                            out.close();
                            Snackbar.make(findViewById(R.id.parent2), filename + " \n" + getString(R.string.successfully_exported), Snackbar.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent2), R.string.choose_save_location_settings, Snackbar.LENGTH_LONG);
                            snackbar.getView().setBackgroundColor(0xffD81010);
                            snackbar.show();
                        }
                    });
                    adb.setTitle(getString(R.string.results) + ": \t" + t);
                    ScrollView scrollView = new ScrollView(MainActivity.this);
                    TableLayout tableLayout = new TableLayout(MainActivity.this);
                    tableLayout.setPadding(0, (int) QuantumView.pxFromDp(this, 10), 0, (int) QuantumView.pxFromDp(this, 10));
                    short[] measuredQubits = qv.getMeasuredQubits();
                    scrollView.addView(tableLayout);
                    adb.setView(scrollView);
                    AlertDialog ad = adb.create();
                    ad.setOnShowListener((DialogInterface dialogInterface) -> {
                        float dpWidth = ad.getWindow().getDecorView().getWidth() / displayMetrics.density;
                        Log.i("Debug", "Dialog dpwidth: " + dpWidth);
                        int decimalPoints = dpWidth > 280 ? dpWidth > 300 ? dpWidth > 365 ? dpWidth > 420 ? dpWidth > 450 ? dpWidth > 520 ? 10 : 8 : 7 : 6 : 5 : 4 : 3;
                        String decimals = new String(new char[decimalPoints]).replace("\0", "#");
                        DecimalFormat df = new DecimalFormat(stateVector == null ? "0.########" : "0." + decimals);
                        DecimalFormat sf = new DecimalFormat(stateVector == null ? "0.########" : "0." + (decimalPoints < 4 ? decimals.substring(2) : decimals.substring(3)) + "E0");
                        outerFor:
                        for (int i = 0; i < probabilities.length; i++) {
                            TableRow tr = new TableRow(MainActivity.this);
                            tr.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                            tr.setDividerDrawable(getDrawable(R.drawable.vertical_divider));
                            for (int j = 0; j < measuredQubits.length; j++) {
                                if (measuredQubits[j] < 1 && (i >> j) % 2 == 1) {
                                    continue outerFor;
                                }
                            }
                            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                            params.setMargins((int) QuantumView.pxFromDp(this, dpWidth < 330 || MAX_QUBITS > 7 ? 3 : 6), 0, (int) QuantumView.pxFromDp(this, dpWidth < 330 || MAX_QUBITS > 7 ? 3 : 6), 0);
                            AppCompatTextView[] textView = new AppCompatTextView[]{
                                    new AppCompatTextView(MainActivity.this),
                                    new AppCompatTextView(MainActivity.this),
                                    new AppCompatTextView(MainActivity.this)};
                            textView[0].setTypeface(Typeface.DEFAULT_BOLD);
                            textView[0].setText(String.format("%" + MAX_QUBITS + "s", Integer.toBinaryString(i)).replace(' ', '0'));
                            textView[0].setLayoutParams(params);
                            textView[1].setTypeface(Typeface.MONOSPACE);
                            if (probabilities[i] * Math.pow(10, decimalPoints) < 1 && probabilities[i] != 0)
                                textView[1].setText(sf.format(probabilities[i]));
                            else
                                textView[1].setText(df.format(probabilities[i]));
                            textView[1].setLayoutParams(params);
                            textView[2].setTypeface(Typeface.MONOSPACE);
                            textView[2].setText((stateVector != null ? stateVector[i].toString(decimalPoints) : ""));
                            textView[2].setLayoutParams(params);
                            if (dpWidth < 330 && stateVector != null) {
                                textView[0].setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                textView[1].setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                textView[2].setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            }
                            tr.addView(textView[0]);
                            tr.addView(textView[1]);
                            tr.addView(textView[2]);
                            tableLayout.addView(tr);

                            try {
                                progressDialog.cancel();
                            } catch (Exception e) {
                                progressDialog = null;
                                e.printStackTrace();
                            }
                        }
                    });
                    ad.show();
                });
            });
            expThread.start();
        }), 200);

        int help_shown = pref.getInt("help_shown", 0);
        if (help_shown < 5) {
            Snackbar.make(findViewById(R.id.parent2), R.string.click_to_start, Snackbar.LENGTH_LONG).show();
            pref.edit().putInt("help_shown", ++help_shown).apply();
        }

        drawerLayout = findViewById(R.id.activity_main2);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.yes, R.string.no);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.nv);
        View v = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.navigation_header, null, false);
        try {
            ((TextView) v.findViewById(R.id.version)).setText(getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception e) {
            e.printStackTrace();
            ((TextView) v.findViewById(R.id.version)).setText("?.?.?");
        }
        navigationView.addHeaderView(v);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                tintSystemBars(Color.parseColor("#171717"),
                        ContextCompat.getColor(MainActivity.this, R.color.ic_launcher_background), 400);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                tintSystemBars(ContextCompat.getColor(MainActivity.this, R.color.ic_launcher_background),
                        Color.parseColor("#171717"), 500);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }

            private void tintSystemBars(int initial, int fin, int duration) {

                ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
                anim.addUpdateListener((ValueAnimator animation) -> {
                    float position = animation.getAnimatedFraction();
                    int blended = blendColors(initial, fin, position);
                    getWindow().setStatusBarColor(blended);
                    blended = blendColors(initial, fin, position);
                    ColorDrawable background = new ColorDrawable(blended);
                    getSupportActionBar().setBackgroundDrawable(background);
                });

                anim.setDuration(duration).start();
            }

            private int blendColors(int from, int to, float ratio) {
                final float inverseRatio = 1f - ratio;

                final float r = Color.red(to) * ratio + Color.red(from) * inverseRatio;
                final float g = Color.green(to) * ratio + Color.green(from) * inverseRatio;
                final float b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio;

                return Color.rgb((int) r, (int) g, (int) b);
            }
        });
        navigationView.setNavigationItemSelectedListener((MenuItem item) -> {
            new Handler().postDelayed(() -> {
                switch (item.getItemId()) {
                    case R.id.probability:
                        navigationView.getMenu().getItem(navigationView.getMenu().size() - 2).setIcon(ContextCompat.getDrawable(MainActivity.this, probabilityMode > 0 ? R.drawable.alpha_p_circle_outline : R.drawable.alpha_p_circle));
                        probabilityMode = 1 - probabilityMode;
                        break;
                    case R.id.prefs:
                        startActivity(new Intent(MainActivity.this, PreferenceActivity.class));
                        break;
                    case R.id.help:
                        startActivity(new Intent(MainActivity.this, HelpActivity.class));
                        break;
                    case R.id.clear:
                        saved = true;
                        qv.clearScreen();
                        break;
                    case R.id.matrix:
                        startActivity(new Intent(MainActivity.this, MatrixEditorActivity.class));
                        break;
                    default:
                }
            }, 200);
            drawerLayout.closeDrawer(GravityCompat.START);
            switch (item.getItemId()) {
                case R.id.probability:
                case R.id.prefs:
                case R.id.help:
                case R.id.clear:
                case R.id.matrix:
                    return true;
                default:
                    return false;
            }
        });
    }

    public void displayBlochSphere() {
        blochSpherePos = 0;
        Qubit q2 = new Qubit();
        for (VisualOperator v : qv.getOperators()) {
            for (int q : v.getQubitIDs())
                if (q == blochSpherePos && !v.isMultiQubit()) {
                    q2 = v.operateOn(new Qubit[]{q2})[0];
                    break;
                }
        }
        BlochSphereView blochSphereView = new BlochSphereView(this);
        blochSphereView.setQBit(q2);
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getString(R.string.bloch_sphere) + ": q1");
        adb.setPositiveButton("OK", null);
        adb.setNegativeButton(R.string.next_qubit, null);
        adb.setNeutralButton(R.string.previous_qubit, null);
        adb.setCancelable(false);
        adb.setView(blochSphereView);
        AlertDialog d = adb.create();
        d.setOnShowListener((DialogInterface dialogInterface) -> {
            d.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener((View view) -> {
                if (++blochSpherePos >= qv.getDisplayedQubits()) blochSpherePos = 0;
                Qubit q3 = new Qubit();
                for (VisualOperator v : qv.getOperators()) {
                    for (int q : v.getQubitIDs())
                        if (q == blochSpherePos && !v.isMultiQubit()) {
                            q3 = v.operateOn(new Qubit[]{q3})[0];
                            break;
                        }
                }
                blochSphereView.setQBit(q3);
                d.setTitle(getString(R.string.bloch_sphere) + ": q" + (blochSpherePos + 1));

            });
            d.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener((View view) -> {
                if (--blochSpherePos < 0) blochSpherePos = qv.getDisplayedQubits() - 1;
                Qubit q3 = new Qubit();
                for (VisualOperator v : qv.getOperators()) {
                    for (int q : v.getQubitIDs())
                        if (q == blochSpherePos && !v.isMultiQubit()) {
                            q3 = v.operateOn(new Qubit[]{q3})[0];
                            break;
                        }
                }
                blochSphereView.setQBit(q3);
                d.setTitle(getString(R.string.bloch_sphere) + ": q" + (blochSpherePos + 1));
            });
        });
        d.show();
    }

    public void showAddGateDialog(float posx, float posy) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        final VisualOperator v;
        if ((v = qv.whichGate(posx, posy)) != null) {
            adb.setTitle(R.string.select_action);
            adb.setNegativeButton(R.string.delete_gate, (DialogInterface dialogInterface, int i) -> {
                qv.deleteGateAt(posx, posy);
                saved = false;
            });
            adb.setPositiveButton(R.string.edit_gate, null);
            adb.setNeutralButton(R.string.cancel, null);
        }
        adb.setCancelable(true);

        final AlertDialog d = adb.create();

        Runnable r = () -> {
            final LinkedList<VisualOperator> operators = new LinkedList<>();
            final LinkedList<String> operatorNames = new LinkedList<>();
            new Thread(() -> {
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
                            if (file.getName().endsWith(VisualOperator.FILE_EXTENSION_LEGACY)) {
                                ObjectInputStream oi = new ObjectInputStream(getContentResolver().openInputStream(file.getUri()));
                                VisualOperator m = (VisualOperator) oi.readObject();
                                oi.close();
                                operatorNames.add(m.getName());
                                operators.add(m);
                            } else if (file.getName().endsWith(VisualOperator.FILE_EXTENSION)) {
                                BufferedReader in = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(file.getUri())));
                                StringBuilder total = new StringBuilder();
                                for (String line; (line = in.readLine()) != null; ) {
                                    total.append(line).append('\n');
                                }
                                String json = total.toString();
                                VisualOperator m = VisualOperator.fromJSON(new JSONObject(json));
                                operatorNames.add(m.getName());
                                operators.add(m);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IndexOutOfBoundsException ie) {
                    Log.i("GateAdder", "Probably no home selected");
                } catch (Exception e) {
                    Log.e("GateAdder", "Some error has happened :(");
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> {
                Looper.prepare();
                final View mainView = MainActivity.this.getLayoutInflater().inflate(R.layout.gate_selector, null);
                final Spinner gateType = mainView.findViewById(R.id.type_spinner);
                final Spinner filter = mainView.findViewById(R.id.filter_spinner);
                final Spinner gateName = mainView.findViewById(R.id.gate_name_spinner);
                final ConstraintLayout subLayout = mainView.findViewById(R.id.sub_layout);
                final ConstraintLayout rotLayout = mainView.findViewById(R.id.rot_layout);
                final SeekBar rx = mainView.findViewById(R.id.rx);
                final SeekBar rz = mainView.findViewById(R.id.rz);
                final TextView rxText = mainView.findViewById(R.id.rx_text);
                final TextView rzText = mainView.findViewById(R.id.rz_text);
                final CheckBox fixedValues = mainView.findViewById(R.id.fixed_values);
                final SeekBar[] qX = new SeekBar[]{
                        mainView.findViewById(R.id.order_first),
                        mainView.findViewById(R.id.order_second),
                        mainView.findViewById(R.id.order_third),
                        mainView.findViewById(R.id.order_fourth)};
                final TextView[] tX = new TextView[]{
                        mainView.findViewById(R.id.qtext1),
                        mainView.findViewById(R.id.qtext2),
                        mainView.findViewById(R.id.qtext3),
                        mainView.findViewById(R.id.qtext4)};
                final LinkedList<String> mGates = VisualOperator.getPredefinedGateNames();
                Collections.sort(mGates);
                ArrayAdapter<String> gateAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                gateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                runOnUiThread(() -> {
                    gateName.setAdapter(gateAdapter);
                    for (int i = 0; i < qX.length; i++) {
                        final int loop_pos = i;
                        qX[i].setMax(qv.getDisplayedQubits() - 1);
                        qX[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int j, boolean b) {
                                tX[loop_pos].setText("q" + (j + 1));
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }
                        });
                    }
                    if (v != null) {
                        if (!v.isRotation()) {
                            for (int i = 0; i < v.getQubitIDs().length; i++) {
                                qX[i].setProgress(v.getQubitIDs()[i]);
                                int pos = mGates.indexOf(v.getName());
                                if (pos >= 0) gateName.setSelection(pos);
                            }
                            switch (v.getQubitIDs().length) {
                                case 4:
                                    qX[3].setVisibility(View.VISIBLE);
                                    tX[3].setVisibility(View.VISIBLE);
                                case 3:
                                    qX[2].setVisibility(View.VISIBLE);
                                    tX[2].setVisibility(View.VISIBLE);
                                case 2:
                                    qX[1].setVisibility(View.VISIBLE);
                                    tX[1].setVisibility(View.VISIBLE);
                                default:
                            }
                            switch (v.getQubitIDs().length) {
                                case 1:
                                    qX[1].setVisibility(View.INVISIBLE);
                                    tX[1].setVisibility(View.INVISIBLE);
                                case 2:
                                    qX[2].setVisibility(GONE);
                                    tX[2].setVisibility(GONE);
                                case 3:
                                    qX[3].setVisibility(GONE);
                                    tX[3].setVisibility(GONE);
                                default:
                            }
                        }
                    } else {
                        qX[0].setProgress(qv.whichQubit(posy));
                    }
                    gateType.post(() ->
                            gateType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    if (i == 0) {
                                        filter.setEnabled(true);
                                        subLayout.setVisibility(VISIBLE);
                                        rotLayout.setVisibility(GONE);
                                        if (filter.getSelectedItemPosition() == 0) {
                                            ArrayAdapter<String> gateAdapter =
                                                    new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                                            gateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            gateName.setAdapter(gateAdapter);
                                        } else {
                                            LinkedList<String> mGates = VisualOperator.getPredefinedGateNames(filter.getSelectedItemPosition() == 1);
                                            Collections.sort(mGates);
                                            ArrayAdapter<String> gateAdapter =
                                                    new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                                            gateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            gateName.setAdapter(gateAdapter);
                                        }
                                    } else if (i == 1) {
                                        if (operators.size() > 0) {
                                            filter.setEnabled(false);
                                            filter.setSelection(0);
                                            subLayout.setVisibility(VISIBLE);
                                            rotLayout.setVisibility(GONE);
                                            ArrayAdapter<String> gateAdapter =
                                                    new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, operatorNames);
                                            gateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            gateName.setAdapter(gateAdapter);
                                        } else {
                                            gateType.setSelection(0);
                                            Toast t = Toast.makeText(MainActivity.this, R.string.no_user_gates, Toast.LENGTH_SHORT);
                                            t.setGravity(Gravity.CENTER, 0, 0);
                                            t.show();
                                        }
                                    } else if (i == 2) {
                                        for (int k = 1; k < qX.length; k++) {
                                            qX[k].setVisibility(GONE);
                                            tX[k].setVisibility(GONE);
                                        }
                                        subLayout.setVisibility(GONE);
                                        rotLayout.setVisibility(VISIBLE);
                                        filter.setSelection(0);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                }
                            }));
                    filter.post(() ->
                            filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    if (gateType.getSelectedItemPosition() == 0) {
                                        if (i == 0) {
                                            ArrayAdapter<String> gateAdapter =
                                                    new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                                            gateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            gateName.setAdapter(gateAdapter);
                                        } else {
                                            LinkedList<String> mGates = VisualOperator.getPredefinedGateNames(i == 1);
                                            Collections.sort(mGates);
                                            ArrayAdapter<String> gateAdapter =
                                                    new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mGates);

                                            gateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            gateName.setAdapter(gateAdapter);
                                        }
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            }));
                    mainView.postDelayed(() -> {
                        fixedValues.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b) ->
                                new Handler().postDelayed(() -> {
                                    if (b) {
                                        rx.setMax(importantAngles.length - 1);
                                        rz.setMax(importantAngles2PI.length - 1);
                                        rx.setProgress(0);
                                        rz.setProgress(0);
                                    } else {
                                        rx.setMax(3141);
                                        rz.setMax(6282);
                                        rx.setProgress(0);
                                        rz.setProgress(0);
                                    }
                                }, 100));
                        DecimalFormat df = new DecimalFormat("0.000");
                        rx.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                if (fixedValues.isChecked()) {
                                    rxText.setText(String.format("\u03B8 %-4s", importantAngleNames[i]));
                                } else {
                                    rxText.setText("\u03B8 " + df.format(i / 1000f));
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        rz.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                if (fixedValues.isChecked()) {
                                    rzText.setText(String.format("\u03C6 %-4s", importantAngleNames2PI[i]));
                                } else {
                                    rzText.setText("\u03C6 " + df.format(i / 1000f));
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        if (v != null && v.isRotation()) {
                            gateType.setSelection(2);
                            qX[0].setProgress(v.getQubitIDs()[0]);
                            rx.setProgress((int) Math.abs(v.getAngles()[0] * 1000));
                            rz.setProgress((int) Math.abs(v.getAngles()[1] * 1000));
                            if (v.getAngles()[0] < 0 || v.getAngles()[1] < 0) {
                                ((CheckBox) mainView.findViewById(R.id.hermitianConjugate)).setChecked(true);
                            }
                        } else {
                            subLayout.setVisibility(VISIBLE);
                        }
                        gateName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                LinkedList<String> gates = VisualOperator.getPredefinedGateNames(filter.getSelectedItemPosition() == 1);
                                Collections.sort(gates);
                                ArrayAdapter<String> adapter =
                                        new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, filter.getSelectedItemPosition() == 0 ? mGates : gates);

                                int qubits = gateType.getSelectedItemPosition() == 0 ? VisualOperator.findGateByName(adapter.getItem(i)).getQubits() : operators.get(i).getQubits();
                                switch (qubits) {
                                    case 4:
                                        qX[3].setVisibility(View.VISIBLE);
                                        tX[3].setVisibility(View.VISIBLE);
                                    case 3:
                                        qX[2].setVisibility(View.VISIBLE);
                                        tX[2].setVisibility(View.VISIBLE);
                                    case 2:
                                        qX[1].setVisibility(View.VISIBLE);
                                        tX[1].setVisibility(View.VISIBLE);
                                    default:
                                }
                                switch (qubits) {
                                    case 1:
                                        qX[1].setVisibility(View.INVISIBLE);
                                        tX[1].setVisibility(View.INVISIBLE);
                                    case 2:
                                        qX[2].setVisibility(GONE);
                                        tX[2].setVisibility(GONE);
                                    case 3:
                                        qX[3].setVisibility(GONE);
                                        tX[3].setVisibility(GONE);
                                    default:
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });
                        mainView.findViewById(R.id.cancel).setOnClickListener((View view) -> d.cancel());
                        mainView.findViewById(R.id.ok).setOnClickListener((View view) -> {
                            if (gateType.getSelectedItemPosition() != 2) {
                                if (operators.size() == 0 && gateType.getSelectedItemPosition() != 0) {
                                    return;
                                }
                                LinkedList<String> gates = VisualOperator.getPredefinedGateNames(filter.getSelectedItemPosition() == 1);
                                Collections.sort(gates);
                                ArrayAdapter<String> adapter =
                                        new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, filter.getSelectedItemPosition() == 0 ? mGates : gates);

                                int qubits = gateType.getSelectedItemPosition() == 0
                                        ? VisualOperator.findGateByName(adapter.getItem(gateName.getSelectedItemPosition())).getQubits()
                                        : operators.get(gateName.getSelectedItemPosition()).getQubits();
                                int[] quids = new int[qubits];
                                for (int i = 0; i < qubits; i++) {
                                    quids[i] = qX[i].getProgress();
                                }
                                for (int i = 0; i < qubits; i++) {
                                    for (int j = i + 1; j < qubits; j++) {
                                        if (quids[i] == quids[j]) {
                                            d.cancel();
                                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent2), R.string.use_different_qubits, Snackbar.LENGTH_LONG);
                                            snackbar.getView().setBackgroundColor(0xffD81010);
                                            snackbar.show();
                                            return;
                                        }
                                    }
                                }
                                saved = false;
                                VisualOperator gate = gateType.getSelectedItemPosition() == 0
                                        ? VisualOperator.findGateByName((String) gateName.getSelectedItem()).copy()
                                        : operators.get(gateName.getSelectedItemPosition()).copy();
                                if (((CheckBox) mainView.findViewById(R.id.hermitianConjugate)).isChecked())
                                    gate.hermitianConjugate();
                                if (v == null)
                                    qv.addGate(quids, gate);
                                else
                                    qv.replaceGateAt(quids, gate, posx, posy);
                                d.cancel();
                            } else {
                                double theta = fixedValues.isChecked() ? importantAngles[rx.getProgress()] : rx.getProgress() / 1000f;
                                double phi = fixedValues.isChecked() ? importantAngles2PI[rz.getProgress()] : rz.getProgress() / 1000f;
                                VisualOperator gate = new VisualOperator(theta, phi);
                                if (((CheckBox) mainView.findViewById(R.id.hermitianConjugate)).isChecked())
                                    gate.hermitianConjugate();
                                if (v == null)
                                    qv.addGate(new int[]{qX[0].getProgress()}, gate);
                                else
                                    qv.replaceGateAt(new int[]{qX[0].getProgress()}, gate, posx, posy);
                                d.cancel();
                            }
                        });
                    }, 100);
                    d.setTitle(R.string.select_gate);
                    try {
                        d.setView(mainView);
                        d.setContentView(mainView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }).start();
        };

        if (v != null) {
            d.setOnShowListener((DialogInterface dialog) -> {
                final Button multi = d.getButton(AlertDialog.BUTTON_POSITIVE);
                multi.setOnClickListener((View view) -> runOnUiThread(r));
            });
        } else {
            runOnUiThread(r);
        }
        d.show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item))
            return true;
        else {
            switch (item.getItemId()) {
                case R.id.bloch:
                    displayBlochSphere();
                    break;
                case R.id.save:
                    if (qv.getOperators().size() < 1) {
                        Snackbar.make(findViewById(R.id.parent2), getString(R.string.no_gates), Snackbar.LENGTH_LONG).show();
                        return true;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("'exp'_yyyy-MM-dd_HHmmss'" + QuantumView.FILE_EXTENSION + "'", Locale.UK);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String filename = sdf.format(new Date());
                    AlertDialog.Builder adb = new AlertDialog.Builder(this);
                    adb.setTitle(R.string.select_filename);
                    LinearLayout container = new LinearLayout(this);
                    container.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins((int) QuantumView.pxFromDp(this, 20), 0, (int) QuantumView.pxFromDp(this, 20), 0);
                    EditText editText = new EditText(this);
                    InputFilter[] filterArray = new InputFilter[]{new InputFilter.LengthFilter(32), (CharSequence source, int start, int end, Spanned dest, int sta, int en) -> {
                        if (source != null && "/\\:?;!~\'\",^ˇ|+<>[]{}".contains(("" + source))) {
                            return "";
                        }
                        return null;
                    }
                    };
                    editText.setText(qv.name);
                    editText.setFilters(filterArray);
                    editText.setHint(filename);
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT);
                    container.addView(editText, params);
                    adb.setView(container);
                    adb.setPositiveButton(R.string.save, (DialogInterface dialogInterface, int i) -> {
                        try {
                            qv.name = editText.getText().toString().length() < 1 ? filename : editText.getText().toString();
                            if (!qv.name.endsWith(QuantumView.FILE_EXTENSION)) {
                                qv.name += QuantumView.FILE_EXTENSION;
                            }
                            Uri uri = getContentResolver().getPersistedUriPermissions().get(0).getUri();
                            DocumentFile pickedDir = DocumentFile.fromTreeUri(MainActivity.this, uri);
                            if (!pickedDir.exists()) {
                                getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                pickedDir = null;
                            }
                            DocumentFile newFile = pickedDir.findFile(qv.name) == null ? pickedDir.createFile("application/octet-stream", qv.name) : pickedDir.findFile(qv.name);
                            OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
                            out.write(qv.exportGates(qv.name).toString(2).getBytes());
                            out.flush();
                            out.close();
                            Snackbar.make(findViewById(R.id.parent2), getString(R.string.experiment_saved) + " \n" + qv.name, Snackbar.LENGTH_LONG).show();
                        } catch (IndexOutOfBoundsException iout) {
                            iout.printStackTrace();
                            Snackbar.make(findViewById(R.id.parent2), R.string.choose_save_location, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.select, (View view2) ->
                                            startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 43)).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent2), R.string.unknown_error, Snackbar.LENGTH_LONG);
                            snackbar.getView().setBackgroundColor(0xffD81010);
                            snackbar.show();
                        }
                        saved = true;
                    });
                    adb.setNeutralButton(R.string.cancel, null);
                    adb.show();
                    break;
                default:

            }

            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK && requestCode == 42) {
            Uri uri = resultData.getData();
            DocumentFile pickedFile = DocumentFile.fromSingleUri(this, uri);
            try {
                if (pickedFile.getName().endsWith(QuantumView.FILE_EXTENSION_LEGACY)) {
                    Object obj = new ObjectInputStream(getContentResolver().openInputStream(pickedFile.getUri())).readObject();
                    if (!qv.importGatesLegacy(obj, pickedFile.getName())) {
                        throw new Exception("Maybe empty gate sequence?");
                    } else {
                        saved = true;
                        Snackbar.make(findViewById(R.id.parent2), R.string.successfully_imported, Snackbar.LENGTH_LONG).show();
                    }
                } else if (pickedFile.getName().endsWith(QuantumView.FILE_EXTENSION)) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(pickedFile.getUri())));
                    StringBuilder total = new StringBuilder();
                    for (String line; (line = in.readLine()) != null; ) {
                        total.append(line).append('\n');
                    }
                    String json = total.toString();
                    if (!qv.importGates(new JSONObject(json))) {
                        throw new Exception("Maybe empty gate sequence?");
                    } else {
                        saved = true;
                        Snackbar.make(findViewById(R.id.parent2), R.string.successfully_imported, Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.parent2), R.string.invalid_file, Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(0xffD81010);
                    snackbar.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar snackbar = Snackbar.make(findViewById(R.id.parent2), R.string.unknown_error, Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(0xffD81010);
                snackbar.show();
            }
        } else if (resultCode == RESULT_OK && requestCode == 43) {
            Uri treeUri = resultData.getData();
            getContentResolver().takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
            try {
                DocumentFile newFile = pickedDir.createFile("application/octet-stream", qv.name);
                OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
                out.write(qv.exportGates(qv.name).toString(2).getBytes());
                out.flush();
                out.close();
                Snackbar.make(findViewById(R.id.parent2), getString(R.string.experiment_saved) + " \n" + qv.name, Snackbar.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar snackbar = Snackbar.make(findViewById(R.id.parent2), R.string.unknown_error, Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(0xffD81010);
                snackbar.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!saved && qv.getOperators().size() > 2) {
            final Dialog d = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
            View v = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.back_dialog, null);
            v.findViewById(R.id.exit).setOnClickListener((View view) -> MainActivity.super.onBackPressed());
            v.findViewById(R.id.cancel_back).setOnClickListener((View view) -> d.cancel());
            v.findViewById(R.id.exitMain).setOnClickListener((View view) -> d.cancel());
            d.setContentView(v);
            d.setCanceledOnTouchOutside(true);
            d.setCancelable(true);
            d.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        if (probabilityMode != 1)
            probabilityMode = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("shots", "4096").equals("0") ? 2 : 0;
        navigationView.getMenu().getItem(navigationView.getMenu().size() - 2).setIcon(ContextCompat.getDrawable(MainActivity.this, probabilityMode == 0 ? R.drawable.alpha_p_circle_outline : R.drawable.alpha_p_circle));
        navigationView.getMenu().getItem(navigationView.getMenu().size() - 2).setEnabled(probabilityMode != 2);
        super.onResume();
    }
}
