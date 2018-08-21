package example.api.location.google;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import example.api.location.google.Models.user_model;
import example.api.location.google.preferenceManager.preferenceManager;

public class createAccount extends AppCompatActivity {

    EditText email, password, name;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        name = (EditText) findViewById(R.id.name);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), home.class));
            finish();
        }
    }

    public void openLoginUp(View view) {
        startActivity(new Intent(getApplicationContext(), starterActivity.class));
        finish();
    }

    public void accessAccount(View view) {

        if (TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(name.getText().toString()) || TextUtils.isEmpty(password.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Provide all data", Toast.LENGTH_LONG).show();
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Creating account");
        dialog.show();

        auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
                    String key = ref.push().getKey();
                    user_model objUser = new user_model(name.getText().toString(), email.getText().toString(), password.getText().toString(), key);
                    ref.child(key).setValue(objUser);
                    dialog.dismiss();
                    preferenceManager.setKey(getApplicationContext(), key);
                    startActivity(new Intent(getApplicationContext(), setProfilePicture.class));
                    finish();
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
