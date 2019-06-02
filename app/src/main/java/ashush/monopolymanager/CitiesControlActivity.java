package ashush.monopolymanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import ashush.monopolymanager.Entities.ElementEntity;
import ashush.monopolymanager.Entities.Location;
import ashush.monopolymanager.Entities.UserEntity;

public class CitiesControlActivity extends AppCompatActivity implements CitiesAdapter.OnCityMenuListener, View.OnClickListener,ServerCommunicationService.ServerCommunicationListener{
    private static final String TAG = "CitiesControlActivity";

    private ArrayList<String> citiesName ;
    private ArrayList<ElementEntity> citiesArrayList;


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private FloatingActionButton fab;
    private boolean mBound = false;
    private ServerCommunicationService.LocalBinder mBinder;
    private ServerCommunicationService mServerCommunicationService;

    private UserEntity userEntity;
    private String ip;
    private String port;
    private String smartSpace;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities_control);

        userEntity =  (UserEntity) getIntent().getSerializableExtra("userEntity");
        ip = getIntent().getStringExtra("ip");
        port = getIntent().getStringExtra("port");
        smartSpace = userEntity.getUserSmartspace();
        email = userEntity.getUserEmail();

        citiesArrayList = new ArrayList<>();

        mRecyclerView = findViewById(R.id.cities_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new CitiesAdapter(this,citiesArrayList,this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            mBinder.registerServerUpdatesListener(CitiesControlActivity.this);
            mServerCommunicationService.getCitiesFromServer(email,smartSpace,port,ip);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mBinder.unRegisterServerUpdatesListener();
        }
    };


    @Override
    public void onCityMenuClicked(int cityPosition, int menuItemPosition) {
        ElementEntity city = citiesArrayList.get(cityPosition);

        switch (menuItemPosition){
            case 0://edit
                openEditCityActivity(city);
                break;
            case 1://delete//add
                setCityValidition(city,!city.isExpired());

                break;
            case 2://zoom
                openCityPopUp(city);
                break;
        }
    }



    private void openCityPopUp(ElementEntity city) {
        Intent intent = new Intent(CitiesControlActivity.this, CityPopUp.class);
        intent.putExtra("name", city.getName());
        intent.putExtra("ownerName", city.getMoreAttributes().get("ownerName") + "");
        intent.putExtra("ownerId", city.getMoreAttributes().get("ownerId") + "");
        intent.putExtra("price", (double)city.getMoreAttributes().get("price"));
        intent.putExtra("fine", (double)city.getMoreAttributes().get("fine"));
        intent.putExtra("location", city.getLocation().getX()+","+city.getLocation().getY());
        intent.putExtra("creatorEmail", city.getCreatorEmail()+"");
        intent.putExtra("time", city.getCreationTimestamp().toString().trim());
        intent.putExtra("state", city.isExpired() ? "Expired" : "Valid");
        startActivity(intent);
    }

    private void setCityValidition(ElementEntity city,boolean newState) {
        Toast.makeText(this, "Change city state clicked", Toast.LENGTH_SHORT).show();
        city.setExpired(newState);
        mServerCommunicationService.updateCityFromServer(email, smartSpace, port, ip,city);

    }

    private void openEditCityActivity(ElementEntity city) {
        Toast.makeText(this, "Edit City Clicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CitiesControlActivity.this,CityUpdatePopUp.class);
        intent.putExtra("ElementEntity",city);
        intent.putExtra("ip",ip);
        intent.putExtra("port",port);
        intent.putExtra("userEntity",userEntity);
        startActivity(intent);


    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.fab){
            openAddNewCityWindow();
        }
    }

    private void openAddNewCityWindow() {
        //TODO need to implement
        Toast.makeText(this, "Add new city clicked", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(CitiesControlActivity.this,AddNewCityPopUp.class);
        intent.putExtra("ip",ip);
        intent.putExtra("port",port);
        intent.putExtra("userEntity",userEntity);
        startActivity(intent);
    }

    @Override
    public void onDataChanged(ArrayList<ElementEntity> citesList) {
        citiesArrayList.clear();
        citiesArrayList.addAll(citesList);
        mAdapter.notifyDataSetChanged();
    }

}
