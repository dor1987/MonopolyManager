package ashush.monopolymanager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import ashush.monopolymanager.Entities.ElementEntity;
import ashush.monopolymanager.Entities.Location;
import ashush.monopolymanager.Entities.UserEntity;

public class AddNewCityPopUp extends Activity  {
    private static final String TAG = "AddNewCityPopUp";
    private UserEntity userEntity;
    private String ip;
    private String port;
    private String smartSpace;
    private String email;

    EditText nameEditText;
    EditText locationEditText;
    EditText priceEditText;
    EditText fineEditText;
    Button saveButton;

    TextView nameTextView;
    TextView locationTextView;
    TextView priceTextView;
    TextView fineTextView;

    private boolean mBound = false;
    private ServerCommunicationService.LocalBinder mBinder;
    private ServerCommunicationService mServerCommunicationService;
    private View mProgressBarView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_city_pop_up_layout);

        userEntity =  (UserEntity) getIntent().getSerializableExtra("userEntity");
        ip = getIntent().getStringExtra("ip");
        port = getIntent().getStringExtra("port");
        smartSpace = userEntity.getUserSmartspace();
        email = userEntity.getUserEmail();


        mProgressBarView = findViewById(R.id.add_new_city_popup_progress_bar);
        nameEditText = findViewById(R.id.add_new_city_popup_name_text);
        locationEditText = findViewById(R.id.add_new_city_popup_location_x_text);
        priceEditText = findViewById(R.id.add_new_city_popup_price_text);
        fineEditText = findViewById(R.id.add_new_city_popup_fine_text);
        saveButton = findViewById(R.id.add_new_city_popup_save_button);

        nameTextView = findViewById(R.id.add_new_city_popup_name_title);
        locationTextView = findViewById(R.id.add_new_city_popup_Location_title);
        priceTextView = findViewById(R.id.add_new_city_popup_price_title);
        fineTextView = findViewById(R.id.add_new_city_popup_fine_title);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.9),(int)(height*0.55));

    }

    @Override
    protected void onStart() {
        super.onStart();
        initService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }

    private void initService(){
        Log.e(TAG,"on start");
        if(!mBound) {
            Intent intent = new Intent(this, ServerCommunicationService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.e(TAG,"onServiceConnected");
            mBinder = (ServerCommunicationService.LocalBinder) service;
            mServerCommunicationService = mBinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void onSaveClicked(View view) {
        if(isInputValid()){
            ElementEntity city = createNewCity(smartSpace,email);
            mServerCommunicationService.addNewCityFromServer(email, smartSpace, port, ip, city);
            finish();
        }
        else{
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
        }

    }

    private ElementEntity createNewCity(String smartSpace,String email ) {
        ElementEntity city = new ElementEntity();
        city.setName(nameEditText.getText().toString().trim());
        city.setExpired(false);
        city.setType("city");
        city.setCreatorSmartspace(smartSpace);
        city.setCreatorEmail(email);
        city.setLocation(new Location(Double.parseDouble(locationEditText.getText().toString()),0));

        HashMap<String, Object> moreAttributes= new HashMap<String, Object>();
        moreAttributes.put("price", Long.parseLong(priceEditText.getText().toString().trim()));
        moreAttributes.put("fine", Long.parseLong(fineEditText.getText().toString().trim()));
        moreAttributes.put("ownerId", "");
        moreAttributes.put("ownerName", "");
        moreAttributes.put("visitors", new ArrayList<String>());
        city.setMoreAttributes(moreAttributes);

        return city;
    }

    private boolean isInputValid() {
        boolean result = true;
        if(nameEditText.getText().toString().trim().isEmpty()) {
            result = false;
            nameTextView.setTextColor(Color.RED);
        }
        else
            nameTextView.setTextColor(Color.BLACK);


        if(locationEditText.getText().toString().trim().isEmpty()) {
            result = false;
            locationTextView.setTextColor(Color.RED);
        }
        else
            locationTextView.setTextColor(Color.BLACK);

        if(priceEditText.getText().toString().trim().isEmpty()) {
            result = false;
            priceTextView.setTextColor(Color.RED);
        }
        else
            priceTextView.setTextColor(Color.BLACK);

        if(fineEditText.getText().toString().trim().isEmpty()) {
            result = false;
            fineTextView.setTextColor(Color.RED);
        }
        else
            fineTextView.setTextColor(Color.BLACK);

        return result;
    }

    public void toShowProgressBar(boolean toShow){
        if(toShow)
            mProgressBarView.setVisibility(View.VISIBLE);
        else
            mProgressBarView.setVisibility(View.GONE);
    }

}
