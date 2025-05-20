package Wuziqi;

//存储玩家信息
public class Player{
    public String username;
    public String password;
    public int win_count;       //胜局数目
    public int lose_count;      //失败数目
    public int esc_count;       //逃跑数目
    public int level;

    public Player(String username, String password, int win_count, int lose_count, int esc_count){
        this.username = username;
        this.password = password;
        this.win_count = win_count;
        this.lose_count = lose_count;
        this.esc_count = esc_count;
        this.level = (int)(win_count*0.7+lose_count*0.3);
    }
    public Player(String username, String password){
        this(username, password, 0, 0, 0);
    }
    public void updateLevel(){
        level = (int)(win_count*0.7+lose_count*0.3);
    }
}
