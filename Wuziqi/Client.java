package Wuziqi;

import javax.swing.*;
import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Client{
    private Socket socket;
    private JFrame frame;
    private BoardPanel bp;
    private BufferedReader in;
    private BufferedWriter out;
    private JLabel infoLabel;
    private JButton quitButton;
    private String oppName;     //对手用户名
    private boolean myTurn;
    private int myColor;
    private boolean gameActive;

    public Client(String host, int port){
        try{
            socket=new Socket(host,port);
            in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        catch(IOException e){
            System.out.println("创建客户端失败!"+e.getMessage());
            System.exit(0);
        }
        boolean loggedIn=false;
        while(!loggedIn){
            String[] options={"登录","注册"};
            int choice=JOptionPane.showOptionDialog(null,"请选择登录或注册：","登录",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,options,options[0]);
            if(choice==JOptionPane.CLOSED_OPTION){
                System.exit(0);
            }
            String username=JOptionPane.showInputDialog(null,"用户名:");
            String password=JOptionPane.showInputDialog(null,"密码");
            try{
                if(choice==0){      //登录
                    out.write("玩家登录 "+username+" "+password);
                    out.newLine();
                    out.flush();
                }
                else{       //注册
                    out.write("玩家注册 "+username+" "+password);
                    out.newLine();
                    out.flush();
                }
                //等待服务端回复
                String response=in.readLine();
                if(response==null){
                    JOptionPane.showMessageDialog(null,"与服务器断开连接");
                    System.exit(0);
                }
                if(response.startsWith("登录成功")||response.startsWith("注册成功")){
                    loggedIn=true;
                    JOptionPane.showMessageDialog(null,"登录成功:"+username);
                }
                else if(response.startsWith("登录失败")){
                    JOptionPane.showMessageDialog(null,"登录失败!用户名或密码错误");
                }
                else if(response.startsWith("注册失败")){
                    JOptionPane.showMessageDialog(null,"注册失败!用户名已经存在");
                }
            }
            catch(IOException e){
                System.out.println("通信错误:"+e.getMessage());
            }
        }

        //创建UI
        frame=new JFrame("五子棋游戏");
        bp=new BoardPanel(this);
        infoLabel=new JLabel("等待对手连接");
        quitButton=new JButton("退出");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                send("退出");
                closeConn();
                System.exit(0);
            }
        });

        JPanel btmPanel=new JPanel();
        btmPanel.add(quitButton);
        frame.add(infoLabel,"North");
        frame.add(bp,"Center");
        frame.add(btmPanel,"South");
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run(){
                try{
                    String line;
                    while((line=in.readLine())!=null){
                        String[] parts=line.split(" ");
                        if(parts[0].equals("对手")){
                            oppName=parts[1];
                            String oppLevel=parts[2];
                            String oppWins=parts[3];
                            String oppLoses=parts[4];
                            String oppEscapes=parts[5];
                            String msg="找到对手："+oppName+"\n"+"等级: "+oppLevel+"  胜: "+oppWins+"  负: "+oppLoses+"  逃逸: "+oppEscapes+"\n"+"是否开始游戏？";
                            int res=JOptionPane.showConfirmDialog(frame,msg,"对局请求",JOptionPane.YES_NO_CANCEL_OPTION);
                            if(res==JOptionPane.YES_OPTION){
                                send("同意");
                                infoLabel.setText("同意与 "+oppName+"开始游戏，等待对方确认");
                            }
                            else{
                                send("拒绝");
                                infoLabel.setText("拒绝了与 "+oppName+"的对局");
                            }
                        }
                        else if(parts[0].equals("游戏开始")){
                            gameActive=true;
                            if(parts[1].equals("黑方")){
                                myColor=1;
                                myTurn=true;
                                infoLabel.setText("游戏开始!作为黑方，优先落子");
                            }
                            else if(parts[1].equals("白方")){
                                myColor=2;
                                myTurn=false;
                                infoLabel.setText("游戏开始!作为白方，等待黑方落完子");
                            }
                        }
                        else if(parts[0].equals("落子位置")){
                            int r=Integer.parseInt(parts[1]);
                            int c=Integer.parseInt(parts[2]);
                            int color=Integer.parseInt(parts[3]);

                            bp.placePiece(r,c,color);
                            if(color==myColor){
                                myTurn=false;
                                infoLabel.setText("已落子，等待对方落子...");
                            }
                            else{
                                myTurn=true;
                                infoLabel.setText("轮到你落子");
                            }
                        }
                        else if(parts[0].equals("游戏结束")){
                            gameActive=false;
                            myTurn=false;
                            String winner=parts[1];
                            String msg;
                            if((winner.equals("黑方胜利")&&myColor==1)||(winner.equals("白方胜利")&&myColor==2)){
                                msg="游戏结束，你获胜";
                            }
                            else {
                                msg="游戏结束，你输了";
                            }
                            JOptionPane.showMessageDialog(frame,msg);
                            infoLabel.setText("游戏结束: "+(winner.equals("黑方胜利")?"黑方胜利":"白方胜利"));
                        }
                        else if(parts[0].equals("取消对局")){
                            gameActive=false;
                            myTurn=false;
                            JOptionPane.showMessageDialog(frame,"对局取消");
                            infoLabel.setText("对局已取消");
                            break;
                        }
                    }
                }
                catch(IOException e){}
                finally{
                    closeConn();
                }
            }
        }).start();

    }

    //用于发送消息
    public void send(String msg){
        try{
            out.write(msg);
            out.newLine();
            out.flush();
        }
        catch(IOException e){
            System.out.println("消息发送失败!"+e.getMessage());
        }
    }

    //关闭连接和流
    private void closeConn(){
        try{
            if(in!=null) in.close();
            if(out!=null) out.close();
            if(socket!=null) socket.close();
        }
        catch(IOException ignored){}
    }

    public boolean getMyTurn(){
        return myTurn;
    }

    public void setMyTurn(boolean turn){
        myTurn=turn;
    }

    public boolean getGameActive(){
        return gameActive;
    }

    public static void main(String[] args) throws IOException{
        SwingUtilities.invokeLater(() -> new Client("127.0.0.1", 1001));
    }
}
