package example.api.location.google.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import example.api.location.google.Models.user_model;
import example.api.location.google.R;
import example.api.location.google.storeRoom;


public class peopleAdapter extends RecyclerView.Adapter<peopleAdapter.myHolder> {

    Context context;
    ArrayList<user_model> modelArrayList, temp;
    String currentIterationUID;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position, String operation);
    }

    public peopleAdapter(ArrayList<user_model> modelArrayList, OnItemClickListener onItemClickListener) {
        this.temp = modelArrayList;
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public myHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.people_single_row, null);
        v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        myHolder obj = new myHolder(v);
        return obj;
    }

    @Override
    public void onBindViewHolder(final myHolder holder, final int position) {
        String name = modelArrayList.get(position).getName();
        if (name.charAt(0) != '@') {
            name = "@" + name;
        }
        holder.username.setText(name);

        Log.i("12sdsdsd", "getting : " + modelArrayList.get(position).getProfileAddress());
        if (!modelArrayList.get(position).getProfileAddress().equalsIgnoreCase("none")) {
            Log.i("12sdsdsd", "setting : " + modelArrayList.get(position).getProfileAddress());
            Glide.with(context).load(storeRoom.getImagesRootDir(modelArrayList.get(position).getProfileAddress())).listener(requestListener).into(holder.userDP);
        } else {
            holder.userDP.setImageResource(R.drawable.user);
        }


        holder.addToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<user_model> suspect = reverseList();
                for (int i = 0; i < suspect.size(); i++) {
                    if (suspect.get(i).getKey().equals(modelArrayList.get(position).getKey())) {
                        mOnItemClickListener.onItemClick(v, i, "addToList");
                    }
                }


            }
        });
    }

    private RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            // todo log exception to central service or something like that
            Log.i("1323213123", e.getMessage());
            // important to return false so the error placeholder can be placed
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            return false;
        }


    };

    @Override
    public int getItemCount() {
        modelArrayList = new ArrayList<>();
        for (int i = temp.size() - 1; i > -1; i--) {
            modelArrayList.add(temp.get(i));
        }
        return modelArrayList.size();
    }

    public ArrayList<user_model> reverseList() {
        ArrayList<user_model> arrayList = new ArrayList<>();
        for (int i = modelArrayList.size() - 1; i > -1; i--) {
            arrayList.add(modelArrayList.get(i));
        }
        return arrayList;
    }

    public class myHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView addToList, userDP;

        public myHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();

            username = (TextView) itemView.findViewById(R.id.username);
            addToList = (ImageView) itemView.findViewById(R.id.addToList);
            userDP = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }
}