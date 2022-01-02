package com.cheatdatabase.cheatdetailview;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
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
import com.cheatdatabase.adapters.FavoritesCheatViewGalleryListAdapter;
import com.cheatdatabase.callbacks.FavoritesCheatViewGalleryImageClickListener;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.rest.RestApi;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * List of all cheatList for a game optimized for handsets.
 *
 * @author Dominik Erbsland
 * @version 1.0
 */
@AndroidEntryPoint
public class FavoritesCheatViewFragment extends Fragment implements FavoritesCheatViewGalleryImageClickListener {

    private static final String TAG = FavoritesCheatViewFragment.class.getSimpleName();

    @Inject
    Tools tools;

    @Inject
    RestApi restApi;

    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
    @BindView(R.id.table_cheat_list_main)
    TableLayout mainTable;
    @BindView(R.id.cheat_content)
    TextView tvCheatText;
    @BindView(R.id.text_cheat_before_table)
    TextView tvTextBeforeTable;
    @BindView(R.id.text_cheat_title)
    TextView tvCheatTitle;
    @BindView(R.id.gallery_info)
    TextView tvSwipeHorizontallyInfoText;
    @BindView(R.id.gallery_recycler_view)
    RecyclerView galleryRecyclerView;
    @BindView(R.id.reload)
    ImageView reloadView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private Cheat cheatObj;
    private Game game;
    private int offset;
    private Member member;

    private SharedPreferences settings;
    private Editor editor;

    private FavoritesCheatViewPageIndicator favoritesCheatViewPageIndicatorActivity;
    private List<File> screenshotList;

    public static FavoritesCheatViewFragment newInstance(Game game, int offset) {
        FavoritesCheatViewFragment fragment = new FavoritesCheatViewFragment();
        fragment.game = game;
        fragment.offset = offset;
        return fragment;
    }

    public FavoritesCheatViewFragment() {
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

        settings = getActivity().getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);

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

        List<Cheat> cheatList = game.getCheatList();
        cheatObj = cheatList.get(offset);
        getCheatRating();

        tvCheatTitle.setText(cheatObj.getCheatTitle());

        tvTextBeforeTable.setVisibility(View.VISIBLE);
        tvSwipeHorizontallyInfoText.setVisibility(View.INVISIBLE);
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
        reloadView.setVisibility(View.GONE);

        screenshotList = getScreenshotsOnSdCard();
        if (screenshotList.size() > 0) {
            FavoritesCheatViewGalleryListAdapter cheatViewGalleryListAdapter = new FavoritesCheatViewGalleryListAdapter();
            cheatViewGalleryListAdapter.setScreenshotUrlList(screenshotList);
            cheatViewGalleryListAdapter.setClickListener(this);

            galleryRecyclerView.setAdapter(cheatViewGalleryListAdapter);
            RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(favoritesCheatViewPageIndicatorActivity, 2, GridLayoutManager.HORIZONTAL, false);
            galleryRecyclerView.setLayoutManager(gridLayoutManager);

            if ((cheatObj.getScreenshotList() == null) || (cheatObj.getScreenshotList().size() <= 3)) {
                tvSwipeHorizontallyInfoText.setVisibility(View.GONE);
            } else {
                tvSwipeHorizontallyInfoText.setVisibility(View.VISIBLE);
            }
        } else {
            tvSwipeHorizontallyInfoText.setVisibility(View.GONE);
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

        editor = settings.edit();
        editor.putString("cheat" + offset, new Gson().toJson(cheatObj));
        editor.apply();
    }

    private List<File> getScreenshotsOnSdCard() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + Konstanten.APP_PATH_SD_CARD + cheatObj.getCheatId());
        File[] files = dir.listFiles();

        ArrayList<File> fileList = new ArrayList<>();

        if (files != null && files.length > 0) {
            Arrays.sort(files);

            for (File f : files) {
                Log.d(TAG, "XXXXX getScreenshotsOnSdCard: file: " + f.getAbsolutePath());
                fileList.add(f);
            }
        }

        return fileList;
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
        trTh.addView(tvFirstThCol);

        TextView tvSecondThCol = new TextView(favoritesCheatViewPageIndicatorActivity);
        tvSecondThCol.setText(Html.fromHtml(secondThColumn));
        tvSecondThCol.setPadding(5, 1, 1, 1);
        tvSecondThCol.setTextAppearance(favoritesCheatViewPageIndicatorActivity, R.style.NormalText);
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
            trTd.addView(tvFirstTdCol);

            TextView tvSecondTdCol = new TextView(favoritesCheatViewPageIndicatorActivity);
            tvSecondTdCol.setText(secondTdColumn);
            tvSecondTdCol.setPadding(10, 1, 30, 1);
            tvSecondTdCol.setTextAppearance(favoritesCheatViewPageIndicatorActivity, R.style.NormalText);
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
        Call<Cheat> call = restApi.getCheatById(cheatObj.getCheatId());
        call.enqueue(new Callback<Cheat>() {
            @Override
            public void onResponse(Call<Cheat> metaInfo, Response<Cheat> response) {
                updateUI(response.body());
            }

            @Override
            public void onFailure(Call<Cheat> call, Throwable e) {
                Log.e(TAG, "getCheatBody onFailure: " + e.getLocalizedMessage());
                tools.showSnackbar(mainLayout, getContext().getString(R.string.err_somethings_wrong), 5000);
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
            Call<JsonObject> call = restApi.getMemberRatingByCheatId(member.getMid(), cheatObj.getCheatId());
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
    public void onScreenshotClicked(File screenshot, int position) {
        // TODO FIXME image viewer ersetzen
        // TODO FIXME image viewer ersetzen
        // TODO FIXME image viewer ersetzen
        // TODO FIXME image viewer ersetzen
        //new StfalconImageViewer.Builder<>(favoritesCheatViewPageIndicatorActivity, screenshotList, (imageView, image) -> Picasso.get().load(image).placeholder(R.drawable.image_placeholder).into(imageView)).withStartPosition(position).show();
    }

}