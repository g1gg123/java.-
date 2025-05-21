package Wuziqi;

//用于服务端判定每次下的棋子是否导致游戏结束
public class GameLogic {
    private static int[][] directions = {{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1},{0,1},{1,1}};
    /*
    * @para row:当前下的棋子的行
    * @para col:当前下的棋子的列
    * */
    public static boolean checkWin(int[][] board,int row,int col,int color) {
        int n=board.length;
        for(int i=0;i<8;i++){
            int dr=directions[i][0];
            int dc=directions[i][1];
            int count=1;
            int r=row+dr,c=col+dc;
            while(r>=0&&r<n&&c>=0&&c<n&&board[r][c]==color){
                count++;
                r=r+dr;
                c=c+dc;
            }
            if(count>=5){return true;}
        }
        return false;
    }

}
