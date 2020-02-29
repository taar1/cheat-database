package com.cheatdatabase.cheat_detail_view;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.callbacks.GalleryLoadingCallback;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.Game;
import com.cheatdatabase.model.Member;
import com.cheatdatabase.model.Screenshot;
import com.crashlytics.android.Crashlytics;
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
import butterknife.OnClick;
import needle.Needle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * List of Cheats from a Game.
 *
 * @author Dominik Erbsland
 * @version 1.1
 */
public class CheatViewFragment extends Fragment {
    private static final String TAG = "CheatViewFragment";
    private static final String KEY_CONTENT = "CheatViewFragment:Content";

    // TODO FIXME die member bewertung vom cheat holt es noch nicht....
    // TODO FIXME die member bewertung vom cheat holt es noch nicht....
    // TODO FIXME die member bewertung vom cheat holt es noch nicht....
    // TODO FIXME die member bewertung vom cheat holt es noch nicht....
    // TODO FIXME die member bewertung vom cheat holt es noch nicht....
    // TODO FIXME die member bewertung vom cheat holt es noch nicht....

    private LinearLayout outerLinearLayout;

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
    @BindView(R.id.reload)
    ImageView reloadView;

    private int biggestHeight;
    private Cheat cheatObj;
    private List<Cheat> cheatList;
    private Game game;
    private String cheatTitle;
    private int offset;
    private List<ImageView> imageViews;
    private Member member;
    private SharedPreferences settings;
    private Editor editor;
    private String mContent = "???";
    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private CheatViewPageIndicatorActivity cheatViewPageIndicatorActivity;

    public CheatViewFragment() {
        cheatList = new ArrayList<>();
        imageViews = new ArrayList<>();
    }

