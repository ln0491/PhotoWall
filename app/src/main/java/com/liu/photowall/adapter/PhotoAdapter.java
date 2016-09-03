package com.liu.photowall.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.liu.photowall.R;
import com.liu.photowall.util.OtherUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 刘楠 on 2016/9/3 0003.14:45
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {


    private Context        mContext;
    private LayoutInflater mInflater;
    private List<String>   mDatas;
    private String         mDirPath;
    private List<String> mSelectedList = new ArrayList<>();
    private boolean      mIsLastUse    = true;


    public String getDirPath() {
        return mDirPath;
    }

    public void setDirPath(String dirPath) {
        mDirPath = dirPath;
    }

    public List<String> getDatas() {
        return mDatas;
    }

    public void setDatas(List<String> datas) {
        mDatas = datas;
    }

    public List<String> getSelectedList() {
        return mSelectedList;
    }

    public void setSelectedList(List<String> selectedList) {
        mSelectedList = selectedList;
    }

    public PhotoAdapter(Context context, List<String> datas, String dirPath, boolean isLastUse) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
        mDirPath = dirPath;
        mIsLastUse = isLastUse;
    }

    public boolean getIsLastUse() {
        return mIsLastUse;
    }

    public void setLastUse(boolean lastUse) {
        mIsLastUse = lastUse;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = mInflater.inflate(R.layout.item_photo_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String path = mDatas.get(position);


        if (mIsLastUse) {
            if (mSelectedList.contains(path)) {

                holder.mIbSelect.setSelected(true);
                holder.mIvPhoto.setColorFilter(Color.parseColor("#77000000"));
            } else {
                holder.mIbSelect.setSelected(false);

                holder.mIvPhoto.setColorFilter(null);
            }
            Picasso.with(mContext).load(new File(mDirPath + "" + path)).error(R.mipmap.pictures_no).placeholder(R.mipmap.pictures_no).centerCrop().resize(OtherUtils.dip2px(mContext, 120), OtherUtils.dip2px(mContext, 100)).into(holder.mIvPhoto);
        } else {
            if (mSelectedList.contains(mDirPath + "/" + path)) {

                holder.mIbSelect.setSelected(true);
                holder.mIvPhoto.setColorFilter(Color.parseColor("#77000000"));
            } else {
                holder.mIbSelect.setSelected(false);

                holder.mIvPhoto.setColorFilter(null);
            }
            Picasso.with(mContext).load(new File(mDirPath + "/" + path)).error(R.mipmap.pictures_no).placeholder(R.mipmap.pictures_no).centerCrop().resize(OtherUtils.dip2px(mContext, 120), OtherUtils.dip2px(mContext, 100)).into(holder.mIvPhoto);
        }


    }

    @Override
    public int getItemCount() {

        return mDatas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mIvPhoto;

        ImageButton mIbSelect;

        public ViewHolder(View itemView) {
            super(itemView);

            initView(itemView);
            initListener();
        }

        private void initView(View itemView) {
            mIvPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
            mIbSelect = (ImageButton) itemView.findViewById(R.id.ibSelect);

        }

        private void initListener() {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(getAdapterPosition(), v);
                    }
                }
            });

        }
    }

    private OnItemClickListener mOnItemClickListener;

    public void setmOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {

        void onItemClick(int position, View itemView);
    }
}
