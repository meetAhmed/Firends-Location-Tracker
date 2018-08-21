package example.api.location.google.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import example.api.location.google.Models.notification_model;
import example.api.location.google.Models.user_model;
import example.api.location.google.R;
import example.api.location.google.storeRoom;


public class notificationAdapter extends RecyclerView.Adapter<notificationAdapter.myHolder> {

    Context context;
    ArrayList<notification_model> modelArrayList, temp;
    String userkey;

    private notificationAdapter.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position, String operation);
    }

    public notificationAdapter(ArrayList<notification_model> chapterArrayList, notificationAdapter.OnItemClickListener onItemClickListener, String userkey) {
        this.temp = (chapterArrayList);
        mOnItemClickListener = onItemClickListener;
        this.userkey = userkey;
    }

    @Override
    public int getItemViewType(int position) {
        if (modelArrayList.get(position).getStatus().equalsIgnoreCase("Pending")) {
            if (userkey.trim().equals(modelArrayList.get(position).getSender())) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 2;
        }
    }

    @Override
    public myHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case 0:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_single_row_owner, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_single_row, parent, false);
                break;
            case 2:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_single_row_done, parent, false);
                break;
        }
        v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        myHolder obj = new myHolder(v);
        return obj;
    }

    @Override
    public void onBindViewHolder(final myHolder holder, final int position) {

        holder.time.setText(storeRoom.getDateForNotification(modelArrayList.get(position).getTime()));

        if (getItemViewType(position) == 0) {
            // sender of pending notification
            FirebaseDatabase.getInstance().getReference().child("users").child(modelArrayList.get(position).getReceiver()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    if (name.charAt(0) != '@') {
                        name = "@" + name;
                    }
                    holder.content.setText("You sent Friend Request to " + name);
                    String profileRef = dataSnapshot.child("profileAddress").getValue().toString();
                    if (!profileRef.equalsIgnoreCase("none")) {
                        Glide.with(context).load(storeRoom.getImagesRootDir(profileRef)).into(holder.user_dp);
                    }else {
                        holder.user_dp.setImageResource(R.drawable.user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<notification_model> suspect = reverseList();
                    for (int i = 0; i < suspect.size(); i++) {
                        if (suspect.get(i).getNodeAddress().equals(modelArrayList.get(position).getNodeAddress())) {
                            mOnItemClickListener.onItemClick(v, i, "cancelRequest");
                        }
                    }

                }
            });

        } else if (getItemViewType(position) == 1) {
            // receiver of pending notification

            FirebaseDatabase.getInstance().getReference().child("users").child(modelArrayList.get(position).getSender()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    if (name.charAt(0) != '@') {
                        name = "@" + name;
                    }
                    holder.content.setText(name + " Sent you Friend Request");
                    String profileRef = dataSnapshot.child("profileAddress").getValue().toString();
                    if (!profileRef.equalsIgnoreCase("none")) {
                        Glide.with(context).load(storeRoom.getImagesRootDir(profileRef)).into(holder.user_dp);
                    }else {
                        holder.user_dp.setImageResource(R.drawable.user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            holder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<notification_model> suspect = reverseList();
                    for (int i = 0; i < suspect.size(); i++) {
                        if (suspect.get(i).getNodeAddress().equals(modelArrayList.get(position).getNodeAddress())) {
                            mOnItemClickListener.onItemClick(v, i, "acceptRequest");
                        }
                    }


                }
            });
            holder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<notification_model> suspect = reverseList();
                    for (int i = 0; i < suspect.size(); i++) {
                        if (suspect.get(i).getNodeAddress().equals(modelArrayList.get(position).getNodeAddress())) {
                            mOnItemClickListener.onItemClick(v, i, "rejectRequest");
                        }
                    }


                }
            });
        } else if (getItemViewType(position) == 2){
            // completed notifications

            if (userkey.equals(modelArrayList.get(position).getReceiver())) {
                FirebaseDatabase.getInstance().getReference().child("users").child(modelArrayList.get(position).getSender()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        if (name.charAt(0) != '@') {
                            name = "@" + name;
                        }
                        String profileRef = dataSnapshot.child("profileAddress").getValue().toString();
                        if (!profileRef.equalsIgnoreCase("none")) {
                            Glide.with(context).load(storeRoom.getImagesRootDir(profileRef)).into(holder.user_dp);
                        }else {
                            holder.user_dp.setImageResource(R.drawable.user);
                        }
                        if (modelArrayList.get(position).getStatus().equals("dropped")) {
                            holder.content.setText(name + " dropped the friend request sent to you");
                        } else if (modelArrayList.get(position).getStatus().equalsIgnoreCase("removed")) {
                            holder.content.setText(name + " removed you from Friend List.");
                        } else {
                            holder.content.setText("You " + modelArrayList.get(position).getStatus() + " " + name + " Friend Request.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }// receiver of completed notifications
            else {
                FirebaseDatabase.getInstance().getReference().child("users").child(modelArrayList.get(position).getReceiver()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        if (name.charAt(0) != '@') {
                            name = "@" + name;
                        }
                        String profileRef = dataSnapshot.child("profileAddress").getValue().toString();
                        if (!profileRef.equalsIgnoreCase("none")) {
                            Glide.with(context).load(storeRoom.getImagesRootDir(profileRef)).into(holder.user_dp);
                        }else {
                            holder.user_dp.setImageResource(R.drawable.user);
                        }
                        if (modelArrayList.get(position).getStatus().equals("dropped")) {
                            holder.content.setText("You dropped the friend request sent to " + name);
                        } else if (modelArrayList.get(position).getStatus().equalsIgnoreCase("removed")) {
                            holder.content.setText("You " + modelArrayList.get(position).getStatus() + " " + name + " from Friend List.");
                        } else {
                            holder.content.setText("Your Friend Request was " + modelArrayList.get(position).getStatus() + " by " + name + ".");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        modelArrayList = new ArrayList<>();
        for (int i = temp.size() - 1; i > -1; i--) {
            modelArrayList.add(temp.get(i));
        }
        return modelArrayList.size();
    }

    public ArrayList<notification_model> reverseList() {
        ArrayList<notification_model> arrayList = new ArrayList<>();
        for (int i = modelArrayList.size() - 1; i > -1; i--) {
            arrayList.add(modelArrayList.get(i));
        }
        return arrayList;
    }


    public class myHolder extends RecyclerView.ViewHolder {
        TextView content, time;
        ImageView user_dp;
        Button accept, reject, cancel;

        public myHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();

            user_dp = (ImageView) itemView.findViewById(R.id.user_dp);
            content = (TextView) itemView.findViewById(R.id.content);
            time = (TextView) itemView.findViewById(R.id.time);
            accept = (Button) itemView.findViewById(R.id.accept);
            reject = (Button) itemView.findViewById(R.id.reject);
            cancel = (Button) itemView.findViewById(R.id.cancel);
        }
    }
}