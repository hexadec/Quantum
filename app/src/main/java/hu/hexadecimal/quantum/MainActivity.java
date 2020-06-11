package hu.hexadecimal.quantum;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
import androidx.lifecycle.ViewModelProvider;
import hu.hexadecimal.quantum.graphics.BlochSphereView;
import hu.hexadecimal.quantum.graphics.ExecutionProgressDialog;
import hu.hexadecimal.quantum.graphics.GateView;
import hu.hexadecimal.quantum.graphics.GraphView;
import hu.hexadecimal.quantum.graphics.QuantumView;
import hu.hexadecimal.quantum.math.Complex;
import hu.hexadecimal.quantum.math.Qubit;
import hu.hexadecimal.quantum.math.VisualOperator;
import hu.hexadecimal.quantum.tools.ExperimentRunner;
import hu.hexadecimal.quantum.tools.QuantumViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Main screen of the app
 * Let's say it's responsible for almost everything (no)
 */
public class MainActivity extends AppCompatActivity {

    QuantumView qv;
    private ExecutionProgressDialog progressDialog;
    private ExperimentRunner experimentRunner;
    private Thread expThread;

    private int probabilityMode;
    private int blochSpherePos = 0;
    private boolean hasMultiQubitGate;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private TableLayout gateHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        probabilityMode = 0;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#171717")));
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#171717"));

        LinearLayout relativeLayout = findViewById(R.id.linearLayout);
        findViewById(R.id.activity_main2).setBackgroundColor(0xffeeeeee);
        qv = new QuantumView(this);
        relativeLayout.addView(qv);
        qv.setBackgroundColor(0xffeeeeee);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        qv.setLayoutParams(new LinearLayout.LayoutParams((int) (displayMetrics.widthPixels * 2.2), qv.getRecommendedHeight()));
        qv.saved = true;
        qv.setLongClickable(true);

