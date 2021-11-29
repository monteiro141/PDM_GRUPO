package pt.ubi.di.pdm.restinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.ViewHolder> {

    private List<ItemModel> items;

    public CardStackAdapter(List<ItemModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_card,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView name;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            image = itemView.findViewById(R.id.item_image);
            name = itemView.findViewById(R.id.item_name);
        }

        public void setData(ItemModel data) {
            String url="https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=800" +
                    "&photo_reference=" +data.getImageurl()+
                    "&key=AIzaSyCkPy2xKkFKwz4wr49yUXU9v66Bb7J38-Y";
            //Picasso.get().load(url2).into(restaurantView);
            Picasso.get()
                    .load(url)
                    .fit()
                    .centerCrop()
                    .into(image);
            name.setText(data.getName());

        }
    }
    public List<ItemModel> getItems() {
        return items;
    }

    public void setItems(List<ItemModel> items) {
        this.items = items;
    }
}
