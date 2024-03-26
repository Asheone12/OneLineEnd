package com.muen.onelineend.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muen.onelineend.MainActivity;
import com.muen.onelineend.R;
import com.muen.onelineend.model.bean.Bean_Road;
import com.muen.onelineend.ui.fragment.DifficultyDetailFragment;
import com.muen.onelineend.util.ActivityUtil;
import com.muen.onelineend.util.ValueUtil;

import java.util.List;

public class Adapter_difficulty extends RecyclerView.Adapter {

    private List<List<Bean_Road>> roadValuesList;

    public Adapter_difficulty(List<List<Bean_Road>> roadValuesList){
        this.roadValuesList = roadValuesList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_difficulty,parent,false)){};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Button button = holder.itemView.findViewById(R.id.itemdifficulty);
        button.setText("关卡"+(position+1));
        button.setOnClickListener(v->{
            final Activity activity = ActivityUtil.getInstance().getCurActivity();
            if(activity instanceof MainActivity){
                Bundle bundle = new Bundle();
                bundle.putInt(ValueUtil.Cur_Detail_Position,position);
                ((MainActivity)activity).startFragment(DifficultyDetailFragment.class,0,bundle,null);
            }else {
                button.post(()->Toast.makeText(button.getContext(),"跳转失败，请联系管理员",Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public int getItemCount() {
        return roadValuesList.size();
    }
}
