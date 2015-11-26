package com.github.crazyorr.newmoviesexpress.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.controller.HttpCallback;
import com.github.crazyorr.newmoviesexpress.model.AddKeywordsData;
import com.github.crazyorr.newmoviesexpress.model.Keyword;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;

/**
 * Created by wanglei02 on 2015/11/12.
 */
public class AddKeywordsActivity extends BackableActivity {
    private static final String TAG = AddKeywordsActivity.class.getSimpleName();

    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private List<Keyword> mKeywords;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_keywords);
        ButterKnife.bind(this);

        Toolbar toolbar = findById(this, R.id.toolbar);
        toolbar.setTitle(R.string.add_keywords);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mKeywords = new LinkedList<>();
        mKeywords.add(new Keyword());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new AddKeywordsItemsAdapter(this, mKeywords));

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocusFromTouch();
                switch (v.getId()) {
                    case R.id.btn_add_one:
                        mKeywords.add(new Keyword());
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        break;
                    case R.id.btn_submit:
                        AddKeywordsData data = new AddKeywordsData();
                        String[] categories = getResources().getStringArray(R.array.categories);
                        for (Keyword keyword : mKeywords) {
                            if (TextUtils.isEmpty(keyword.getValue())) {
                                Toast.makeText(AddKeywordsActivity.this, R.string.please_submit_after_completion, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String value = keyword.getValue();
                            String category = categories[keyword.getCategoryIndex()];
                            if(category.equals(getString(R.string.titles))){
                                data.addTitle(value);
                            }else if(category.equals(getString(R.string.casts))){
                                data.addCast(value);
                            }else if(category.equals(getString(R.string.directors))){
                                data.addDirector(value);
                            }
                        }
                        showLoadingDialog();
                        HttpUrl url = Const.getMyUrlBuilder().addPathSegment("add_keywords").build();
                        HttpHelper.postAsync(url, data.toJson(), new HttpCallback() {
                            @Override
                            public void onFailure(Request request, IOException e) {
                                super.onFailure(request, e);
                                dismissLoadingDialog();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(AddKeywordsActivity.this, R.string.submit_fail, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                super.onResponse(response);
                                dismissLoadingDialog();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(AddKeywordsActivity.this, R.string.submit_succeed, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                finish();
                            }
                        });
                        break;
                }
            }
        };
        Button btnAddOne = findById(this, R.id.btn_add_one);
        btnAddOne.setOnClickListener(onClickListener);
        Button btnSubmit = findById(this, R.id.btn_submit);
        btnSubmit.setOnClickListener(onClickListener);
    }

    class AddKeywordsItemsAdapter
            extends RecyclerView.Adapter<AddKeywordsItemsAdapter.ViewHolder> {

        private Context mContext;
        private List<Keyword> mKeywords;

        public AddKeywordsItemsAdapter(Context context, List<Keyword> keywords) {
            mContext = context;
            mKeywords = keywords;
        }

        @Override
        public AddKeywordsItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_keyword_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AddKeywordsItemsAdapter.ViewHolder holder, final int position) {
            final Keyword keyword = mKeywords.get(position);
            holder.mCategory.setSelection(keyword.getCategoryIndex());
            holder.mCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    keyword.setCategoryIndex(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            holder.mKeyword.setText(keyword.getValue());
            holder.mKeyword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        String input = ((EditText) v).getText().toString();
                        if (!TextUtils.isEmpty(input)) {
                            keyword.setValue(input);
                        }
                    }
                }
            });

            if (position == 0) {
                holder.mDelete.setEnabled(false);
            } else {
                holder.mDelete.setEnabled(true);
            }
            holder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mKeywords.remove(position);
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            if (mKeywords == null) {
                return 0;
            } else {
                return mKeywords.size();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.spn_category)
            public Spinner mCategory;
            @Bind(R.id.et_keyword)
            public EditText mKeyword;
            @Bind(R.id.btn_delete)
            public Button mDelete;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(mContext,
                        R.array.categories, android.R.layout.simple_spinner_item);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mCategory.setAdapter(dataAdapter);
            }
        }
    }
}
