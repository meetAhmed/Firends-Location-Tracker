package example.api.location.google;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import example.api.location.google.preferenceManager.preferenceManager;

public class starterActivity extends AppCompatActivity {

    EditText user_name, password;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        user_name = (EditText) findViewById(R.id.editText);
        password = (EditText) findViewById(R.id.editText2);


        startService(new Intent(getApplicationContext(), locationGetter.class));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), home.class));
            finish();
        }
    }

    public void openSignUp(View view) {
        startActivity(new Intent(getApplicationContext(), createAccount.class));
        finish();
    }

    public void accessAccount(View view) {

        if (TextUtils.isEmpty(user_name.getText().toString()) || TextUtils.isEmpty(password.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Provide all data", Toast.LENGTH_LONG).show();
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Checking account");
        dialog.show();

        auth.signInWithEmailAndPassword(user_name.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
                    databaseReference.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(final DataSnapshot data, String s) {
                            if (data.child("email").getValue().toString().trim().equals(user_name.getText().toString().trim())) {
                                preferenceManager.setKey(getApplicationContext(), data.child("key").getValue().toString().trim());
                                dialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), home.class));
                                finish();
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            String nodeAddress = dataSnapshot.child("nodeAddress").getValue().toString();
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }
}
