package com.cheatdatabase.handset.cheatview;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cheatdatabase.CheatForumActivity_;
import com.cheatdatabase.LoginActivity_;
import com.cheatdatabase.R;
import com.cheatdatabase.SubmitCheatActivity_;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.CheatMetaDialog;
import com.cheatdatabase.dialogs.RateCheatDialog;
import com.cheatdatabase.dialogs.ReportCheatDialog;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.events.CheatReportingFinishedEvent;
import com.cheatdatabase.helpers.Helper;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;
import com.splunk.mint.Mint;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Swipe through cheats horizontally with this CheatViewPageIndicator.
 *
 * @author Dominik Erbsland
 * @version 1.0
 */
@EBean
public class CheatViewPageIndicator extends AppCompatActivity {

    private static final String TAG = CheatViewPageIndicator.class.getSimpleName();

    @Bean
    Tools tools;

    private Intent intent;

    private View viewLayout;
    private int pageSelected;

    private Game gameObj;
    private Cheat[] cheatArray;
    private Cheat visibleCheat;

    private MoPubView mAdView;

    private SharedPreferences settings;
    private Editor editor;

    private Member member;

    public AlertDialog.Builder builder;

    private CheatViewFragmentAdapter mAdapter;
    private ViewPager mPager;

    private int activePage;

    private Toolbar mToolbar;
    private ShareActionProvider mShare;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();

        LayoutInflater inflater = LayoutInflater.from(this);
        viewLayout = inflater.inflate(intent.getIntExtra("layoutResourceId", R.layout.activity_cheatview_pager), null);
        setContentView(viewLayout);

        init();

