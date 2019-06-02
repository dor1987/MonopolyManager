package ashush.monopolymanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import ashush.monopolymanager.Boundaries.ElementBoundary;
import ashush.monopolymanager.Boundaries.UserBoundary;
import ashush.monopolymanager.Entities.ElementEntity;
import ashush.monopolymanager.Entities.UserEntity;

public class ServerCommunicationService extends Service {
    private static final String TAG = "ServerService";
    private RequestQueue queue;
    private Gson gson = new Gson();
    private LocalBinder binder = new LocalBinder();
    private ServerCommunicationListener mServerCommunicationListener;
    private ServerUpdateCityListener mServerUpdateCityListener;
    private LogInToServerListener mLogInToServerListener;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        queue = Volley.newRequestQueue(this);// this = context
    }

    public class LocalBinder extends Binder {
        public ServerCommunicationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServerCommunicationService.this;
        }

        void registerServerUpdatesListener(ServerCommunicationListener listener){
            mServerCommunicationListener = listener;
        }

        void unRegisterServerUpdatesListener(){
            mServerCommunicationListener = null;
        }
        void registerServerUpdateCityListener(ServerUpdateCityListener listener){
            mServerUpdateCityListener = listener;
        }

        void UnRegisterServerUpdateCityListener(){
            mServerUpdateCityListener = null;
        }

        void registerLogInToServerListener(LogInToServerListener listener){
            mLogInToServerListener = listener;
        }

        void unRegisterLogInToServerListener(){
            mLogInToServerListener = null;
        }

    }

    public interface ServerCommunicationListener{
        void onDataChanged(ArrayList<ElementEntity> citesList);
    }

    public interface ServerUpdateCityListener{
        void onUpdateFinish();
        void onUpdateFailed();
    }

    public interface LogInToServerListener{
        //used at loging page to let the app know when more than 1 player is online
        //and a game can start
        void onLogInGood(UserEntity userEntity);
        void onLogInBad();
    }

    public void getCitiesFromServer(final String email, final String smartspace,final String port, final String ip){
        final String url = "http://"+ip+":"+port+"/smartspace/elements/"+smartspace+"/"+email+"?search=type&value=city";
        final ArrayList<ElementEntity> arrayOfCities = new ArrayList<>();

        // prepare the Request
        final JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
                        // display response
                        Log.d("Response", response.toString());

                        for(int i = 0; i< response.length();i++){
                            try {
                                JSONObject jresponse = response.getJSONObject(i);
                                ElementBoundary elementBoundary = gson.fromJson(jresponse.toString(),ElementBoundary.class);
                                arrayOfCities.add(elementBoundary.toEntity());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if(mServerCommunicationListener!=null)
                            mServerCommunicationListener.onDataChanged(arrayOfCities);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", "error");
                    }
                });

        // add it to the RequestQueue
        queue.add(getRequest);
    }

    public void updateCityFromServer(final String email, final String smartspace,final String port, final String ip,ElementEntity city){
        final String url = "http://"+ip+":"+port+"/smartspace/elements/"+smartspace+"/"+email+"/"+city.getElementSmartspace()+"/"+city.getElementId() ;

        try{
            ElementBoundary cityBoundary = new ElementBoundary(city);
            cityBoundary.setKey(null);

            JSONObject tempJson = new JSONObject(gson.toJson(cityBoundary));
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, tempJson, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("Response", "update Successful");
                    if(mServerUpdateCityListener!=null)
                        mServerUpdateCityListener.onUpdateFinish();

                    getCitiesFromServer(email,smartspace,port,ip);
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", "update error "+ error+"");
                            if(mServerUpdateCityListener!=null)
                                mServerUpdateCityListener.onUpdateFailed();
                        }
                    }){

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    try {
                        String json = new String(
                                response.data,
                                "UTF-8"
                        );

                        if (json.length() == 0) {
                            return Response.success(
                                    null,
                                    HttpHeaderParser.parseCacheHeaders(response)
                            );
                        }
                        else {
                            return super.parseNetworkResponse(response);
                        }
                    }
                    catch (UnsupportedEncodingException e) {
                        return Response.error(new ParseError(e));
                    }
                }
            };

        // add it to the RequestQueue
        queue.add(putRequest);
        } catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void addNewCityFromServer(final String email, final String smartspace,final String port, final String ip,ElementEntity city){
        final String url = "http://"+ip+":"+port+"/smartspace/elements/"+smartspace+"/"+email;
        try {
        ElementBoundary cityBoundary = new ElementBoundary(city);
        cityBoundary.setKey(null);
        JSONObject tempJson = new JSONObject(gson.toJson(cityBoundary));

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, tempJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        getCitiesFromServer(email,smartspace,port,ip);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "");
                    }
                });

        // add it to the RequestQueue
        queue.add(postRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void playerLogin(final String email, final String smartspace,final String port, final String ip){
        final String url = "http://"+ip+":"+port+"/smartspace/users/login/"+smartspace+"/"+email;
        Log.e(TAG,url);
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        UserBoundary userBoundary = gson.fromJson(response.toString(), UserBoundary.class);
                        UserEntity userEntity = userBoundary.toEntity();

                        if(mLogInToServerListener!= null){
                            mLogInToServerListener.onLogInGood(userEntity);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(mLogInToServerListener!=null){
                            mLogInToServerListener.onLogInBad();
                        }
                        Log.d("Error.Response", "error: "+error);
                    }
                }
        );
        //add it to the RequestQueue
        queue.add(getRequest);
    }
}
