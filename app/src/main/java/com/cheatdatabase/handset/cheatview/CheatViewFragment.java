package com.cheatdatabase.handset.cheatview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.businessobjects.Screenshot;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * List of all cheats for a game optimized for handsets.
 *
 * @author Dominik Erbsland
 * @version 1.0
 */
public class CheatViewFragment extends Fragment implements OnClickListener {

    private TableLayout mainTable;
    private LinearLayout outerLinearLayout;
    private TextView tvCheatText;
    private TextView tvTextBeforeTable;
    private TextView tvCheatTitle;
    private TextView tvGalleryInfo;
    private Gallery screenshotGallery;
    private int biggestHeight;

    private Cheat cheatObj;
    private Cheat[] cheats;
    private Game game;
    private int offset;
    public ArrayList<String[]> al_images;
    private ImageView[] imageViews;
    private ProgressBar progressBar;
    private Member member;

    private SharedPreferences settings;
    private Editor editor;

    public AlertDialog.Builder builder;
    public AlertDialog alert;

    private static final String KEY_CONTENT = "CheatViewFragment:Content";
    private static final String TAG = CheatViewFragment.class.getSimpleName();

    public static CheatViewFragment newInstance(String cheatTitle, Game gameObj, int offset) {

        CheatViewFragment fragment = new CheatViewFragment();

        // Uebergebene Parameter ins Bundle Objekt eintragen
        Bundle args = new Bundle();
        args.putSerializable("gameObj", gameObj);
        args.putInt("offset", offset);
        fragment.setArguments(args);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            builder.append(cheatTitle).append(" ");
        }
        builder.deleteCharAt(builder.length() - 1);
        fragment.mContent = builder.toString();

