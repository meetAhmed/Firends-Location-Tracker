package example.api.location.google;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class customInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public customInfoAdapter(Context ctx) {
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.marker_info_window, null);

        TextView place = view.findViewById(R.id.place);
        TextView time = view.findViewById(R.id.time);
        TextView username = view.findViewById(R.id.username);

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        place.setText(infoWindowData.getPlace());
        time.setText(infoWindowData.getTime());

        String name = infoWindowData.getUsername();
        if(name != null && name.trim().length()>0){
            if (name.charAt(0) == '@') {
                username.setText(name);
            } else {
                username.setText("@" + name);
            }
        }


        return view;
    }
}
