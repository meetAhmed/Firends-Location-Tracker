package example.api.location.google._Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import example.api.location.google.Adapters.notificationAdapter;
import example.api.location.google.Models.notification_model;
import example.api.location.google.R;
import example.api.location.google.preferenceManager.preferenceManager;

public class notifications extends Fragment {

    RecyclerView peopleList;
    notificationAdapter ob;
    ArrayList<notification_model> arrayList;
    DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
    String userKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        arrayList = new ArrayList<>();
        userKey = preferenceManager.getkey(getActivity());
        peopleList = (RecyclerView) view.findViewById(R.id.recViewer);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        peopleList.setLayoutManager(layoutManager);
        ob = new notificationAdapter(arrayList, new notificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String operation) {

                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                        .child("LinkedPeople")
                        .child("Friends");

                final notification_model suspect = arrayList.get(position);

                switch (operation) {
                    case "acceptRequest":
                        databaseReference.child(suspect.getReceiver()).child(suspect.getSender()).child("requestStatus").setValue("accepted").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                databaseReference.child(suspect.getSender()).child(suspect.getReceiver()).child("requestStatus").setValue("accepted");
                                notificationRef.child(suspect.getNodeAddress()).child("status").setValue("accepted");
                                arrayList.clear();
                                readNotifications();
                                Toast.makeText(getActivity(), "Added Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), "Error while accepting the reuqest", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case "rejectRequest":

                        databaseReference.child(suspect.getSender()).child(suspect.getReceiver()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                databaseReference.child(suspect.getReceiver()).child(suspect.getSender()).removeValue();
                                notificationRef.child(suspect.getNodeAddress()).child("status").setValue("rejected");
                                arrayList.clear();
                                readNotifications();
                                Toast.makeText(getActivity(), "Request Rejected Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), "Error while rejecting the reuqest", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case "cancelRequest":
                        databaseReference.child(suspect.getSender()).child(suspect.getReceiver()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                databaseReference.child(suspect.getReceiver()).child(suspect.getSender()).removeValue();
                                notificationRef.child(suspect.getNodeAddress()).child("status").setValue("dropped");
                                arrayList.clear();
                                readNotifications();
                                Toast.makeText(getActivity(), "Request dropped Successfully", Toast.LENGTH_SHORT).show();
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), "Error while dropping the reuqest", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;

                }

            }
        }, userKey);
        peopleList.setAdapter(ob);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readNotifications();
    }

    public void readNotifications() {
        //orderByChild("sender").equalTo(preferenceManager.getkey(getActivity()))
        notificationRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                notification_model notificationModel = dataSnapshot.getValue(notification_model.class);
                if (userKey.equals(notificationModel.getSender()) || userKey.equals(notificationModel.getReceiver())) {
                    arrayList.add(notificationModel);
                    ob.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
