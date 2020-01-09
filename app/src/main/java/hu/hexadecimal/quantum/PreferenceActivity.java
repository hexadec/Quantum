package hu.hexadecimal.quantum;

import android.content.Intent;
import android.content.UriPermission;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.text.Html;
import android.util.Log;
import android.view.Window;
import androidx.documentfile.provider.DocumentFile;

public class PreferenceActivity extends android.preference.PreferenceActivity {

    Uri treeUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#171717"));

        Preference saveLoc = findPreference("save_loc");
        Preference halp = findPreference("help");
        halp.setSummary(Html.fromHtml(getString(R.string.settings_help_long)));
        String path = getString(R.string.no_folder_selected);
        try {
            Uri uri = getContentResolver().getPersistedUriPermissions().get(0).getUri();
            DocumentFile dir = DocumentFile.fromTreeUri(this, uri);
            path = dir.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        saveLoc.setSummary(path);
        saveLoc.setOnPreferenceClickListener((Preference preference) -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, 42);
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK && requestCode == 42) {
            treeUri = resultData.getData();
            try {
                UriPermission oldUri = getContentResolver().getPersistedUriPermissions().get(0);
                if (oldUri.getUri().equals(treeUri)) {
                    Log.w("Preferences", "User selected previous directory");
                    return;
                }
                getContentResolver().releasePersistableUriPermission(oldUri.getUri(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            findPreference("save_loc").setSummary(DocumentFile.fromTreeUri(this, treeUri).getName());
        }
    }
}
