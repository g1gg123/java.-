package Wuziqi;

import java.io.*;
import java.net.Socket;

//服务端用于处理客户端通信的线程类
//负责处理消息交互，登陆验证以及下棋指令
public class ClientThread extends Thread {
    public Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Player player;
    private boolean waiting;        //等待对手
    private ClientThread opponent;      //对手
    private GameSession game;
    private int color;      //1代表黑色，2代表白色

    public ClientThread(Socket socket){
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("通信错误:" + e.getMessage());
        }
    }


    //向服务端发送信息
    public void send(String msg) {
        try{
            out.write(msg);
            out.newLine();
            out.flush();
        }catch(IOException e){
            System.out.println("消息传输失败:"+e.getMessage());
        }
    }

    @Override
    public void run() {
        try{
            String line;
            while(true){
                line=in.readLine();
                System.out.println(line);
                if(line==null){return;}
                String[] parts=line.split(" ");     //分割消息，便于处理
                //处理登录和注册请求
                if(parts[0].equals("玩家登录")){
                    if(parts.length>=3){
                        String username=parts[1];
                        String password=parts[2];
                        Player p=PlayerManager.login(username,password);
                        if(p!=null){
                            player=p;
                            send("登录成功");
                            break;
                        }
                        else{
                            send("登录失败");
                        }
                    }
                }
                else if(parts[0].equals("玩家注册")){
                    if(parts.length>=3){
                        String username=parts[1];
                        String password=parts[2];
                        Player p=PlayerManager.register(username,password);
                        if(p!=null){
                            player=p;
                            send("注册成功");
                            break;
                        }
                        else{
                            send("注册失败");
                        }
                    }
                }
            }
            Server.matchPlayer(this);
            while((line=in.readLine())!=null){
                String[] parts=line.split(" ");
                if(parts[0].equals("同意")){
                    if(game!=null){
                        game.decision(this,true);
                    }
                }
                else if(parts[0].equals("拒绝")){
                    if(game!=null){
                        game.decision(this,false);
                    }
                }
                else if(parts[0].equals("落子位置")){
                    if(game!=null&&parts.length>=3){
                        try{
                            int r=Integer.parseInt(parts[1]);
                            int c=Integer.parseInt(parts[2]);
                            game.processMove(this,r,c);
                        }
                        catch(NumberFormatException e){}
                    }
                }
                else if(parts.equals("退出")){
                    if(game!=null){
                        game.processQuit(this);
                    }
                    else{
                        Server.removeWaitingPlayer(this);
                    }
                    break;
                }
            }
        }catch(IOException e){
            if(game!=null){
                game.processQuit(this);
            }
            else{
                Server.removeWaitingPlayer(this);
            }
        }finally{
            try{
                if(in!=null) in.close();
                if(out!=null) out.close();
                if(socket!=null&&!socket.isClosed()) socket.close();
            }catch(IOException e){}
        }

    }


    public Player getPlayer() {
        return player;
    }
    public void setOpp(ClientThread opponent) {
        this.opponent = opponent;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public void setGame(GameSession game) {
        this.game = game;
    }
    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    //获取玩家信息
    public String getPlayerInfo(){
        return player.username+" "+player.level+" "+player.win_count+" "+player.lose_count+" "+player.esc_count;
    }
}
