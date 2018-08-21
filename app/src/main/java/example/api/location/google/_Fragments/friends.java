package example.api.location.google._Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import example.api.location.google.Activities.logsViewer;
import example.api.location.google.Adapters.friendsAdapter;
import example.api.location.google.MapsActivity;
import example.api.location.google.Models.linkedPeople;
import example.api.location.google.Models.notification_model;
import example.api.location.google.R;
import example.api.location.google.model;
import example.api.location.google.preferenceManager.preferenceManager;

public class friends extends Fragment {

    RecyclerView peopleList;
    friendsAdapter ob;
    ArrayList<linkedPeople> arrayList;
    String userKey;

    DatabaseReference linkedPeopleRef = FirebaseDatabase.getInstance().getReference()
            .child("LinkedPeople")
            .child("Friends");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        peopleList = (RecyclerView) view.findViewById(R.id.recViewer);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        peopleList.setLayoutManager(layoutManager);
        ob = new friendsAdapter(arrayList, new friendsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String operation, String name) {

                linkedPeople people = arrayList.get(position);
                if (userKey == null) {
                    userKey = preferenceManager.getkey(getActivity());
                }
                String otherKey = null;
                if (userKey.equalsIgnoreCase(people.getSender())) {
                    otherKey = people.getReceiver();
                } else {
                    otherKey = people.getSender();
                }

                if (operation.equals("remove")) {
                    removeFriend(arrayList.get(position), name);
                } else if (operation.equals("showLogs")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("user", otherKey);
                    Intent intent = new Intent(getActivity(), logsViewer.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else if (operation.equals("route")) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user-Locations");
                    Query lastQuery = databaseReference.child(otherKey).orderByKey().limitToLast(1);
                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                model o = data.getValue(model.class);
                                Bundle bundle = new Bundle();
                                bundle.putDouble("lon", o.getLon());
                                bundle.putDouble("lat", o.getLat());
                                Intent intent = new Intent(getActivity(), MapsActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //Handle possible errors.
                        }
                    });

                }

            }
        });
        peopleList.setAdapter(ob);
        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayList = new ArrayList<>();
        userKey = preferenceManager.getkey(getActivity()).trim();
        readUsernames();
    }

    public void readUsernames() {


        linkedPeopleRef.child(userKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                linkedPeople model = dataSnapshot.getValue(linkedPeople.class);
                if (model.getRequestStatus().equalsIgnoreCase("accepted")) {
                    arrayList.add(model);
                    ob.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                for (linkedPeople o : arrayList) {
                    if (o.getReceiver().equalsIgnoreCase(key) || o.getSender().equalsIgnoreCase(key)) {
                        arrayList.remove(o);
                        ob.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void removeFriend(final linkedPeople object, final String name) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View customView = layoutInflater.inflate(R.layout.block_people_custom, null);
        AlertDialog.Builder myBox = new AlertDialog.Builder(getActivity());
        myBox.setView(customView);
        final AlertDialog dialog = myBox.create();
        dialog.setCancelable(false);
        final LinearLayout requestbutton = (LinearLayout) customView.findViewById(R.id.requestbutton);
        final ImageView cancel = (ImageView) customView.findViewById(R.id.cancel);
        final TextView content = (TextView) customView.findViewById(R.id.content);
        final TextView suspectname = (TextView) customView.findViewById(R.id.suspectname);

        String firstName = "";
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == 32) {
                break;
            } else {
                firstName += name.charAt(i);
            }
        }

        content.setText("Remove " + name + " from friends list!\n" + firstName + " and you will not be able to view each other locations.");
        suspectname.setText("Remove " + firstName);

        final String finalFirstName = firstName;
        final String finalFirstName1 = firstName;
        requestbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference()
                        .child("Notifications");

                final String notificationKey = notificationRef.push().getKey();

                String otherKey = null;
                if (userKey.equalsIgnoreCase(object.getSender())) {
                    otherKey = object.getReceiver();
                } else {
                    otherKey = object.getSender();
                }


                final notification_model notificationModel = new notification_model();
                notificationModel.setStatus("removed");
                notificationModel.setSender(userKey);
                notificationModel.setReceiver(otherKey);
                notificationModel.setNodeAddress(notificationKey);
                notificationModel.setTime(System.currentTimeMillis());


                final String finalOtherKey = otherKey;
                linkedPeopleRef.child(otherKey).child(userKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            linkedPeopleRef.child(userKey).child(finalOtherKey).removeValue();
                            notificationRef.child(notificationKey).setValue(notificationModel);
                            // arrayList.clear();
                            // readUsernames();
                            dialog.dismiss();
                            Toast.makeText(getActivity(), name + " Removed From Friend List Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error while removing " + name + " From Friend List \n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
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
