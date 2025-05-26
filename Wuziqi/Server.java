package Wuziqi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server{
    private ServerSocket ss;
    private static ArrayList<ClientThread> waitingPlayers=new ArrayList<>();
    private static final int PORT=1001;

    public Server() {
        try{
            ss=new ServerSocket(PORT);
            PlayerManager.loadData();
        }catch(IOException e){
            System.out.println("无法启动服务器:"+e.getMessage());
        }
    }

    public void start(){
        while(true){
            try{
                Socket socket=ss.accept();
                System.out.println("连接来自:"+socket.getRemoteSocketAddress());
                ClientThread ct=new ClientThread(socket);
                ct.start();
            }catch(IOException e){
                System.out.println("服务器异常:"+e.getMessage());
                break;
            }
        }
    }

    public static synchronized void matchPlayer(ClientThread ct){
        if(waitingPlayers.isEmpty()){
            waitingPlayers.add(ct);
            ct.setWaiting(true);
        }
        else{
            ClientThread opp=waitingPlayers.removeFirst();      //对手
            opp.setWaiting(false);
            GameSession game=new GameSession(15,opp,ct);
            opp.setGame(game);
            ct.setGame(game);
            opp.setOpp(ct);
            ct.setOpp(opp);

            opp.setColor(1);
            ct.setColor(2);

            opp.send("对手 "+ct.getPlayerInfo());
            ct.send("对手 "+opp.getPlayerInfo());
        }
    }

    public static synchronized void removeWaitingPlayer(ClientThread ct){
        waitingPlayers.remove(ct);
    }
    public static void main(String[] args) {
        Server server=new Server();
        server.start();
    }
}
