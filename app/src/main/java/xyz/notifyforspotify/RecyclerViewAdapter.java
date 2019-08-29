package xyz.notifyforspotify;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private ArrayList<String> mImages;
    private ArrayList<String> mInfo;
    private ArrayList<String> mIcon;
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<String> info, ArrayList<String> images, ArrayList<String> icon) {
        mContext = context;
        mInfo = info;
        mImages = images;
        mIcon = icon;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_layout, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        Glide.with(mContext).asBitmap().load(mImages.get(i)).into(holder.image);
        holder.info.setText(mInfo.get(i));
        Glide.with(mContext).asBitmap().load(mIcon.get(i)).into(holder.icon);
    }

    @Override
    public int getItemCount() {
        return mInfo.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView info;
        RelativeLayout parentLayout;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            info = itemView.findViewById(R.id.info);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            icon = itemView.findViewById(R.id.newIcon);
        }
    }

    public void reset() {
        mInfo.clear();
        mImages.clear();
        mIcon.clear();
        notifyDataSetChanged();
    }

    public void add(String info, String image, String icon) {
        mInfo.add(info);
        mImages.add(image);
        mIcon.add(icon);
        notifyDataSetChanged();
    }
}
