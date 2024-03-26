package com.muen.onelineend.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.muen.onelineend.MainActivity;
import com.muen.onelineend.R;
import com.muen.onelineend.model.MySql;
import com.muen.onelineend.model.bean.Bean_Road;
import com.muen.onelineend.ui.fragment.RoadFragment;
import com.muen.onelineend.util.ActivityUtil;
import com.muen.onelineend.util.RoadValuesUtil;
import com.muen.onelineend.util.ValueUtil;

import java.util.List;

public class Adapter_difficulty_detail extends RecyclerView.Adapter {

    private List<Bean_Road> roadList;
    private MySql mySql;
    private int curDetailPosition;

    public Adapter_difficulty_detail(int curDetailPosition){
        this.roadList = RoadValuesUtil.roadValuesList.get(curDetailPosition);
        this.curDetailPosition = curDetailPosition;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_difficulty_detail,parent,false)){};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(mySql == null) mySql=new MySql(holder.itemView.getContext());
        TextView textView = holder.itemView.findViewById(R.id.itemDifficultyDetail);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(textView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        textView.setText(""+(position+1));
        if(mySql.checkPassedYibi(roadList.get(position))){
            textView.setBackgroundResource(R.drawable.shape_gray_selected);
        }else {
            textView.setBackgroundResource(R.drawable.shape_gray_unselected);
        }
        textView.setOnClickListener(v->{
            final Activity activity = ActivityUtil.getInstance().getCurActivity();
            if(activity instanceof MainActivity){
                Bundle bundle = new Bundle();
                bundle.putInt(ValueUtil.Cur_Detail_Position,curDetailPosition);
                bundle.putInt(ValueUtil.Cur_Road_Position,position);
                ((MainActivity)activity).startFragment(RoadFragment.class,0,bundle,null);
            }else {
                textView.post(()->Toast.makeText(textView.getContext(),"跳转失败，请联系管理员",Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public int getItemCount() {
        return roadList.size();
    }
}
