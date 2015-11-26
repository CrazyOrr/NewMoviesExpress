package com.github.crazyorr.newmoviesexpress.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.model.Person;
import com.github.crazyorr.newmoviesexpress.util.GlobalVar;
import com.github.crazyorr.newmoviesexpress.util.Util;
import com.github.crazyorr.newmoviesexpress.view.MovieDetailActivity;
import com.google.common.base.Function;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wanglei02 on 2015/11/5.
 */
public class MovieRecyclerViewAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IFooter {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_FOOTER = 2;

    private Context mContext;
    private List<MovieSimple> mMovies;
    private boolean isFootShow;

    public MovieRecyclerViewAdapter(Context context, List<MovieSimple> items) {
        mContext = context;
        mMovies = items;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = VIEW_TYPE_ITEM;
        if (position < mMovies.size()) {
            viewType = VIEW_TYPE_ITEM;
        } else if (position == mMovies.size()) {
            viewType = VIEW_TYPE_FOOTER;
        }
        return viewType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                viewHolder = new ViewHolderItem(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_list_item, parent, false));
                break;
            case VIEW_TYPE_FOOTER:
                viewHolder = new ViewHolderFooter(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_footer_load_more, parent, false));
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderItem) {
            final ViewHolderItem holderItem = (ViewHolderItem) holder;
            final MovieSimple movie = mMovies.get(position);
            final String poster = movie.getImages().getMedium();
            Picasso.with(holderItem.mPoster.getContext())
                    .load(poster)
                    .placeholder(R.drawable.movie_default)
                    .into(holderItem.mPoster);
            holderItem.mTitle.setText(movie.getTitle());
            holderItem.mRating.setRating(movie.getRating().getAverage() / movie.getRating().getMax() * holderItem.mRating.getNumStars());
            Function<Person, String> getName = new Function<Person, String>() {
                @Override
                public String apply(Person input) {
                    return input.getName();
                }
            };
            holderItem.mDirectors.setText(Util.flat(movie.getDirectors(), getName));
            holderItem.mCasts.setText(Util.flat(movie.getCasts(), getName));
            holderItem.mPubdate.setText(movie.getMainland_pubdate());

            holderItem.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, MovieDetailActivity.class);
                    GlobalVar.selectedMovie = movie;
//                    intent.putExtra(MovieDetailActivity.EXTRA_TITLE, movie.getTitle());
//                    intent.putExtra(MovieDetailActivity.EXTRA_POSTER, poster);
//                    intent.putExtra(MovieDetailActivity.EXTRA_ID, movie.getId());
//                    ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                            getActivity(),
//                            new Pair<View, String>(holder.mPoster,
//                                    getString(R.string.transition_name_poster)),
//                            new Pair<View, String>(holder.mTitle,
//                                    getString(R.string.transition_name_title)));
                    Activity activity = (Activity) mContext;
                    ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                            holderItem.mPoster,
                            mContext.getString(R.string.transition_name_poster));
                    ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
                }
            });
        }else if (holder instanceof ViewHolderFooter) {
            ViewHolderFooter holderFooter = (ViewHolderFooter) holder;
            holderFooter.mPrompt.setText(String.format(mContext.getString(R.string.loading_more),
                    mContext.getResources().getInteger(R.integer.item_count_per_load)));
        }
    }

    @Override
    public int getItemCount() {
        int count;
        if(isFootShow){
            count = mMovies.size() + 1;
        }else{
            count = mMovies.size();
        }
        return count;
    }

    @Override
    public void showFooter() {
        isFootShow = true;
        notifyDataSetChanged();
    }

    @Override
    public void dismissFooter() {
        isFootShow = false;
        notifyDataSetChanged();
    }

    class ViewHolderItem extends RecyclerView.ViewHolder {
        @Bind(R.id.id_iv_poster)
        public ImageView mPoster;
        @Bind(R.id.id_tv_title)
        public TextView mTitle;
        @Bind(R.id.id_rb_rating)
        public RatingBar mRating;
        @Bind(R.id.id_tv_directors)
        public TextView mDirectors;
        @Bind(R.id.id_tv_casts)
        public TextView mCasts;
        @Bind(R.id.id_tv_pubdate)
        public TextView mPubdate;

        public ViewHolderItem(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ViewHolderFooter extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_prompt)
        public TextView mPrompt;
        public ViewHolderFooter(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}