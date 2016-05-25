package com.github.crazyorr.newmoviesexpress.activity;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.databinding.ActivityNotificationsBinding;
import com.github.crazyorr.newmoviesexpress.databinding.ViewLoadMoreFooterBinding;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.model.PagedList;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.util.GlobalVar;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.util.NewMoviesExpressService;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;
import com.github.crazyorr.newmoviesexpress.widget.MovieRecyclerViewAdapter;
import com.github.crazyorr.newmoviesexpress.widget.PagedStates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

import static butterknife.ButterKnife.findById;

/**
 * Created by wanglei02 on 2015/11/18.
 */
public class NotificationsActivity extends BackableActivity {

    public static final String INTENT_EXTRA_NOTIFICATIONS_FILTER = "INTENT_EXTRA_NOTIFICATIONS_FILTER";

    ActivityNotificationsBinding mBinding;
    private MovieRecyclerViewAdapter mAdapter;
    private List<MovieSimple> mMovies;
    private PagedStates mPagedStates;
    private int mNotificationsFilter;
    private Map<String, String> mOptionalParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_notifications);
        ButterKnife.bind(this);

        Toolbar toolbar = findById(this, R.id.toolbar);
        toolbar.setTitle(R.string.notifications);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mNotificationsFilter = getIntent().getIntExtra(INTENT_EXTRA_NOTIFICATIONS_FILTER,
                NewMoviesExpressService.NOTIFICATIONS_ALL);
        switch (mNotificationsFilter) {
            case NewMoviesExpressService.NOTIFICATIONS_ALL:
                mOptionalParams = null;
                break;
            case NewMoviesExpressService.NOTIFICATIONS_DUE:
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                Resources res = getResources();
                int daysBefore = sharedPref.getInt(getString(R.string.pref_key_notify_days_before),
                        res.getInteger(R.integer.default_days_before));
                int daysAfter = sharedPref.getInt(getString(R.string.pref_key_notify_days_after),
                        res.getInteger(R.integer.default_days_after));
                mOptionalParams = new ArrayMap<>();
                mOptionalParams.put(NewMoviesExpressService.QUERY_PARAM_BEFORE, String.valueOf(daysBefore));
                mOptionalParams.put(NewMoviesExpressService.QUERY_PARAM_AFTER, String.valueOf(daysAfter));
                break;
        }

        mMovies = new ArrayList<>();

        mBinding.swiperefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        // 在顶部向下拉刷新
        mBinding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });

        // 滑到底部加载更多
        mBinding.nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // We take the last son in the scrollview
                View view = v.getChildAt(v.getChildCount() - 1);
                int diff = (view.getBottom() - (v.getHeight() + v.getScrollY()));
                int footerHeight = mBinding.footer.llFooter.getHeight();
                boolean hasScrolledDown = scrollY - oldScrollY > 0;
                boolean hasReachedBottom = diff <= footerHeight;
                if (hasScrolledDown && hasReachedBottom) {
                    if (mPagedStates.hasMoreToLoad()) {
                        loadNotifications(mPagedStates.getCur(), Const.ITEM_COUNT_PER_PAGE);
                    }
                }
            }
        });

        // 点击footer加载更多
        mBinding.footer.llFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPagedStates.hasMoreToLoad()) {
                    loadNotifications(mPagedStates.getCur(), Const.ITEM_COUNT_PER_PAGE);
                }
            }
        });

//        mBinding.recyclerview.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mBinding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerview.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = getResources().getDimensionPixelSize(R.dimen.movie_item_vertical_spacing);
            }
        });
        mAdapter = new MovieRecyclerViewAdapter(this, mMovies);
        mBinding.recyclerview.setAdapter(mAdapter);
        mBinding.recyclerview.setNestedScrollingEnabled(false);

        mPagedStates = new PagedStates();
        mPagedStates.setOnStateChangeListener(new PagedStates.OnStateChangeListener() {
            @Override
            public void onStateChange(PagedStates.State state, int cur, int total, String message) {
                if (state != PagedStates.State.LOADING) {
                    mBinding.swiperefresh.setRefreshing(false);
                }

                ViewLoadMoreFooterBinding footer = mBinding.footer;

                switch (state) {
                    case FAILED:
                    case LOADING:
                    case NO_MORE_CONTENT:
                        if (cur > 0) {
                            footer.llFooter.setVisibility(View.VISIBLE);
                        } else {
                            footer.llFooter.setVisibility(View.INVISIBLE);
                            break;
                        }

                        switch (state) {
                            case LOADING:
                                footer.tvLoadingMore.setText(R.string.loading);
                                break;
                            case NO_MORE_CONTENT:
                                footer.tvLoadingMore.setText(R.string.no_more_content);
                                break;
                            case FAILED:
                                footer.tvLoadingMore.setText(R.string.load_more_fail);
                                break;
                        }

                        break;
                    default:
                        footer.llFooter.setVisibility(View.INVISIBLE);
                }

                if (state == PagedStates.State.LOADING) {
                    footer.pbLoadingMore.setVisibility(View.VISIBLE);
                } else {
                    footer.pbLoadingMore.setVisibility(View.GONE);
                }
            }
        });

        refresh(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh(false);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadNotifications(final int start, int count) {
        if (mPagedStates.getState() == PagedStates.State.LOADING) {
            return;
        } else {
            mPagedStates.onPageLoading();
        }
        HttpHelper.mNewMoviesExpressService.notifications(GlobalVar.getToken(), start, count, mNotificationsFilter, mOptionalParams
        ).enqueue(new HttpCallback<PagedList<MovieSimple>>() {

            @Override
            public void onSuccess(Call<PagedList<MovieSimple>> call, Response<PagedList<MovieSimple>> response) {
                PagedList<MovieSimple> movieList = response.body();
                mMovies.addAll(movieList.getSubjects());
                int realStart = movieList.getStart();
                int realCount = movieList.getCount();
                mAdapter.notifyItemRangeChanged(realStart, realCount);
                mPagedStates.onPageLoadSucceed(realStart, realCount, movieList.getTotal());
            }

            @Override
            public void onError(Call<PagedList<MovieSimple>> call, Response<PagedList<MovieSimple>> response, ApiError error) {
                mPagedStates.onPageLoadFail(error.getMsg());
            }

            @Override
            public void onFailure(Call<PagedList<MovieSimple>> call, Throwable t) {
                super.onFailure(call, t);
                mPagedStates.onPageLoadFail(getString(R.string.load_fail));
            }
        });
    }

    private void refresh(boolean isTriggeredBySwipe) {
        if (!isTriggeredBySwipe) {
            if (!mBinding.swiperefresh.isRefreshing()) {
                mBinding.swiperefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        mBinding.swiperefresh.setRefreshing(true);
                    }
                });
            }
        }

        mPagedStates.setCur(0);
        mMovies.clear();
        mAdapter.notifyDataSetChanged();
        loadNotifications(0, Const.ITEM_COUNT_PER_PAGE);
    }
}
