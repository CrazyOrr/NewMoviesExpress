package com.github.crazyorr.newmoviesexpress.view;

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
import com.github.crazyorr.newmoviesexpress.controller.HttpCallback;
import com.github.crazyorr.newmoviesexpress.controller.MovieRecyclerViewAdapter;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
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
        getSupportActivity().setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActivity().getSupportActionBar();
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
            HttpUrl url = Const.getMyUrlBuilder().addPathSegment("api").addPathSegment("notifications").build();
            HttpHelper.getAsync(url, new HttpCallback() {

                @Override
                public void onResponse(Response response) throws IOException {
                    super.onResponse(response);
                    String json = response.body().string();
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<MovieSimple>>() {
                    }.getType();
                    final List<MovieSimple> list = gson.fromJson(json, listType);
                    getSupportActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.setAdapter(new MovieRecyclerViewAdapter(getActivity(), list));
                        }
                    });
                    dismissLoadingDialog();
                }

                @Override
                public void onFailure(Request request, IOException e) {
                    super.onFailure(request, e);
                    dismissLoadingDialog();
                    getSupportActivity().runOnUiThread(new Runnable() {
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
