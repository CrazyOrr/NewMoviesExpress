package com.github.crazyorr.newmoviesexpress.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.model.PagedList;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.util.GlobalVar;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.util.NewMoviesExpressService;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;
import com.github.crazyorr.newmoviesexpress.widget.MovieRecyclerViewAdapter;
import com.github.crazyorr.newmoviesexpress.widget.OnRecyclerViewScrollReachListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

import static butterknife.ButterKnife.findById;

/**
 * Created by wanglei02 on 2015/11/18.
 */
public class NotificationsActivity extends BackableActivity {

    public static final String INTENT_EXTRA_NOTIFICATIONS_FILTER = "INTENT_EXTRA_NOTIFICATIONS_FILTER";
    private static final String TAG = NotificationsActivity.class.getSimpleName();
    @Bind(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private MovieRecyclerViewAdapter mAdapter;
    private List<MovieSimple> mMovies;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private int mTotal;
    private int mCur;
    private boolean isLoading;
    private int mNotificationsFilter;
    private Map<String, String> mOptionalParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
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

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });

//        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mAdapter = new MovieRecyclerViewAdapter(this, mMovies);
        mRecyclerView.setAdapter(mAdapter);

        mOnScrollListener = new OnRecyclerViewScrollReachListener() {

            @Override
            public void onBottomReach() {
                super.onBottomReach();
                if (!isLoading) {
                    if (mCur < mTotal) {
                        loadNotifications(mCur, Const.ITEM_COUNT_PER_PAGE);
                        Toast.makeText(NotificationsActivity.this, R.string.loading,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NotificationsActivity.this, R.string.all_loaded,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        refresh(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
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
        isLoading = true;
        HttpHelper.mNewMoviesExpressService.notifications(GlobalVar.token, start, count, mNotificationsFilter, mOptionalParams
        ).enqueue(new HttpCallback<PagedList<MovieSimple>>() {

            @Override
            public void onSuccess(Call<PagedList<MovieSimple>> call, Response<PagedList<MovieSimple>> response) {
                onRefreshComplete();
                if (start < mCur) {
                    mMovies.clear();
                }
                PagedList<MovieSimple> movieList = response.body();
                mTotal = movieList.getTotal();
                mCur = movieList.getStart() + movieList.getCount();
                mMovies.addAll(movieList.getSubjects());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Call<PagedList<MovieSimple>> call, Response<PagedList<MovieSimple>> response, ApiError error) {
                onRefreshComplete();
                Toast.makeText(NotificationsActivity.this, error.getMsg(), Toast.LENGTH_SHORT).show();
                switch (error.getCode()) {
                    case Const.StatusCode.TOKEN_MISSING:
                    case Const.StatusCode.TOKEN_EXPIRED:
                        startActivity(new Intent(NotificationsActivity.this, LoginActivity.class));
                        break;
                }
            }

            @Override
            public void onFailure(Call<PagedList<MovieSimple>> call, Throwable t) {
                super.onFailure(call, t);
                onRefreshComplete();
            }
        });
    }

    private void refresh(boolean isTriggeredBySwipe) {
        if (!isTriggeredBySwipe) {
            if (!mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
            }
        }

        loadNotifications(0, Const.ITEM_COUNT_PER_PAGE);
    }

    private void onRefreshComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
        isLoading = false;
    }
}
