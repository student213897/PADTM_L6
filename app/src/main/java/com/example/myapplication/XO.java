package com.example.myapplication;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class XO extends AppCompatActivity {

    public static final String STATUS = "Status";
    public static final String MOVES = "Moves";
    public static final String GAME_ID = "Game_id";
    public static final String PLAYER = "Player";
    public static final int NEW_GAME = 0;
    public static final int YOUR_TURN = 1;
    public static final int WAIT = 2;
    public static final int ERROR = 3;
    public static final int CONNECTION = 4;
    public static final int NETWORK_ERROR = 5;
    public static final int WIN = 6;
    public static final int LOSE = 7;

    private int status;
    private int game_id;
    private String moves;
    private int player;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xo);

        //Geting actual game status or 0
        status = getIntent().getIntExtra(XO.STATUS, XO.NEW_GAME);
        //Geting actual game id or 0
        game_id = getIntent().getIntExtra(XO.GAME_ID, XO.NEW_GAME);
        moves = getIntent().getStringExtra(XO.MOVES);
        player=getIntent().getIntExtra(XO.PLAYER,1);
        hints(status);

        //in XOBoard adapter constructor we put History of Moves as initialization
        GridView gv = findViewById(R.id.gridViewXO);
        gv.setAdapter(new XOBoard(this, moves));

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if(status!=XO.WAIT){
                    status = XO.WAIT;
                    hints(XO.CONNECTION);

                    GridView gv = findViewById(R.id.gridViewXO);
                    XOBoard game = (XOBoard) gv.getAdapter();

                    if(game.add(arg3+1)!=null) {
                        gv.setAdapter(game);
                        Intent intencja = new Intent(getApplicationContext(), HttpService.class);

                        PendingIntent pendingResult = createPendingResult(HttpService.X_O, new Intent(), 0);

                        if(game_id == XO.NEW_GAME){
                            intencja.putExtra(HttpService.URL, HttpService.XO);
                            intencja.putExtra(HttpService.METHOD, HttpService.POST);
                        }else {
                            intencja.putExtra(HttpService.URL, HttpService.XO+game_id);
                            intencja.putExtra(HttpService.METHOD, HttpService.PUT);
                        }
                        intencja.putExtra(HttpService.PARAMS, "moves="+moves+(arg3+1));
                        intencja.putExtra(HttpService.RETURN, pendingResult);
                        startService(intencja);
                    }
                    else {
                        hints(XO.ERROR);
                    }


                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HttpService.X_O) {
            try {
                JSONObject response = new JSONObject(data.getStringExtra(HttpService.RESPONSE));
                if (resultCode == 200) {
                    if (game_id == 0)
                        game_id = response.getInt("game_id");

                    GridView gv = findViewById(R.id.gridViewXO);
                    XOBoard game = (XOBoard) gv.getAdapter();
                    int game_status = game.checkWin();
                    if (game_status == 0) {
                        hints(XO.WAIT);
                    } else {
                        if (game_status == player)
                            hints(XO.WIN);
                        else
                            hints(XO.LOSE);
                    }
                } else {
                    if (resultCode == 500)
                        hints(XO.NETWORK_ERROR);
                    else
                        hints(XO.ERROR);

                    Log.d("DEBUG", response.getString("http_status"));
                }
                Thread.sleep(5000);
                refresh(null);
            } catch (Exception ex) {
                hints(XO.ERROR);
                ex.printStackTrace();
            }
        } else if (requestCode == HttpService.REFRESH) {
            try {
                JSONObject response = new JSONObject(data.getStringExtra(HttpService.RESPONSE));

                moves = response.getString("moves");
                GridView gv = findViewById(R.id.gridViewXO);
                XOBoard game = new XOBoard(this, moves);
                gv.setAdapter(game);

                if (response.getInt("status") == player) {
                    if (game.checkWin() == player) {
                        hints(XO.WIN);
                    } else if (game.checkWin() != 0) {
                        hints(XO.LOSE);
                    } else {
                        status = XO.YOUR_TURN;
                        hints(status);
                    }
                } else {
                    Thread.sleep(5000);
                    refresh(null);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    public void refresh(MenuItem item){
        Intent intencja = new Intent(getApplicationContext(), HttpService.class);

        PendingIntent pendingResult = createPendingResult(HttpService.REFRESH, new Intent(),0);
        intencja.putExtra(HttpService.URL, HttpService.XO+game_id);
        //Set data - method of request
        intencja.putExtra(HttpService.METHOD, HttpService.GET);
        //Set data - intent for result
        intencja.putExtra(HttpService.RETURN, pendingResult);
        //Start unBound Service in another Thread
        startService(intencja);
    }

    private void hints(int status){
        TextView hint = findViewById(R.id.XOHint);

        switch (status){
            case XO.YOUR_TURN:
                hint.setText(getString(R.string.your_turn));
                break;
            case XO.WAIT:
                hint.setText(getString(R.string.wait));
                break;
            case XO.ERROR:
                hint.setText(getString(R.string.error));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        refresh(null);
                    }

                }, 2000);

                break;
            case XO.CONNECTION:
                hint.setText(getString(R.string.connection));
                break;
            case XO.NETWORK_ERROR:
                hint.setText(getString(R.string.network_error));
                break;
            case XO.WIN:
                hint.setText(getString(R.string.win));
                break;
            case XO.LOSE:
                hint.setText(getString(R.string.lose));
                break;
            default:
                hint.setText(getString(R.string.new_game));
                break;
        }
    }


}
