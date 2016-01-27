package com.cheatdatabase.members.cheatview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.businessobjects.Screenshot;
import com.cheatdatabase.dialogs.CheatMetaDialog;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MemberCheatViewFragment extends Fragment implements OnClickListener {

    private TableLayout mainTable;
    private LinearLayout ll;
    private TextView tvCheatText;
    private TextView tvTextBeforeTable;
    private TextView tvCheatTitle;
    private TextView tvGalleryInfo;
    private Gallery screenshotGallery;
    private int biggestHeight;

    private Cheat cheatObj;
    private ArrayList<Cheat> cheats;
    // private Game game;
    private int offset;
    public ArrayList<String[]> al_images;
    private ImageView[] imageViews;
    private ProgressBar progressBar;
    private Member member;

    private SharedPreferences settings;
    private Editor editor;

    public AlertDialog.Builder builder;
    public AlertDialog alert;

    private static final String KEY_CONTENT = "MemberCheatViewFragment:Content";

    public static MemberCheatViewFragment newInstance(String content, ArrayList<Cheat> cheats, int offset) {

        MemberCheatViewFragment fragment = new MemberCheatViewFragment();

        // übergebene Parameter ins Bundle Objekt eintragen
        Bundle arguments = new Bundle();
        arguments.putSerializable("cheatObj", cheats);
        arguments.putInt("offset", offset);
        fragment.setArguments(arguments);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            builder.append(content).append(" ");
        }
        builder.deleteCharAt(builder.length() - 1);
        fragment.mContent = builder.toString();

        return fragment;
    }

    private String mContent = "???";
    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private MemberCheatViewPageIndicator ca;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }

        ca = (MemberCheatViewPageIndicator) getActivity();

        latoFontLight = Tools.getFont(ca.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(ca.getAssets(), Konstanten.FONT_BOLD);

        settings = ca.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getFragmentRelevantData();

        try {
            ll = (LinearLayout) inflater.inflate(R.layout.fragment_member_cheat_detail, container, false);

            member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

            cheatObj = cheats.get(offset);
            new FetchCheatRatingOnlineBackgroundTask().execute();

            mainTable = (TableLayout) ll.findViewById(R.id.tblCheatListMain);

            tvTextBeforeTable = (TextView) ll.findViewById(R.id.text_cheat_before_table);
            tvTextBeforeTable.setOnClickListener(this);
            tvTextBeforeTable.setVisibility(View.VISIBLE);

            tvCheatTitle = (TextView) ll.findViewById(R.id.text_cheat_title);
            tvCheatTitle.setTypeface(latoFontBold);
            tvCheatTitle.setText(cheatObj.getCheatTitle());

            tvGalleryInfo = (TextView) ll.findViewById(R.id.gallery_info);
            tvGalleryInfo.setVisibility(View.INVISIBLE);
            tvGalleryInfo.setTypeface(latoFontLight);

            screenshotGallery = (Gallery) ll.findViewById(R.id.gallery);

            progressBar = (ProgressBar) ll.findViewById(R.id.progressBar1);
            progressBar.setVisibility(View.INVISIBLE);

            tvCheatText = (TextView) ll.findViewById(R.id.text_cheat_text);
            tvCheatText.setTypeface(latoFontLight);

            /**
             * Get thumbnails if there are screenshots.
             */
            if (cheatObj.isScreenshots()) {
                biggestHeight = 100; // init value
                imageViews = new ImageView[cheatObj.getScreens().length];
                progressBar.setVisibility(View.VISIBLE);

                new LoadScreenshotsInBackgroundTask().execute();
            } else {
                tvGalleryInfo.setVisibility(View.INVISIBLE);
                screenshotGallery.setVisibility(View.GONE);
            }

            /**
             * If the user came from the search results the cheat-text might not
             * be complete (trimmed for the search results) and therefore has to
             * be re-fetched in a background process.
             */
            // FIXME das gibt noch einen fehler (walkthrough holen)
            // boolean isFromSearchResults = true;
            if ((cheatObj.getCheatText() == null) || (cheatObj.getCheatText().length() < 10)) {
                // if ((isFromSearchResults == true) || (cheat.getCheatText() ==
                // null) || (cheat.getCheatText().length() < 10)) {
                progressBar.setVisibility(View.VISIBLE);
                new FetchCheatTextTask().execute();
            } else {
                progressBar.setVisibility(View.GONE);
                populateView();
            }

            editor.putString("cheat" + offset, new Gson().toJson(cheatObj));
            editor.commit();
        } catch (Exception e) {
            Log.e(MemberCheatViewFragment.class.getName(), "BB: " + e.getMessage());
        }

        return ll;
    }

    private void getFragmentRelevantData() {
        Bundle arguments = getArguments();
        try {
            cheats = (ArrayList<Cheat>) arguments.getSerializable("cheatObj");
            offset = arguments.getInt("offset");
        } catch (Exception e) {
            offset = 0;
            // TODO message ausgeben, dass kein Game objekt besteht
        }
    }

    private void buildGallery() {
        screenshotGallery.setAdapter(new ImageAdapter(ca));
        screenshotGallery.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Screenshot[] screens = cheatObj.getScreens();
                Screenshot screenShot = screens[position];

                Uri uri = Uri.parse(Konstanten.SCREENSHOT_ROOT_WEBDIR + screenShot.getCheatId() + screenShot.getFilename());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

    }

    private void populateView() {
        try {
            if (cheatObj.getCheatText().contains("</td>")) {
                fillTableContent();
            } else {
                fillSimpleContent();
            }
        } catch (Exception e) {
            Log.e(MemberCheatViewFragment.class.getName(), "Cheat " + cheatObj.getCheatId() + " contains(</td>) - Error creating table");
            fillSimpleContent();
        }
    }

    /**
     * Populate Table Layout
     */
    private void fillTableContent() {

        mainTable.setColumnShrinkable(0, true);
        // mainTable.setColumnShrinkable(1, true);
        mainTable.setOnClickListener(this);
        mainTable.setVisibility(View.VISIBLE);

        // Cheat-Text oberhalb der Tabelle
        String[] textBeforeTable = null;

        // Einige tabellarische Cheats beginnen direkt mit der
        // Tabelle
        if (cheatObj.getCheatText().startsWith("<br><table")) {
            textBeforeTable = cheatObj.getCheatText().split("<br>");
            tvTextBeforeTable.setVisibility(View.GONE);
        } else {
            textBeforeTable = cheatObj.getCheatText().split("<br><br>");
            if (textBeforeTable[0].trim().length() > 2) {
                tvTextBeforeTable.setText(textBeforeTable[0].replaceAll("<br>", "\n").trim());
            }
        }

        String[] trs = cheatObj.getCheatText().split("</tr><tr valign='top'>");

        // Check, ob die Tabelle ein TH Element besitzt.
        String firstTag = "th";
        if (!trs[0].contains("</" + firstTag + ">")) {
            firstTag = "td";
        }

        String[] ths = trs[0].split("</" + firstTag + "><" + firstTag + ">");
        String[] th1 = ths[0].split("<" + firstTag + ">");
        String[] th2 = ths[1].split("</" + firstTag + ">");

        String firstThColumn = "<b>" + th1[1].trim() + "</b>";
        String secondThColumn = "<b>" + th2[0].trim() + "</b>";

		/* Create a new row to be added. */
        TableRow trTh = new TableRow(ca);
        trTh.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        TextView tvFirstThCol = new TextView(ca);
        tvFirstThCol.setText(Html.fromHtml(firstThColumn));
        tvFirstThCol.setPadding(1, 1, 5, 1);
        tvFirstThCol.setMinimumWidth(Konstanten.TABLE_ROW_MINIMUM_WIDTH);
        tvFirstThCol.setTextAppearance(ca, R.style.NormalText);
        tvFirstThCol.setTypeface(latoFontLight);
        trTh.addView(tvFirstThCol);

        TextView tvSecondThCol = new TextView(ca);
        tvSecondThCol.setText(Html.fromHtml(secondThColumn));
        tvSecondThCol.setPadding(5, 1, 1, 1);
        tvSecondThCol.setTextAppearance(ca, R.style.NormalText);
        tvSecondThCol.setTypeface(latoFontLight);
        trTh.addView(tvSecondThCol);

		/* Add row to TableLayout. */
        mainTable.addView(trTh, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        for (int i = 1; i < trs.length; i++) {

            String[] tds = trs[i].split("</td><td>");
            String[] td1 = tds[0].split("<td>");
            String[] td2 = tds[1].split("</td>");

            String firstTdColumn = td1[1].replaceAll("<br>", "\n").trim();
            String secondTdColumn = td2[0].replaceAll("<br>", "\n").trim();

			/* Create a new row to be added. */
            TableRow trTd = new TableRow(ca);
            trTd.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            TextView tvFirstTdCol = new TextView(ca);
            tvFirstTdCol.setText(firstTdColumn);
            tvFirstTdCol.setPadding(1, 1, 10, 1);
            tvFirstTdCol.setMinimumWidth(Konstanten.TABLE_ROW_MINIMUM_WIDTH);
            tvFirstTdCol.setTextAppearance(ca, R.style.NormalText);
            tvFirstTdCol.setTypeface(latoFontLight);
            trTd.addView(tvFirstTdCol);

            TextView tvSecondTdCol = new TextView(ca);
            tvSecondTdCol.setText(secondTdColumn);
            tvSecondTdCol.setPadding(10, 1, 30, 1);
            tvSecondTdCol.setTextAppearance(ca, R.style.NormalText);
            tvSecondTdCol.setTypeface(latoFontLight);
            trTd.addView(tvSecondTdCol);

			/* Add row to TableLayout. */
            mainTable.addView(trTd, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }
    }

    private void fillSimpleContent() {
        mainTable.setVisibility(View.GONE);
        tvTextBeforeTable.setVisibility(View.GONE);

        CharSequence styledText = Html.fromHtml(cheatObj.getCheatText());
        tvCheatText.setText(styledText);

        if (cheatObj.isWalkthroughFormat()) {
            tvCheatText.setTextAppearance(ca, R.style.WalkthroughText);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getResources().getConfiguration();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    @Override
    public void onClick(View v) {
        Log.d("onClick", "onClick");
        Bundle arguments = new Bundle();
        arguments.putInt("CHANGEME", 1);
        arguments.putSerializable("cheatObj", cheatObj);
    }

    private class FetchCheatTextTask extends AsyncTask<Void, Void, Void> {

        String fullCheatText;

        @Override
        protected Void doInBackground(Void... params) {
            fullCheatText = Webservice.getCheatById(cheatObj.getCheatId());

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (fullCheatText.substring(0, 1).equalsIgnoreCase("2")) {
                cheatObj.setWalkthroughFormat(true);
            }
            progressBar.setVisibility(View.GONE);
            cheatObj.setCheatText(fullCheatText.substring(1));

            populateView();
        }
    }

    private class FetchCheatRatingOnlineBackgroundTask extends AsyncTask<Void, Void, Void> {

        float cheatRating;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                cheatRating = Webservice.getCheatRatingByMemberId(member.getMid(), cheatObj.getCheatId());
            } catch (Exception e) {
                cheatRating = 0;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (cheatRating > 0) {
                ca.setRating(offset, cheatRating);
            }
        }
    }

    private class LoadScreenshotsInBackgroundTask extends AsyncTask<Void, Void, Bitmap[]> {
        Bitmap bms[];

        @Override
        protected Bitmap[] doInBackground(Void... params) {
            try {
                Screenshot[] screens = cheatObj.getScreens();

                String[] myRemoteImages = new String[screens.length];

                for (int i = 0; i < screens.length; i++) {
                    Screenshot s = screens[i];
                    String filename = s.getCheatId() + s.getFilename();
                    myRemoteImages[i] = Konstanten.SCREENSHOT_ROOT_WEBDIR + "image.php?width=150&image=/cheatpics/" + filename;
                }

                bms = new Bitmap[imageViews.length];
                for (int i = 0; i < imageViews.length; i++) {

					/*
                     * Open a new URL and get the InputStream to load data from
					 * it.
					 */
                    URL aURL = new URL(myRemoteImages[i]);
                    URLConnection conn = aURL.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    /* Buffered is always good for a performance plus. */
                    BufferedInputStream bis = new BufferedInputStream(is);
                    /* Decode url-data to a bitmap. */
                    Bitmap bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    is.close();

                    bms[i] = bm;

                    if (biggestHeight < bm.getHeight()) {
                        biggestHeight = bm.getHeight();
                    }

                }


            } catch (IOException e) {
                Log.e(MemberCheatViewFragment.class.getName(), "Remtoe Image Exception", e);
            }

            return bms;
        }

        @Override
        protected void onPostExecute(Bitmap[] bms) {
            super.onPostExecute(bms);

            /*
             * Apply the Bitmap to the ImageView that will be returned.
             */
            for (int i = 0; i < imageViews.length; i++) {
                imageViews[i] = new ImageView(ca);
                imageViews[i].setScaleType(ImageView.ScaleType.MATRIX);
                imageViews[i].setLayoutParams(new Gallery.LayoutParams(300, biggestHeight));
                imageViews[i].setImageBitmap(bms[i]);
            }

            progressBar.setVisibility(View.GONE);
            if (cheatObj.getScreens().length <= 1) {
                tvGalleryInfo.setVisibility(View.GONE);
            } else {
                tvGalleryInfo.setVisibility(View.VISIBLE);
            }
            buildGallery();
        }
    }

    /**
     * Innere Klasse zum Anzeigen der Screenshot-Thumbnails
     * <p/>
     * Copyright (c) 2010-2012<br>
     *
     * @author Dominik Erbsland
     * @version 1.0
     */
    public class ImageAdapter extends BaseAdapter {

        /**
         * Simple Constructor saving the 'parent' context.
         */
        public ImageAdapter(Context c) {
        }

        @Override
        public int getCount() {
            return imageViews.length;
            // return cheat.getScreens().length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Returns a new ImageView to be displayed, depending on the position
         * passed.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return imageViews[position];
        }

        /**
         * Returns the size (0.0f to 1.0f) of the views depending on the
         * 'offset' to the center.
         */
        public float getScale(boolean focused, int offset) {
            /* Formula: 1 / (2 ^ offset) */
            return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
        }

    }

    public void highlightRatingIcon(boolean highlight) {
        // rating icon highlighten...
        // if (highlight) {
        // btnRateCheat.setImageResource(R.drawable.ic_action_star);
        // } else {
        // btnRateCheat.setImageResource(R.drawable.ic_action_not_important);
        // }
    }

    public void showMetaInfo(Context context) {
        CheatMetaDialog cmDialog = new CheatMetaDialog(ca, cheatObj);
        cmDialog.show();
    }

}