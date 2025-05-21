package Wuziqi;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

public class Client{
    private JFrame frame;
    private static BoardPanel bp=new BoardPanel();
    public static void main(String[] args) throws IOException{
        JFrame frame = new JFrame("Wuziqi");
        frame.add(bp);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

    }
}
