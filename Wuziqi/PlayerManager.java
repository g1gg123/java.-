package Wuziqi;

import java.io.*;
import java.util.HashMap;

//管理用户登陆和注册以及管理玩家数据
public class PlayerManager {
    private static HashMap<String, Player> players=new HashMap<>();        //避免重名
    private final static String DATA_FILE="users.txt";

    //加载数据
    public static void loadData(){
        BufferedReader br=null;
        try{
            File file=new File(DATA_FILE);
            //System.out.println(file.getAbsolutePath());
            br=new BufferedReader(new FileReader(file));
            String line;
            while((line=br.readLine())!=null){
                //System.out.println(line);
                String[] data=line.split(",");
                String username=data[0];
                String password=data[1];
                int wins=Integer.parseInt(data[2]);
                int losses=Integer.parseInt(data[3]);
                int escapes=Integer.parseInt(data[4]);
                Player player=new Player(username,password,wins,losses,escapes);
                players.put(username,player);
            }
            br.close();
        }
        catch(FileNotFoundException e){
            throw new RuntimeException(e);
        }catch(IOException e){
            System.out.println("用户加载失败");
            throw new RuntimeException(e);
        }
    }

    //存储数据
    public static void saveData(){
        try{
            File file=new File(DATA_FILE);
            BufferedWriter bw=new BufferedWriter(new FileWriter(file));
            for(Player p:players.values()){
                bw.write(p.username+","+p.password+","+p.win_count+","+p.lose_count+","+p.esc_count+","+p.level);
                bw.newLine();
            }
            bw.close();
        }catch(IOException e){
            System.out.printf("保存失败"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //登陆
    public static synchronized Player login(String username, String password){
        if(!players.containsKey(username)){
            return null;
        }
        Player player = players.get(username);
        //密码不匹配，返回空值
        if(!password.equals(player.password)){
            return null;
        }
        return player;
    }

    //注册
    public static synchronized Player register(String username,String password){
        if(players.containsKey(username)){
            return null;
        }
        Player newPlayer = new Player(username,password);
        players.put(username, newPlayer);
        return newPlayer;
    }

    //记录结果
    public static synchronized void recordResult(Player winner,Player loser,boolean escape){
        if(winner != null){
            winner.win_count++;
            winner.updateLevel();
        }
        if(loser!=null){
            if(escape){
                loser.esc_count++;
            }
            loser.lose_count++;
            loser.updateLevel();
        }
    }
    public static void setPlayers(Player player){
        players.put(player.username,player);
    }
}
