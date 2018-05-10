package com.example.affine.chatapp2;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {
    private List<DataModel> mDataset = Lists.newArrayList();
    private int userId;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message;
        public TextView sentTime;
        public LinearLayout messageLayout;

        public ViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.chatMessage);
            sentTime = v.findViewById(R.id.sentTime);
            messageLayout = v.findViewById(R.id.messageParentLayout);
        }
    }

    public MessageListAdapter() {

    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public MessageListAdapter(List<DataModel> myDataset) {
        mDataset = myDataset;
    }

    public void setmDataset(List<DataModel> myDataset) {
        mDataset = myDataset;
        notifyDataSetChanged();
    }

    @Override
    public MessageListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataModel dataModel = mDataset.get(position);
        if (dataModel == null) {
            return;
        }
        holder.message.setText(dataModel.getMessage());
        holder.sentTime.setText(TimeDateUtil.formatTimeOnly(dataModel.getMsgId()));
        if (dataModel.getUserId() != userId) {
            holder.messageLayout.setGravity(Gravity.LEFT);
            holder.message.setBackgroundResource(R.drawable.their_message);
        } else {
            holder.messageLayout.setGravity(Gravity.RIGHT);
            holder.message.setBackgroundResource(R.drawable.my_message);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}