        FloatingActionButton fab = findViewById(R.id.fab_main);
        FloatingActionButton execute = findViewById(R.id.fab_matrix);

        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent e) {
                qv.showContextMenu();
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
                qv.highlightOperator(new int[]{-1, -1});
                if (loc[0] < rx && loc[1] < ry) return false;
                showAddGateDialog(x, y, null);
                return true;
            }
        });

        qv.setOnTouchListener((View view, MotionEvent motionEvent) -> gestureDetector.onTouchEvent(motionEvent));

        fab.setOnClickListener((View view) -> {
            Toast.makeText(MainActivity.this, R.string.choose_experiment, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 42);
            findViewById(R.id.scrollView).scrollTo(0, 0);
            findViewById(R.id.tallScrollView).scrollTo(0, 0);
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
            int oldMax = qv.getLastUsedQubit();
            if (pref.getBoolean("optimize", true)) {
                qv.optimizeCircuit();
                if (oldMax != qv.getLastUsedQubit()) {
                    qv.undoList.clear();
                    qv.redoList.clear();
                }
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
                    adb.setNegativeButton(R.string.graph, null);
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
                    tableLayout.setPadding(0, (int) UIHelper.pxFromDp(this, 10), 0, (int) UIHelper.pxFromDp(this, 10));
                    short[] measuredQubits = qv.getMeasuredQubits();
                    scrollView.addView(tableLayout);
                    adb.setView(scrollView);
                    AlertDialog ad = adb.create();
                    progressDialog.setProgress(experimentRunner.status, shots);
                    ad.setOnShowListener((DialogInterface dialogInterface) -> {
                        float dpWidth = ad.getWindow().getDecorView().getWidth() / displayMetrics.density;
                        Log.i("Debug", "Dialog dpwidth: " + dpWidth);
                        int decimalPoints = dpWidth > 280 ? dpWidth > 300 ? dpWidth > 365 ? dpWidth > 420 ? dpWidth > 450 ? dpWidth > 520 ? 10 : 8 : 7 : 6 : 5 : 4 : 3;
                        String decimals = new String(new char[decimalPoints]).replace("\0", "#");
                        NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
                        NumberFormat nf2 = NumberFormat.getInstance(Locale.US);
                        DecimalFormat df = (DecimalFormat) nf1;
                        DecimalFormat sf = (DecimalFormat) nf2;
                        df.applyPattern(stateVector == null ? "0.########" : "0." + decimals);
                        sf.applyPattern(stateVector == null ? "0.########" : "0." + (decimalPoints < 4 ? decimals.substring(2) : decimals.substring(3)) + "E0");
                        outerFor:
                        for (int i = 0; i < probabilities.length; i++) {
                            if (shots == 1 && probabilities[i] == 0) {
                                continue;
                            }
                            if (probabilities.length > Math.pow(2, 7) && probabilities[i] == 0) {
                                continue;
                            }
                            TableRow tr = new TableRow(MainActivity.this);
                            tr.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                            tr.setDividerDrawable(getDrawable(R.drawable.vertical_divider));
                            for (int j = 0; j < measuredQubits.length; j++) {
                                if (measuredQubits[j] < 1 && (i >> j) % 2 == 1) {
                                    continue outerFor;
                                }
                            }
                            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                            params.setMargins((int) UIHelper.pxFromDp(this, dpWidth < 330 || MAX_QUBITS > 7 ? 3 : 6), 0, (int) UIHelper.pxFromDp(this, dpWidth < 330 || MAX_QUBITS > 7 ? 3 : 6), 0);
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
                        }

                        try {
                            progressDialog.cancel();
                        } catch (Exception e) {
                            progressDialog = null;
                            e.printStackTrace();
                        }
                        ad.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener((View v) -> {
                            View layout = getLayoutInflater().inflate(R.layout.graph_layout, null);
                            int size = (int) Math.ceil(Math.pow(2, qv.getLastUsedQubit() + 1));
                            ((GraphView) layout.findViewById(R.id.graphView)).setData(probabilities, size == 0 ? 2 : size);
                            ((Button) layout.findViewById(R.id.closeButton)).setOnClickListener((View button) -> {
                                ad.dismiss();
                            });
                            ad.setContentView(layout);
                        });
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
                        navigationView.getMenu().getItem(navigationView.getMenu().size() - 2).setIcon(ContextCompat.getDrawable(MainActivity.this, probabilityMode > 0 ? R.drawable.alpha_s_box_outline : R.drawable.alpha_s_box));
                        probabilityMode = 1 - probabilityMode;
                        break;
                    case R.id.prefs:
                        startActivity(new Intent(MainActivity.this, PreferenceActivity.class));
                        break;
                    case R.id.help:
                        startActivity(new Intent(MainActivity.this, HelpActivity.class));
                        break;
                    case R.id.clear:
                        UIHelper.clearScreen(qv, MainActivity.this);
                        break;
                    case R.id.matrix:
                        startActivity(new Intent(MainActivity.this, MatrixEditorActivity.class));
                        break;
                    case R.id.openqasm:
                        UIHelper.saveFileUI(qv, MainActivity.this, true);
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
        gateHolder = findViewById(R.id.gate_view_holder);
        gateHolder.postDelayed(() -> {
            int navWidth = findViewById(R.id.nvParent).getWidth();
            setUpShortcuts(gateHolder, pref, displayMetrics, getResources().getConfiguration(), navWidth);
        }, 300);

        qv.postDelayed(() -> {
            QuantumViewModel model = new ViewModelProvider(MainActivity.this).get(QuantumViewModel.class);
            model.get().observe(MainActivity.this, data -> {
                try {
                    qv.setData(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }, 200);
    }

    public void displayBlochSphere() {
        blochSpherePos = 0;
        hasMultiQubitGate = false;
        Qubit q2 = new Qubit();
        outer:
        for (VisualOperator v : (LinkedList<VisualOperator>) qv.getOperators()) {
            for (int q : v.getQubitIDs())
                if (q == blochSpherePos) {
                    if (v.isMultiQubit()) {
                        hasMultiQubitGate = true;
                        break outer;
                    }
                    q2 = v.operateOn(new Qubit[]{q2})[0];
                    break;
                }
        }
        BlochSphereView blochSphereView = new BlochSphereView(this);
        blochSphereView.setQBit(q2, hasMultiQubitGate);
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
                outer:
                for (VisualOperator v : (LinkedList<VisualOperator>) qv.getOperators()) {
                    hasMultiQubitGate = false;
                    for (int q : v.getQubitIDs())
                        if (q == blochSpherePos) {
                            if (v.isMultiQubit()) {
                                hasMultiQubitGate = true;
                                break outer;
                            }
                            q3 = v.operateOn(new Qubit[]{q3})[0];
                            break;
                        }
                }
                blochSphereView.setQBit(q3, hasMultiQubitGate);
                d.setTitle(getString(R.string.bloch_sphere) + ": q" + (blochSpherePos + 1));

            });
            d.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener((View view) -> {
                if (--blochSpherePos < 0) blochSpherePos = qv.getDisplayedQubits() - 1;
                Qubit q3 = new Qubit();
                outer:
                for (VisualOperator v : (LinkedList<VisualOperator>) qv.getOperators()) {
                    hasMultiQubitGate = false;
                    for (int q : v.getQubitIDs())
                        if (q == blochSpherePos) {
                            if (v.isMultiQubit()) {
                                hasMultiQubitGate = true;
                                break outer;
                            }
                            q3 = v.operateOn(new Qubit[]{q3})[0];
                            break;
                        }
                }
                blochSphereView.setQBit(q3, hasMultiQubitGate);
                d.setTitle(getString(R.string.bloch_sphere) + ": q" + (blochSpherePos + 1));
            });
        });
        d.show();
    }

    public void showAddGateDialog(float posx, float posy, VisualOperator vo) {
        Dialog d = new Dialog(MainActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
        final VisualOperator prevOperator;

        if (vo != null) {
            vo.setQubitIDs(new int[]{0});
            prevOperator = vo;
        } else if ((prevOperator = qv.whichGate(posx, posy)) != null) {
            d.setTitle(R.string.select_action);
            View layout = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.gate_action_selector, null);
            new UIHelper().applyActions(MainActivity.this, qv, prevOperator, posx, posy, d, layout);
            d.setContentView(layout);
        }

        Runnable r = () -> {
            new UIHelper().runnableForGateSelection(MainActivity.this, qv, prevOperator, posx, posy, d);
        };
        d.setCancelable(true);
        d.setCanceledOnTouchOutside(true);

        if (prevOperator != null && vo == null) {

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
                    UIHelper.saveFileUI(qv, this, false);
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
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.parent2), R.string.fileformat_not_supported, Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(0xffD81010);
                    snackbar.show();
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
                        qv.saved = true;
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
            UIHelper.saveFileActivityResult(treeUri, this, qv, false);
        } else if (resultCode == RESULT_OK && requestCode == 44) {
            Uri treeUri = resultData.getData();
            getContentResolver().takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            UIHelper.saveFileActivityResult(treeUri, this, qv, true);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else if (!qv.saved && qv.getOperators().size() > 2) {
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
        navigationView.getMenu().getItem(navigationView.getMenu().size() - 2).setIcon(ContextCompat.getDrawable(MainActivity.this, probabilityMode == 0 ? R.drawable.alpha_s_box_outline : R.drawable.alpha_s_box));
        navigationView.getMenu().getItem(navigationView.getMenu().size() - 2).setEnabled(probabilityMode != 2);
        findViewById(R.id.gate_view_holder).setVisibility(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enable_shortcuts", true) ? VISIBLE : GONE);
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            View v = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.navigation_header, null, false);
            ((TextView) v.findViewById(R.id.version)).setText(getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName);
            navigationView.removeHeaderView(navigationView.getHeaderView(0));
            navigationView.addHeaderView(v);
            if (gateHolder != null)
                new Handler().postDelayed(() -> {
                    setUpShortcuts(gateHolder, PreferenceManager.getDefaultSharedPreferences(this), getResources().getDisplayMetrics(), newConfig, findViewById(R.id.nvParent).getWidth());
                }, 200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_Z && event.isCtrlPressed()) {
            //UNDO
            qv.undo();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_Y && event.isCtrlPressed()) {
            //REDO
            qv.redo();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_D && event.isCtrlPressed()) {
            //CLEAR TIMELINE
            UIHelper.clearScreen(qv, MainActivity.this);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_M && event.isCtrlPressed()) {
            //CHANGE STATEVECTOR MODE
            navigationView.getMenu().getItem(navigationView.getMenu().size() - 2).setIcon(ContextCompat.getDrawable(MainActivity.this, probabilityMode > 0 ? R.drawable.alpha_s_box_outline : R.drawable.alpha_s_box));
            probabilityMode = 1 - probabilityMode;
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_S && event.isCtrlPressed()) {
            //SAVE
            UIHelper.saveFileUI(qv, this, false);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_R && event.isCtrlPressed()) {
            //RUN
            findViewById(R.id.fab_matrix).callOnClick();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_E && event.isCtrlPressed()) {
            //EXPORT
            UIHelper.saveFileUI(qv, this, true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_A && event.isCtrlPressed()) {
            //ADD GATE
            showAddGateDialog(0, 0, null);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_B && event.isCtrlPressed()) {
            //SHOW BLOCH SPHERE
            displayBlochSphere();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_O && event.isCtrlPressed()) {
            //OPEN FILE
            findViewById(R.id.fab_main).callOnClick();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.isCtrlPressed()) {
            //SCROLL RIGHT
            findViewById(R.id.scrollView).scrollBy((int) UIHelper.pxFromDp(this, 250), 0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.isCtrlPressed()) {
            //SCROLL LEFT
            findViewById(R.id.scrollView).scrollBy((int) -UIHelper.pxFromDp(this, 250), 0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.isCtrlPressed()) {
            //SCROLL UP
            findViewById(R.id.tallScrollView).scrollBy(0, (int) -UIHelper.pxFromDp(this, 150));
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.isCtrlPressed()) {
            //SCROLL DOWN
            findViewById(R.id.tallScrollView).scrollBy(0, (int) UIHelper.pxFromDp(this, 150));
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MOVE_HOME) {
            //SCROLL HOME
            findViewById(R.id.scrollView).scrollTo(0, 0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MOVE_END) {
            //SCROLL END
            findViewById(R.id.scrollView).scrollTo(10000000, 0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PAGE_UP) {
            //SCROLL UP A LOT
            findViewById(R.id.tallScrollView).scrollBy(0, (int) -UIHelper.pxFromDp(this, 300));
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
            //SCROLL DOWN A LOT
            findViewById(R.id.tallScrollView).scrollBy(0, (int) UIHelper.pxFromDp(this, 300));
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_W) {
            //MOVE SELECTION UP
            qv.moveHighlight(0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_A) {
            //MOVE SELECTION LEFT
            qv.moveHighlight(1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_S) {
            //MOVE SELECTION DOWN
            qv.moveHighlight(2);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_D) {
            //MOVE SELECTION RIGHT
            qv.moveHighlight(3);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_E) {
            //OPEN SELECTION
            RectF rect = qv.getRectInGrid(qv.getHighlight());
            if (rect != null)
                showAddGateDialog(rect.centerX(), rect.centerY(), null);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        QuantumViewModel model = new ViewModelProvider(MainActivity.this).get(QuantumViewModel.class);
        model.set(qv.getData());
        super.onDestroy();
    }

    public void setUpShortcuts(TableLayout gateHolder, SharedPreferences pref, DisplayMetrics displayMetrics, Configuration config, int navWidth) {
        gateHolder.removeAllViews();
        LinkedList<VisualOperator> operators = VisualOperator.getPredefinedGates(false);
        operators.add(0, new VisualOperator(0f, 0f));
        operators.add(0, new VisualOperator(0f, 0f, 0f));
        operators.add(0, new VisualOperator(2, false));
        TableRow tr = new TableRow(MainActivity.this);
        int gwMargin = 3;
        float dpWidth = UIHelper.dpFromPx(MainActivity.this, navWidth);
        if (dpWidth <= 0) dpWidth = 256;
        int margin = dpWidth > 270 ? dpWidth > 300 ? 15 : 10 : 5;
        int gateWidth = (int) (dpWidth - gwMargin * 10 - 2 * (margin + 1) - GateView.PADDING_DP * 5) / 10;
        for (int i = 0; i < operators.size(); i++) {
            GateView gw = new GateView(MainActivity.this, operators.get(i), gateWidth);
            TableRow.LayoutParams params = new TableRow.LayoutParams(gw.minSize(), gw.minSize());
            params.setMargins((int) UIHelper.pxFromDp(MainActivity.this, gwMargin),
                    (int) UIHelper.pxFromDp(MainActivity.this, gwMargin),
                    (int) UIHelper.pxFromDp(MainActivity.this, gwMargin),
                    (int) UIHelper.pxFromDp(MainActivity.this, gwMargin));
            gw.setLayoutParams(params);
            gw.setOnClickListener((View view) -> {
                showAddGateDialog(-1, -1, gw.visualOperator);
                if (pref.getBoolean("shortcuts_autoclose", true))
                    drawerLayout.closeDrawers();
            });
            tr.addView(gw);
            if ((i + 1) % 5 == 0 || i + 1 == operators.size()) {
                gateHolder.addView(tr);
                tr = new TableRow(MainActivity.this);
            }
        }
        ConstraintLayout.LayoutParams lp = ((ConstraintLayout.LayoutParams) gateHolder.getLayoutParams());
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
            lp.setMargins((int) UIHelper.pxFromDp(MainActivity.this, margin),
                    (int) UIHelper.pxFromDp(MainActivity.this, margin),
                    (int) UIHelper.pxFromDp(MainActivity.this, margin),
                    (int) UIHelper.pxFromDp(MainActivity.this, margin * 2));
        else
            lp.setMargins((int) UIHelper.pxFromDp(MainActivity.this, margin),
                    (int) UIHelper.pxFromDp(MainActivity.this, margin / 2),
                    (int) UIHelper.pxFromDp(MainActivity.this, margin),
                    (int) UIHelper.pxFromDp(MainActivity.this, margin / 2));
        gateHolder.setLayoutParams(lp);
        gateHolder.bringToFront();
    }
}
