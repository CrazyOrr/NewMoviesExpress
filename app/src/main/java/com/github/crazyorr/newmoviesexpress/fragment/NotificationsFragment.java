package com.github.crazyorr.newmoviesexpress.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;
import com.github.crazyorr.newmoviesexpress.widget.MovieRecyclerViewAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public class NotificationsFragment extends BaseFragment {

    private static final String TAG = NotificationsFragment.class.getSimpleName();

    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;

    public static NotificationsFragment newInstance() {
        NotificationsFragment fragment = new NotificationsFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.activity_notifications, container, false);
        ButterKnife.bind(this, view);

        Toolbar toolbar = findById(view, R.id.toolbar);
        toolbar.setTitle(R.string.notifications);
        getBaseActivity().setSupportActionBar(toolbar);

        final ActionBar ab = getBaseActivity().getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            showLoadingDialog();

            HttpHelper.mNewMoviesExpressService.notifications().enqueue(new HttpCallback<List<MovieSimple>>() {
                @Override
                public void onSuccess(retrofit2.Call<List<MovieSimple>> call, retrofit2.Response<List<MovieSimple>> response) {
                    final List<MovieSimple> list = response.body();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.setAdapter(new MovieRecyclerViewAdapter(getActivity(), list));
                        }
                    });
                    dismissLoadingDialog();
                }

                @Override
                public void onFailure(retrofit2.Call<List<MovieSimple>> call, Throwable t) {
                    super.onFailure(call, t);
                    dismissLoadingDialog();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), R.string.load_fail, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
