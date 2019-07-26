package com.cheatdatabase.members.cheatview;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

public class MemberCheatViewFragment extends Fragment {
    private static final String TAG = MemberCheatViewFragment.class.getSimpleName();

    LinearLayout linearLayout;

    @BindView(R.id.table_cheat_list_main)
    TableLayout mainTable;
    @BindView(R.id.cheat_content)
    TextView tvCheatText;
    @BindView(R.id.text_cheat_before_table)
    TextView tvTextBeforeTable;
    @BindView(R.id.text_cheat_title)
    TextView tvCheatTitle;
    @BindView(R.id.gallery_info)
    TextView tvGalleryInfo;
    @BindView(R.id.gallery)
    Gallery screenshotGallery;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private int biggestHeight;

    private Cheat cheatObj;
    private List<Cheat> cheats;
    private int offset;
    private List<ImageView> imageViews;
    private Member member;

    private SharedPreferences settings;
    private Editor editor;

    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private MemberCheatViewPageIndicator cheatViewPageIndicator;

    private static final String KEY_CONTENT = "MemberCheatViewFragment:Content";

    public MemberCheatViewFragment() {
        imageViews = new ArrayList<>();
    }

    public static MemberCheatViewFragment newInstance(List<Cheat> cheats, int offset) {
        MemberCheatViewFragment fragment = new MemberCheatViewFragment();
        fragment.cheats = cheats;
        fragment.offset = offset;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
//            mContent = savedInstanceState.getString(KEY_CONTENT);
//        }

        cheatViewPageIndicator = (MemberCheatViewPageIndicator) getActivity();

        latoFontLight = Tools.getFont(cheatViewPageIndicator.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(cheatViewPageIndicator.getAssets(), Konstanten.FONT_BOLD);

        settings = cheatViewPageIndicator.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putString(KEY_CONTENT, mContent);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        getFragmentRelevantData();

        try {
            linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_member_cheat_detail, container, false);
            ButterKnife.bind(this, linearLayout);

            member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
            cheatObj = cheats.get(offset);

            getCheatRating();

//            mainTable = linearLayout.findViewById(R.id.table_cheat_list_main);
//            tvTextBeforeTable = linearLayout.findViewById(R.id.text_cheat_before_table);
//            tvCheatTitle = linearLayout.findViewById(R.id.text_cheat_title);
//            tvGalleryInfo = linearLayout.findViewById(R.id.gallery_info);
//            screenshotGallery = linearLayout.findViewById(R.id.gallery);
//            progressBar = linearLayout.findViewById(R.id.progress_bar);
//            tvCheatText = linearLayout.findViewById(R.id.cheat_content);

            tvTextBeforeTable.setVisibility(View.VISIBLE);
            tvCheatTitle.setTypeface(latoFontBold);
            tvCheatTitle.setText(cheatObj.getCheatTitle());
            tvGalleryInfo.setVisibility(View.INVISIBLE);
            tvGalleryInfo.setTypeface(latoFontLight);
            progressBar.setVisibility(View.INVISIBLE);
            tvCheatText.setTypeface(latoFontLight);

            /**
             * Get thumbnails if there are screenshots.
             */
            if (cheatObj.isScreenshots()) {
                biggestHeight = 100; // setMemberList value
                progressBar.setVisibility(View.VISIBLE);

                loadScreenshots();
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
                // if ((isFromSearchResults == true) || (cheat.getFullCheatText() ==
                // null) || (cheat.getFullCheatText().length() < 10)) {
                progressBar.setVisibility(View.VISIBLE);
                getFullCheatText();
            } else {
                progressBar.setVisibility(View.GONE);
                populateView();
            }

            editor.putString("cheat" + offset, new Gson().toJson(cheatObj));
            editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "BB: " + e.getMessage());
        }

        return linearLayout;
    }

