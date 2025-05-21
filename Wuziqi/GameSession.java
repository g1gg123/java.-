package Wuziqi;

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

        }
    }

    //处理玩家下棋的指令
    public synchronized void processMove(ClientThread p,int row,int col){

    }

    //处理玩家中途退出
    public synchronized void processQuit(ClientThread quitter){

    }

}
