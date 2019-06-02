package ashush.monopolymanager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cleveroad.sy.cyclemenuwidget.CycleMenuWidget;
import com.cleveroad.sy.cyclemenuwidget.OnMenuItemClickListener;

import java.util.ArrayList;

import ashush.monopolymanager.Entities.ElementEntity;

public class CitiesAdapter extends RecyclerView.Adapter<CitiesAdapter.ViewHolder> {
    static final String TAG = "CitiesAdapter";
    private ArrayList<ElementEntity> cityList;
    private Context mContext;
    private OnCityMenuListener mOnMenuCityListener;

    public CitiesAdapter(Context context,ArrayList<ElementEntity> cityList,OnCityMenuListener onMenuCityListener) {
        this.cityList = cityList;
        this.mContext = context;
        this.mOnMenuCityListener = onMenuCityListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
       View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.city_card_item,viewGroup,false);
       ViewHolder holder = new ViewHolder(view,mOnMenuCityListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {


        if(cityList.get(i).isExpired())
            viewHolder.cycleMenuWidget.setMenuRes(R.menu.expired_city_menu);
        else
            viewHolder.cycleMenuWidget.setMenuRes(R.menu.city_menu);


        viewHolder.creatorTextView.setText(cityList.get(i).getCreatorEmail()+"");
        viewHolder.locationTextView.setText(cityList.get(i).getLocation().getX()+","+cityList.get(i).getLocation().getY());
        viewHolder.nameTextView.setText(cityList.get(i).getName()+"");
        viewHolder.priceTextView.setText(cityList.get(i).getMoreAttributes().get("price")+"");
        viewHolder.stateTextView.setText(cityList.get(i).isExpired() ? "Expired" : "Valid");
        viewHolder.fineTextView.setText(cityList.get(i).getMoreAttributes().get("fine")+"");


    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements OnMenuItemClickListener {
        public TextView nameTextView;
        public TextView locationTextView;
        public TextView stateTextView;
        public TextView priceTextView;
        public TextView creatorTextView;
        public TextView fineTextView;
        public CycleMenuWidget cycleMenuWidget;
        public OnCityMenuListener onCityMenuListener;


        public ViewHolder(@NonNull View itemView,OnCityMenuListener onCityMenuListener) {
            super(itemView);
            this.onCityMenuListener = onCityMenuListener;
            nameTextView = itemView.findViewById(R.id.city_card_item_name_text);
            locationTextView = itemView.findViewById(R.id.city_card_item_location_text);
            stateTextView = itemView.findViewById(R.id.city_card_item_is_expired_text);
            priceTextView = itemView.findViewById(R.id.city_card_item_price_text);
            creatorTextView = itemView.findViewById(R.id.city_card_item_creator_text);
            fineTextView = itemView.findViewById(R.id.city_card_item_fine_text);
            cycleMenuWidget = itemView.findViewById(R.id.itemCycleMenuWidget);

            cycleMenuWidget.setOnMenuItemClickListener(this);
        }

        @Override
        public void onMenuItemClick(View view, int itemPosition) {
            onCityMenuListener.onCityMenuClicked(getAdapterPosition(),itemPosition);
            Log.i(TAG,"city:"+getAdapterPosition()+" item: "+itemPosition);

        }

        @Override
        public void onMenuItemLongClick(View view, int itemPosition) {

        }
    }
    public interface OnCityMenuListener{
        void onCityMenuClicked(int cityPosition, int menuItemPosition);
    }
}
