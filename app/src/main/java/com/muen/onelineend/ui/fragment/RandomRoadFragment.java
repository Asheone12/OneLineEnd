package com.muen.onelineend.ui.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;

import com.muen.onelineend.R;
import com.muen.onelineend.databinding.LayoutRoadRandomBinding;
import com.muen.onelineend.model.bean.Bean_Collection;
import com.muen.onelineend.model.bean.Bean_Road;
import com.muen.onelineend.util.AnimUtil;
import com.muen.onelineend.util.FindRoadUtil;
import com.muen.onelineend.util.OtherUtil;
import com.muen.onelineend.util.ThreadUtil;
import com.muen.onelineend.util.ValueUtil;
import com.muen.onelineend.util.ViewUtil;
import com.muen.onelineend.widget.Grid_Yibi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomRoadFragment extends BaseFragment<LayoutRoadRandomBinding> implements View.OnClickListener, Grid_Yibi.yibiListener {

    private Random random=new Random();
    private AlertDialog waittingDialog,collectionDialog;
    private FindRoadUtil _findRoadUtil;
    private int rows=5,columns=5,difficulties=4;
    private boolean firstPassed=false;
    public boolean passPassed=false;
    private boolean getNoPassedRoad=false;
    private boolean ishelping=false;
    private boolean isCreatedHint=true;


    @Override
    public boolean onBackPressed() {
        return stopGettingRoad();
    }

    @Override
    public boolean stopGettingRoad(){
        if(waittingDialog!=null&&waittingDialog.isShowing()){
            waittingDialog.dismiss();
            return true;
        }
        return false;
    }

    @Override
    LayoutRoadRandomBinding onCreateViewBinding(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutRoadRandomBinding.inflate(inflater, container, false);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_road_random;
    }

    @Override
    public boolean initView() {
        rows=getPreferences().getInt("rows",5);
        columns=getPreferences().getInt("columns",5);
        difficulties=getPreferences().getInt("difficulties",4);
        passPassed=getPreferences().getBoolean("passPassed",false);
        isCreatedHint=getPreferences().getBoolean("isCreatedHint",true);

        boolean haveSavedYibi=getPreferences().getBoolean("haveSavedYibi",false);
        String roadposition=getPreferences().getString("roadposition","");
        String passedposition=getPreferences().getString("passedposition","");

        viewBinding.nextButton.setOnClickListener(this);
        viewBinding.returnButton.setOnClickListener(this);
        viewBinding.refreshButton.setOnClickListener(this);
        viewBinding.helpButton.setOnClickListener(this);
        viewBinding.collectionButton.setOnClickListener(this);
        viewBinding.setting.setOnClickListener(this);
        viewBinding.setting.setText("点击设置\n"+rows+"*"+columns+" | "+difficulties);
        viewBinding.passPassed.setChecked(passPassed);
        viewBinding.passPassed.setOnCheckedChangeListener((buttonView, isChecked) -> {
            passPassed=isChecked;
            ValueUtil.roadQueue.clear();
            getPreferencesEditor().putBoolean("passPassed",passPassed);
            getPreferencesEditor().apply();
        });
        viewBinding.createdHint.setOnLongClickListener(v -> {
            AnimUtil.removeAnimator(viewBinding.createdHint);
            isCreatedHint=false;
            getPreferencesEditor().putBoolean("isCreatedHint",false);
            getPreferencesEditor().apply();
            viewBinding.createdHint.setVisibility(View.GONE);
            showToast("已取消提示");
            return false;
        });

        waittingDialog = ViewUtil.getWaittingDialog();
        _findRoadUtil =new FindRoadUtil(getContext());
        waittingDialog.setOnDismissListener(dialog -> {
            runOnUiThread(()->{
                if(waittingDialog!=null){
                    View h = waittingDialog.findViewById(R.id.hint);
                    if(h!=null){
                        h.clearAnimation();
                    }
                }
            });
            if(_findRoadUtil !=null){
                _findRoadUtil.startToFindRoad=false;
            }
            getNoPassedRoad=false;
        });

        if(!haveSavedYibi){
            initGirdRoad(rows,columns,difficulties);
        }else {
            checkPassedView(new Bean_Road(rows,columns,ValueUtil.getIntListFromStrs(roadposition.split("[,]"))));
            viewBinding.gridYibi.initPassedGrid(rows,columns,difficulties,roadposition,passedposition,this);
        }


        ValueUtil.findRoadToQueue(getContext(),rows,columns,difficulties,passPassed);
        ThreadUtil.getInstance().schedule(()->
                runOnUiThread(()->{
                            final int kc,dl;
                            if(ValueUtil.queueFindRoadUtil!=null
                                    && ValueUtil.queueFindRoadUtil.rows==rows
                                    && ValueUtil.queueFindRoadUtil.columns==columns
                                    && ValueUtil.queueFindRoadUtil.difficulties==difficulties){
                                kc = getMySql().getSavedCount(rows,columns,difficulties);
                                dl = ValueUtil.roadQueue.size();
                            }else {
                                kc = 0;
                                dl = 0;
                            }
                    viewBinding.sum.setText("库存:"+kc+"\n队列:"+dl);
                        }),
                300);

        return true;
    }

    private void checkPassedView(Bean_Road road){
        runOnUiThread(()->{
            if(getMySql().checkPassedYibi(road)){
                viewBinding.passedHint.setVisibility(View.VISIBLE);
                viewBinding.passPassed.setVisibility(View.VISIBLE);
            }else {
                viewBinding.passedHint.setVisibility(View.GONE);
                viewBinding.passPassed.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        AnimUtil.clearAnimator();
        ValueUtil.roadQueue.clear();
    }

    @Override
    public void onDestroyView() {
        ThreadUtil.getInstance().removeRunable("getRoadToQueue");
        super.onDestroyView();
    }

    @Override
    public void initGirdRoad(final int initRows,final int initColums,final int initDifficulties) {
        firstPassed=false;
        if(waittingDialog!=null&&!waittingDialog.isShowing()){
            runOnUiThread(() -> waittingDialog.show());
        }

        ThreadUtil.getInstance().addRunableToSingleThead("initGirdRoad",() -> {
            Bean_Road aRoad=null;
            if(passPassed){
                getNoPassedRoad=true;
                long starttime=System.currentTimeMillis();
                boolean t=false;
                while (getNoPassedRoad){
                    //先从数据库中找
                    Cursor cursor=getMySql().getSavedYibi(initRows,initColums,initDifficulties);
                    if(cursor.getCount()>0){
                        boolean sqlGot=false;
                        cursor.moveToPosition(random.nextInt(cursor.getCount()));
                        while (cursor.getPosition()!=cursor.getCount()){
                            String[] roadpositions=cursor.getString(4).split("[,]");
                            aRoad=new Bean_Road(initRows,initColums,ValueUtil.getIntListFromStrs(roadpositions));
                            if(!getMySql().checkPassedYibi(aRoad)){
                                sqlGot=true;
                                getNoPassedRoad=false;
                                break;
                            }
                            cursor.moveToNext();
                        }
                        if(!sqlGot){
                            while (cursor.moveToPrevious()){
                                String[] roadpositions=cursor.getString(4).split("[,]");
                                aRoad=new Bean_Road(initRows,initColums,ValueUtil.getIntListFromStrs(roadpositions));
                                if(!getMySql().checkPassedYibi(aRoad)){
                                    sqlGot=true;
                                    getNoPassedRoad=false;
                                    break;
                                }
                            }
                        }
                        if(sqlGot) break;
                    }
                    cursor.close();


                    //找不到再从队列拿或者直接生成
                    if(!ValueUtil.roadQueue.isEmpty()){
                        aRoad=ValueUtil.roadQueue.poll();
                    }else {
                        aRoad=_findRoadUtil.getAppointedRoad(initRows,initColums,initDifficulties,passPassed);
                    }
                    if(aRoad!=null){
                        if(!getMySql().checkPassedYibi(aRoad)){
                            getNoPassedRoad=false;
                            break;
                        }
                    }
                    if(!t&&System.currentTimeMillis()-starttime>=3000){
                        showToast("或许你已经全通关当前所设置的难度了,你可以选择其它难度");
                        t=true;
                    }
                }
            }else {
                //先从数据库中随机找
                Cursor cursor=getMySql().getSavedYibi(initRows,initColums,initDifficulties);
                if(cursor.getCount()>0){
                    cursor.moveToPosition(random.nextInt(cursor.getCount()));
                    String[] roadpositions=cursor.getString(4).split("[,]");
                    aRoad=new Bean_Road(rows,columns,ValueUtil.getIntListFromStrs(roadpositions));
                }
                cursor.close();

                //找不到再从队列拿或直接生成
                if(aRoad==null){
                    if(!ValueUtil.roadQueue.isEmpty()){
                        aRoad=ValueUtil.roadQueue.poll();
                    }else {
                        aRoad= _findRoadUtil.getAppointedRoad(initRows,initColums,initDifficulties,passPassed);
                    }
                }
            }

            final Bean_Road road=aRoad;

            final boolean ispassed;
            if(road!=null){
                ispassed = getMySql().checkPassedYibi(road);
            }else {
                ispassed=false;
            }
            if(road!=null&&(road.getRows()!=initRows||road.getColumns()!=initColums||road.getDifficulties()!=initDifficulties)&&getNoPassedRoad){
                initGirdRoad(initRows,initColums,initDifficulties);
                return;
            }

            if(passPassed&&ispassed&&getNoPassedRoad){
                initGirdRoad(initRows,initColums,initDifficulties);
                return;
            }

            runOnUiThread(() -> {
                if(road!=null){
                    checkPassedView(road);
                    if(isCreatedHint){
                        viewBinding.createdHint.setVisibility(View.VISIBLE);
                        AnimUtil.doScale(viewBinding.createdHint,0,1,0,1,null,true);
                    }
                    rows=initRows;
                    columns=initColums;
                    difficulties=initDifficulties;
                    viewBinding.gridYibi.initGrid(road, RandomRoadFragment.this);
                    saveYibi(road,new ArrayList<>());
                    viewBinding.setting.setText("点击设置\n"+rows+"*"+columns+" | "+difficulties);
                }else {
                    showToast("取消构图");
                    if(rows!=initRows || initColums!=columns || difficulties!=initDifficulties)
                        ValueUtil.findRoadToQueue(getContext(),rows,columns,difficulties,passPassed);
                }
            });
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.refreshButton:
                runOnUiThread(()->viewBinding.gridYibi.refreshGrid());
                break;
            case R.id.nextButton:
                initGirdRoad(rows,columns,difficulties);
                break;
            case R.id.returnButton:
                onBackClick();
                break;
            case R.id.setting:
                View selectorView=ViewUtil.getSelectorView();
                initSelectorView(selectorView);
                break;
            case R.id.helpButton:
                if(!ishelping){
                    viewBinding.gridYibi.getHelp();
                }else {
                    runOnUiThread(()->viewBinding.gridYibi.refreshGrid());
                }
                break;
            case R.id.collectionButton:
                showCollectionView();
                break;
        }
    }

    private void showCollectionView() {
        runOnUiThread(()->{
            Cursor cursor=getMySql().getAllPassedYibi();
            List<Bean_Collection> collectionList=new ArrayList<>();
            while (cursor.moveToNext()){
                Bean_Collection collection=new Bean_Collection(cursor.getInt(1)
                        ,cursor.getInt(2)
                        ,cursor.getInt(3));
                boolean h=false;
                for(Bean_Collection c:collectionList){
                    if(c.equals(collection)){
                        c.addSum();
                        h=true;
                    }
                }
                if(!h){
                    collection.addSum();
                    collectionList.add(collection);
                }
            }
            cursor.close();
            Collections.sort(collectionList, (o1, o2) -> {
                if(o1.getSum()>o2.getSum()){
                    return -1;
                }else if(o1.getSum()==o2.getSum()){
                    return 0;
                }
                return 1;
            });
            collectionDialog = ViewUtil.getCollectionDialog(collectionList,v -> ViewUtil.getAskDialog(
                    "", "确认清空记录？",
                    new OtherUtil.OnCallBackListenerImpl<Boolean>(){
                        @Override
                        public void OnCallBackFirst(Boolean... params) {
                            getMySql().cleanPassedYibi();
                            if(collectionDialog!=null){
                                collectionDialog.dismiss();
                            }
                            showCollectionView();
                        }
                    }));
        });
    }

    @Override
    public void setIsHelping(boolean isHelping) {
        runOnUiThread(()->{
            if(isHelping){
                viewBinding.helpButton.setBackgroundResource(R.drawable.ic_helping);
            }else {
                viewBinding.helpButton.setBackgroundResource(R.drawable.ic_help);
            }
            this.ishelping=isHelping;
        });
    }

    @Override
    public boolean isHelping() {
        return ishelping;
    }

    private void initSelectorView(View selectorView) {
        runOnUiThread(()->{
            final NumberPicker rowPicker=selectorView.findViewById(R.id.rowPicker);
            final NumberPicker columnPicker=selectorView.findViewById(R.id.columnPicker);
            final NumberPicker forbiddenPicker=selectorView.findViewById(R.id.forbiddenPicker);
            final CheckBox passPassedBox=selectorView.findViewById(R.id.passPassed);
            final Button qd=selectorView.findViewById(R.id.qd);
            final AlertDialog dialog=(AlertDialog) selectorView.getTag();

            passPassedBox.setChecked(passPassed);


            qd.setOnClickListener(v -> {
                dialog.dismiss();
                if(rows!=rowPicker.getValue() || columns!=columnPicker.getValue() || difficulties!=forbiddenPicker.getValue()){
                    initGirdRoad(rowPicker.getValue(),columnPicker.getValue(),forbiddenPicker.getValue());
                    ValueUtil.findRoadToQueue(getContext(),rowPicker.getValue(),columnPicker.getValue(),forbiddenPicker.getValue(),passPassed);
                }
                viewBinding.passPassed.setChecked(passPassedBox.isChecked());
            });

            NumberPicker.OnValueChangeListener listener= (picker, oldVal, newVal) -> {
                int rv=rowPicker.getValue();
                int cv=columnPicker.getValue();
                forbiddenPicker.setMinValue(Math.min(rv,cv)-1);
                forbiddenPicker.setMaxValue(rv*cv/2-2);
            };

            rowPicker.setOnValueChangedListener(listener);
            columnPicker.setOnValueChangedListener(listener);

            rowPicker.setMinValue(3);
            rowPicker.setMaxValue(6);
            rowPicker.setValue(rows);

            columnPicker.setMinValue(3);
            columnPicker.setMaxValue(6);
            columnPicker.setValue(columns);

            int rv=rowPicker.getValue();
            int cv=columnPicker.getValue();
            forbiddenPicker.setMinValue(Math.min(rv,cv)-1);
            forbiddenPicker.setMaxValue(rv*cv/2-2);
            forbiddenPicker.setValue(difficulties);
        });
    }


    @Override
    public void passed(Bean_Road road) {
        if(road==null) return;
        if(!firstPassed){
            firstPassed=true;
            checkPassedView(road);
            getMySql().insertPassedYibi(road);
            ViewUtil.getAskDialog( "","恭喜通过",new OtherUtil.OnCallBackListenerImpl<Boolean>(){
                @Override
                public void OnCallBackFirst(Boolean... params) {
                    initGirdRoad(rows,columns,difficulties);
                }
            },"再来", "算了");
        }
    }

    @Override
    public void saveYibi(Bean_Road road, List<Integer> passedPositions) {
        if(road==null) return;
        getPreferencesEditor().putBoolean("haveSavedYibi",true);
        getPreferencesEditor().putInt("rows",road.getRows());
        getPreferencesEditor().putInt("columns",road.getColumns());
        getPreferencesEditor().putInt("difficulties",road.getDifficulties());

        getPreferencesEditor().putString("roadposition",road.getRoadString());

        String passedString= ValueUtil.getListString(passedPositions);
        getPreferencesEditor().putString("passedposition",passedString);

        getPreferencesEditor().apply();
    }
}
