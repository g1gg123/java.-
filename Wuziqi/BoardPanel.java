package Wuziqi;

import javax.swing.*;
import java.awt.*;

//绘制棋盘以及处理下棋的逻辑
public class BoardPanel extends JPanel{
    private static final int GRID_SIZE=15;      //15x15网格线
    private static final int CELL_SIZE=40;     //格子大小
    private static final int MARGIN=20;        //棋盘边缘与界面边缘的间隔
    private int[][] board=new int[GRID_SIZE][GRID_SIZE];

    public BoardPanel(){
        setPreferredSize(new Dimension(MARGIN*2+CELL_SIZE*(GRID_SIZE-1),MARGIN*2+CELL_SIZE*(GRID_SIZE-1)));
    }

    //用于绘制棋盘上的棋子而不影响其他组件
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(new Color(240,230,140));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        //绘制棋格线
        for(int i=0;i<GRID_SIZE;i++){
            int pos=MARGIN+i*CELL_SIZE;
            //绘制水平线
            g.drawLine(MARGIN,pos,MARGIN+(GRID_SIZE-1)*CELL_SIZE,pos);
            //绘制竖直线
            g.drawLine(pos,MARGIN,pos,MARGIN+(GRID_SIZE-1)*CELL_SIZE);
        }

        for(int i=0;i<GRID_SIZE;i++){
            for(int j=0;j<GRID_SIZE;j++){
                if(board[i][j]==1){
                    //黑棋
                    g.setColor(Color.BLACK);
                    g.fillOval(MARGIN+j*CELL_SIZE-10,MARGIN+i*CELL_SIZE-10,20,20);
                }
                else if(board[i][j]==2){
                    //绘制白旗
                    g.setColor(Color.WHITE);
                    g.fillOval(MARGIN+j*CELL_SIZE-10,MARGIN+i*CELL_SIZE-10,20,20);
                    g.setColor(Color.BLACK);
                    g.drawOval(MARGIN+j*CELL_SIZE-10,MARGIN+i*CELL_SIZE-10,20,20);
                }
            }
        }
    }

    //放置棋子，1代表黑色，2代表白色
    public void placePiece(int row,int col,int color){
        board[row][col]=color;
        repaint();
    }
    public int[][] getBoard(){
        return board;
    }
}
