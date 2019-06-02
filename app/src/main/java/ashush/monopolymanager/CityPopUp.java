package ashush.monopolymanager;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class CityPopUp extends Activity {
    TextView cityNameTextView;
    TextView locationTextView;
    TextView ownerNameTextView;
    TextView ownerIdTextView;
    TextView priceTextView;
    TextView fineTextView;
    TextView creatorEmailTextView;
    TextView creationTimeStampTextView;
    TextView stateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_pop_up_layout);

        cityNameTextView = findViewById(R.id.city_popup_name_text);
        locationTextView = findViewById(R.id.city_popup_location_text);
        ownerNameTextView = findViewById(R.id.city_popup_owner_text);
        priceTextView = findViewById(R.id.city_popup_price_text);
        fineTextView = findViewById(R.id.city_popup_fine_text);
        creatorEmailTextView = findViewById(R.id.city_popup_creator_text);
        creationTimeStampTextView = findViewById(R.id.city_popup_creation_timestamp_text);
        ownerIdTextView = findViewById(R.id.city_popup_owner_id_text);
        stateTextView = findViewById(R.id.city_popup_is_expired_text);
        Bundle extras = getIntent().getExtras();
        if(extras == null){
            try {
                throw new Exception("City information is missing");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else{
            cityNameTextView.setText(extras.getString("name"));
            ownerIdTextView.setText(extras.getString("ownerId"));
            locationTextView.setText(extras.getString("location"));
            ownerNameTextView.setText(extras.getString("ownerName").trim());
            priceTextView.setText(extras.getDouble("price")+"");
            fineTextView.setText(extras.getDouble("fine")+"");
            creatorEmailTextView.setText(extras.getString("creatorEmail"));
            creationTimeStampTextView.setText(extras.getString("time"));
            stateTextView.setText(extras.getString("state"));
        }


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.9),(int)(height*0.65));
    }
}
