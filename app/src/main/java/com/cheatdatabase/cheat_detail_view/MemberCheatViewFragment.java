package com.cheatdatabase.cheat_detail_view;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.CheatViewGalleryListAdapter;
import com.cheatdatabase.callbacks.CheatViewGalleryImageClickListener;
import com.cheatdatabase.data.helper.CheatArrayHolder;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.data.model.Screenshot;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemberCheatViewFragment extends Fragment implements CheatViewGalleryImageClickListener {
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
    TextView tvSwipeHorizontallyInfoText;
    @BindView(R.id.gallery_recycler_view)
    RecyclerView galleryRecyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private Cheat cheatObj;
    private List<Cheat> cheats;
    private int offset;
    private Member member;

    private SharedPreferences settings;
    private Editor editor;

    private Typeface latoFontLight;
    private MemberCheatViewPageIndicator cheatViewPageIndicatorActivity;

    private LinearLayout outerLayout;

    public static MemberCheatViewFragment newInstance(List<Cheat> cheats, int offset, LinearLayout outerLayout) {
        MemberCheatViewFragment fragment = new MemberCheatViewFragment();
        fragment.cheats = cheats;
        fragment.offset = offset;
        fragment.outerLayout = outerLayout;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the screen has been rotated we re-set the values
        if (savedInstanceState != null) {
            CheatArrayHolder cheatArrayHolder = savedInstanceState.getParcelable("cheatArrayHolder");
            cheats = cheatArrayHolder.getCheatList();
            offset = savedInstanceState.getInt("offset");
        }

        cheatViewPageIndicatorActivity = (MemberCheatViewPageIndicator) getActivity();

        latoFontLight = Tools.getFont(cheatViewPageIndicatorActivity.getAssets(), Konstanten.FONT_LIGHT);

        settings = cheatViewPageIndicatorActivity.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("cheatArrayHolder", new CheatArrayHolder(cheats));
        outState.putInt("offset", offset);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_member_cheat_detail, container, false);
        ButterKnife.bind(this, linearLayout);

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        cheatObj = cheats.get(offset);

        getCheatRating();

        tvTextBeforeTable.setVisibility(View.VISIBLE);
        tvCheatTitle.setText(cheatObj.getCheatTitle());
        tvSwipeHorizontallyInfoText.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);


        for (Screenshot s : cheatObj.getScreenshotList()) {
            Log.d(TAG, "XXXXX onCreateView: screenshot: " + s.getFullPath());
        }

        /**
         * Get thumbnails if there are screenshots.
         */
        if (cheatObj.hasScreenshots()) {
            CheatViewGalleryListAdapter cheatViewGalleryListAdapter = new CheatViewGalleryListAdapter();
            cheatViewGalleryListAdapter.setScreenshotList(cheatObj.getScreenshotList());
            cheatViewGalleryListAdapter.setClickListener(this);

            galleryRecyclerView.setAdapter(cheatViewGalleryListAdapter);
            RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(cheatViewPageIndicatorActivity, 2, GridLayoutManager.HORIZONTAL, false);
            galleryRecyclerView.setLayoutManager(gridLayoutManager);

            if ((cheatObj.getScreenshotList() == null) || (cheatObj.getScreenshotList().size() < 3)) {
                tvSwipeHorizontallyInfoText.setVisibility(View.GONE);
            } else {
                tvSwipeHorizontallyInfoText.setVisibility(View.VISIBLE);
            }

            progressBar.setVisibility(View.VISIBLE);
        } else {
            tvSwipeHorizontallyInfoText.setVisibility(View.GONE);
            galleryRecyclerView.setVisibility(View.GONE);
        }

        /**
         * If the user came from the search results the cheat-text might not
         * be complete (trimmed for the search results) and therefore has to
         * be re-fetched in a background process.
         */
        if ((cheatObj.getCheatText() == null) || (cheatObj.getCheatText().length() < 10)) {
            progressBar.setVisibility(View.VISIBLE);
            getCheatBody();
        } else {
            progressBar.setVisibility(View.GONE);
            populateView();
        }

        editor.putString("cheat" + offset, new Gson().toJson(cheatObj));
        editor.commit();

        return linearLayout;
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
            tvCheatText.setTextAppearance(cheatViewPageIndicatorActivity, R.style.WalkthroughText);
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

    private void getCheatBody() {
        Call<Cheat> call = cheatViewPageIndicatorActivity.getRestApi().getCheatById(cheatObj.getCheatId());
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
        cheatObj.setCheatText(cheat.getCheatText());

        populateView();
    }

    private void getCheatRating() {
        if (member != null) {
            Call<JsonObject> call = cheatViewPageIndicatorActivity.getRestApi().getMemberRatingByCheatId(member.getMid(), cheatObj.getCheatId());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> ratingInfo, Response<JsonObject> response) {
                    JsonObject ratingJsonObj = response.body();
                    Log.d(TAG, "getCheatRating SUCCESS: " + ratingJsonObj.get("rating").getAsFloat());

                    float cheatRating = ratingJsonObj.get("rating").getAsFloat();
                    if (cheatRating > 0) {
                        cheatViewPageIndicatorActivity.setRating(offset, cheatRating);
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
        new StfalconImageViewer.Builder<>(cheatViewPageIndicatorActivity, cheatObj.getScreenshotList(), (imageView, image) -> Picasso.get().load(image.getFullPath()).placeholder(R.drawable.image_placeholder).into(imageView)).withStartPosition(position).show();
    }

//    @Override
//    public void onScreenshotUrlClicked(String screenshot, int position) {
    // TODO FIXME can either be deleted later on if not used or changing the listener to this method and delete the above method....
    // TODO FIXME can either be deleted later on if not used or changing the listener to this method and delete the above method....
    // TODO FIXME can either be deleted later on if not used or changing the listener to this method and delete the above method....
    // TODO FIXME can either be deleted later on if not used or changing the listener to this method and delete the above method....
//    }
}