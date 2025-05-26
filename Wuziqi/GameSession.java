package Wuziqi;

import java.io.IOException;

//于服务端中维护一局五子棋的对局
//包含两个玩家线程的引用，当前的棋盘数组
public class GameSession {
    private int[][] board;
    private ClientThread p1;
    private ClientThread p2;
    private boolean p1Agree=false;      //是否同意对局
    private boolean p2Agree=false;
    private boolean start=false;
    private int currenColor=1;      //当前由黑色或是白色下
    String[] colors={"黑","白"};
    public GameSession(int size,ClientThread p1,ClientThread p2){
        board=new int[size][size];
        this.p1=p1;
        this.p2=p2;
    }

    //处理是否开启对局
    public synchronized void decision(ClientThread p,boolean agree){
        if(start){
            return;
        }
        if(!agree){
            p1.send("取消对局");
            p2.send("取消对局");
            try{
                p1.socket.close();
            }
            catch(IOException e){}
            try{
                p2.socket.close();
            }
            catch(IOException e){}
        }
        else{
            if(p==p1){
                p1Agree=true;
            }
            else if(p==p2){
                p2Agree=true;
            }
            if(p1Agree&&p2Agree){
                p1.send("游戏开始 黑方");
                p2.send("游戏开始 白方");
                start=true;
                currenColor=1;
            }
        }
    }

    //处理玩家下棋的指令
    public synchronized void processMove(ClientThread p,int row,int col){
        if(!start){return;}
        if((p==p1&&currenColor!=1)||(p==p2&&currenColor!=2)){
            p.send("无效落子!");
            return;
        }
        if(row<0||row>=board.length||col<0||col>=board.length||board[row][col]!=0){
            p.send("无效落子");
            return;
        }
        int color=p==p1?1:2;

        //广播信息到所有玩家
        String msg="落子位置 "+row+" "+col+" "+colors[color-1];
        p1.send(msg);
        p2.send(msg);
        //处理落子是否导致游戏结束
        if(GameLogic.checkWin(board,row,col,color)){
            Player winner=p.getPlayer();
            Player loser=(p==p1?p2.getPlayer():p1.getPlayer());
            //更新信息
            PlayerManager.recordResult(winner,loser,false);
            String resMsg=color==1?"游戏结束 黑方胜利":"游戏结束 白方胜利";
            p1.send(resMsg);
            p2.send(resMsg);
            try{
                p1.socket.close();
            }
            catch(IOException e){}
            try{
                p2.socket.close();
            }
            catch(IOException e){}
        }
        else{
            currenColor=(currenColor==1?2:1);
        }
    }

    //处理玩家中途退出
    public synchronized void processQuit(ClientThread quitter){
        //游戏没开始前有人退出
        if(!start){
            decision(quitter,false);
            return;
        }
        //游戏中途有人退出
        //退出者的对手
        ClientThread quitterOpp=(quitter==p1?p2:p1);
        Player winner=quitterOpp.getPlayer();
        Player loser=quitter.getPlayer();
        PlayerManager.recordResult(winner,loser,true);
        String resMsg=(quitter==p1?"白方胜利":"黑方胜利");
        quitterOpp.send(resMsg);
        try{
            p1.socket.close();
        }
        catch(IOException e){}
        try{
            p2.socket.close();
        }
        catch(IOException e){}
        start=false;
    }

}
