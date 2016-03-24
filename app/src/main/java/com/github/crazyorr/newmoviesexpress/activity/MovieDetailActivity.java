package com.github.crazyorr.newmoviesexpress.activity;

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
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.github.crazyorr.newmoviesexpress.model.MovieDetail;
import com.github.crazyorr.newmoviesexpress.model.MovieNotificationStatus;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.model.Person;
import com.github.crazyorr.newmoviesexpress.util.GlobalVar;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.util.Util;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;
import com.google.common.base.Function;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Response;

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
    private boolean isFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.id_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (TextUtils.isEmpty(GlobalVar.token)) {
//                    Toast.makeText(MovieDetailActivity.this, "请先登录",
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
                if (isFollowing) {
                    HttpHelper.mNewMoviesExpressService.removeMovieNotification(mMovie.getId(), GlobalVar.token).enqueue(
                            new HttpCallback<Void>() {
                                @Override
                                public void onSuccess(Call<Void> call, Response<Void> response) {
                                    isFollowing = false;
                                    fab.setImageResource(R.drawable.ic_add_24dp);
                                    Toast.makeText(MovieDetailActivity.this, "已删除",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(Call<Void> call, Response<Void> response, ApiError error) {
                                    Toast.makeText(MovieDetailActivity.this, error.getMsg(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                } else {
                    HttpHelper.mNewMoviesExpressService.addMovieNotification(mMovie.getId(), GlobalVar.token).enqueue(
                            new HttpCallback<Void>() {
                                @Override
                                public void onSuccess(Call<Void> call, Response<Void> response) {
                                    isFollowing = true;
                                    fab.setImageResource(R.drawable.ic_remove_24dp);
                                    Toast.makeText(MovieDetailActivity.this, "已添加",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(Call<Void> call, Response<Void> response, ApiError error) {
                                    Toast.makeText(MovieDetailActivity.this, error.getMsg(),
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    super.onFailure(call, t);
                                    Toast.makeText(MovieDetailActivity.this, "添加提醒失败",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
            }
        });

        mMovie = GlobalVar.selectedMovie;
        String movieTitle = mMovie.getTitle();
        mCollapsingToolbar.setTitle(movieTitle);

        loadPoster();

        String movieId = mMovie.getId();
        getMovieDetail(movieId);

        if (GlobalVar.isLoggedIn()) {
            HttpHelper.mNewMoviesExpressService.queryMovieNotification(mMovie.getId(), GlobalVar.token).enqueue(
                    new HttpCallback<MovieNotificationStatus>() {
                        @Override
                        public void onSuccess(Call<MovieNotificationStatus> call, Response<MovieNotificationStatus> response) {
                            MovieNotificationStatus status = response.body();
                            isFollowing = status.is_following();
                            if (isFollowing) {
                                fab.setImageResource(R.drawable.ic_remove_24dp);
                            } else {
                                fab.setImageResource(R.drawable.ic_add_24dp);
                            }
                            fab.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Call<MovieNotificationStatus> call, Response<MovieNotificationStatus> response, ApiError error) {
//                        Toast.makeText(MovieDetailActivity.this, error.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }

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
        HttpHelper.mDoubanService.getMovieDetail(movieId).enqueue(new HttpCallback<MovieDetail>() {
            @Override
            public void onSuccess(retrofit2.Call<MovieDetail> call, retrofit2.Response<MovieDetail> response) {
                final MovieDetail movieDetail = response.body();
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

            @Override
            public void onError(Call<MovieDetail> call, Response<MovieDetail> response, ApiError error) {
                Toast.makeText(MovieDetailActivity.this, error.getMsg(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(retrofit2.Call<MovieDetail> call, Throwable t) {
                super.onFailure(call, t);
                Toast.makeText(MovieDetailActivity.this, R.string.load_fail,
                        Toast.LENGTH_SHORT).show();
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
