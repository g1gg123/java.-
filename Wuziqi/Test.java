package Wuziqi;

public class Test {
    public static void main(String[] args) {
        Player player=new Player("C","123");
        PlayerManager.setPlayers(player);
        PlayerManager.saveData();
        PlayerManager.loadData();
    }
}
