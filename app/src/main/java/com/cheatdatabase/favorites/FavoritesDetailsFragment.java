package com.cheatdatabase.favorites;

import android.app.AlertDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.cheatdatabase.CheatDetailTabletFragment;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.businessobjects.Screenshot;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Helper;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

public class FavoritesDetailsFragment extends Fragment implements OnClickListener, Serializable {

    private FavoriteCheatListActivity ca;

    private View rootView;

    private SharedPreferences settings;
    private Editor editor;

    private Member member;

    private Typeface latoFontBold;
    private Typeface latoFontLight;

    private TextView tvTextBeforeTable;
    private TextView tvGalleryInfo;
    private TextView textCheatTitle;
    private TextView tvCheatText;

    private Gallery screenshotGallery;

    private ProgressBar progressBar;

    private ImageView[] imageViews;

    private Cheat cheatObj;

    private int biggestHeight;

    private TableLayout mainTable;

    private ImageButton btnRateCheat;
    private ImageButton btnMetaInfo;
    private ImageButton btnForum;
    private ImageButton btnDelete;
    private ImageButton btnReport;
    private ImageButton btnShare;
    private ImageButton btnViewCheat;

    private FavoritesCheatForumFragment favoritesCheatForumFragment;
    private FavoritesCheatMetaFragment favoritesCheatMetaFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ca = (FavoriteCheatListActivity) getActivity();

        latoFontLight = Tools.getFont(ca.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(ca.getAssets(), Konstanten.FONT_BOLD);

        settings = ca.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        favoritesCheatForumFragment = (FavoritesCheatForumFragment) getArguments().getSerializable("favoritesCheatForumFragment");
        favoritesCheatMetaFragment = (FavoritesCheatMetaFragment) getArguments().getSerializable("favoritesCheatMetaFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cheat_detail, container, false);

        Bundle element = this.getArguments();
        int position = element.getInt("position");
        Game gameObj = (Game) element.getSerializable("gameObj");
        if (gameObj == null) {
            cheatObj = (Cheat) element.getSerializable("cheatObj");
        } else {
            cheatObj = gameObj.getCheats()[position];
        }

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        new FetchCheatRatingOnlineBackgroundTask().execute();

        mainTable = (TableLayout) rootView.findViewById(R.id.tblCheatListMain);
        btnViewCheat = (ImageButton) rootView.findViewById(R.id.btn_view_cheat);
        btnViewCheat.setOnClickListener(this);
        btnMetaInfo = (ImageButton) rootView.findViewById(R.id.btn_meta_info);
        btnMetaInfo.setOnClickListener(this);
        btnForum = (ImageButton) rootView.findViewById(R.id.btn_forum);
        btnForum.setOnClickListener(this);
        btnDelete = (ImageButton) rootView.findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(this);
        btnReport = (ImageButton) rootView.findViewById(R.id.btn_report);
        btnReport.setVisibility(View.GONE);
        btnShare = (ImageButton) rootView.findViewById(R.id.btn_share);
        btnShare.setOnClickListener(this);
        btnRateCheat = (ImageButton) rootView.findViewById(R.id.btn_rate_cheat);
        btnRateCheat.setOnClickListener(this);
        if (cheatObj.getMemberRating() > 0) {
            btnRateCheat.setImageResource(R.drawable.ic_action_star);
        } else {
            btnRateCheat.setImageResource(R.drawable.ic_action_not_important);
        }

        textCheatTitle = (TextView) rootView.findViewById(R.id.text_cheat_title);
        textCheatTitle.setVisibility(View.VISIBLE);
        textCheatTitle.setTypeface(latoFontBold);

        tvTextBeforeTable = (TextView) rootView.findViewById(R.id.text_cheat_before_table);
        tvTextBeforeTable.setVisibility(View.VISIBLE);
        tvTextBeforeTable.setTypeface(latoFontLight);

        tvGalleryInfo = (TextView) rootView.findViewById(R.id.gallery_info);
        tvGalleryInfo.setVisibility(View.INVISIBLE);
        tvGalleryInfo.setTypeface(latoFontLight);

        screenshotGallery = (Gallery) rootView.findViewById(R.id.gallery);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar1);
        progressBar.setVisibility(View.INVISIBLE);

        tvCheatText = (TextView) rootView.findViewById(R.id.text_cheat_text);
        tvCheatText.setTypeface(latoFontLight);

        /**
         * Thumbnails holen, falls Screenshots vorhanden sind.
         */
        if (cheatObj.isScreenshots() == true) {
            biggestHeight = 100; // init value
            imageViews = new ImageView[cheatObj.getScreens().length];
            progressBar.setVisibility(View.VISIBLE);

            new LoadScreenshotsInBackgroundTask().execute();
        } else {
            tvGalleryInfo.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            screenshotGallery.setVisibility(View.GONE);
        }

        populateView();
        return rootView;
    }

