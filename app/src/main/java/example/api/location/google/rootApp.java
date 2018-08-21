package example.api.location.google;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class rootApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
