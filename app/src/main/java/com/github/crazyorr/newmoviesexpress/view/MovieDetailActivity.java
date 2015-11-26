package com.github.crazyorr.newmoviesexpress.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.model.MovieDetail;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.model.Person;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.util.GlobalVar;
import com.github.crazyorr.newmoviesexpress.controller.HttpCallback;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.util.Util;
import com.google.common.base.Function;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class MovieDetailActivity extends BackableActivity {
    public static final String EXTRA_TITLE = "movie_title";
    public static final String EXTRA_POSTER = "movie_poster";
    public static final String EXTRA_ID = "movie_id";
    private static final String TAG = MovieDetailActivity.class.getSimpleName();
    @Bind(R.id.id_collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.id_iv_backdrop)
    ImageView mBackdrop;
    @Bind(R.id.id_iv_poster)
    ImageView mPoster;
    @Bind(R.id.tv_directors)
    TextView mDirectors;
    @Bind(R.id.tv_writers)
    TextView mWriters;
    @Bind(R.id.tv_casts)
    TextView mCasts;
    @Bind(R.id.tv_genres)
    TextView mGenres;
    @Bind(R.id.tv_countries)
    TextView mCountries;
    @Bind(R.id.tv_languages)
    TextView mLanguages;
    @Bind(R.id.tv_pubdates)
    TextView mPubdates;
    @Bind(R.id.tv_durations)
    TextView mDurations;
    @Bind(R.id.id_tv_summary)
    TextView mSummary;

    private MovieSimple mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.id_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        mMovie = GlobalVar.selectedMovie;
        String movieTitle = mMovie.getTitle();
        mCollapsingToolbar.setTitle(movieTitle);

        loadPoster();

        String movieId = mMovie.getId();
        getMovieDetail(movieId);

//        Intent intent = getIntent();
//        final String movieTitle = intent.getStringExtra(EXTRA_TITLE);
//        mCollapsingToolbar.setTitle(movieTitle);
//        final String moviePoster = intent.getStringExtra(EXTRA_POSTER);
//        Picasso.with(this)
//                .load(moviePoster)
//                .noFade()
//                .into(mPoster);
//        final String movieId = intent.getStringExtra(EXTRA_ID);
//        getMovieDetail(movieId);

    }

    private void getMovieDetail(String movieId) {
        HttpUrl url = Const.getDoubanUrlBuilder()
                .addPathSegment("movie")
                .addPathSegment("subject")
                .addPathSegment(movieId)
                .build();

        HttpHelper.getAsync(url, new HttpCallback() {
            @Override
            public void onResponse(Response response) throws IOException {
                super.onResponse(response);
                final MovieDetail movieDetail = MovieDetail.fromJson(response.body().string());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Function<Person, String> getName = new Function<Person, String>() {
                            @Override
                            public String apply(Person input) {
                                return input.getName();
                            }
                        };
                        mDirectors.setText(Util.flat(movieDetail.getDirectors(), getName));
                        mWriters.setText(Util.flat(movieDetail.getWriters(), getName));
                        mCasts.setText(Util.flat(movieDetail.getCasts(), getName));
                        mGenres.setText(Util.flat(movieDetail.getGenres()));
                        mCountries.setText(Util.flat(movieDetail.getCountries()));
                        mLanguages.setText(Util.flat(movieDetail.getLanguages()));
                        mPubdates.setText(Util.flat(movieDetail.getPubdates()));
                        mDurations.setText(Util.flat(movieDetail.getDurations()));
                        mSummary.setText(movieDetail.getSummary());
                    }
                });
            }
        });
    }

    private void loadPoster() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && addTransitionListener()) {
            // If we're running on Lollipop and we have added a listener to the shared element
            // transition, load the thumbnail. The listener will load the full-size image when
            // the transition is complete.
            loadThumbnail();
        } else {
            // If all other cases we should just load the full-size image now
            loadFullSizeImage();
        }
    }

    /**
     * Load the item's thumbnail image into our {@link ImageView}.
     */
    private void loadThumbnail() {
        Picasso.with(this)
                .load(mMovie.getImages().getMedium())
                .noFade()
                .into(mPoster);
    }

    /**
     * Load the item's full-size image into our {@link ImageView}.
     */
    private void loadFullSizeImage() {
        Context context = MovieDetailActivity.this;
        String postLarge = mMovie.getImages().getLarge();
        Picasso.with(context)
                .load(postLarge)
                .resize(200, 100)
                .centerCrop()
                .transform(new BlurTransformation(context, 5, 2))
                .into(mBackdrop);
        Picasso.with(context)
                .load(postLarge)
                .noFade()
                .noPlaceholder()
                .into(mPoster);
    }

    /**
     * Try and add a {@link Transition.TransitionListener} to the entering shared element
     * {@link Transition}. We do this so that we can load the full-size image after the transition
     * has completed.
     *
     * @return true if we were successful in adding a listener to the enter transition
     */
    @TargetApi(21)
    private boolean addTransitionListener() {
        final Transition transition = getWindow().getSharedElementEnterTransition();

        if (transition != null) {
            // There is an entering shared element transition so add a listener to it
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    // As the transition has ended, we can now load the full-size image
                    loadFullSizeImage();

                    // Make sure we remove ourselves as a listener
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionStart(Transition transition) {
                    // No-op
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    // Make sure we remove ourselves as a listener
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionPause(Transition transition) {
                    // No-op
                }

                @Override
                public void onTransitionResume(Transition transition) {
                    // No-op
                }
            });
            return true;
        }

        // If we reach here then we have not added a listener
        return false;
    }
}
