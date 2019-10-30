package hu.hexadecimal.quantum;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MatrixEditorActivity extends AppCompatActivity {

    RecyclerViewAdapter adapter;
    private DocumentFile pickedDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ArrayList<VisualOperator> operators = new ArrayList<>();
        try {
            Uri uri = getContentResolver().getPersistedUriPermissions().get(0).getUri();
            pickedDir = DocumentFile.fromTreeUri(this, uri);
            if (!pickedDir.exists()) {
                getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                pickedDir = null;
            }

            for (DocumentFile file : pickedDir.listFiles()) {
                try {
                    if (file.getName().endsWith(VisualOperator.FILE_EXTENSION)) {
                        VisualOperator m = (VisualOperator) new ObjectInputStream(getContentResolver().openInputStream(file.getUri())).readObject();
                        operators.add(m);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this, operators);
        adapter.setClickListener(new RecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VisualOperator vo = operators.get(position);
                AlertDialog.Builder adb = new AlertDialog.Builder(MatrixEditorActivity.this);
                adb.setTitle(R.string.delete_gate_question);
                adb.setNegativeButton(R.string.no, null);
                adb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            DocumentFile gate = pickedDir.findFile(vo.getName() + VisualOperator.FILE_EXTENSION);
                            Log.e("A", "" + gate.delete());
                            recreate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                adb.show();
            }
        });
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
                adb.setNeutralButton(R.string.check_matrix, null);
                AlertDialog d = adb.create();
                d.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        final SeekBar seekBar = (SeekBar) v.findViewById(R.id.colorBar);
                        seekBar.setMax(256 * 7 - 1);
                        seekBar.setOnSeekBarChangeListener(stuff);
                        seekBar.setProgress((int) (256 * 3.5));
                        SeekBar qubits = v.findViewById(R.id.qbitsBar);
                        TextInputEditText namee = v.findViewById(R.id.name0);
                        TextInputEditText symbolse = v.findViewById(R.id.symbols0);
                        TextInputEditText matrixe = v.findViewById(R.id.editText30);
                        final Button okButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                int qs = qubits.getProgress() + 1;
                                int DIM;
                                switch (qs) {
                                    case 1:
                                        DIM = 2;
                                        break;
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
                                String name = namee.getText().toString();
                                if (name.length() < 1) {
                                    showErr(2);
                                    return;
                                } else {
                                    disableErr(2);
                                }
                                for (int i = 0; i < operators.size(); i++) {
                                    if (operators.get(i).getName().equals(name)) {
                                        showErr(2);
                                        return;
                                    }
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
                                String[][] strMatrix = new String[DIM][DIM];
                                Log.e("D", "sym: " + symbols.length + " " + qs);
                                if (symbols.length != qs) {
                                    showErr(1);
                                    return;
                                }
                                if (tempm.length != DIM) {
                                    showErr(0);
                                    return;
                                }
                                for (int i = 0; i < strMatrix[0].length; i++) {
                                    String[] temp = tempm[i].split(",");
                                    if (temp.length != strMatrix[0].length) {
                                        showErr(0);
                                        return;
                                    }
                                    strMatrix[i] = temp;
                                }
                                Complex[][] cmatrix = new Complex[DIM][DIM];
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
                                VisualOperator v = new VisualOperator(DIM, cmatrix, name, symbols, color);
                                if (!v.isSpecial() || !v.isUnitary()) {
                                    showErr(4);
                                    return;
                                }
                                try {
                                    DocumentFile newFile = pickedDir.createFile("application/octet-stream", name + VisualOperator.FILE_EXTENSION);
                                    OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
                                    ObjectOutputStream out2 = new ObjectOutputStream(out);
                                    out2.writeObject(v);
                                    out2.close();
                                    out.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                d.cancel();
                                recreate();
                            }

                        });
                        final Button checkButton = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                        checkButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int qs = qubits.getProgress() + 1;
                                int DIM;
                                switch (qs) {
                                    case 1:
                                        DIM = 2;
                                        break;
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
                                if (matrixe.getText().toString().replace(" ", "").length() < 1) {
                                    showErr(0);
                                    return;
                                } else {
                                    disableErr(0);
                                }
                                String[] tempm = matrixe.getText().toString().replace(" ", "").split("\n");
                                String[][] strMatrix = new String[DIM][DIM];
                                if (tempm.length != DIM) {
                                    showErr(0);
                                    return;
                                }
                                for (int i = 0; i < strMatrix[0].length; i++) {
                                    String[] temp = tempm[i].split(",");
                                    if (temp.length != strMatrix[0].length) {
                                        showErr(0);
                                        return;
                                    }
                                    strMatrix[i] = temp;
                                }
                                Complex[][] cmatrix = new Complex[DIM][DIM];
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
                                VisualOperator vo = new VisualOperator(DIM, cmatrix);
                                ((TextInputLayout) v.findViewById(R.id.editText3)).setErrorEnabled(true);
                                ((TextInputLayout) v.findViewById(R.id.editText3))
                                        .setErrorTextColor(ColorStateList.valueOf(!vo.isSpecial() || !vo.isUnitary() ? Color.RED : Color.GREEN));
                                ((TextInputLayout) v.findViewById(R.id.editText3))
                                        .setError(getString(R.string.determinant) + ": "
                                                + vo.determinant().toString3Decimals() + ". "
                                                + getString(R.string.unitary) + ": "
                                                + (vo.isUnitary() ? getString(R.string.yes) : getString(R.string.no)));

                            }
                        });
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
            pickedDir = DocumentFile.fromTreeUri(this, treeUri);
        }
    }

}