//    private void getFragmentRelevantData() {
//        Bundle arguments = getArguments();
//        try {
//            cheats = (ArrayList<Cheat>) arguments.getSerializable("cheatObj");
//            offset = arguments.getInt("offset");
//        } catch (Exception e) {
//            offset = 0;
//            // TODO message ausgeben, dass kein Game objekt besteht
//            Log.e("Error", e.getLocalizedMessage());
//        }
//    }

    private void buildGallery() {
        screenshotGallery.setAdapter(new ImageAdapter(cheatViewPageIndicator));
        screenshotGallery.setOnItemClickListener((parent, v, position, id) -> {
            Screenshot screenShot = cheatObj.getScreenshotList().get(position);

            Uri uri = Uri.parse(Konstanten.SCREENSHOT_ROOT_WEBDIR + screenShot.getCheatId() + screenShot.getFilename());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
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
        mainTable.setVisibility(View.VISIBLE);
        mainTable.setHorizontalScrollBarEnabled(true);

        // Cheat Text oberhalb der Tabelle
        String[] textBeforeTable;

        // Einige tabellarische Cheats beginnen direkt mit der Tabelle
        if (cheatObj.getCheatText().startsWith("<br><table")) {
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
        tvSecondThCol.setTextAppearance(getContext(), R.style.NormalText);
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
            tvFirstTdCol.setTextAppearance(getContext(), R.style.NormalText);
            tvFirstTdCol.setTypeface(latoFontLight);
            trTd.addView(tvFirstTdCol);

            TextView tvSecondTdCol = new TextView(cheatViewPageIndicator);
            tvSecondTdCol.setSingleLine(false);
            tvSecondTdCol.setText(secondTdColumn);
            tvSecondTdCol.canScrollHorizontally(1);
            tvSecondTdCol.setPadding(10, 1, 30, 1);
            tvSecondTdCol.setTextAppearance(getContext(), R.style.NormalText);
            tvSecondTdCol.setTypeface(latoFontLight);
            trTd.addView(tvSecondTdCol);

            /* Add row to TableLayout. */
            mainTable.addView(trTd, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }

        mainTable.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                displayTableInWebview();
            }
        });
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

    private void displayTableInWebview() {
        MaterialDialog md = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.layout_cheat_content_table, true)
                .theme(Theme.DARK)
                .positiveText(R.string.close)
                .cancelable(true)
                .show();

        View dialogView = md.getCustomView();

        WebView webview = dialogView.findViewById(R.id.webview);
        webview.loadDataWithBaseURL("", cheatObj.getCheatText(), "text/html", "UTF-8", "");
    }

    @Override
    public void onResume() {
        super.onResume();

        getResources().getConfiguration();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

//    @Override
//    public void onClick(View v) {
//        Log.d("onClick", "onClick");
//        Bundle arguments = new Bundle();
//        arguments.putInt("CHANGEME", 1);
//        arguments.putSerializable("cheatObj", cheatObj);
//    }

//    private class FetchCheatTextTask extends AsyncTask<Void, Void, Void> {
//
//        String fullCheatText;
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            fullCheatText = Webservice.getCheatById(cheatObj.getCheatId());
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//            if (fullCheatText.substring(0, 1).equalsIgnoreCase("2")) {
//                cheatObj.setWalkthroughFormat(true);
//            }
//            progressBar.setVisibility(View.GONE);
//            cheatObj.setCheatText(fullCheatText.substring(1));
//
//            populateView();
//        }
//    }

    private void getFullCheatText() {
        Needle.onBackgroundThread().execute(() -> updateUI(Webservice.getCheatById(cheatObj.getCheatId())));
    }

    private void updateUI(String fullCheatText) {
        Needle.onMainThread().execute(() -> {
            if (fullCheatText.substring(0, 1).equalsIgnoreCase("2")) {
                cheatObj.setWalkthroughFormat(true);
            }
            progressBar.setVisibility(View.GONE);
            cheatObj.setCheatText(fullCheatText.substring(1));

            populateView();
        });
    }

//    private class FetchCheatRatingOnlineBackgroundTask extends AsyncTask<Void, Void, Void> {
//        float cheatRating;
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            try {
//                cheatRating = Webservice.getCheatRatingByMemberId(member.getMid(), cheatObj.getCheatId());
//            } catch (Exception e) {
//                cheatRating = 0;
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//
//            if (cheatRating > 0) {
//                cheatViewPageIndicator.setRating(offset, cheatRating);
//            }
//        }
//    }

    private void getCheatRating() {
        Needle.onBackgroundThread().execute(() -> {
            float cheatRating = 0;
            try {
                cheatRating = Webservice.getCheatRatingByMemberId(member.getMid(), cheatObj.getCheatId());
            } catch (Exception e) {
            }
            updateRatingUI(cheatRating);
        });
    }

    private void updateRatingUI(float cheatRating) {
        if (cheatRating > 0) {
            cheatViewPageIndicator.setRating(offset, cheatRating);
        }
    }

    private void loadScreenshots() {
        Needle.onBackgroundThread().execute(() -> {
            List<Bitmap> bitmapList = new ArrayList<>();

            try {
                List<Screenshot> screens = cheatObj.getScreenshotList();

                for (Screenshot s : screens) {
                    String screenUrl = Konstanten.SCREENSHOT_ROOT_WEBDIR + "image.php?width=150&image=/cheatpics/" + s.getCheatId() + s.getFilename();

                    /*
                     * Open a new URL and get the InputStream to load data from it.
                     */
                    URL aURL = new URL(screenUrl);
                    URLConnection conn = aURL.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    /* Buffered is always good for a performance plus. */
                    BufferedInputStream bis = new BufferedInputStream(is);
                    /* Decode url-data to a bitmap. */
                    Bitmap bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    is.close();

                    bitmapList.add(bm);

                    if (biggestHeight < bm.getHeight()) {
                        biggestHeight = bm.getHeight();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Remote Image Exception", e);
            }

            prepareGallery(bitmapList);
        });
    }

    private void prepareGallery(List<Bitmap> bitmapList) {
        // Apply the Bitmap to the ImageView that will be returned.
        for (Bitmap b : bitmapList) {
            ImageView iv = new ImageView(cheatViewPageIndicator);
            iv.setScaleType(ImageView.ScaleType.MATRIX);
            iv.setLayoutParams(new Gallery.LayoutParams(300, biggestHeight));
            iv.setImageBitmap(b);

            imageViews.add(iv);
        }

        Needle.onMainThread().execute(() -> {
            progressBar.setVisibility(View.GONE);
            if (cheatObj.getScreenshotList().size() <= 1) {
                tvGalleryInfo.setVisibility(View.GONE);
            } else {
                tvGalleryInfo.setVisibility(View.VISIBLE);
            }
            buildGallery();
        });
    }

    /**
     * Innere Klasse zum Anzeigen der Screenshot-Thumbnails
     * <p/>
     * Copyright (c) 2010-2018<br>
     *
     * @author Dominik Erbsland
     * @version 1.1
     */
    public class ImageAdapter extends BaseAdapter {

        /**
         * Simple Constructor saving the 'parent' context.
         */
        public ImageAdapter(Context c) {
        }

        @Override
        public int getCount() {
            return imageViews.size();
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
            return imageViews.get(position);
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
        CheatMetaDialog cmDialog = new CheatMetaDialog(cheatViewPageIndicator, cheatObj);
        cmDialog.show();
    }

}