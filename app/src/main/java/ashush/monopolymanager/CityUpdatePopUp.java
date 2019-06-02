package ashush.monopolymanager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.util.HashMap;

import ashush.monopolymanager.Entities.ElementEntity;
import ashush.monopolymanager.Entities.Location;
import ashush.monopolymanager.Entities.UserEntity;

public class CityUpdatePopUp  extends Activity  implements ServerCommunicationService.ServerUpdateCityListener {
    private static final String TAG = "CityUpdatePopUp";
    private UserEntity userEntity;
    private String ip;
    private String port;
    private String smartSpace;
    private String email;

    ElementEntity city;
    EditText nameEditText;
    EditText locationEditText;
    EditText priceEditText;
    EditText fineEditText;
    Button saveButton;
    private boolean mBound = false;
    private ServerCommunicationService.LocalBinder mBinder;
    private ServerCommunicationService mServerCommunicationService;
    private boolean isNameChagned;
    private boolean isLocationChagned;
    private boolean isPriceChagned;
    private boolean isFineChagned;
    private View mProgressBarView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_update_pop_up_layout);

        userEntity =  (UserEntity) getIntent().getSerializableExtra("userEntity");
        ip = getIntent().getStringExtra("ip");
        port = getIntent().getStringExtra("port");
        smartSpace = userEntity.getUserSmartspace();
        email = userEntity.getUserEmail();

        isNameChagned = false;
        isLocationChagned = false;
        isPriceChagned = false;
        isFineChagned = false;

        mProgressBarView = findViewById(R.id.city_update_popup_progress_bar);
        nameEditText = findViewById(R.id.city_update_popup_name_text);
        locationEditText = findViewById(R.id.city_update_popup_location_x_text);
        priceEditText = findViewById(R.id.city_update_popup_price_text);
        fineEditText = findViewById(R.id.city_update_popup_fine_text);
        saveButton = findViewById(R.id.city_update_popup_save_button);

        city =  (ElementEntity) getIntent().getSerializableExtra("ElementEntity");


        nameEditText.setText(city.getName());
        locationEditText.setText(city.getLocation().getX()+"");
        priceEditText.setText(city.getMoreAttributes().get("price").toString());
        fineEditText.setText(city.getMoreAttributes().get("fine").toString());

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isNameChagned = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        fineEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isFineChagned = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        priceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isPriceChagned = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        locationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isLocationChagned = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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
            mBinder.registerServerUpdateCityListener(CityUpdatePopUp.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mBinder.UnRegisterServerUpdateCityListener();
        }
    };

    public void onSaveClicked(View view) {
        boolean isChanged = false;

        if(isNameChagned) {
            city.setName(nameEditText.getText().toString().trim());
            isChanged = true;
        }

        if(isLocationChagned) {
            Location tempLocation = new Location();
            Double x = Double.parseDouble(locationEditText.getText().toString());
            tempLocation.setX(x);
            city.setLocation(tempLocation);
            isChanged = true;
        }

        if(isPriceChagned) {
            HashMap<String, Object> moreAttributes = (HashMap<String, Object>)city.getMoreAttributes();
            moreAttributes.put("price", Long.parseLong(priceEditText.getText().toString()));
            city.setMoreAttributes(moreAttributes);
            isChanged = true;
        }

        if(isFineChagned) {
            HashMap<String, Object> moreAttributes = (HashMap<String, Object>)city.getMoreAttributes();
            moreAttributes.put("fine", Long.parseLong(fineEditText.getText().toString()));
            city.setMoreAttributes(moreAttributes);
            isChanged = true;
        }

        if(isChanged) {
            mServerCommunicationService.updateCityFromServer(email, smartSpace, port, ip, city);

            toShowProgressBar(true);
        }
        else
            finish();
    }

    public void toShowProgressBar(boolean toShow){
        if(toShow)
            mProgressBarView.setVisibility(View.VISIBLE);
        else
            mProgressBarView.setVisibility(View.GONE);
    }

    @Override
    public void onUpdateFinish() {
        toShowProgressBar(false);
        Toast.makeText(this, "City updated", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onUpdateFailed() {
        toShowProgressBar(false);
        Toast.makeText(this, "Failed to update city", Toast.LENGTH_SHORT).show();
    }
}
