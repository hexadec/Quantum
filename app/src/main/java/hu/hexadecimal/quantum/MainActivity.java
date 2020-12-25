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
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
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
import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
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
import hu.hexadecimal.quantum.graphics.QuantumView;
import hu.hexadecimal.quantum.graphics.ExecutionResultDialog;
import hu.hexadecimal.quantum.math.Complex;
import hu.hexadecimal.quantum.math.Qubit;
import hu.hexadecimal.quantum.math.VisualOperator;
import hu.hexadecimal.quantum.tools.ExperimentRunner;
import hu.hexadecimal.quantum.tools.QuantumViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static hu.hexadecimal.quantum.UIHelper.SNACKBAR_ERROR_COLOR;
import static hu.hexadecimal.quantum.UIHelper.STATUS_BAR_COLOR;

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

    private final int QUANTUM_VIEW_BG_COLOR = 0xffeeeeee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        probabilityMode = 0;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(STATUS_BAR_COLOR)));
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor(STATUS_BAR_COLOR));

        LinearLayout relativeLayout = findViewById(R.id.linearLayout);
        findViewById(R.id.activity_main2).setBackgroundColor(QUANTUM_VIEW_BG_COLOR);
        qv = new QuantumView(this);
        relativeLayout.addView(qv);
        qv.setBackgroundColor(QUANTUM_VIEW_BG_COLOR);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        qv.setLayoutParams(new LinearLayout.LayoutParams((int) (displayMetrics.widthPixels * 2.2), qv.getRecommendedHeight()));
        qv.saved = true;
        qv.setLongClickable(true);

        FloatingActionButton openFileFab = findViewById(R.id.fab_main);
        FloatingActionButton executeFab = findViewById(R.id.fab_matrix);

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
                executeFab.getLocationOnScreen(loc);
                qv.highlightOperator(new int[]{-1, -1});
                if (loc[0] < rx && loc[1] < ry) return false;
                if (qv.isStartRow(x) && qv.whichQubit(y) >= 0) {
                    qv.toggleIgnoredState(qv.whichQubit(y));
                } else {
                    showAddGateDialog(x, y, null);
                }
                return true;
            }
        });

        qv.setOnTouchListener((View view, MotionEvent motionEvent) -> gestureDetector.onTouchEvent(motionEvent));

        openFileFab.setOnClickListener((View view) -> {
            Toast.makeText(MainActivity.this, R.string.choose_experiment, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 42);
            findViewById(R.id.scrollView).scrollTo(0, 0);
            findViewById(R.id.tallScrollView).scrollTo(0, 0);
        });


        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        executeFab.postDelayed(() -> executeFab.setOnClickListener((View view) -> {
            //To prevent double clicking
            executeFab.setClickable(false);
            new Handler().postDelayed(() -> executeFab.setClickable(true), 500);
            if (!pref.getBoolean("measurement_toggle_help", false)) {
                AlertDialog.Builder meas_dialog_builder = new AlertDialog.Builder(MainActivity.this);
                meas_dialog_builder.setTitle(R.string.ignoring_qubit_values);
                meas_dialog_builder.setMessage(R.string.ignore_qubit_howto);
                meas_dialog_builder.setCancelable(false);
                meas_dialog_builder.setPositiveButton("OK", (DialogInterface dialog, int which) -> {
                        pref.edit().putBoolean("measurement_toggle_help", true).apply();
                });
                meas_dialog_builder.show();
            }
            final int shots = Integer.parseInt(pref.getString("shots", "4096"));
            final int threads = Integer.parseInt(pref.getString("threads", "8"));
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
                    ExecutionResultDialog executionResultDialog = new ExecutionResultDialog(MainActivity.this, stateVector, probabilities);
                    executionResultDialog.setupDialog();
                    executionResultDialog.setTitle(getString(R.string.results) + ": \t" + t);
                    AlertDialog ad = executionResultDialog.create(qv, shots, progressDialog);
                    progressDialog.setProgress(experimentRunner.status, shots);
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
        for (VisualOperator v : qv.getOperators()) {
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
                for (VisualOperator v : qv.getOperators()) {
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
                for (VisualOperator v : qv.getOperators()) {
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
        Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
        final VisualOperator prevOperator;

        if (vo != null) {
            vo.setQubitIDs(new int[]{0});
            prevOperator = vo;
        } else if ((prevOperator = qv.whichGate(posx, posy)) != null) {
            dialog.setTitle(R.string.select_action);
            View layout = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.gate_action_selector, null);
            new UIHelper().applyActions(MainActivity.this, qv, prevOperator, posx, posy, dialog, layout);
            dialog.setContentView(layout);
        }

        Runnable r = () ->
                new UIHelper().runnableForGateSelection(MainActivity.this, qv, prevOperator, posx, posy, dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        if (!(prevOperator != null && vo == null))
            runOnUiThread(r);
        dialog.show();
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
        if (resultCode == RESULT_OK && requestCode == 42 && resultData.getData() != null) {
            Uri uri = resultData.getData();
            DocumentFile pickedFile = DocumentFile.fromSingleUri(this, uri);
            try {
                if (pickedFile.getName().endsWith(QuantumView.FILE_EXTENSION)) {
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
                    int snackbarTextResId;
                    if (pickedFile.getName().endsWith(QuantumView.FILE_EXTENSION_LEGACY))
                        snackbarTextResId = R.string.fileformat_not_supported;
                    else if (pickedFile.getName().endsWith(QuantumView.OPENQASM_FILE_EXTENSION))
                        snackbarTextResId = R.string.openqasm_support_export_only;
                    else if (pickedFile.getName().endsWith(VisualOperator.FILE_EXTENSION))
                        snackbarTextResId = R.string.open_gate_files_gate_editor;
                    else
                        snackbarTextResId = R.string.invalid_file;
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.parent2), snackbarTextResId, Snackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(SNACKBAR_ERROR_COLOR);
                    ((TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text)).setSingleLine(false);
                    snackbar.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar snackbar = Snackbar.make(findViewById(R.id.parent2), R.string.unknown_error, Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(SNACKBAR_ERROR_COLOR);
                snackbar.show();
            }
        } else if (resultCode == RESULT_OK && requestCode == 43 && resultData.getData() != null) {
            Uri treeUri = resultData.getData();
            getContentResolver().takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            UIHelper.saveFileActivityResult(treeUri, this, qv, false);
        } else if (resultCode == RESULT_OK && requestCode == 44 && resultData.getData() != null) {
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
        setUpNavbar(getResources().getConfiguration());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpNavbar(newConfig);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.isCtrlPressed()) {
            // Shortcuts with control key
            switch (keyCode) {
                case KeyEvent.KEYCODE_Z:
                    //UNDO
                    qv.undo();
                    return true;
                case KeyEvent.KEYCODE_Y:
                    //REDO
                    qv.redo();
                    return true;
                case KeyEvent.KEYCODE_D:
                    //CLEAR TIMELINE
                    UIHelper.clearScreen(qv, MainActivity.this);
                    return true;
                case KeyEvent.KEYCODE_M:
                    //TOGGLE STATEVECTOR MODE
                    navigationView.getMenu().getItem(navigationView.getMenu().size() - 2).setIcon(ContextCompat.getDrawable(MainActivity.this, probabilityMode > 0 ? R.drawable.alpha_s_box_outline : R.drawable.alpha_s_box));
                    probabilityMode = 1 - probabilityMode;
                    return true;
                case KeyEvent.KEYCODE_S:
                    //SAVE
                    UIHelper.saveFileUI(qv, this, false);
                    return true;
                case KeyEvent.KEYCODE_R:
                    //RUN
                    findViewById(R.id.fab_matrix).callOnClick();
                    return true;
                case KeyEvent.KEYCODE_E:
                    //EXPORT
                    UIHelper.saveFileUI(qv, this, true);
                    return true;
                case KeyEvent.KEYCODE_A:
                    //ADD GATE
                    showAddGateDialog(0, 0, null);
                    return true;
                case KeyEvent.KEYCODE_B:
                    //SHOW BLOCH SPHERE
                    displayBlochSphere();
                    return true;
                case KeyEvent.KEYCODE_O:
                    //OPEN FILE
                    findViewById(R.id.fab_main).callOnClick();
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    //SCROLL RIGHT
                    findViewById(R.id.scrollView).scrollBy((int) UIHelper.pxFromDp(this, 250), 0);
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    //SCROLL LEFT
                    findViewById(R.id.scrollView).scrollBy((int) -UIHelper.pxFromDp(this, 250), 0);
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP:
                    //SCROLL UP
                    findViewById(R.id.tallScrollView).scrollBy(0, (int) -UIHelper.pxFromDp(this, 150));
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    //SCROLL DOWN
                    findViewById(R.id.tallScrollView).scrollBy(0, (int) UIHelper.pxFromDp(this, 150));
                    return true;
            }
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MOVE_HOME:
                    //SCROLL HOME
                    findViewById(R.id.scrollView).scrollTo(0, 0);
                    return true;
                case KeyEvent.KEYCODE_MOVE_END:
                    //SCROLL END
                    findViewById(R.id.scrollView).scrollTo(10000000, 0);
                    return true;
                case KeyEvent.KEYCODE_PAGE_UP:
                    //SCROLL UP A LOT
                    findViewById(R.id.tallScrollView).scrollBy(0, (int) -UIHelper.pxFromDp(this, 300));
                    return true;
                case KeyEvent.KEYCODE_PAGE_DOWN:
                    //SCROLL DOWN A LOT
                    findViewById(R.id.tallScrollView).scrollBy(0, (int) UIHelper.pxFromDp(this, 300));
                    return true;
                case KeyEvent.KEYCODE_W:
                    //MOVE SELECTION UP
                    qv.moveHighlight(0);
                    return true;
                case KeyEvent.KEYCODE_A:
                    //MOVE SELECTION LEFT
                    qv.moveHighlight(1);
                    return true;
                case KeyEvent.KEYCODE_S:
                    //MOVE SELECTION DOWN
                    qv.moveHighlight(2);
                    return true;
                case KeyEvent.KEYCODE_D:
                    //MOVE SELECTION RIGHT
                    qv.moveHighlight(3);
                    return true;
                case KeyEvent.KEYCODE_E:
                    //OPEN SELECTION
                    RectF rect = qv.getRectInGrid(qv.getHighlight());
                    if (rect != null)
                        showAddGateDialog(rect.centerX(), rect.centerY(), null);
                    return true;
                case KeyEvent.KEYCODE_M:
                    //TOGGLE MEASUREMENT STATUS
                    RectF selection = qv.getRectInGrid(qv.getHighlight());
                    if (selection != null)
                        qv.toggleIgnoredState(qv.whichQubit(selection.centerY()));
                    return true;

            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        QuantumViewModel model = new ViewModelProvider(MainActivity.this).get(QuantumViewModel.class);
        model.set(qv.getData());
        super.onDestroy();
    }

    private void setUpNavbar(Configuration config) {
        try {
            View v = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.navigation_header, null, false);
            ((TextView) v.findViewById(R.id.version)).setText(getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName);
            navigationView.removeHeaderView(navigationView.getHeaderView(0));
            navigationView.addHeaderView(v);
            if (gateHolder != null)
                new Handler().postDelayed(() -> {
                    setUpShortcuts(gateHolder, PreferenceManager.getDefaultSharedPreferences(this), getResources().getDisplayMetrics(), config, findViewById(R.id.nvParent).getWidth());
                }, 200);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    (int) UIHelper.pxFromDp(MainActivity.this, margin * 2f));
        else
            lp.setMargins((int) UIHelper.pxFromDp(MainActivity.this, margin),
                    (int) UIHelper.pxFromDp(MainActivity.this, margin / 2f),
                    (int) UIHelper.pxFromDp(MainActivity.this, margin),
                    (int) UIHelper.pxFromDp(MainActivity.this, margin / 2f));
        gateHolder.setLayoutParams(lp);
        gateHolder.bringToFront();
    }
}
