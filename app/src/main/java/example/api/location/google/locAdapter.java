package example.api.location.google;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class locAdapter extends RecyclerView.Adapter<locAdapter.myHolder> {

    Context context;
    ArrayList<model> modelArrayList, temp;

    public locAdapter(ArrayList<model> chapterArrayList) {
        this.temp = (chapterArrayList);
    }

    @Override
    public myHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.logs_single_view, null);
        v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        myHolder obj = new myHolder(v);
        return obj;
    }

    @Override
    public void onBindViewHolder(final myHolder holder, final int position) {
        String formatter = "";
        String loc = storeRoom.getUserLocAddress(modelArrayList.get(position).getLat(), modelArrayList.get(position).getLon(), context);
        if (loc != null) {
            formatter += loc + "\n\n";
        }
        formatter += "latitude : " + modelArrayList.get(position).getLat() + "\nlongitude : " + modelArrayList.get(position).getLon();
        formatter += "\nTime : " + storeRoom.getDateForNotification(modelArrayList.get(position).getTime());
        holder.content.setText(formatter);
    }

    @Override
    public int getItemCount() {

        modelArrayList = new ArrayList<>();
        for (int i = temp.size() - 1; i > -1; i--) {
            modelArrayList.add(temp.get(i));
        }
        return modelArrayList.size();
    }

    public class myHolder extends RecyclerView.ViewHolder {
        TextView content;

        public myHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();

            content = (TextView) itemView.findViewById(R.id.content);
        }
    }
}