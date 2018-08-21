package example.api.location.google.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import example.api.location.google.Models.linkedPeople;
import example.api.location.google.Models.notification_model;
import example.api.location.google.R;
import example.api.location.google.model;
import example.api.location.google.preferenceManager.preferenceManager;
import example.api.location.google.storeRoom;


public class friendsAdapter extends RecyclerView.Adapter<friendsAdapter.myHolder> {

    Context context;
    ArrayList<linkedPeople> modelArrayList, temp;
    String name;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position, String operation, String name);
    }

    public friendsAdapter(ArrayList<linkedPeople> modelArrayList, OnItemClickListener onItemClickListener) {
        this.temp = modelArrayList;
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public myHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_single_row, null);
        v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        myHolder obj = new myHolder(v);
        return obj;
    }

    @Override
    public void onBindViewHolder(final myHolder holder, final int position) {

        String userKey = preferenceManager.getkey(context);
        if (userKey.equals(modelArrayList.get(position).getSender())) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user-Locations");
            Query lastQuery = databaseReference.child(modelArrayList.get(position).getReceiver()).orderByKey().limitToLast(1);
            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String formatter = "";
                        model o = data.getValue(model.class);
                        String loc = storeRoom.getUserLocAddress(o.getLat(), o.getLon(), context);
                        if (loc != null) {
                            formatter += "Location : " + loc + "\n";
                        }
                        formatter += "Latitude : " + o.getLat() + "\n";
                        formatter += "Longitude : " + o.getLon() + "\n";
                        formatter += "Obtained at : " + storeRoom.getDateForNotification(o.getTime());
                        holder.content.setText(formatter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Handle possible errors.
                }
            });

            FirebaseDatabase.getInstance().getReference().child("users").child(modelArrayList.get(position).getReceiver()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name = dataSnapshot.child("name").getValue().toString();
                    if (name.charAt(0) != '@') {
                        name = "@" + name;
                    }
                    holder.username.setText(name);
                    String profileRef = dataSnapshot.child("profileAddress").getValue().toString();
                    if (!profileRef.equalsIgnoreCase("none")) {
                        Glide.with(context).load(storeRoom.getImagesRootDir(profileRef)).listener(requestListener).into(holder.user_dp);
                    }else {
                        holder.user_dp.setImageResource(R.drawable.user);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else if (userKey.equals(modelArrayList.get(position).getReceiver())) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user-Locations");
            Query lastQuery = databaseReference.child(modelArrayList.get(position).getSender()).orderByKey().limitToLast(1);
            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String formatter = "";
                        model o = data.getValue(model.class);
                        String loc = storeRoom.getUserLocAddress(o.getLat(), o.getLon(), context);
                        if (loc != null) {
                            formatter += "Location : " + loc + "\n";
                        }
                        formatter += "Latitude : " + o.getLat() + "\n";
                        formatter += "Longitude : " + o.getLon() + "\n";
                        formatter += "Obtained at  " + storeRoom.getDate(o.getTime());
                        holder.content.setText(formatter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Handle possible errors.
                }
            });

            FirebaseDatabase.getInstance().getReference().child("users").child(modelArrayList.get(position).getSender()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name = dataSnapshot.child("name").getValue().toString();
                    if (name.charAt(0) != '@') {
                        name = "@" + name;
                    }
                    holder.username.setText(name);
                    String profileRef = dataSnapshot.child("profileAddress").getValue().toString();
                    if (!profileRef.equalsIgnoreCase("none")) {
                        Glide.with(context).load(storeRoom.getImagesRootDir(profileRef)).listener(requestListener).into(holder.user_dp);
                    }else {
                        holder.user_dp.setImageResource(R.drawable.user);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<linkedPeople> suspect = reverseList();
                for (int i = 0; i < suspect.size(); i++) {
                    if (suspect.get(i).getReceiver().equals(modelArrayList.get(position).getReceiver())  && suspect.get(i).getSender().equals(modelArrayList.get(position).getSender())
                            && suspect.get(i).getRequestStatus().equals(modelArrayList.get(position).getRequestStatus()) ) {
                        mOnItemClickListener.onItemClick(v, i, "remove", name);
                    }
                }

            }
        });
        holder.route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<linkedPeople> suspect = reverseList();
                for (int i = 0; i < suspect.size(); i++) {
                    if (suspect.get(i).getReceiver().equals(modelArrayList.get(position).getReceiver())  && suspect.get(i).getSender().equals(modelArrayList.get(position).getSender())
                            && suspect.get(i).getRequestStatus().equals(modelArrayList.get(position).getRequestStatus()) ) {
                        mOnItemClickListener.onItemClick(v, i, "route", name);
                    }
                }


            }
        });
        holder.showLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<linkedPeople> suspect = reverseList();
                for (int i = 0; i < suspect.size(); i++) {
                    if (suspect.get(i).getReceiver().equals(modelArrayList.get(position).getReceiver())  && suspect.get(i).getSender().equals(modelArrayList.get(position).getSender())
                            && suspect.get(i).getRequestStatus().equals(modelArrayList.get(position).getRequestStatus()) ) {
                        mOnItemClickListener.onItemClick(v, i, "showLogs", name);
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

    public ArrayList<linkedPeople> reverseList() {
        ArrayList<linkedPeople> arrayList = new ArrayList<>();
        for (int i = modelArrayList.size() - 1; i > -1; i--) {
            arrayList.add(modelArrayList.get(i));
        }
        return arrayList;
    }

    @Override
    public int getItemCount() {
        modelArrayList = new ArrayList<>();
        for (int i = temp.size() - 1; i > -1; i--) {
            modelArrayList.add(temp.get(i));
        }
        return modelArrayList.size();
    }

    public class myHolder extends RecyclerView.ViewHolder {
        TextView username, content;
        ImageView user_dp, remove, route;
        LinearLayout showLogs;

        public myHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();

            username = (TextView) itemView.findViewById(R.id.username);
            content = (TextView) itemView.findViewById(R.id.content);
            user_dp = (ImageView) itemView.findViewById(R.id.user_dp);
            remove = (ImageView) itemView.findViewById(R.id.remove);
            route = (ImageView) itemView.findViewById(R.id.route);
            showLogs = (LinearLayout) itemView.findViewById(R.id.showLogs);
        }
    }
}