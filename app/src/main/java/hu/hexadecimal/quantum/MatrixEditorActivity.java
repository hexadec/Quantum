package hu.hexadecimal.quantum;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import hu.hexadecimal.quantum.graphics.ContextMenuRecyclerView;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MatrixEditorActivity extends AppCompatActivity {

    RecyclerViewAdapter adapter;
    private DocumentFile pickedDir;
    private ArrayList<VisualOperator> operators;

    static final String[] FILENAME_RESERVED_CHARS = {"/", "|", "\\", "?", "*", "<", "\"", ":", ">", "'", "~", "+", "[", "]"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#171717")));
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#171717"));

        new Thread(() -> {

            operators = new ArrayList<>();
            try {
                Uri uri = getContentResolver().getPersistedUriPermissions().get(0).getUri();
                pickedDir = DocumentFile.fromTreeUri(this, uri);
                if (!pickedDir.exists()) {
                    getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    pickedDir = null;
                }

                for (DocumentFile file : pickedDir.listFiles()) {
                    if (file.isDirectory()) continue;
                    try {
                        if (file.getName().endsWith(VisualOperator.FILE_EXTENSION_LEGACY)) {
                            VisualOperator m = (VisualOperator) new ObjectInputStream(getContentResolver().openInputStream(file.getUri())).readObject();
                            operators.add(m);
                        } else if (file.getName().endsWith(VisualOperator.FILE_EXTENSION)) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(file.getUri())));
                            StringBuilder total = new StringBuilder();
                            for (String line; (line = in.readLine()) != null; ) {
                                total.append(line).append('\n');
                            }
                            String json = total.toString();
                            operators.add(VisualOperator.fromJSON(new JSONObject(json)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                final RecyclerView recyclerView = findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                adapter = new RecyclerViewAdapter(this, operators);
                registerForContextMenu(recyclerView);
                adapter.setClickListener((View view, int position) -> {
                    VisualOperator vo = operators.get(position);
                    displayGateEditorDialog(operators, vo);
                });
                recyclerView.setAdapter(adapter);
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MatrixEditorActivity.this);
                int help_shown = pref.getInt("matrix_help_shown", 0);
                if (help_shown < 5 && operators.size() > 0) {
                    Snackbar.make(findViewById(R.id.parent3), R.string.long_click_to_delete, Snackbar.LENGTH_LONG).show();
                    pref.edit().putInt("matrix_help_shown", ++help_shown).apply();
                }
            });
        }).start();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            if (getContentResolver().getPersistedUriPermissions().size() < 1) {
                Snackbar.make(view, R.string.choose_save_location, Snackbar.LENGTH_LONG)
                        .setAction(R.string.select, (View view2) ->
                                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 42)).show();
                return;
            }
            displayGateEditorDialog(operators, null);
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void displayGateEditorDialog(ArrayList<VisualOperator> operators, VisualOperator overridden) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MatrixEditorActivity.this);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View v = layoutInflater.inflate(R.layout.edit_matrix, null);
        adb.setView(v);
        adb.setPositiveButton(R.string.save, null);
        adb.setNegativeButton(R.string.cancel, null);
        adb.setNeutralButton(R.string.check_matrix, null);
        adb.setCancelable(false);
        AlertDialog d = adb.create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final SeekBar seekBar = v.findViewById(R.id.colorBar);
                seekBar.setMax(256 * 7 - 1);
                seekBar.setOnSeekBarChangeListener(stuff);
                seekBar.setProgress((int) (256 * 3.5));
                SeekBar qubits = v.findViewById(R.id.qbitsBar);
                TextInputEditText nameEditText = v.findViewById(R.id.name0);
                TextInputEditText symbolsEditText = v.findViewById(R.id.symbols0);
                TextInputEditText matrixEditText = v.findViewById(R.id.editText30);
                final Button okButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
                View.OnClickListener onClickListener = (View view) -> {
                    int qs = qubits.getProgress() + 1;
                    int DIM;
                    switch (qs) {
                        case 2:
                            DIM = 4;
                            break;
                        case 3:
                            DIM = 8;
                            break;
                        case 4:
                            DIM = 16;
                            break;
                        default:
                            DIM = 2;
                            break;
                    }
                    int color = Color.DKGRAY;
                    Drawable background = v.findViewById(R.id.color_stuff).getBackground();
                    if (background instanceof ColorDrawable)
                        color = ((ColorDrawable) background).getColor();
                    String name = nameEditText.getText().toString();
                    if (name.length() < 1) {
                        showErr(2);
                        return;
                    } else {
                        disableErr(2);
                    }
                    for (String s : FILENAME_RESERVED_CHARS)
                        name = name.replace(s, "_");

                    if (overridden == null) {
                        for (int i = 0; i < operators.size(); i++) {
                            if (operators.get(i).getName().equals(name)) {
                                showErr(2);
                                return;
                            }
                        }
                    }
                    if (symbolsEditText.getText().toString().replace(" ", "").length() < 1) {
                        showErr(1);
                        return;
                    } else {
                        disableErr(1);
                    }
                    String[] symbols = symbolsEditText.getText().toString().replace(" ", "").split(",");
                    if (matrixEditText.getText().toString().replace(" ", "").length() < 1) {
                        showErr(0);
                        return;
                    } else {
                        disableErr(0);
                    }
                    for (String symbol : symbols) {
                        if (symbol.length() == 0 || symbol.length() > 3) {
                            showErr(5);
                            return;
                        }
                    }
                    disableErr(1);
                    String[] tempMatrix = matrixEditText.getText().toString().replace(" ", "").split("\n");
                    String[][] strMatrix = new String[DIM][DIM];
                    if (symbols.length != qs) {
                        showErr(1);
                        return;
                    }
                    if (tempMatrix.length != DIM) {
                        showErr(0);
                        return;
                    }
                    for (int i = 0; i < strMatrix[0].length; i++) {
                        String[] temp = tempMatrix[i].split(",");
                        if (temp.length != strMatrix[0].length) {
                            showErr(0);
                            return;
                        }
                        strMatrix[i] = temp;
                    }
                    Complex[][] cMatrix = new Complex[DIM][DIM];
                    try {
                        for (int i = 0; i < strMatrix[0].length; i++) {
                            for (int j = 0; j < strMatrix[0].length; j++) {
                                cMatrix[i][j] = Complex.parse(strMatrix[i][j]);
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        showErr(3);
                        return;
                    }
                    VisualOperator v = new VisualOperator(DIM, cMatrix, name, symbols, color);
                    if (!v.isSpecial() || !v.isUnitary()) {
                        showErr(4);
                        return;
                    }
                    try {
                        DocumentFile newFile;
                        if (overridden == null) {
                            newFile = pickedDir.createFile("application/octet-stream", name + VisualOperator.FILE_EXTENSION);
                        } else {
                            DocumentFile df = pickedDir.findFile(name + VisualOperator.FILE_EXTENSION);
                            if (df == null || df.isDirectory()) {
                                newFile = pickedDir.createFile("application/octet-stream", name + VisualOperator.FILE_EXTENSION);
                                pickedDir.findFile(name + VisualOperator.FILE_EXTENSION_LEGACY).delete();
                            } else {
                                newFile = df;
                            }
                        }
                        OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
                        out.write(v.toJSON().toString(2).getBytes());
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.parent3), R.string.unknown_error, Snackbar.LENGTH_LONG);
                        snackbar.getView().setBackgroundColor(0xffD81010);
                        snackbar.show();
                        d.cancel();
                        return;
                    }
                    d.cancel();
                    recreate();
                };
                okButton.setOnClickListener(onClickListener);
                final Button checkButton = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                checkButton.setOnClickListener((View view) -> {
                    int qs = qubits.getProgress() + 1;
                    int DIM;
                    switch (qs) {
                        case 2:
                            DIM = 4;
                            break;
                        case 3:
                            DIM = 8;
                            break;
                        case 4:
                            DIM = 16;
                            break;
                        default:
                            DIM = 2;
                            break;
                    }
                    if (matrixEditText.getText().toString().replace(" ", "").length() < 1) {
                        showErr(0);
                        return;
                    } else {
                        disableErr(0);
                    }
                    String[] tempMatrix = matrixEditText.getText().toString().replace(" ", "").split("\n");
                    String[][] strMatrix = new String[DIM][DIM];
                    if (tempMatrix.length != DIM) {
                        showErr(0);
                        return;
                    }
                    for (int i = 0; i < strMatrix[0].length; i++) {
                        String[] temp = tempMatrix[i].split(",");
                        if (temp.length != strMatrix[0].length) {
                            showErr(0);
                            return;
                        }
                        strMatrix[i] = temp;
                    }
                    Complex[][] cMatrix = new Complex[DIM][DIM];
                    try {
                        for (int i = 0; i < strMatrix[0].length; i++) {
                            for (int j = 0; j < strMatrix[0].length; j++) {
                                cMatrix[i][j] = Complex.parse(strMatrix[i][j]);
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        showErr(3);
                        return;
                    }
                    VisualOperator vo = new VisualOperator(DIM, cMatrix);
                    ((TextInputLayout) v.findViewById(R.id.editText3)).setErrorEnabled(true);
                    ((TextInputLayout) v.findViewById(R.id.editText3))
                            .setErrorTextColor(ColorStateList.valueOf(!vo.isSpecial() || !vo.isUnitary() ? Color.RED : Color.GREEN));
                    ((TextInputLayout) v.findViewById(R.id.editText3))
                            .setError(getString(R.string.determinant) + ": "
                                    + new DecimalFormat("0.0###").format(vo.determinantMod()) + ". "
                                    + getString(R.string.unitary) + ": "
                                    + (vo.isUnitary() ? getString(R.string.yes) : getString(R.string.no)));

                });
                if (overridden != null) {
                    qubits.setProgress(overridden.getQubits() - 1);
                    v.findViewById(R.id.color_stuff).setBackground(new ColorDrawable(overridden.getColor()));
                    nameEditText.setText(overridden.getName());
                    nameEditText.setEnabled(false);
                    symbolsEditText.setText(TextUtils.join(",", overridden.getSymbols()));
                    matrixEditText.setText(overridden.toString(6));
                }
            }

            public void showErr(int which) {
                switch (which) {
                    case 0:
                        ((TextInputLayout) v.findViewById(R.id.editText3)).setErrorTextColor(ColorStateList.valueOf(Color.RED));
                        ((TextInputLayout) v.findViewById(R.id.editText3)).setError(getString(R.string.invalid_dim));
                        break;
                    case 1:
                        ((TextInputLayout) v.findViewById(R.id.symbols)).setError(getString(R.string.invalid_symbols));
                        break;
                    case 2:
                        ((TextInputLayout) v.findViewById(R.id.name)).setError(getString(R.string.invalid_name));
                        break;
                    case 3:
                        ((TextInputLayout) v.findViewById(R.id.editText3)).setError(getString(R.string.invalid_complex));
                        break;
                    case 4:
                        ((TextInputLayout) v.findViewById(R.id.editText3)).setError(getString(R.string.invalid_matrix));
                        break;
                    case 5:
                        ((TextInputLayout) v.findViewById(R.id.symbols)).setError(getString(R.string.invalid_symbol));
                        break;
                    default:
                        break;
                }
            }

            public void disableErr(int which) {
                switch (which) {
                    case 0:
                        ((TextInputLayout) v.findViewById(R.id.editText3)).setError(null);
                        break;
                    case 1:
                        ((TextInputLayout) v.findViewById(R.id.symbols)).setError(null);
                        break;
                    case 2:
                        ((TextInputLayout) v.findViewById(R.id.name)).setError(null);
                        break;
                    default:
                        break;
                }
            }
        });
        d.show();
    }

    private final SeekBar.OnSeekBarChangeListener stuff = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int r = 0;
            int g = 0;
            int b = 0;

            if (progress < 256) {
                b = progress;
            } else if (progress < 256 * 2) {
                g = progress % 256;
                b = 256 - progress % 256;
            } else if (progress < 256 * 3) {
                g = 255;
                b = progress % 256;
            } else if (progress < 256 * 4) {
                r = progress % 256;
                g = 256 - progress % 256;
                b = 256 - progress % 256;
            } else if (progress < 256 * 5) {
                r = 255;
                g = 0;
                b = progress % 256;
            } else if (progress < 256 * 6) {
                r = 255;
                g = progress % 256;
                b = 256 - progress % 256;
            } else if (progress < 256 * 7) {
                r = 255;
                g = 255;
                b = progress % 256;
            }

            ((View) seekBar.getParent()).findViewById(R.id.color_stuff).setBackgroundColor(Color.argb(255, r, g, b));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK && requestCode == 42) {
            Uri treeUri = resultData.getData();
            getContentResolver().takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            pickedDir = DocumentFile.fromTreeUri(this, treeUri);
            recreate();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.add(Menu.NONE, view.getId(), Menu.NONE, getString(R.string.delete_gate));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        VisualOperator vo = operators.get(info.position);
        String name = vo.getName();
        for (String s : FILENAME_RESERVED_CHARS)
            name = name.replace(s, "_");
        try {
            DocumentFile gate = pickedDir.findFile(name + VisualOperator.FILE_EXTENSION);
            gate.delete();
            recreate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Source: https://stackoverflow.com/questions/41025200/android-view-inflateexception-error-inflating-class-android-webkit-webview
     */
    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT <= 22) {
            return;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

}
