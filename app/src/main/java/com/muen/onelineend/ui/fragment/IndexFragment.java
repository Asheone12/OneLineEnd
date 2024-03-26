package com.muen.onelineend.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.muen.onelineend.MainActivity;
import com.muen.onelineend.R;
import com.muen.onelineend.databinding.LayoutIndexBinding;
import com.muen.onelineend.util.ActivityUtil;
import com.muen.onelineend.util.ViewUtil;

public class IndexFragment extends BaseFragment<LayoutIndexBinding> {

    @Override
    LayoutIndexBinding onCreateViewBinding(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutIndexBinding.inflate(inflater, container, false);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_index;
    }

    @Override
    public boolean initView() {
        viewBinding.btPrimary.setOnClickListener(v -> {
            if(getActivity() instanceof MainActivity){
                ((MainActivity)getActivity()).startFragment(DifficultyFragment.class,0,null,null);
            }else {
                showToast("跳转失败，请联系开发人员");
            }
        });
        viewBinding.btRandom.setOnClickListener(v -> {
            if(getActivity() instanceof MainActivity){
                ((MainActivity)getActivity()).startFragment(RandomRoadFragment.class,0,null,null);
            }else {
                showToast("跳转失败，请联系开发人员");
            }
        });
        viewBinding.btSetting.setOnClickListener(v-> ViewUtil.getSettingDialog(getPreferencesEditor()));
        viewBinding.btFinsh.setOnClickListener(v -> ActivityUtil.getInstance().finishActivity());
        return true;
    }
}