    /**
     * Create Layout
     */
    private void populateView() {
        try {
            textCheatTitle.setText(cheatObj.getCheatTitle());
            if (cheatObj.getCheatText().contains("</td>")) {
                fillTableContent();
            } else {
                fillSimpleContent();
            }
        } catch (Exception e) {
            Log.e(CheatDetailTabletFragment.class.getName(), "Cheat " + cheatObj.getCheatId() + " contains(</td>) - Error creating table");
            fillSimpleContent();
        }
    }

    /**
     * Populate Table Layout
     */
    private void fillTableContent() {

        mainTable.setColumnShrinkable(0, true);
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
        CharSequence styledText = Html.fromHtml(cheatObj.getCheatText());
        tvCheatText.setText(styledText);

        mainTable.setVisibility(View.GONE);
        tvTextBeforeTable.setVisibility(View.GONE);

        if (cheatObj.isWalkthroughFormat()) {
            tvCheatText.setTextAppearance(ca, R.style.WalkthroughText);
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
                // editor.putFloat("c" + cheatObj.getCheatId(), cheatRating);
                // editor.commit();

                cheatObj.setMemberRating(cheatRating);
                highlightRatingIcon(true);
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
                Log.e(CheatDetailTabletFragment.class.getName(), "Remtoe Image Exception", e);
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

    @Override
    public void onClick(View v) {
        Log.d("onClick", "onClick");

        if (favoritesCheatForumFragment == null) {
            favoritesCheatForumFragment = (FavoritesCheatForumFragment) getArguments().getSerializable("favoritesCheatForumFragment");
        }
        if (favoritesCheatMetaFragment == null) {
            favoritesCheatMetaFragment = (FavoritesCheatMetaFragment) getArguments().getSerializable("favoritesCheatMetaFragment");
        }

        Bundle arguments = new Bundle();
        arguments.putInt(CheatDetailTabletFragment.ARG_ITEM_ID, 1);
        arguments.putSerializable("cheatObj", cheatObj);
        arguments.putSerializable("favoritesDetailsFragment", this);
        arguments.putSerializable("favoritesCheatForumFragment", favoritesCheatForumFragment);
        arguments.putSerializable("favoritesCheatMetaFragment", favoritesCheatMetaFragment);

        if (v == btnViewCheat) {
            Log.d("onClick", "btnViewCheat");
        } else if (v == btnMetaInfo) {
            Log.d("onClick", "btnMetaInfo");
            favoritesCheatMetaFragment.setArguments(arguments);
            ca.getSupportFragmentManager().beginTransaction().replace(R.id.favorite_detail_container, favoritesCheatMetaFragment).commit();
        } else if (v == btnForum) {
            Log.d("onClick", "btnForum");
            favoritesCheatForumFragment.setArguments(arguments);
            ca.getSupportFragmentManager().beginTransaction().replace(R.id.favorite_detail_container, favoritesCheatForumFragment).commit();
        } else if (v == btnDelete) {
            Log.d("onClick", "btnDelete");

            new AlertDialog.Builder(ca).setIcon(R.drawable.new_app_icon).setTitle(R.string.confirm).setMessage(R.string.remove_favorite_areyousure).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                private CheatDatabaseAdapter db;

                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked OK */

                    db = new CheatDatabaseAdapter(ca);
                    db.open();
                    db.deleteFavorite(cheatObj);

                    Intent upIntent = NavUtils.getParentActivityIntent(ca);
                    if (NavUtils.shouldUpRecreateTask(ca, upIntent)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            TaskStackBuilder.create(ca).addNextIntentWithParentStack(upIntent).startActivities();
                        }
                    } else {
                        NavUtils.navigateUpTo(ca, upIntent);
                    }

                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
            }).create().show();

        } else if (v == btnShare) {
            Log.d("onClick", "btnShare");
            Helper.shareCheat(cheatObj, ca);
        } else if (v == btnRateCheat) {
            Log.d("onClick", "btnRateCheat");
            ca.showRatingDialog();
        }
    }

    public void highlightRatingIcon(boolean highlight) {
        try {
            if (highlight) {
                btnRateCheat.setImageResource(R.drawable.ic_action_star);
            } else {
                btnRateCheat.setImageResource(R.drawable.ic_action_not_important);
            }
        } catch (NullPointerException e) {
        }
    }

    public void updateMemberCheatRating(float newRating) {
        cheatObj.setMemberRating(newRating);
    }

    @Override
    public void onResume() {
        super.onResume();
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

}
