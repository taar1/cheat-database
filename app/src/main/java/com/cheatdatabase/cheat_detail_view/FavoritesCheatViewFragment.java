package com.cheatdatabase.cheat_detail_view;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.adapters.CheatViewGalleryListAdapter;
import com.cheatdatabase.callbacks.CheatViewGalleryImageClickListener;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.Game;
import com.cheatdatabase.model.Member;
import com.cheatdatabase.model.Screenshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * List of all cheatList for a game optimized for handsets.
 *
 * @author Dominik Erbsland
 * @version 1.0
 */
public class FavoritesCheatViewFragment extends Fragment implements CheatViewGalleryImageClickListener {

    private static final String TAG = FavoritesCheatViewFragment.class.getSimpleName();

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
    @BindView(R.id.gallery_recycler_view)
    RecyclerView galleryRecyclerView;
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
    private LinearLayout outerLayout;

    private SharedPreferences settings;
    private Editor editor;

    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private FavoritesCheatViewPageIndicator favoritesCheatViewPageIndicatorActivity;

    // TODO FIXME screenshots should be downloaded to device so the cheat can be displayed without using the internet
    // TODO FIXME screenshots should be downloaded to device so the cheat can be displayed without using the internet
    // TODO FIXME screenshots should be downloaded to device so the cheat can be displayed without using the internet

    public static FavoritesCheatViewFragment newInstance(Game game, int offset, LinearLayout outerLayout) {
        FavoritesCheatViewFragment fragment = new FavoritesCheatViewFragment();
        fragment.game = game;
        fragment.offset = offset;
        fragment.outerLayout = outerLayout;
        return fragment;
    }

