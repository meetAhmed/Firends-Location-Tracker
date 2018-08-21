package example.api.location.google.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import example.api.location.google.R;
import example.api.location.google.locAdapter;
import example.api.location.google.model;

public class logsViewer extends AppCompatActivity {

    RecyclerView peopleList;
    locAdapter ob;
    ArrayList<model> arrayList;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs_viewer);

        getSupportActionBar().setTitle("Logs");

        arrayList = new ArrayList<>();
        peopleList = (RecyclerView) findViewById(R.id.recViewer);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        peopleList.setLayoutManager(layoutManager);
        ob = new locAdapter(arrayList);
        peopleList.setAdapter(ob);

        username = getIntent().getExtras().getString("user");
        readLogs();
    }

    public void readLogs() {


        FirebaseDatabase.getInstance().getReference().child("user-Locations").child(username).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                model modelOBJ = dataSnapshot.getValue(model.class);
                arrayList.add(modelOBJ);
                ob.notifyDataSetChanged();
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
