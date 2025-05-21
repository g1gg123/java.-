package Wuziqi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{
    public Server() throws IOException{
        ServerSocket ss=new ServerSocket(8888);
        Socket s=ss.accept();
    }
}
