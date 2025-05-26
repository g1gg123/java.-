package Wuziqi;

//用于服务端判定每次下的棋子是否导致游戏结束
public class GameLogic {
    /*
     * @para row:当前下的棋子的行
     * @para col:当前下的棋子的列
     * */
    public static boolean checkWin(int[][] board,int row,int col,int color){
        int n=board.length;
        //四个基准方向：水平、垂直、主对角线、副对角线
        int[][] directions={{0,1},{1,0},{1,1},{1,-1}};
        for(int[] dir: directions){
            int dr=dir[0], dc=dir[1];
            int count=1;  // 包含当前落子
            //沿指定方向累加同色棋子
            int r=row+dr, c=col+dc;
            while(r>=0&&r<n&&c>=0&&c<n&&board[r][c]==color){
                count++;
                r+=dr;
                c+=dc;
            }
            //沿相反方向累加同色棋子
            r=row-dr;
            c=col-dc;
            while(r>=0&&r<n&&c>=0&&c<n&&board[r][c]==color){
                count++;
                r-=dr;
                c-=dc;
            }
            if(count>=5){
                return true;
            }
        }
        return false;
    }

}
