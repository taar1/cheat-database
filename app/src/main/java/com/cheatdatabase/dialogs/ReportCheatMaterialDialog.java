package com.cheatdatabase.dialogs;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.ForumPost;
import com.cheatdatabase.model.Member;
import com.cheatdatabase.helpers.Webservice;

import java.util.List;

import needle.Needle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Material Design Cheat Reporting Dialog.
 */
public class ReportCheatMaterialDialog {

    private static final String TAG = ReportCheatMaterialDialog.class.getSimpleName();

    Activity activity;

    public ReportCheatMaterialDialog(final Activity activity, final Cheat cheat, final Member member) {
        this.activity = activity;

        final String[] reasons = activity.getResources().getStringArray(R.array.report_reasons);

        new MaterialDialog.Builder(activity)
                .title(R.string.report_cheat_title)
                .items(R.array.report_reasons)
                .negativeText(R.string.cancel)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, final int which, CharSequence text) {











                        Needle.onBackgroundThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Webservice.reportCheat(cheat.getCheatId(), member.getMid(), reasons[which]);
                                    postReporting(true);
                                } catch (Exception e) {
                                    postReporting(false);
                                }
                            }
                        });

                        return false;
                    }
                })
                .theme(Theme.DARK)
                .show();
    }

    private void postReporting(final boolean isSuccess) {
        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                if (isSuccess) {
                    Toast.makeText(activity, activity.getString(R.string.thanks_for_reporting), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, activity.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void reportCheat() {

        // TODO FIXME restApi übergeben via activity oder als parameter....
        // TODO FIXME restApi übergeben via activity oder als parameter....
        // TODO FIXME restApi übergeben via activity oder als parameter....
        // TODO FIXME restApi übergeben via activity oder als parameter....
        // TODO FIXME restApi übergeben via activity oder als parameter....
        // TODO FIXME restApi übergeben via activity oder als parameter....
        // TODO FIXME restApi übergeben via activity oder als parameter....
        //restApi = activity.getRestApi();

//        Call<List<ForumPost>> call = restApi.getForum(cheatObj.getCheatId());
//        call.enqueue(new Callback<List<ForumPost>>() {
//            @Override
//            public void onResponse(Call<List<ForumPost>> forum, Response<List<ForumPost>> response) {
//                Log.d(TAG, "XXXXX get forum: SUCCESS");
//
//                List<ForumPost> forumThread = response.body();
//                for (ForumPost f : forumThread) {
//                    Log.d(TAG, "XXXXX onResponse: " + f.getText());
//                }
//
//                reloadView.setVisibility(View.GONE);
//
//                llForumMain.removeAllViews();
//                if (forumThread.size() > 0) {
//                    tvEmpty.setVisibility(View.GONE);
//
//                    for (ForumPost forumPost : forumThread) {
//                        LinearLayout linearLayout = createForumPosts(forumPost);
//                        llForumMain.addView(linearLayout, new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                    }
//                } else {
//                    tvEmpty.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<ForumPost>> call, Throwable e) {
//                Log.e(TAG, "XXXXX load forum onFailure: " + e.getLocalizedMessage());
//
//                tvEmpty.setVisibility(View.VISIBLE);
//            }
//        });
    }

}
