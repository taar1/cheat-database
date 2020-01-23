package com.cheatdatabase.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.model.Cheat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Code abstracting helper class
 *
 * @author Dominik Erbsland
 */
public class Helper extends Activity {

    public static void startNewActivity(Context context, Class<?> cls) {
        Intent explicitIntent = new Intent(context, cls);
        context.startActivity(explicitIntent);
    }

    /**
     * Showing a popup with an error message which closes the view upon clicking
     * the OK button
     *
     * @param context
     * @param act
     */
    public static void error(String errorTitle, String errorBody, Context context, final Activity act) {
        Log.e("Helper:error()", "caught error: " + context.getPackageName() + "/" + act.getTitle());
        new AlertDialog.Builder(context).setIcon(R.drawable.ic_action_warning).setTitle(errorTitle).setMessage(errorBody).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                act.finish();
            }
        }).create().show();
    }

    /**
     * Sharing feature for tablets where the sharing button is not inside the action bar.
     *
     * @param cheat
     * @param context
     */
    public static void shareCheat(Cheat cheat, Context context) {
        String fullBody = cheat.getGameName() + " (" + cheat.getSystemName() + "): " + cheat.getCheatTitle() + "\n";
        fullBody += Konstanten.BASE_URL + "display/switch.php?id=" + cheat.getCheatId() + "\n\n";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String result = String.format(context.getString(R.string.share_email_subject), cheat.getGameName());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, result);
        shareIntent.putExtra(Intent.EXTRA_TEXT, fullBody);
        try {
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, context.getString(R.string.share_error_no_client), Toast.LENGTH_SHORT).show();
        }
    }

    public static void addFavorite(Context context, View parentView, Cheat visibleCheat) {
        DatabaseHelper db = new DatabaseHelper(context);

        if (db.insertFavoriteCheat(visibleCheat) > 0) {
            Tools.showSnackbar(parentView, context.getString(R.string.add_favorite_ok));
        } else {
            Tools.showSnackbar(parentView, context.getString(R.string.favorite_error));
        }
    }

    /**
     * Gets the Remote App ID of this application. To use for Facebook sharing.
     * Set this as key hash at:
     * https://developers.facebook.com/apps/176028095867190/summary/ Source:
     * http://stackoverflow.com/
     * questions/1820908/how-to-turn-off-the-eclipse-code
     * -formatter-for-certain-sections-of-java-code
     *
     * @param context
     */
    public static void getRemoteAppId(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo("com.cheatdatabase1", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException | NoSuchAlgorithmException e) {
        }
    }

}