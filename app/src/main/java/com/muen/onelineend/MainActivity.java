package com.muen.onelineend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.muen.onelineend.databinding.ActivityMainBinding;
import com.muen.onelineend.service.MusicService;
import com.muen.onelineend.ui.activity.BaseActivity;
import com.muen.onelineend.ui.fragment.BaseFragment;
import com.muen.onelineend.ui.fragment.IndexFragment;
import com.muen.onelineend.util.ThreadUtil;
import com.muen.onelineend.util.ValueUtil;
import com.muen.onelineend.util.ViewUtil;

import java.util.List;

/**
 * @author muen
 * @version 1.0
 * Create by 2024/3/20 13:06
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> implements BaseFragment.OnBackClickListener{

    public <t extends BaseFragment> void startFragment(@NonNull Class<t> fragmentClass , int requestCode, @Nullable Bundle bundle, @Nullable List<View> shareElements) {
        ViewUtil.startFragment(getSupportFragmentManager(),getCurrentFragment(),R.id.fragmentLayout, fragmentClass, requestCode, bundle, shareElements);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        ValueUtil.toFindRoadToSql = getPreferences().getBoolean(ValueUtil.key_toFindRoadToSql,true);
        ValueUtil.toPlayMusic = getPreferences().getBoolean(ValueUtil.key_toPlayMusic,true);
        if(ValueUtil.toFindRoadToSql) ValueUtil.findRanRoadToSql(this);
        final List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if(ValueUtil.isListEmpty(fragments)){
            startFragment(IndexFragment.class,0,null,null);
        }else {
            boolean hasIndex = false;
            for(Fragment fragment : fragments){
                if(fragment instanceof IndexFragment){
                    hasIndex = true;
                    break;
                }
            }
            if(!hasIndex){
                startFragment(IndexFragment.class,0,null,null);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(ValueUtil.key_toPlayMusic,ValueUtil.toPlayMusic?0:2);
        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(ValueUtil.key_toPlayMusic,1);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, MusicService.class));
        ThreadUtil.getInstance().removeRunable("findRoadToSql");
        super.onDestroy();
    }

    @Override
    protected ActivityMainBinding onCreateViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    public boolean isNeedCleanFragments() {
        return false;
    }

    @Override
    public void onBackClick() {
        onBackPressed();
    }
}

