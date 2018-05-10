package com.example.affine.chatapp2;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.ViewHolder> {
    private List<UserInfo> mDataset = Lists.newArrayList();
    private RecyclerViewClickListener mListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message;
        public LinearLayout layout;

        public ViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.channelName);
            layout = v.findViewById(R.id.fitemLayout);
        }
    }

    public FriendsListAdapter() {

    }

    public interface RecyclerViewClickListener {
        void onClick(UserInfo userInfo);
    }


    public void setOnItemClickListener(RecyclerViewClickListener mListener) {
        this.mListener = mListener;
    }

    public FriendsListAdapter(List<UserInfo> myDataset) {
        mDataset = myDataset;
    }

    public void setmDataset(List<UserInfo> myDataset) {
        mDataset = myDataset;
        notifyDataSetChanged();
    }

    @Override
    public FriendsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final UserInfo dataModel = mDataset.get(position);
        holder.message.setText(dataModel.getName() + "-" + dataModel.getId());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(dataModel);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}