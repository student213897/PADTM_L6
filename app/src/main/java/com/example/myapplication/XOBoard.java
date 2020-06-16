package com.example.myapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class XOBoard extends BaseAdapter{

    private Context context;
    private int player;
    private int[][] board = new int[3][3];

    @Override
    public int getCount() {
        return 3*3;
    }
    @Override
    public Object getItem(int position) {
        return position%9;
    }
    @Override
    public long getItemId(int position) {
        return position%9;
    }

    //sets objects on board. Empty, player1, player2
    //TODO: change shapes for X's and O's
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv = new ImageView(context);

        int row = (position)/3;
        int col = (position)%3;

        switch (board[row][col]){
            case 0:
                iv.setImageResource(R.drawable.circle);
                break;
            case 1:
                iv.setImageResource(R.drawable.player1);
                break;
            case 2:
                iv.setImageResource(R.drawable.player2);
                break;
        }
        iv.setLayoutParams(new LinearLayout.LayoutParams(120,120));
        return iv;
    }

    public XOBoard(Context cont, String moves) {
        context = cont;
        int mvs = 0;

        for (String move:moves.split("(?!^)")){
            if(move!=""){
                try {
                    this.move(Integer.parseInt(move), mvs++%2);
                }catch (Exception e){}
            }
        }
        player = mvs%2;
    }

    private boolean move(int position, int player){

        //calculate position from single number between 1 and 9 to col and row position

        int row = (position-1)/3;
        int col = (position-1)%3;

        if(board[row][col]!=0){
            return false;
        }

        try {
            board[row][col]=player+1;
        }catch (Exception ex){
            return false;
        }
        return true;
    }

    public XOBoard add(long position){
        if(this.move((int) position,player))
            return this;
        return null;
    }

    public int checkWin(){
        int inRow = 0;

        //rows
        for(int row=0; row<3; row++) {
            for (int col = 0; col < 2; col++) {
                if (board[row][col] == board[row][col + 1]) {
                    inRow++;
                    if (inRow == 2 && board[row][col] != 0) {
                        return board[row][col];
                    }
                } else {
                    inRow = 0;
                }
            }
        }

        //cols
        inRow=0;
        for(int col=0; col<3; col++, inRow=0) {
            for (int row = 0; row < 2; row++) {
                if (board[row][col] == board[row+1][col]) {
                    inRow++;
                    if (inRow == 2 && board[row][col] != 0) {
                        return board[row][col];
                    }
                } else {
                    inRow = 0;
                }
            }
        }

        //rising horizontal
        /*
        for (int posx=3; posx<6;posx++){
            for (int posy=0;posy<4;posy++){
                inRow = 0;
                for(int x=posx, y=posy; x>0 && y<6;x--,y++) {
                    if (board[x][y] == board[x - 1][y + 1]) {
                        inRow++;
                        if (inRow == 3 && board[x][y] != 0) {
                            return board[x][y];
                        }
                    } else {
                        inRow = 0;
                    }
                }
            }
        }*/inRow=0;
        for (int row=2, col=0; row>0 && col<2;row--, col++){
                if(board[row][col]==board[row-1][col+1]){
                    inRow++;
                    if (inRow == 2 && board[row][col] != 0) {
                        return board[row][col];
                    }
                }else {
                    inRow = 0;
                }
        }


        //falling horizontal
        /*
        for (int posx=0; posx<3;posx++) {
            for (int posy = 0; posy < 4; posy++) {
                inRow = 0;
                for (int x = posx, y = posy; x < 5 && y < 6; x++, y++){
                    if (board[x][y] == board[x + 1][y + 1]) {
                        inRow++;
                        if (inRow == 3 && board[x][y] != 0) {
                            return board[x][y];
                        }
                    } else {
                        inRow = 0;
                    }
                }
            }
        }*/
        /*for (int row=0, col=2; row>2 && col<0;row++, col--){
                if(board[row][col]==board[row+1][col-1]){
                    inRow++;
                    if (inRow == 2 && board[row][col] != 0) {
                        return board[row][col];
                    }
                }else {
                    inRow = 0;
                }
        }*/inRow=0;
        for (int row=0, col=0; row<2 && col<2;row++, col++){
            if(board[row][col]==board[row+1][col+1]){
                inRow++;
                if (inRow == 2 && board[row][col] != 0) {
                    return board[row][col];
                }
            }else {
                inRow = 0;
            }
        }




        return 0;

    }


}
