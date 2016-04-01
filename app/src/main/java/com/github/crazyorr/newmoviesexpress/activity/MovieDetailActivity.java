package com.github.crazyorr.newmoviesexpress.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.databinding.ActivityMovieDetailBinding;
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

import jp.wasabeef.picasso.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Response;

public class MovieDetailActivity extends BackableActivity {
//    public static final String EXTRA_TITLE = "movie_title";
//    public static final String EXTRA_POSTER = "movie_poster";
//    public static final String EXTRA_ID = "movie_id";

    private MovieSimple mMovie;
    private ObservableBoolean isFollowing = new ObservableBoolean();

    private ActivityMovieDetailBinding mActivityMovieDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovie = GlobalVar.selectedMovie;

        mActivityMovieDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);
        mActivityMovieDetailBinding.setToken(GlobalVar.getToken());
        mActivityMovieDetailBinding.setIsFollowing(isFollowing);
        mActivityMovieDetailBinding.setMovie(mMovie);
        mActivityMovieDetailBinding.setHandlers(new FabClickHandlers());

        setSupportActionBar(mActivityMovieDetailBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Function<Person, String> getName = new Function<Person, String>() {
            @Override
            public String apply(Person input) {
                return input.getName();
            }
        };
        mActivityMovieDetailBinding.tvDirectors.setText(Util.flat(mMovie.getDirectors(), getName));
        mActivityMovieDetailBinding.tvCasts.setText(Util.flat(mMovie.getCasts(), getName));
        mActivityMovieDetailBinding.tvGenres.setText(Util.flat(mMovie.getGenres()));
        mActivityMovieDetailBinding.tvPubdates.setText(Util.flat(mMovie.getPubdates()));
        mActivityMovieDetailBinding.tvDurations.setText(Util.flat(mMovie.getDurations()));

        loadPoster();

        String movieId = mMovie.getId();
        getMovieDetail(movieId);

        if (GlobalVar.hasToken()) {
            HttpHelper.mNewMoviesExpressService.queryMovieNotification(mMovie.getId(), GlobalVar.getToken()).enqueue(
                    new HttpCallback<MovieNotificationStatus>() {
                        @Override
                        public void onSuccess(Call<MovieNotificationStatus> call, Response<MovieNotificationStatus> response) {
                            MovieNotificationStatus status = response.body();
                            isFollowing.set(status.is_following());
                        }

                        @Override
                        public void onError(Call<MovieNotificationStatus> call, Response<MovieNotificationStatus> response, ApiError error) {
//                          Toast.makeText(MovieDetailActivity.this, error.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
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
                mActivityMovieDetailBinding.tvDirectors.setText(Util.flat(movieDetail.getDirectors(), getName));
                mActivityMovieDetailBinding.tvWriters.setText(Util.flat(movieDetail.getWriters(), getName));
                mActivityMovieDetailBinding.tvCasts.setText(Util.flat(movieDetail.getCasts(), getName));
                mActivityMovieDetailBinding.tvGenres.setText(Util.flat(movieDetail.getGenres()));
                mActivityMovieDetailBinding.tvCountries.setText(Util.flat(movieDetail.getCountries()));
                mActivityMovieDetailBinding.tvLanguages.setText(Util.flat(movieDetail.getLanguages()));
                mActivityMovieDetailBinding.tvPubdates.setText(Util.flat(movieDetail.getPubdates()));
                mActivityMovieDetailBinding.tvDurations.setText(Util.flat(movieDetail.getDurations()));
                mActivityMovieDetailBinding.idTvSummary.setText(movieDetail.getSummary());
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
                .into(mActivityMovieDetailBinding.ivPoster);
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
                .into(mActivityMovieDetailBinding.ivBackdrop);
        Picasso.with(context)
                .load(postLarge)
                .noFade()
                .noPlaceholder()
                .into(mActivityMovieDetailBinding.ivPoster);
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

    public class FabClickHandlers {
        public void onClickAdd(View view) {
            HttpHelper.mNewMoviesExpressService.addMovieNotification(mMovie.getId(), GlobalVar.getToken()).enqueue(
                    new HttpCallback<Void>() {
                        @Override
                        public void onSuccess(Call<Void> call, Response<Void> response) {
                            isFollowing.set(true);
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

        public void onClickRemove(View view) {
            HttpHelper.mNewMoviesExpressService.removeMovieNotification(mMovie.getId(), GlobalVar.getToken()).enqueue(
                    new HttpCallback<Void>() {
                        @Override
                        public void onSuccess(Call<Void> call, Response<Void> response) {
                            isFollowing.set(false);
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
        }
    }
}
