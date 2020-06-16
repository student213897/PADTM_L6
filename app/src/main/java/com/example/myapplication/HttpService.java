package com.example.myapplication;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpService extends IntentService {
    public static final int GAMES_LIST = 1;
    public static final int IN_ROW = 2;
    public static final int REFRESH = 3;
    public static final int GAME_INFO = 4;
    public static final int X_O = 2;
    public static final String URL = "URL";
    public static final String METHOD = "Method";
    public static final String PARAMS = "Params";
    public static final String RETURN = "Return";
    public static final String RESPONSE = "Response";
    public static final String LINES = "http://games.antons.pl/lines/";
    public static final String XO = "http://games.antons.pl/xo/";
    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;

    public HttpService() {
        super("HTTP calls handler");
    }

    /*@Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }*/

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String urlstr = intent.getStringExtra(HttpService.URL);
            URL url = new URL(urlstr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            switch (intent.getIntExtra(HttpService.METHOD,1)){
                case HttpService.POST:
                    conn.setRequestMethod("POST");
                    break;
                case HttpService.PUT:
                    conn.setRequestMethod("PUT");
                    break;
                default:
                    conn.setRequestMethod("GET");
                    break;
            }
            Config conf = new Config(getApplicationContext());
            conn.setRequestProperty("PKEY",conf.getPublic().replace("\n",""));
            conn.setRequestProperty("SIGN", conf.sign(urlstr).replace("\n",""));

            String params = intent.getStringExtra(HttpService.PARAMS);
            if(params!=null){
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(params);
                writer.flush();
                writer.close();
            }

            conn.connect();

            int responseCode = conn.getResponseCode();

            String response = "";
            if(responseCode==200){
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                while((line = reader.readLine()) != null){
                    response += line;
                }
                reader.close();
            }
            conn.disconnect();

            Intent returns = new Intent();
            returns.putExtra(HttpService.RESPONSE, response);
            PendingIntent reply = intent.getParcelableExtra(HttpService.RETURN);
            reply.send(this, responseCode, returns);
        }catch (Exception e){
            e.printStackTrace();
            //Log.d("CONNERROR", e.toString());
        }

    }
}
