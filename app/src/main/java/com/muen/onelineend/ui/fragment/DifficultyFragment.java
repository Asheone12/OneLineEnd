package com.muen.onelineend.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.muen.onelineend.R;
import com.muen.onelineend.adapter.Adapter_difficulty;
import com.muen.onelineend.databinding.LayoutDifficultyBinding;
import com.muen.onelineend.util.RoadValuesUtil;

public class DifficultyFragment extends BaseFragment<LayoutDifficultyBinding> {

    @Override
    LayoutDifficultyBinding onCreateViewBinding(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutDifficultyBinding.inflate(inflater, container, false);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_difficulty;
    }

    @Override
    public boolean initView() {
        viewBinding.returnButton.setOnClickListener(v -> onBackClick());
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewBinding.recyclerView.setAdapter(new Adapter_difficulty(RoadValuesUtil.roadValuesList));
        viewBinding.difficultyHint.setVisibility(View.GONE);
        return true;
    }
}