    public static CheatViewFragment newInstance(String cheatTitle, Game gameObj, int offset) {
        CheatViewFragment cheatViewFragment = new CheatViewFragment();
        cheatViewFragment.game = gameObj;
        cheatViewFragment.cheatTitle = cheatTitle;
        cheatViewFragment.offset = offset;
        return cheatViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        // TODO hier genauer anschauen wegen dem setzen des contents. TODO TODO
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    private void init() {
        cheatViewPageIndicatorActivity = (CheatViewPageIndicatorActivity) getActivity();

        latoFontLight = Tools.getFont(cheatViewPageIndicatorActivity.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(cheatViewPageIndicatorActivity.getAssets(), Konstanten.FONT_BOLD);

        settings = getActivity().getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        outerLinearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_cheat_detail_view, container, false);
        ButterKnife.bind(this, outerLinearLayout);

        if (cheatList != null && game != null) {
            cheatList = game.getCheatList();
            cheatObj = cheatList.get(offset);

            tvCheatTitle.setText(cheatObj.getCheatTitle());

            tvTextBeforeTable.setVisibility(View.VISIBLE);
            tvGalleryInfo.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            if (Reachability.reachability.isReachable) {
                getOnlineContent();
            } else {
                reloadView.setVisibility(View.VISIBLE);
                Toast.makeText(cheatViewPageIndicatorActivity, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        } else {
            Tools.showSnackbar(outerLinearLayout, getString(R.string.err_data_not_accessible));
        }

        countForumPosts();
        return outerLinearLayout;
    }

    @OnClick(R.id.reload)
    void clickReload() {
        if (Reachability.reachability.isReachable) {
            getOnlineContent();
        } else {
            Toast.makeText(cheatViewPageIndicatorActivity, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void getOnlineContent() {
        reloadView.setVisibility(View.GONE);

        /**
         * Get thumbnails if there are screenshots.
         */
        if (cheatObj.isScreenshots()) {
            biggestHeight = 100; // setMemberList value
            progressBar.setVisibility(View.VISIBLE);

            getScreenshotsOnline(cheatObj, new GalleryLoadingCallback() {
                @Override
                public void success(List<Bitmap> bitmapList) {
                    progressBar.setVisibility(View.GONE);
                    displayScreenshotsInGallery(bitmapList);
                }

                @Override
                public void fail(Exception e) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            tvGalleryInfo.setVisibility(View.GONE);
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
        String[] textBeforeTable = null;

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
        TableRow trTh = new TableRow(cheatViewPageIndicatorActivity);
        trTh.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        TextView tvFirstThCol = new TextView(cheatViewPageIndicatorActivity);
        tvFirstThCol.setText(Html.fromHtml(firstThColumn));
        tvFirstThCol.setPadding(1, 1, 5, 1);
        tvFirstThCol.setMinimumWidth(Konstanten.TABLE_ROW_MINIMUM_WIDTH);
        tvFirstThCol.setTextAppearance(cheatViewPageIndicatorActivity, R.style.NormalText);
        tvFirstThCol.setTypeface(latoFontLight);
        trTh.addView(tvFirstThCol);

        TextView tvSecondThCol = new TextView(cheatViewPageIndicatorActivity);
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
            TableRow trTd = new TableRow(cheatViewPageIndicatorActivity);
            trTd.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            TextView tvFirstTdCol = new TextView(cheatViewPageIndicatorActivity);
            tvFirstTdCol.setSingleLine(false);
            tvFirstTdCol.setText(firstTdColumn);
            tvFirstTdCol.setPadding(1, 1, 10, 1);
            tvFirstTdCol.setMinimumWidth(Konstanten.TABLE_ROW_MINIMUM_WIDTH);
            tvFirstTdCol.setTextAppearance(getContext(), R.style.NormalText);
            tvFirstTdCol.setTypeface(latoFontLight);
            trTd.addView(tvFirstTdCol);

            TextView tvSecondTdCol = new TextView(cheatViewPageIndicatorActivity);
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

        mainTable.setOnClickListener(view -> displayTableInWebview());

    }

    private void fillSimpleContent() {
        mainTable.setVisibility(View.GONE);
        tvTextBeforeTable.setVisibility(View.GONE);

        if (cheatObj != null) {
            CharSequence styledText = Html.fromHtml(cheatObj.getCheatText());
            tvCheatText.setText(styledText);

            if (cheatObj.isWalkthroughFormat()) {
                tvCheatText.setTextAppearance(getContext(), R.style.WalkthroughText);
            }
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

    private void getCheatText() {
        Needle.onBackgroundThread().execute(() -> setCheatText(Webservice.getCheatById(cheatObj.getCheatId())));
    }

    private void countForumPosts() {
        Log.d(TAG, "countForumPosts: " + cheatObj.getCheatId());
        Call<JsonObject> call = cheatViewPageIndicatorActivity.getRestApi().countForumPosts(cheatObj.getCheatId());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> forum, Response<JsonObject> response) {
                JsonObject forumPostsCount = response.body();
                int forumCount = forumPostsCount.get("forumCount").getAsInt();
                cheatObj.setForumCount(forumCount);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable e) {
                // Do nothing
            }
        });
    }

    private void setCheatText(String fullCheatText) {
        if (fullCheatText != null && fullCheatText.length() > 1) {
            if (fullCheatText.substring(0, 1).equalsIgnoreCase("2")) {
                cheatObj.setWalkthroughFormat(true);
            }
            cheatObj.setCheatText(fullCheatText.substring(1));

            Needle.onMainThread().execute(() -> populateView());
        }
    }

    private void getScreenshotsOnline(Cheat cheat, GalleryLoadingCallback callback) {
        List<Bitmap> bitmapList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);

        Needle.onBackgroundThread().execute(() -> {
            try {
                List<Screenshot> screens = cheat.getScreenshotList();

                if (screens != null) {
                    for (Screenshot s : screens) {
                        String screenUrl = Konstanten.SCREENSHOT_ROOT_WEBDIR + "image.php?width=150&image=/cheatpics/" + s.getCheatId() + s.getFilename();

                        // Open a new URL and get the InputStream to load data from it.
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
                }

                Needle.onMainThread().execute(() -> callback.success(bitmapList));
            } catch (IOException e) {
                Log.e(TAG, "Remote Image Exception", e);
                Needle.onMainThread().execute(() -> callback.fail(e));
            }
        });
    }

    void displayScreenshotsInGallery(List<Bitmap> bitmapList) {
        imageViews = new ArrayList<>();

        for (Bitmap b : bitmapList) {
            ImageView iv = new ImageView(cheatViewPageIndicatorActivity);
            iv.setScaleType(ImageView.ScaleType.MATRIX);
            iv.setLayoutParams(new Gallery.LayoutParams(300, biggestHeight));
            iv.setImageBitmap(b);

            imageViews.add(iv);
        }

        if ((cheatObj.getScreenshotList() == null) || (cheatObj.getScreenshotList().size() <= 1)) {
            tvGalleryInfo.setVisibility(View.GONE);
        } else {
            tvGalleryInfo.setVisibility(View.VISIBLE);
        }

        try {
            screenshotGallery.setAdapter(new ImageAdapter(cheatViewPageIndicatorActivity));
            screenshotGallery.setOnItemClickListener((parent, v, position, id) -> {
                Screenshot screenShot = cheatObj.getScreenshotList().get(position);

                Uri uri = Uri.parse(Konstanten.SCREENSHOT_ROOT_WEBDIR + screenShot.getCheatId() + screenShot.getFilename());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                cheatViewPageIndicatorActivity.startActivity(intent);
            });
        } catch (ActivityNotFoundException e) {
            Crashlytics.logException(e);
        }
    }

    /**
     * Inner class to display gallery thumbnails
     */
    public class ImageAdapter extends BaseAdapter {

        /**
         * Simple Constructor saving the 'parent' context.
         */
        ImageAdapter(Context c) {
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
         * Returns a new ImageView to be displayed, depending on the position passed.
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

}