        return fragment;
    }

    private String mContent = "???";
    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private CheatViewPageIndicator cheatViewPageIndicator;
    private ImageView reloadView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getActivity().getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        init();

        // TODO hier genauer anschauen wegen dem setzen des contents. TODO TODO
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    private void init() {
        cheatViewPageIndicator = (CheatViewPageIndicator) getActivity();

        latoFontLight = Tools.getFont(cheatViewPageIndicator.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(cheatViewPageIndicator.getAssets(), Konstanten.FONT_BOLD);

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getFragmentRelevantData();

        outerLinearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_cheat_detail_handset, container, false);

        cheats = game.getCheats();
        cheatObj = cheats[offset];

        new FetchCheatRatingOnlineBackgroundTask().execute();

        mainTable = (TableLayout) outerLinearLayout.findViewById(R.id.table_cheat_list_main);

        tvTextBeforeTable = (TextView) outerLinearLayout.findViewById(R.id.text_cheat_before_table);
        tvTextBeforeTable.setOnClickListener(this);
        tvTextBeforeTable.setVisibility(View.VISIBLE);
        tvTextBeforeTable.setTypeface(latoFontLight);

        tvCheatTitle = (TextView) outerLinearLayout.findViewById(R.id.text_cheat_title);
        tvCheatTitle.setTypeface(latoFontBold);
        tvCheatTitle.setText(cheatObj.getCheatTitle());

        tvGalleryInfo = (TextView) outerLinearLayout.findViewById(R.id.gallery_info);
        tvGalleryInfo.setVisibility(View.INVISIBLE);
        tvGalleryInfo.setTypeface(latoFontLight);

        screenshotGallery = (Gallery) outerLinearLayout.findViewById(R.id.gallery);

        progressBar = (ProgressBar) outerLinearLayout.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        tvCheatText = (TextView) outerLinearLayout.findViewById(R.id.cheat_content);
        tvCheatText.setTypeface(latoFontLight);

        reloadView = (ImageView) outerLinearLayout.findViewById(R.id.reload);
        if (Reachability.reachability.isReachable) {
            getOnlineContent();
        } else {
            reloadView.setVisibility(View.VISIBLE);
            reloadView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Reachability.reachability.isReachable) {
                        getOnlineContent();
                    } else {
                        Toast.makeText(cheatViewPageIndicator, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Toast.makeText(cheatViewPageIndicator, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        return outerLinearLayout;
    }

    private void getOnlineContent() {
        reloadView.setVisibility(View.GONE);

        /**
         * Get thumbnails if there are screenshots.
         */
        if (cheatObj.isScreenshots()) {
            biggestHeight = 100; // init value
            imageViews = new ImageView[cheatObj.getScreens().length];
            progressBar.setVisibility(View.VISIBLE);

            new LoadScreenshotsInBackgroundTask().execute();
        } else {
            tvGalleryInfo.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            screenshotGallery.setVisibility(View.GONE);
        }

        /**
         * If the user came from the search results the cheat-text might not be
         * complete (trimmed for the search results) and therefore has to be
         * re-fetched in a background process.
         */
        if ((cheatObj.getCheatText() == null) || (cheatObj.getCheatText().length() < 10)) {
            progressBar.setVisibility(View.VISIBLE);
            new FetchCheatTextTask().execute();
        } else {
            populateView();
        }

        editor.putString("cheat" + offset, new Gson().toJson(cheatObj));
        editor.commit();
    }

    private void getFragmentRelevantData() {
        Bundle arguments = getArguments();
        try {
            game = (Game) arguments.getSerializable("gameObj");
            offset = arguments.getInt("offset");
        } catch (Exception e) {
            offset = 0;
            // TODO message ausgeben, dass kein Game objekt besteht
            Log.e("Error", e.getLocalizedMessage());
        }
    }

    private void buildGallery() {
        screenshotGallery.setAdapter(new ImageAdapter(cheatViewPageIndicator));
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
            Log.e(TAG, "Cheat " + cheatObj.getCheatId() + " contains(</td>) - Error creating table");
            fillSimpleContent();
        }
    }

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
        TableRow trTh = new TableRow(cheatViewPageIndicator);
        trTh.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        TextView tvFirstThCol = new TextView(cheatViewPageIndicator);
        tvFirstThCol.setText(Html.fromHtml(firstThColumn));
        tvFirstThCol.setPadding(1, 1, 5, 1);
        tvFirstThCol.setMinimumWidth(Konstanten.TABLE_ROW_MINIMUM_WIDTH);
        tvFirstThCol.setTextAppearance(cheatViewPageIndicator, R.style.NormalText);
        tvFirstThCol.setTypeface(latoFontLight);
        trTh.addView(tvFirstThCol);

        TextView tvSecondThCol = new TextView(cheatViewPageIndicator);
        tvSecondThCol.setText(Html.fromHtml(secondThColumn));
        tvSecondThCol.setPadding(5, 1, 1, 1);
        tvSecondThCol.setTextAppearance(cheatViewPageIndicator, R.style.NormalText);
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
            TableRow trTd = new TableRow(cheatViewPageIndicator);
            trTd.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            TextView tvFirstTdCol = new TextView(cheatViewPageIndicator);
            tvFirstTdCol.setText(firstTdColumn);
            tvFirstTdCol.setPadding(1, 1, 10, 1);
            tvFirstTdCol.setMinimumWidth(Konstanten.TABLE_ROW_MINIMUM_WIDTH);
            tvFirstTdCol.setTextAppearance(cheatViewPageIndicator, R.style.NormalText);
            tvFirstTdCol.setTypeface(latoFontLight);
            trTd.addView(tvFirstTdCol);

            TextView tvSecondTdCol = new TextView(cheatViewPageIndicator);
            tvSecondTdCol.setText(secondTdColumn);
            tvSecondTdCol.setPadding(10, 1, 30, 1);
            tvSecondTdCol.setTextAppearance(cheatViewPageIndicator, R.style.NormalText);
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
            tvCheatText.setTextAppearance(cheatViewPageIndicator, R.style.WalkthroughText);
        }
    }

    // public void highlightRatingIcon(boolean highlight) {
    // if (highlight) {
    // btnRateCheat.setImageResource(R.drawable.ic_action_star);
    // } else {
    // btnRateCheat.setImageResource(R.drawable.ic_action_not_important);
    // }
    // }

    @Override
    public void onResume() {
        super.onResume();
        // TODO update member rating
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
                editor.putFloat("c" + cheatObj.getCheatId(), cheatRating);
                editor.commit();

                cheatViewPageIndicator.setRating(offset, cheatRating);

                // ratingBar.setRating(cheatRating / 2);
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
                Log.e(TAG, "Remtoe Image Exception", e);
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
                imageViews[i] = new ImageView(cheatViewPageIndicator);
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

}