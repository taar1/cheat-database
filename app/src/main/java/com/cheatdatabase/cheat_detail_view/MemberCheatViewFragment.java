package com.cheatdatabase.cheat_detail_view;

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

import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.Member;
import com.cheatdatabase.model.Screenshot;
import com.cheatdatabase.rest.RestApi;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private RestApi restApi;

    private static final String KEY_CONTENT = "MemberCheatViewFragment:Content";
    private LinearLayout outerLayout;
    private Context context;

    public MemberCheatViewFragment() {
        imageViews = new ArrayList<>();
    }

    public static MemberCheatViewFragment newInstance(List<Cheat> cheats, int offset, RestApi restApi, LinearLayout outerLayout, Context context) {
        MemberCheatViewFragment fragment = new MemberCheatViewFragment();
        fragment.cheats = cheats;
        fragment.offset = offset;
        fragment.restApi = restApi;
        fragment.outerLayout = outerLayout;
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "XXXXX onCreateasdfasdfasdf: ");

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

            tvTextBeforeTable.setVisibility(View.VISIBLE);
            tvCheatTitle.setText(cheatObj.getCheatTitle());
            tvGalleryInfo.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            Log.d(TAG, "onCreateView: 1");

            // TODO FIXME hier hat das objekt keine screenshots, obwohl es screenshots hätte....
            // TODO FIXME hier hat das objekt keine screenshots, obwohl es screenshots hätte....
            // TODO FIXME hier hat das objekt keine screenshots, obwohl es screenshots hätte....
            // TODO FIXME hier hat das objekt keine screenshots, obwohl es screenshots hätte....
            // TODO FIXME hier hat das objekt keine screenshots, obwohl es screenshots hätte....

            /**
             * Get thumbnails if there are screenshots.
             */
            if (cheatObj.isScreenshots()) {
                // TODO FIXME hier kommt man nicht rein auch wenn der cheat screenshots hat...
                // TODO FIXME hier kommt man nicht rein auch wenn der cheat screenshots hat...
                // TODO FIXME hier kommt man nicht rein auch wenn der cheat screenshots hat...
                // TODO FIXME hier kommt man nicht rein auch wenn der cheat screenshots hat...
                Log.d(TAG, "onCreateView: 2");

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
            // boolean isFromSearchResults = true;
            if ((cheatObj.getCheatText() == null) || (cheatObj.getCheatText().length() < 10)) {
                // if ((isFromSearchResults == true) || (cheat.getFullCheatText() ==
                // null) || (cheat.getFullCheatText().length() < 10)) {
                progressBar.setVisibility(View.VISIBLE);
                getCheatBody();
            } else {
                progressBar.setVisibility(View.GONE);
                populateView();
            }

            editor.putString("cheat" + offset, new Gson().toJson(cheatObj));
            editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: ", e);
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

        // Text before the table
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

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }

    private void getCheatBody() {
        Call<Cheat> call = restApi.getCheatById(cheatObj.getCheatId());
        call.enqueue(new Callback<Cheat>() {
            @Override
            public void onResponse(Call<Cheat> metaInfo, Response<Cheat> response) {
                updateUI(response.body());
            }

            @Override
            public void onFailure(Call<Cheat> call, Throwable e) {
                Log.e(TAG, "getCheatBody onFailure: " + e.getLocalizedMessage());
                Tools.showSnackbar(outerLayout, context.getString(R.string.err_somethings_wrong), 5000);
            }
        });
    }

    private void updateUI(Cheat cheat) {
        if (cheat.getStyle() == Konstanten.CHEAT_TEXT_FORMAT_WALKTHROUGH) {
            cheatObj.setWalkthroughFormat(true);
        }
        progressBar.setVisibility(View.GONE);
        cheatObj.setCheatText(cheat.getCheatText());

        populateView();
    }

    private void getCheatRating() {
        if (member != null) {
            Call<JsonObject> call = restApi.getMemberRatingByCheatId(member.getMid(), cheatObj.getCheatId());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> ratingInfo, Response<JsonObject> response) {
                    JsonObject ratingJsonObj = response.body();
                    Log.d(TAG, "getCheatRating SUCCESS: " + ratingJsonObj.get("rating").getAsFloat());

                    float cheatRating = ratingJsonObj.get("rating").getAsFloat();
                    if (cheatRating > 0) {
                        cheatViewPageIndicator.setRating(offset, cheatRating);
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable e) {
                }
            });
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
     * Copyright (c) 2010-2020<br>
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

    }
}