package example.api.location.google.preferenceManager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

public class preferenceManager {

    static String location = "userSettings_user_key";

    public static void setKey(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences(location, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("key", name);
        editor.commit();
    } // ends here

    public static String getkey(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(location, Context.MODE_PRIVATE);
        return preferences.getString("key", "nothing");
    } // ends here

    public static void writeLocation(Context context, String lat, String lon) {
        SharedPreferences preferences = context.getSharedPreferences(location, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lat", lat.trim());
        editor.putString("lon", lon.trim());
        editor.commit();
    }

    public static void writeUserName(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences(location, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", name);
        editor.commit();
    }

    public static String getUserName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(location, Context.MODE_PRIVATE);
        return preferences.getString("username", "user");
    } // ends here

    public static LatLng getUserLocation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(location, Context.MODE_PRIVATE);
        String lat = preferences.getString("lat", null);
        String lon = preferences.getString("lon", null);
        if (lat == null || lon == null) {
            return null;
        }
        return new LatLng(Double.parseDouble(lat.trim()), Double.parseDouble(lon.trim()));
    } // ends here

}
