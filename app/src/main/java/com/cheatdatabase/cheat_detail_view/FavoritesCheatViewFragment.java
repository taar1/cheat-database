package com.cheatdatabase.cheat_detail_view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import needle.Needle;

/**
 * List of all cheatList for a game optimized for handsets.
 *
 * @author Dominik Erbsland
 * @version 1.0
 */
public class FavoritesCheatViewFragment extends Fragment {

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
    @BindView(R.id.reload)
    ImageView reloadView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private int biggestHeight;
    private Cheat cheatObj;
    private List<Cheat> cheatList;
    private Game game;
    private int offset;
    private List<ImageView> imageViewList;
    private Member member;

    private SharedPreferences settings;
    private Editor editor;

    private static final String KEY_CONTENT = "CheatViewFragment:Content";
    private static final String TAG = FavoritesCheatViewFragment.class.getSimpleName();

    public static FavoritesCheatViewFragment newInstance(String content, Game game, int offset) {
        FavoritesCheatViewFragment fragment = new FavoritesCheatViewFragment();

        Bundle args = new Bundle();
        args.putParcelable("gameObj", game);
        args.putInt("offset", offset);
        fragment.setArguments(args);

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
    private FavoritesCheatViewPageIndicator ca;

    public FavoritesCheatViewFragment() {
        imageViewList = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ca = (FavoritesCheatViewPageIndicator) getActivity();

        latoFontLight = Tools.getFont(ca.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(ca.getAssets(), Konstanten.FONT_BOLD);

        settings = getActivity().getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        // TODO hier genauer anschauen wegen dem setzen des contents. TODO TODO
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_cheat_detail_handset, container, false);
        ButterKnife.bind(this, linearLayout);

        getFragmentRelevantData();

        cheatList = game.getCheatList();
        cheatObj = cheatList.get(offset);
        getCheatRating();

        tvCheatTitle.setText(cheatObj.getCheatTitle());

        tvTextBeforeTable.setTypeface(latoFontLight);
        tvCheatTitle.setTypeface(latoFontBold);
        tvGalleryInfo.setTypeface(latoFontLight);
        tvCheatText.setTypeface(latoFontLight);

        tvTextBeforeTable.setVisibility(View.VISIBLE);
        tvGalleryInfo.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        if (Reachability.reachability.isReachable) {
            getOnlineContent();
        } else {
            reloadView.setVisibility(View.VISIBLE);
            Toast.makeText(ca, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        return linearLayout;
    }

    @OnClick(R.id.text_cheat_before_table)
    void clickTextCheatBeforeTable() {
        Bundle arguments = new Bundle();
        arguments.putInt("CHANGEME", 1);
        arguments.putParcelable("cheatObj", cheatObj);
    }

    @OnClick(R.id.reload)
    void clickReload() {
        if (Reachability.reachability.isReachable) {
            getOnlineContent();
        } else {
            Toast.makeText(ca, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void getOnlineContent() {
        reloadView.setVisibility(View.GONE);

        /**
         * Get thumbnails if there are screenshots.
         */
        if (cheatObj.isScreenshots()) {
            biggestHeight = 100;
            progressBar.setVisibility(View.VISIBLE);

            loadScreenshots();
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

            getCheatText();
        } else {
            populateView();
        }

        editor.putString("cheat" + offset, new Gson().toJson(cheatObj));
        editor.commit();
    }

    private void getFragmentRelevantData() {
        Bundle arguments = getArguments();
        try {
            game = arguments.getParcelable("gameObj");
            offset = arguments.getInt("offset");
        } catch (Exception e) {
            offset = 0;
            // TODO display error message
            Log.e("Error", e.getLocalizedMessage());
        }
    }

    private void buildGallery() {
        screenshotGallery.setAdapter(new ImageAdapter(ca));
        screenshotGallery.setOnItemClickListener((parent, v, position, id) -> {
            List<Screenshot> screens = cheatObj.getScreenshotList();
            Screenshot screenShot = screens.get(position);

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

        mainTable.setColumnShrinkable(0, true);
        mainTable.setVisibility(View.VISIBLE);

        // Cheat-Text oberhalb der Tabelle
        String[] textBeforeTable = null;

        // Einige tabellarische Cheats beginnen direkt mit der Tabelle
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

    private void getCheatText() {
        Needle.onBackgroundThread().execute(() -> setCheatText(Webservice.getCheatById(cheatObj.getCheatId())));
    }

    private void setCheatText(String fullCheatText) {
        Needle.onMainThread().execute(() -> {
            if (fullCheatText.substring(0, 1).equalsIgnoreCase("2")) {
                cheatObj.setWalkthroughFormat(true);
            }
            progressBar.setVisibility(View.GONE);
            cheatObj.setCheatText(fullCheatText.substring(1));

            populateView();
        });

    }

    private void getCheatRating() {
        Needle.onBackgroundThread().execute(() -> {
            float cheatRating = 0;
            try {
                cheatRating = Webservice.getCheatRatingByMemberId(member.getMid(), cheatObj.getCheatId());
            } catch (Exception e) {
            }

            setCheatRatingOnUI(cheatRating);
        });
    }

    private void setCheatRatingOnUI(float cheatRating) {
        Needle.onMainThread().execute(() -> {
            if (cheatRating > 0) {
                editor.putFloat("c" + cheatObj.getCheatId(), cheatRating);
                editor.apply();

                ca.setRating(offset, cheatRating);
            }
        });
    }

    private void loadScreenshots() {
        Needle.onBackgroundThread().execute(() -> {
            List<Bitmap> bitmapList = new ArrayList<>();

            try {
                List<Screenshot> screens = cheatObj.getScreenshotList();

                for (int i = 0; i < screens.size(); i++) {
                    Screenshot s = screens.get(i);
                    String filename = s.getCheatId() + s.getFilename();
                    String myRemoteUrl = Konstanten.SCREENSHOT_ROOT_WEBDIR + "image.php?width=150&image=/cheatpics/" + filename;

                    /*
                     * Open a new URL and get the InputStream to load data from it.
                     */
                    URL aURL = new URL(myRemoteUrl);
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
                Log.e(TAG, "Remote Image Exception: " + e.getLocalizedMessage());
            }


            updateUI(bitmapList);
        });
    }

    private void updateUI(List<Bitmap> bitmapList) {
        Needle.onMainThread().execute(() -> {
            // Apply the Bitmap to the ImageView that will be returned.
            for (Bitmap bm : bitmapList) {
                ImageView iv = new ImageView(ca);
                iv.setScaleType(ImageView.ScaleType.MATRIX);
                iv.setLayoutParams(new Gallery.LayoutParams(300, biggestHeight));
                iv.setImageBitmap(bm);

                imageViewList.add(iv);
            }

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
            return imageViewList.size();
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
         * Returns a new ImageView to be displayed, depending on the position passed.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return imageViewList.get(position);
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