package com.muen.onelineend.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.muen.onelineend.R;
import com.muen.onelineend.adapter.Adapter_difficulty_detail;
import com.muen.onelineend.databinding.LayoutDifficultyBinding;
import com.muen.onelineend.util.RoadValuesUtil;
import com.muen.onelineend.util.ValueUtil;


public class DifficultyDetailFragment extends BaseFragment<LayoutDifficultyBinding> {
    @Override
    public int getLayoutId() {
        return R.layout.layout_difficulty;
    }

    @Override
    public boolean initView() {
        int position = getNonNullArguments().getInt(ValueUtil.Cur_Detail_Position,0);
        viewBinding.returnButton.setOnClickListener(v -> onBackClick());
        viewBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(),5));
        if(position>=0 && position< RoadValuesUtil.roadValuesList.size()){
            viewBinding.recyclerView.setAdapter(new Adapter_difficulty_detail(position));
        }else {
            showToast("获取地图失败!关卡:"+(position+1));
        }
        viewBinding.difficultyHint.setVisibility(View.VISIBLE);
        viewBinding.difficultyHint.setText("关卡"+(position+1));
        return true;
    }

    @Override
    public void onNewArguments() {
        super.onNewArguments();
        isLoaded = initView();
    }

    @Override
    LayoutDifficultyBinding onCreateViewBinding(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutDifficultyBinding.inflate(inflater, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null&&data.getExtras()!=null) getNonNullArguments().putAll(data.getExtras());
        runOnUiThread(this::initView);
    }
}
