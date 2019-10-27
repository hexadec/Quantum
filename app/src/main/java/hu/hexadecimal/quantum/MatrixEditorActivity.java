package hu.hexadecimal.quantum;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MatrixEditorActivity extends AppCompatActivity {

    RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppBarLayout a = new AppBarLayout(this);
        setContentView(R.layout.activity_matrix_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Uri uri = getContentResolver().getPersistedUriPermissions().get(0).getUri();
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);

        ArrayList<VisualOperator> operators = new ArrayList<>();

        for (DocumentFile file : pickedDir.listFiles()) {
            try {
                Log.e("DF", file.getName());
                if (file.getName().endsWith(".sqg")) {
                    LinearOperator l = (LinearOperator) new ObjectInputStream(getContentResolver().openInputStream(file.getUri())).readObject();
                    operators.add(l);
                    Log.e("DF", file.getName());
                } else if (file.getName().endsWith(".mqg")) {
                    MultiQubitOperator m = (MultiQubitOperator) new ObjectInputStream(getContentResolver().openInputStream(file.getUri())).readObject();
                    operators.add(m);
                    Log.e("DF", file.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this, operators);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getContentResolver().getPersistedUriPermissions().size() < 1) {
                    Snackbar.make(view, R.string.choose_save_location, Snackbar.LENGTH_LONG)
                            .setAction(R.string.select, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                    startActivityForResult(intent, 42);
                                }
                            }).show();
                    return;
                }
                AlertDialog.Builder adb = new AlertDialog.Builder(MatrixEditorActivity.this);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final View v = layoutInflater.inflate(R.layout.edit_matrix, null);
                adb.setView(v);
                adb.setPositiveButton(R.string.add_gate, null);
                adb.setNegativeButton(R.string.cancel, null);
                AlertDialog d = adb.create();
                d.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        final SeekBar seekBar = (SeekBar) v.findViewById(R.id.colorBar);
                        seekBar.setMax(256 * 7 - 1);
                        seekBar.setOnSeekBarChangeListener(stuff);
                        seekBar.setProgress((int) (256 * 3.5));
                        final Button okButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                SeekBar qubits = v.findViewById(R.id.qbitsBar);
                                SeekBar colorBar = v.findViewById(R.id.colorBar);
                                TextInputEditText namee = v.findViewById(R.id.name0);
                                TextInputEditText symbolse = v.findViewById(R.id.symbols0);
                                TextInputEditText matrixe = v.findViewById(R.id.editText30);

                                int qs = qubits.getProgress() + 1;
                                int color = Color.DKGRAY;
                                Drawable background = v.findViewById(R.id.color_stuff).getBackground();
                                if (background instanceof ColorDrawable)
                                    color = ((ColorDrawable) background).getColor();
                                String name = namee.getText().toString();
                                if (name.length() < 1) {
                                    showErr(2);
                                    return;
                                } else {
                                    disableErr(2);
                                }
                                if (symbolse.getText().toString().replace(" ", "").length() < 1) {
                                    showErr(1);
                                    return;
                                } else {
                                    disableErr(1);
                                }
                                String[] symbols = symbolse.getText().toString().replace(" ", "").split(",");
                                if (matrixe.getText().toString().replace(" ", "").length() < 1) {
                                    showErr(0);
                                    return;
                                } else {
                                    disableErr(0);
                                }
                                String[] tempm = matrixe.getText().toString().replace(" ", "").split("\n");
                                String[][] strMatrix = new String[(int) Math.pow(2, qs)][(int) Math.pow(2, qs)];
                                if (symbols.length != qs) {
                                    showErr(1);
                                    return;
                                }
                                if (tempm.length != (int) Math.pow(2, qs)) {
                                    showErr(0);
                                    return;
                                }
                                for (int i = 0; i < strMatrix[0].length; i++) {
                                    String[] temp = tempm[i].split(",");
                                    if (tempm.length != strMatrix[0].length) {
                                        showErr(0);
                                        return;
                                    }
                                    strMatrix[i] = tempm[i].split(",");
                                }
                                Complex[][] cmatrix = new Complex[(int) Math.pow(2, qs)][(int) Math.pow(2, qs)];
                                try {
                                    for (int i = 0; i < strMatrix[0].length; i++) {
                                        for (int j = 0; j < strMatrix[0].length; j++) {
                                            cmatrix[i][j] = Complex.fromString(strMatrix[i][j]);
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                    showErr(3);
                                    return;
                                }
                                VisualOperator v;
                                if (qs == 1) {
                                    v = new LinearOperator(cmatrix, name, symbols[0], color);
                                    if (!((LinearOperator) v).isSpecial() || !((LinearOperator) v).isUnitary()) {
                                        Log.e("Mtr", ((LinearOperator) v).inverse().toString() + "\n----\n" + LinearOperator.hermitianConjugate((LinearOperator)v).toString());
                                        showErr(4);
                                        return;
                                    }

                                } else {
                                    v = new MultiQubitOperator((int) Math.pow(2, qs), cmatrix, name, symbols, color);
                                    if (!((MultiQubitOperator) v).isSpecial() || !((MultiQubitOperator) v).isUnitary()) {
                                        showErr(4);
                                        return;
                                    }
                                }
                                try {
                                    DocumentFile newFile = pickedDir.createFile("application/octet-stream", name + (v instanceof LinearOperator ? ".sqg" : ".mqg"));
                                    OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
                                    ObjectOutputStream out2 = new ObjectOutputStream(out);
                                    out2.writeObject(v);
                                    out2.close();
                                    out.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                d.cancel();
                            }

                            public void showErr(int which) {
                                switch (which) {
                                    case 0:
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
                    }
                });
                d.show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        if (resultCode == RESULT_OK) {
            Uri treeUri = resultData.getData();
            getContentResolver().takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }

}