    public FavoritesCheatViewFragment() {
        imageViewList = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the screen has been rotated we re-set the values
        if (savedInstanceState != null) {
            game = savedInstanceState.getParcelable("game");
            offset = savedInstanceState.getInt("offset");
        }

        favoritesCheatViewPageIndicatorActivity = (FavoritesCheatViewPageIndicator) getActivity();

        latoFontLight = Tools.getFont(favoritesCheatViewPageIndicatorActivity.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(favoritesCheatViewPageIndicatorActivity.getAssets(), Konstanten.FONT_BOLD);

        settings = getActivity().getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("game", game);
        outState.putInt("offset", offset);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_cheat_detail_view, container, false);
        ButterKnife.bind(this, linearLayout);

        cheatList = game.getCheatList();
        cheatObj = cheatList.get(offset);
        getCheatRating();

        tvCheatTitle.setText(cheatObj.getCheatTitle());

        tvTextBeforeTable.setVisibility(View.VISIBLE);
        tvGalleryInfo.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        if (Reachability.reachability.isReachable) {
            getOnlineContent();
        } else {
            reloadView.setVisibility(View.VISIBLE);
            Toast.makeText(favoritesCheatViewPageIndicatorActivity, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        return linearLayout;
    }

    @OnClick(R.id.text_cheat_before_table)
    void clickTextCheatBeforeTable() {
        Bundle arguments = new Bundle();
        arguments.putParcelable("cheatObj", cheatObj);
    }

    @OnClick(R.id.reload)
    void clickReload() {
        if (Reachability.reachability.isReachable) {
            getOnlineContent();
        } else {
            Toast.makeText(favoritesCheatViewPageIndicatorActivity, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void getOnlineContent() {
        Log.d(TAG, "XXXXX getOnlineContent: ");
        reloadView.setVisibility(View.GONE);

        // Get thumbnails if there are screenshots.

        // TODO FIXME isScreenshots() existiert nicht in der favorites ROOM tabelle... manuell durchgehen und schauen ob screenshots auf der SD karte existieren....
        // TODO FIXME isScreenshots() existiert nicht in der favorites ROOM tabelle... manuell durchgehen und schauen ob screenshots auf der SD karte existieren....
        // TODO FIXME isScreenshots() existiert nicht in der favorites ROOM tabelle... manuell durchgehen und schauen ob screenshots auf der SD karte existieren....
        // TODO FIXME isScreenshots() existiert nicht in der favorites ROOM tabelle... manuell durchgehen und schauen ob screenshots auf der SD karte existieren....
        // TODO FIXME isScreenshots() existiert nicht in der favorites ROOM tabelle... manuell durchgehen und schauen ob screenshots auf der SD karte existieren....
        // TODO FIXME isScreenshots() existiert nicht in der favorites ROOM tabelle... manuell durchgehen und schauen ob screenshots auf der SD karte existieren....

        if (cheatObj.isScreenshots()) {
            CheatViewGalleryListAdapter cheatViewGalleryListAdapter = new CheatViewGalleryListAdapter();
            cheatViewGalleryListAdapter.setScreenshotList(cheatObj.getScreenshotList());

            ArrayList<String> screenshotUrlList = new ArrayList<>();
            for (Screenshot s : cheatObj.getScreenshotList()) {
                Log.d(TAG, "XXXXX path on SD: " + s.getFullPathOnSdCard());
                screenshotUrlList.add(s.getFullPathOnSdCard());
            }
            cheatViewGalleryListAdapter.setScreenshotUrlList(screenshotUrlList);

            cheatViewGalleryListAdapter.setClickListener(this);

            galleryRecyclerView.setAdapter(cheatViewGalleryListAdapter);
            RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(favoritesCheatViewPageIndicatorActivity, 2, GridLayoutManager.HORIZONTAL, false);
            galleryRecyclerView.setLayoutManager(gridLayoutManager);

            if ((cheatObj.getScreenshotList() == null) || (cheatObj.getScreenshotList().size() <= 3)) {
                tvGalleryInfo.setVisibility(View.GONE);
            } else {
                tvGalleryInfo.setVisibility(View.VISIBLE);
            }

        } else {
            tvGalleryInfo.setVisibility(View.GONE);
            galleryRecyclerView.setVisibility(View.GONE);
        }

        /**
         * If the user came from the search results the cheat-text might not be
         * complete (trimmed for the search results) and therefore has to be
         * re-fetched in a background process.
         */
        if ((cheatObj.getCheatText() == null) || (cheatObj.getCheatText().length() < 10)) {
            progressBar.setVisibility(View.VISIBLE);
            getCheatBody();
        } else {
            progressBar.setVisibility(View.GONE);
            populateView();
        }

        editor.putString("cheat" + offset, new Gson().toJson(cheatObj));
        editor.apply();
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
        TableRow trTh = new TableRow(favoritesCheatViewPageIndicatorActivity);
        trTh.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        TextView tvFirstThCol = new TextView(favoritesCheatViewPageIndicatorActivity);
        tvFirstThCol.setText(Html.fromHtml(firstThColumn));
        tvFirstThCol.setPadding(1, 1, 5, 1);
        tvFirstThCol.setMinimumWidth(Konstanten.TABLE_ROW_MINIMUM_WIDTH);
        tvFirstThCol.setTextAppearance(favoritesCheatViewPageIndicatorActivity, R.style.NormalText);
        tvFirstThCol.setTypeface(latoFontLight);
        trTh.addView(tvFirstThCol);

        TextView tvSecondThCol = new TextView(favoritesCheatViewPageIndicatorActivity);
        tvSecondThCol.setText(Html.fromHtml(secondThColumn));
        tvSecondThCol.setPadding(5, 1, 1, 1);
        tvSecondThCol.setTextAppearance(favoritesCheatViewPageIndicatorActivity, R.style.NormalText);
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
            TableRow trTd = new TableRow(favoritesCheatViewPageIndicatorActivity);
            trTd.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            TextView tvFirstTdCol = new TextView(favoritesCheatViewPageIndicatorActivity);
            tvFirstTdCol.setText(firstTdColumn);
            tvFirstTdCol.setPadding(1, 1, 10, 1);
            tvFirstTdCol.setMinimumWidth(Konstanten.TABLE_ROW_MINIMUM_WIDTH);
            tvFirstTdCol.setTextAppearance(favoritesCheatViewPageIndicatorActivity, R.style.NormalText);
            tvFirstTdCol.setTypeface(latoFontLight);
            trTd.addView(tvFirstTdCol);

            TextView tvSecondTdCol = new TextView(favoritesCheatViewPageIndicatorActivity);
            tvSecondTdCol.setText(secondTdColumn);
            tvSecondTdCol.setPadding(10, 1, 30, 1);
            tvSecondTdCol.setTextAppearance(favoritesCheatViewPageIndicatorActivity, R.style.NormalText);
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
            tvCheatText.setTextAppearance(favoritesCheatViewPageIndicatorActivity, R.style.WalkthroughText);
        }
    }

    private void getCheatBody() {
        Call<Cheat> call = favoritesCheatViewPageIndicatorActivity.getRestApi().getCheatById(cheatObj.getCheatId());
        call.enqueue(new Callback<Cheat>() {
            @Override
            public void onResponse(Call<Cheat> metaInfo, Response<Cheat> response) {
                updateUI(response.body());
            }

            @Override
            public void onFailure(Call<Cheat> call, Throwable e) {
                Log.e(TAG, "getCheatBody onFailure: " + e.getLocalizedMessage());
                Tools.showSnackbar(outerLayout, getContext().getString(R.string.err_somethings_wrong), 5000);
            }
        });
    }

    private void updateUI(Cheat cheat) {
        if (cheat.getStyle() == Konstanten.CHEAT_TEXT_FORMAT_WALKTHROUGH) {
            cheatObj.setWalkthroughFormat(true);
        }
        progressBar.setVisibility(View.GONE);

        if (cheat.getCheatText().substring(0, 1).equalsIgnoreCase("2")) {
            cheat.setCheatText(cheat.getCheatText().substring(1));
        }

        cheatObj.setCheatText(cheat.getCheatText());

        populateView();
    }

    private void getCheatRating() {
        if (member != null) {
            Call<JsonObject> call = favoritesCheatViewPageIndicatorActivity.getRestApi().getMemberRatingByCheatId(member.getMid(), cheatObj.getCheatId());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> ratingInfo, Response<JsonObject> response) {
                    JsonObject ratingJsonObj = response.body();
                    Log.d(TAG, "getCheatRating SUCCESS: " + ratingJsonObj.get("rating").getAsFloat());

                    float cheatRating = ratingJsonObj.get("rating").getAsFloat();
                    if (cheatRating > 0) {
                        favoritesCheatViewPageIndicatorActivity.setRating(offset, cheatRating);
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable e) {
                }
            });
        }
    }


    @Override
    public void onScreenshotClicked(Screenshot screenshot, int position) {
        new StfalconImageViewer.Builder<>(favoritesCheatViewPageIndicatorActivity, cheatObj.getScreenshotList(), (imageView, image) -> Picasso.get().load(image.getFullPath()).placeholder(R.drawable.image_placeholder).into(imageView)).withStartPosition(position).show();
    }

    @Override
    public void onScreenshotUrlClicked(String screenshot, int position) {
        new StfalconImageViewer.Builder<>(favoritesCheatViewPageIndicatorActivity, cheatObj.getScreenshotList(), (imageView, image) -> Picasso.get().load(image.getFullPath()).placeholder(R.drawable.image_placeholder).into(imageView)).withStartPosition(position).show();
    }
}