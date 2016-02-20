package com.github.crazyorr.newmoviesexpress.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.widget.KeywordsAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by wanglei02 on 2015/11/10.
 */
public class KeywordListFragment extends BaseFragment {

    private static final String KEY_KEYWORDS = "KEY_KEYWORDS";

    ExpandableStickyListHeadersListView mListView;

    public static KeywordListFragment newInstance(List<String> keywords) {
        KeywordListFragment fragment = new KeywordListFragment();
        ArrayList<String> list = new ArrayList<>();
        if(keywords != null){
            list.addAll(keywords);
        }
        Bundle args = new Bundle();
        args.putStringArrayList(KEY_KEYWORDS, list);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_keyword_list, container, false);
        mListView = ButterKnife.findById(view, R.id.list_view);
        mListView.setAdapter(new KeywordsAdapter(getContext(), getArguments().getStringArrayList(KEY_KEYWORDS)));
        mListView.setOnHeaderClickListener(new StickyListHeadersListView.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
                if (mListView.isHeaderCollapsed(headerId)) {
                    mListView.expand(headerId);
                } else {
                    mListView.collapse(headerId);
                }
            }
        });

        return view;
    }
}
