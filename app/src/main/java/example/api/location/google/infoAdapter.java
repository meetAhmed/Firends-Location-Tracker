package example.api.location.google;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import example.api.location.google.Models.linkedPeople;
import example.api.location.google.preferenceManager.preferenceManager;


public class infoAdapter extends RecyclerView.Adapter<infoAdapter.myHolder> {

    Context context;
    ArrayList<linkedPeople> modelArrayList;
    public int selectedPosition = -99;
    String username;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position, String username);
    }

    public int getItemViewType(int position) {
        if (selectedPosition == position) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public infoAdapter.myHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case 0:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_info_row, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_info_row_active, parent, false);
                break;
        }
        myHolder hold = new myHolder(v);
        return hold;
    }

    public infoAdapter(ArrayList<linkedPeople> chapterArrayList, OnItemClickListener onItemClickListener) {
        this.modelArrayList = chapterArrayList;
        mOnItemClickListener = onItemClickListener;
    }


    @Override
    public void onBindViewHolder(final myHolder holder, final int position) {

        String userKey = preferenceManager.getkey(context);
        Log.i("1232323", "userkey is " + userKey);
        String otherKey = null;
        if (userKey.equals(modelArrayList.get(position).getSender())) {
            Log.i("1232323", "pass 1");
            otherKey = modelArrayList.get(position).getReceiver();
        } else {
            Log.i("1232323", "pass 2");
            otherKey = modelArrayList.get(position).getSender();
        }
        Log.i("1232323", "pass 3");
        Log.i("1232323", "sender is " + modelArrayList.get(position).getSender());
        Log.i("1232323", "receiver is " + modelArrayList.get(position).getReceiver());

        FirebaseDatabase.getInstance().getReference().child("users").child(otherKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("name").getValue().toString();
                if (username.charAt(0) != '@') {
                    username = "@" + username;
                }
                holder.username.setText(username);
                String profileRef = dataSnapshot.child("profileAddress").getValue().toString();
                if (!profileRef.equalsIgnoreCase("none")) {
                    Glide.with(context).load(storeRoom.getImagesRootDir(profileRef)).into(holder.user_dp);
                } else {
                    holder.user_dp.setImageResource(R.drawable.user);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, position, holder.username.getText().toString());
            }
        });


    }

    @Override
    public int getItemCount() {
        return modelArrayList.size();
    }

    public class myHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView user_dp;

        public myHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();

            user_dp = (ImageView) itemView.findViewById(R.id.user_dp);
            username = (TextView) itemView.findViewById(R.id.username);
        }
    }
}