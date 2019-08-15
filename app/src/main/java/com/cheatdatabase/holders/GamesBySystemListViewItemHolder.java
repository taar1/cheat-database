package com.cheatdatabase.holders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GamesBySystemListViewItemHolder extends RecyclerView.ViewHolder {
    public View view;
    private Context context;
    private Game game;

    @BindView(R.id.game_name)
    TextView gameName;
    @BindView(R.id.cheat_count)
    TextView cheatCount;

//    public GamesBySystemRecycleListViewAdapter.OnGameItemClickListener onGameItemClickListener;

    public GamesBySystemListViewItemHolder(View view, Context context) {
        super(view);
        this.view = view;
        this.context = context;
        ButterKnife.bind(this, view);

        gameName.setTypeface(Tools.getFont(view.getContext().getAssets(), Konstanten.FONT_BOLD));
        cheatCount.setTypeface(Tools.getFont(view.getContext().getAssets(), Konstanten.FONT_LIGHT));

//        view.setOnClickListener(this);
    }

//    @Override
//    public void onClick(View v) {
//        onGameItemClickListener.onGameClick(this);
//    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;

        gameName.setText(game.getGameName());
        gameName.setTypeface(Tools.getFont(view.getContext().getAssets(), Konstanten.FONT_BOLD));

        if (game.getCheatsCount() > 0) {
            cheatCount.setVisibility(View.VISIBLE);
            cheatCount.setText(game.getCheatsCount() + " " + context.getResources().getQuantityString(R.plurals.entries, game.getCheatsCount()));
            cheatCount.setTypeface(Tools.getFont(view.getContext().getAssets(), Konstanten.FONT_LIGHT));
        } else {
            cheatCount.setVisibility(View.GONE);
        }

    }
}