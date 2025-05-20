package Wuziqi;

import javax.swing.*;

public class Client{
    private JFrame frame;

    public static void main(String[] args){
        JFrame frame = new JFrame("Wuziqi");
        frame.add(new BoardPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }
}
