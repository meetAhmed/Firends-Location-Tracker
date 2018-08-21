package example.api.location.google._Fragments;


import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import example.api.location.google.Adapters.peopleAdapter;
import example.api.location.google.Models.linkedPeople;
import example.api.location.google.Models.notification_model;
import example.api.location.google.Models.user_model;
import example.api.location.google.R;
import example.api.location.google.preferenceManager.preferenceManager;

public class people extends Fragment {

    RecyclerView peopleList;
    peopleAdapter ob;
    ArrayList<user_model> arrayList;
    String userKey;
    DatabaseReference linkedPeopleRef = FirebaseDatabase.getInstance().getReference()
            .child("LinkedPeople")
            .child("Friends");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);

        userKey = preferenceManager.getkey(getActivity()).trim();
        peopleList = (RecyclerView) view.findViewById(R.id.recViewer);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        peopleList.setLayoutManager(layoutManager);
        ob = new peopleAdapter(arrayList, new peopleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String operation) {

                if (operation.equals("addToList")) {
                    createFriend(arrayList.get(position));
                }

            }
        });
        peopleList.setAdapter(ob);
        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayList = new ArrayList<>();
        readUsernames();

    }

    public void readUsernames() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot data, String s) {
                final user_model userObj = data.getValue(user_model.class);
                Log.i("12sdsdsd", data.getValue() + "");
                if (userKey.equals("nothing")) {
                    // user should be log in again because key is not found
                    Toast.makeText(getActivity(), "something wrong with the key", Toast.LENGTH_LONG).show();
                } else {
                    Log.i("12321312", userKey);
                    linkedPeopleRef.child(userKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // if users is already in friend list or wait list dont show them
                            // do not show user in the people area
                            Log.i("12321312", dataSnapshot.getValue() + "");

                            if (!dataSnapshot.hasChild(data.child("key").getValue().toString().trim())) {
                                if (!userKey.equals(data.child("key").getValue().toString().trim())) {
                                    if (!arrayList.contains(userObj)) {
                                        arrayList.add(userObj);
                                        ob.notifyDataSetChanged();
                                    }
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String nodeAddress = dataSnapshot.child("key").getValue().toString();
                for (user_model user : arrayList) {
                    if (user.getKey().equals(nodeAddress)) {
                        arrayList.remove(user);
                        ob.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.i("123123", "method called");

    }

    public void createFriend(final user_model object) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View customView = layoutInflater.inflate(R.layout.add_people_custom, null);
        AlertDialog.Builder myBox = new AlertDialog.Builder(getActivity());
        myBox.setView(customView);
        final AlertDialog dialog = myBox.create();
        final LinearLayout requestbutton = (LinearLayout) customView.findViewById(R.id.requestbutton);
        final ImageView cancel = (ImageView) customView.findViewById(R.id.cancel);
        final TextView content = (TextView) customView.findViewById(R.id.content);
        final TextView suspectname = (TextView) customView.findViewById(R.id.suspectname);

        String name = object.getName();
        String firstName = "";
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == 32) {
                break;
            } else {
                firstName += name.charAt(i);
            }
        }

        content.setText("Add " + name + " to friends list!\n" + firstName + " and you will be able to view each other locations.");
        suspectname.setText("Send " + firstName + " Friend Request");

        final String finalFirstName = firstName;
        final String finalFirstName1 = firstName;
        requestbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference()
                        .child("Notifications");

                final String notificationKey = notificationRef.push().getKey();

                final linkedPeople linkedPeople = new linkedPeople();
                linkedPeople.setReceiver(object.getKey());
                linkedPeople.setSender(preferenceManager.getkey(getActivity()));
                linkedPeople.setRequestStatus("Pending");

                final notification_model notificationModel = new notification_model();
                notificationModel.setStatus("Pending");
                notificationModel.setSender(preferenceManager.getkey(getActivity()));
                notificationModel.setReceiver(object.getKey());
                notificationModel.setNodeAddress(notificationKey);
                notificationModel.setTime(System.currentTimeMillis());


                linkedPeopleRef.child(preferenceManager.getkey(getActivity())).child(object.getKey()).setValue(linkedPeople).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();
                        linkedPeopleRef.child(object.getKey()).child(preferenceManager.getkey(getActivity())).setValue(linkedPeople);
                        notificationRef.child(notificationKey).setValue(notificationModel);
                        arrayList.remove(object);
                        ob.notifyDataSetChanged();
                       // arrayList.clear();
                       // readUsernames();
                        Toast.makeText(getActivity(), "Friend Request send to " + finalFirstName1, Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(getActivity(), "Failed to send friend request to " + finalFirstName, Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}
