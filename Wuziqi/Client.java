package Wuziqi;

import javax.swing.*;
import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.*;
import java.net.Socket;

public class Client{
    private JFrame frame;
    private BoardPanel bp=new BoardPanel();
    private BufferedReader in;
    private BufferedWriter out;
    private JLabel infoLabel;
    private String oppName;     //对手用户名
    private boolean myTurn;
    private int myColor;
    private boolean gameActive;

    public Client(String host, int port){
        try{
            Socket socket=new Socket(host,port);
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
                    out.write("玩家登录"+username+" "+password);
                    out.newLine();
                    out.flush();
                }
                else{       //注册
                    out.write("玩家注册"+username+" "+password);
                    out.newLine();
                    out.flush();
                }
                //等待服务端回复
                String response=in.readLine();
                if(response==null){
                    JOptionPane.showMessageDialog(null,"与服务器断开连接");
                    System.exit(0);
                }
                if(response.startsWith("登陆成功")||response.startsWith("注册成功")){
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

    public static void main(String[] args) throws IOException{
        Client c=new Client("127.0.0.1",1001);
        c.send("111");
    }
}