        // Bei grossen Game Objekten (wie Pokemon Fire Red) muss das Objekt
        // aus dem SharedPreferences geholt werden (ansonsten Absturz)
        gameObj = (Game) intent.getSerializableExtra("gameObj");
        if (gameObj == null) {
            gameObj = new Gson().fromJson(settings.getString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW, null), Game.class);
        }

        editor.putString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW, new Gson().toJson(gameObj));
        editor.commit();

        pageSelected = intent.getIntExtra("selectedPage", 0);
        activePage = pageSelected;
        cheatArray = gameObj.getCheats();
        visibleCheat = cheatArray[pageSelected];

        getSupportActionBar().setTitle(gameObj.getGameName());
        getSupportActionBar().setSubtitle(gameObj.getSystemName());

        initialisePaging();
    }

    private void init() {
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        mToolbar = tools.initToolbarBase(this, mToolbar);
        mAdView = tools.initMoPubAdView(this, mAdView);

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    private void initialisePaging() {
        final String[] cheatTitles = new String[cheatArray.length];
        for (int i = 0; i < cheatArray.length; i++) {
            cheatTitles[i] = cheatArray[i].getCheatTitle();
        }

        try {
            mAdapter = new CheatViewFragmentAdapter(getSupportFragmentManager(), gameObj, cheatTitles);
            mPager = (ViewPager) viewLayout.findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);
            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    // Save the last selected page
                    editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position);
                    editor.commit();

                    activePage = position;

                    try {
                        visibleCheat = cheatArray[position];
                        invalidateOptionsMenu();
                    } catch (Exception e) {
                        Toast.makeText(CheatViewPageIndicator.this, R.string.err_somethings_wrong, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            MagicIndicator magicIndicator = (MagicIndicator) findViewById(R.id.magic_indicator);
            CommonNavigator commonNavigator = new CommonNavigator(this);
            commonNavigator.setSkimOver(true);
            commonNavigator.setAdapter(new CommonNavigatorAdapter() {
                @Override
                public int getCount() {
                    return cheatArray == null ? 0 : cheatArray.length;
                }

                @Override
                public IPagerTitleView getTitleView(Context context, final int index) {
                    SimplePagerTitleView clipPagerTitleView = new ColorTransitionPagerTitleView(context);
                    clipPagerTitleView.setText(cheatTitles[index]);
                    clipPagerTitleView.setNormalColor(Color.parseColor("#88ffffff")); // White transparent
                    clipPagerTitleView.setSelectedColor(Color.WHITE);
                    clipPagerTitleView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPager.setCurrentItem(index);
                        }
                    });
                    return clipPagerTitleView;
                }

                @Override
                public IPagerIndicator getIndicator(Context context) {
                    LinePagerIndicator indicator = new LinePagerIndicator(context);
                    indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                    indicator.setYOffset(UIUtil.dip2px(context, 3));
                    indicator.setColors(Color.WHITE);
                    return indicator;
                }
            });
            magicIndicator.setNavigator(commonNavigator);
            ViewPagerHelper.bind(magicIndicator, mPager);
            mPager.setCurrentItem(pageSelected);

            FloatingActionButton fa = (FloatingActionButton) viewLayout.findViewById(R.id.add_new_cheat_button);
            fa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubmitCheatActivity_.intent(CheatViewPageIndicator.this).gameObj(gameObj).start();
                }
            });
        } catch (Exception e2) {
            Log.e(TAG, "ERROR: " + getPackageName() + "/" + getTitle() + "... " + e2.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Return result code. Login success, Register success etc.
            int intentReturnCode = data.getIntExtra("result", Konstanten.LOGIN_REGISTER_FAIL_RETURN_CODE);

            if (requestCode == Konstanten.LOGIN_REGISTER_OK_RETURN_CODE) {
                member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
                invalidateOptionsMenu();
                if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    Toast.makeText(CheatViewPageIndicator.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
                } else if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    Toast.makeText(CheatViewPageIndicator.this, R.string.login_ok, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((visibleCheat != null) && (visibleCheat.getMemberRating() > 0)) {
            getMenuInflater().inflate(R.menu.handset_cheatview_rating_on_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.handset_cheatview_rating_off_menu, menu);
        }

        if (member != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Sharing
        mShare = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//        setShareText(visibleCheat);

        // Search
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if ((visibleCheat != null) && (visibleCheat.getMemberRating() > 0)) {
            getMenuInflater().inflate(R.menu.handset_cheatview_rating_on_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.handset_cheatview_rating_off_menu, menu);
        }

        if (member != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Sharing
        mShare = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//        setShareText(visibleCheat);

        // Search
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return super.onPrepareOptionsMenu(menu);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShare != null) {
            mShare.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                // TODO FIXME anstatt finish() richtig zu CheatList zurueck gehen. via annotations...

                return true;
            case R.id.action_submit_cheat:
                Intent explicitIntent = new Intent(CheatViewPageIndicator.this, SubmitCheatActivity_.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
                return true;
            case R.id.action_rate:
                showRatingDialog();
                return true;
            case R.id.action_forum:
                if (Reachability.reachability.isReachable) {
//                    Intent forumIntent = new Intent(CheatViewPageIndicator.this, CheatForumActivity.class);
//                    forumIntent.putExtra("gameObj", gameObj);
//                    forumIntent.putExtra("cheatArray", visibleCheat);
//                    startActivity(forumIntent);
                    CheatForumActivity_.intent(this).gameObj(gameObj).cheatObj(visibleCheat).start();
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_add_to_favorites:
                // background task?
                // TODO if cheat already in favorites, change the icon/action to
                // remove cheat from favorites
                Toast.makeText(CheatViewPageIndicator.this, R.string.favorite_adding, Toast.LENGTH_SHORT).show();
                Helper.addFavorite(CheatViewPageIndicator.this, visibleCheat);
                return true;
            case R.id.action_report:
                showReportDialog();
                // TODO FIXME bottom sheet fertigstellen
//                new BottomSheet.Builder(this).title("title").darkTheme().sheet(R.menu.cheat_view_action_menu).listener(new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        switch (which) {
////                            case R.id.help:
////                                q.toast("Help me!");
////                                break;
//                        }
//                    }
//                }).show();
                return true;
            case R.id.action_metainfo:
                if (Reachability.reachability.isReachable) {
                    CheatMetaDialog cmDialog = new CheatMetaDialog(CheatViewPageIndicator.this, visibleCheat);
                    cmDialog.show();
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_login:
                Intent loginIntent = new Intent(CheatViewPageIndicator.this, LoginActivity_.class);
                startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
                return true;
            case R.id.action_logout:
                member = null;
                tools.logout(CheatViewPageIndicator.this, editor);
                invalidateOptionsMenu();
                return true;
            case R.id.action_share:
                setShareIntent(tools.setShareText(CheatViewPageIndicator.this, visibleCheat));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        invalidateOptionsMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        Reachability.unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    public void showReportDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
        } else {
            Bundle args = new Bundle();
            args.putSerializable("cheatArray", visibleCheat);

            FragmentManager fm = getSupportFragmentManager();
            ReportCheatDialog reportCheatDialog = new ReportCheatDialog();
            reportCheatDialog.setArguments(args);
            reportCheatDialog.show(fm, "fragment_report_cheat");
        }
    }

    @Subscribe
    public void onEvent(CheatReportingFinishedEvent result) {
        if (result.isSucceeded()) {
            Toast.makeText(this, R.string.thanks_for_reporting, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public void showRatingDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            Bundle args = new Bundle();
            args.putSerializable("cheatArray", visibleCheat);

            FragmentManager fm = getSupportFragmentManager();
            RateCheatDialog ratingCheatDialog = new RateCheatDialog();
            ratingCheatDialog.setArguments(args);
            ratingCheatDialog.show(fm, "fragment_rating_cheat");
        }
    }

    @Subscribe
    public void onEvent(CheatRatingFinishedEvent result) {
        visibleCheat.setMemberRating(result.getRating());
        cheatArray[activePage].setMemberRating(result.getRating());
        invalidateOptionsMenu();
        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
    }

    public void setRating(int position, float rating) {
        cheatArray[position].setMemberRating(rating);
        invalidateOptionsMenu();
    }

}