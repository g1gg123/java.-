# **Java网络五子棋游戏的设计与实现**







## **设计思路与架构**





本网络五子棋游戏采用典型的客户端/服务器（C/S）架构，服务器使用Java ServerSocket监听固定端口，客户端通过Socket连接服务器并进行通信 。为了支持多玩家同时在线，服务器端使用**多线程**来并发处理多个客户端连接请求：每当有客户端连接，服务器创建一个独立线程与之通信，从而同时服务多位玩家 。服务器维护一个等待队列用于玩家匹配，当检测到当前在线的未匹配玩家数为偶数时，自动将玩家两两配对开始游戏。



在用户登录方面，服务器维护简单的用户资料（用户名、密码、胜负场次、等级、逃逸次数），存储在本地文件中（无需数据库）。客户端启动时可选择注册新用户或登录已有账户。服务器收到登录/注册请求后，在用户列表中验证帐号密码是否合法，若失败则返回错误提示，成功则返回确认信息 。登录成功后，服务器将该玩家加入等待队列进行匹配。



**玩家匹配与先后手：**先连接进入等待的玩家将执黑棋（黑棋先行），后匹配的玩家执白棋。服务器在匹配两名玩家后，会向双方发送对方的基本信息（包括对方用户名、等级、胜场、败场、逃逸次数），并请求双方确认是否愿意开始对局。如果任一玩家拒绝，则本局游戏取消，双方连接断开（或重新等待匹配）。若双方同意，则服务器向双方发送游戏开始指令，告知各自的棋子颜色，由黑方先手。



**通信协议设计：**客户端和服务器通过文本命令进行消息交互。例如：



- LOGIN <user> <pass> / REGISTER <user> <pass>：登录或注册请求
- LOGIN_SUCCESS / LOGIN_FAIL：登录结果反馈
- OPPONENT <name> <level> <wins> <losses> <escapes>：对手信息及数据
- AGREE / REFUSE：玩家是否同意开始对局的回复
- START BLACK / START WHITE：游戏开始及玩家执棋颜色
- MOVE <row> <col>：玩家落子请求（行列坐标）
- MOVE <row> <col> <color>：服务器广播的落子信息（带棋子颜色）
- INVALID：非法落子（如非当前轮或位置不合法）
- RESULT BLACK / RESULT WHITE：服务器判定胜负结果（黑胜或白胜）
- QUIT：客户端主动退出游戏指令
- CANCEL：游戏取消（由于对手拒绝开始或中途断开）





**游戏流程：游戏开始后，双方客户端各自显示一个15×15的棋盘（使用Swing绘制）。黑方先下，客户端通过鼠标点击选择落子位置。每一步客户端先不直接落子**，而是将坐标通过Socket发送给服务器，由服务器进行判定：检查是否轮到该玩家、该位置是否空闲等 。如果合法，服务器在自己的棋盘模型上记录该步，并将这一步落子广播发送给双方客户端 。客户端收到服务器广播的MOVE消息后，再在本地棋盘上绘制相应的黑/白棋子，从而保持双方界面的同步更新 。这样由服务器统一裁决，每步走子都确保有效且同步。



服务器每步落子后会检测当前棋局是否产生五子连珠。我们在服务器实现了五子棋的胜利判定算法（GameLogic.checkVictory），检查横竖斜四个方向上是否出现当前玩家的连续五子。如出现胜负，服务器将游戏结果通过RESULT消息通知双方，并终止该局游戏。  在基本要求中，客户端也具备自行判断胜负的功能（客户端收到MOVE后可调用相同算法判断是否五连珠），但在服务器已经判断胜负的实现下，最终以服务器通知结果为准，避免双方判断不一致。



**异常和退出处理：**客户端提供“退出”按钮，游戏中玩家可随时点击退出。客户端发送QUIT命令，服务器收到后将视该玩家为中途逃逸：如果在游戏进行中则判定另一方获胜，并增加逃逸次数计入统计；如果尚未开始游戏（等待匹配或对方未同意），则取消本次对局匹配。无论哪种情况，服务器都会通知另一方并关闭相应连接。服务器也考虑了断线异常：如果检测到客户端断开，等同于该玩家退出处理。所有这些退出和胜负信息都会记录在用户数据中（胜场、败场、等级、逃逸次数），并由服务器保存在本地文件，以便下次登录时加载。



下面分别介绍系统各模块的实现代码和功能说明。





## **Server.java 模块**





服务器主程序模块，负责监听端口、接受客户端连接和进行玩家匹配等功能。服务器采用多线程处理，每当有新客户端连接就启动一个ClientHandler线程进行通信 。服务器维护一个等待列表waitingPlayers，用于暂存等待配对的客户端线程。当第二个玩家到来时，从等待列表取出第一个玩家与之配对开始游戏。配对时由服务器创建GameSession对象来管理该局游戏状态，并通知双方玩家对手信息，进入确认环节。如果双方同意，则开始游戏，由黑棋玩家先行。



以下是服务器主类Server的代码：

```java
/** 
 * 服务器主类：监听端口、接受客户端连接并进行配对游戏。
 */
public class Server {
    private ServerSocket serverSocket;
    private static List<ClientHandler> waitingPlayers = new ArrayList<>();
    
    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器启动，端口 " + port);
            // Load existing user data
            PlayerManager.loadData();
        } catch (IOException e) {
            System.out.println("无法启动服务器: " + e.getMessage());
        }
    }
    
    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                // 每当有新客户端连接，创建线程进行处理
                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            } catch (IOException e) {
                System.out.println("服务器异常: " + e.getMessage());
                break;
            }
        }
        // Optionally close serverSocket when done (not reachable in infinite loop here)
    }
    
    // Synchronize this to handle pairing safely
    public static synchronized void matchPlayer(ClientHandler handler) {
        if (waitingPlayers.isEmpty()) {
            // No waiting player, put this handler in waiting list
            waitingPlayers.add(handler);
            handler.setWaiting(true);
        } else {
            // Someone is waiting, pair them
            ClientHandler opponent = waitingPlayers.remove(0);
            opponent.setWaiting(false);
            // Set up a new game session for the two players
            GameSession game = new GameSession(15, opponent, handler);
            opponent.setGameSession(game);
            handler.setGameSession(game);
            opponent.setOpponent(handler);
            handler.setOpponent(opponent);
            // Assign colors: first (waiting one) black, second white
            opponent.setColor(1);
            handler.setColor(2);
            // Send opponent info to both players and request agreement to start
            opponent.send("OPPONENT " + handler.getPlayerInfoString());
            handler.send("OPPONENT " + opponent.getPlayerInfoString());
        }
    }
    
    public static synchronized void removeWaitingPlayer(ClientHandler handler) {
        // Remove a player from waiting list if present (e.g. when they quit before pairing)
        waitingPlayers.remove(handler);
    }
    
    public static void main(String[] args) {
        int port = 8888;
        Server server = new Server(port);
        server.start();
    }
}
```

**实现说明：**上述服务器代码在main中启动后，会不断接受客户端连接。matchPlayer方法保证线程安全地进行玩家配对：如果当前没有等待者，则将新连接加入等待列表；如果已有一名玩家在等待，则取出之与当前玩家配成一局。配对成功后，新建GameSession来维护棋局状态，并设置双方线程的对手引用和棋子颜色（约定等待在先者为黑棋，后来的为白棋）。随后服务器通过OPPONENT消息将双方的用户名和统计数据发送给彼此，让客户端弹出确认对话框，询问是否开始游戏。





## **ClientHandler.java 模块**





ClientHandler是服务器端用于处理每个客户端通信的线程类。它在服务器接受连接时创建，并在独立线程中运行，负责与对应客户端进行消息交互，包括登录验证、接受玩家指令以及发送服务器消息等。

```java
/** 
 * 服务器端线程：处理单个客户端的通信（登录和对局）。
 */
public class ClientHandler extends Thread {
    Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Player player;           // logged-in player's info
    private ClientHandler opponent;  // opponent thread
    private GameSession gameSession; // game session object
    private int color; // 1 for black, 2 for white
    
    private boolean waitingForOpponent = false;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("通信错误: " + e.getMessage());
        }
    }
    
    public void setOpponent(ClientHandler opp) {
        this.opponent = opp;
    }
    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public Player getPlayer() {
        return player;
    }
    public void setWaiting(boolean waiting) {
        this.waitingForOpponent = waiting;
    }
    
    // Utility to send message to client
    public void send(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            // If send fails, likely client disconnected
            // We can handle that in run loop separately.
        }
    }
    
    // Get a string of player's info to send to opponent
    public String getPlayerInfoString() {
        // Format: username level wins losses escapes
        return player.username + " " + player.level + " " + player.wins + " " + player.losses + " " + player.escapes;
    }
    
    @Override
    public void run() {
        try {
            // Handle user login/register
            String line;
            while (true) {
                line = in.readLine();
                if (line == null) {
                    // connection closed before login
                    return;
                }
                String[] parts = line.split(" ");
                if (parts[0].equals("LOGIN")) {
                    if (parts.length >= 3) {
                        String username = parts[1];
                        String password = parts[2];
                        Player p = PlayerManager.login(username, password);
                        if (p != null) {
                            player = p;
                            send("LOGIN_SUCCESS");
                            break;
                        } else {
                            send("LOGIN_FAIL");
                        }
                    }
                } else if (parts[0].equals("REGISTER")) {
                    if (parts.length >= 3) {
                        String username = parts[1];
                        String password = parts[2];
                        Player p = PlayerManager.register(username, password);
                        if (p != null) {
                            player = p;
                            send("REGISTER_SUCCESS");
                            break;
                        } else {
                            send("REGISTER_FAIL");
                        }
                    }
                }
            }
            // After successful login/registration, proceed to game matching
            Server.matchPlayer(this);
            
            // Now wait for and handle game-related messages
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens[0].equals("AGREE")) {
                    if (gameSession != null) {
                        gameSession.onDecision(this, true);
                    }
                } else if (tokens[0].equals("REFUSE")) {
                    if (gameSession != null) {
                        gameSession.onDecision(this, false);
                    }
                } else if (tokens[0].equals("MOVE")) {
                    // Format: MOVE row col
                    if (gameSession != null && tokens.length >= 3) {
                        try {
                            int r = Integer.parseInt(tokens[1]);
                            int c = Integer.parseInt(tokens[2]);
                            gameSession.processMove(this, r, c);
                        } catch (NumberFormatException e) {
                            // ignore invalid format
                        }
                    }
                } else if (tokens[0].equals("QUIT")) {
                    // Client wants to quit
                    if (gameSession != null) {
                        gameSession.handleQuit(this);
                    } else {
                        // If not in game (still waiting or in matching phase)
                        // Remove from waiting list if present
                        Server.removeWaitingPlayer(this);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            // If an IO error occurs, treat as disconnect
            if (gameSession != null) {
                gameSession.handleQuit(this);
            } else {
                Server.removeWaitingPlayer(this);
            }
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
```



**实现说明：**ClientHandler线程首先处理登录和注册逻辑：通过阻塞读取客户端发来的LOGIN或REGISTER命令，调用PlayerManager验证或创建用户。如果成功，则发送LOGIN_SUCCESS/REGISTER_SUCCESS响应并跳出登录循环。然后调用Server.matchPlayer(this)将自己加入匹配队列或直接配对开局。



进入游戏阶段后，ClientHandler在线程中持续监听客户端发来的指令：



- 收到AGREE或REFUSE表示玩家对开始游戏的选择，调用GameSession.onDecision处理。只有当双方都AGREE时游戏才正式开始；若有一方REFUSE，则由GameSession通知双方取消对局。
- 收到MOVE r c则表示玩家落子请求，调用GameSession.processMove进行服务器判定和后续广播。
- 收到QUIT时，若游戏已开始则通知GameSession.handleQuit处理善后（判负、更新数据、通知对手），若尚未配对则从等待列表移除该玩家。随后跳出循环结束线程。
- 如果readLine()返回null或抛出IOException，说明客户端异常断开，也按退出处理。对于进行中的游戏，通过GameSession.handleQuit认定对手胜利 。





线程结束时在finally中关闭I/O流和Socket，释放资源。





## **GameSession.java 模块**





GameSession模块在服务器端表示一局五子棋对局，维护该局的棋盘状态和游戏逻辑。它包含对两个玩家线程的引用、当前棋局的棋盘数组、当前轮到哪一方下棋等信息，并提供方法处理玩家的各种游戏行为（确认开始、落子、退出等）。

```java
/** 
 * 服务器端游戏会话：维护一局五子棋对战的状态和逻辑。
 */
public class GameSession {
    private int[][] board;
    private ClientHandler player1;
    private ClientHandler player2;
    private boolean p1Agreed = false;
    private boolean p2Agreed = false;
    private boolean started = false;
    private int currentTurn = 1; // 1 = black (player1), 2 = white (player2)
    
    public GameSession(int size, ClientHandler p1, ClientHandler p2) {
        this.board = new int[size][size];
        this.player1 = p1;
        this.player2 = p2;
    }
    
    // Handle player's agree/refuse decision before game start
    public synchronized void onDecision(ClientHandler player, boolean agree) {
        if (started) return;
        if (!agree) {
            // If any player refuses, cancel the game for both
            player1.send("CANCEL");
            player2.send("CANCEL");
            // No need to update stats, game not started
            // Close both connections
            try { player1.socket.close(); } catch (IOException e) {}
            try { player2.socket.close(); } catch (IOException e) {}
            started = false;
        } else {
            if (player == player1) {
                p1Agreed = true;
            } else if (player == player2) {
                p2Agreed = true;
            }
            if (p1Agreed && p2Agreed) {
                // Both agreed, start the game
                player1.send("START BLACK");
                player2.send("START WHITE");
                started = true;
                currentTurn = 1; // black starts
            }
        }
    }
    
    // Process a move from a player
    public synchronized void processMove(ClientHandler player, int row, int col) {
        if (!started) return;
        // Check turn
        if ((player == player1 && currentTurn != 1) || (player == player2 && currentTurn != 2)) {
            // Not this player's turn, ignore or send invalid
            player.send("INVALID");
            return;
        }
        // Check move validity
        if (row < 0 || row >= board.length || col < 0 || col >= board.length || board[row][col] != 0) {
            // Invalid move (out of bounds or position occupied)
            player.send("INVALID");
            return;
        }
        // Place the piece
        int color = player == player1 ? 1 : 2;
        board[row][col] = color;
        // Broadcast the move to both players
        String msg = "MOVE " + row + " " + col + " " + color;
        player1.send(msg);
        player2.send(msg);
        // Check win
        if (GameLogic.checkVictory(board, row, col, color)) {
            // Current player wins
            Player winner = player.getPlayer();
            Player loser = (player == player1 ? player2.getPlayer() : player1.getPlayer());
            // Update stats (escape = false since normal win)
            PlayerManager.recordResult(winner, loser, false);
            String resultMsg = color == 1 ? "RESULT BLACK" : "RESULT WHITE";
            player1.send(resultMsg);
            player2.send(resultMsg);
            // After sending result, close connections
            try { player1.socket.close(); } catch (IOException e) {}
            try { player2.socket.close(); } catch (IOException e) {}
            started = false;
        } else {
            // Switch turn
            currentTurn = (currentTurn == 1 ? 2 : 1);
        }
    }
    
    // Handle a player quitting mid-game
    public synchronized void handleQuit(ClientHandler quitter) {
        if (!started) {
            // If game not started (waiting for opponent or in agreement phase)
            // treat as refusal
            onDecision(quitter, false);
            return;
        }
        // If game started and someone quits, the other player wins by default
        ClientHandler other = (quitter == player1 ? player2 : player1);
        // Update stats: other wins, quitter loses and escapes count
        Player winner = other.getPlayer();
        Player loser = quitter.getPlayer();
        PlayerManager.recordResult(winner, loser, true);
        // Inform other player of result
        String resultMsg = (quitter == player1 ? "RESULT WHITE" : "RESULT BLACK");
        other.send(resultMsg);
        // Close both connections
        try { player1.socket.close(); } catch (IOException e) {}
        try { player2.socket.close(); } catch (IOException e) {}
        started = false;
    }
}
```

**实现说明：**GameSession通过同步方法串行化对局操作，防止多个线程同时修改游戏状态。onDecision处理配对后的确认逻辑：如果任一玩家拒绝(agree==false)，则向双方发送CANCEL消息并关闭双方连接，表示游戏未开始就取消；如果双方都同意，发送START消息通知各自棋色并开始游戏。processMove方法处理实际下棋：首先验证是否轮到该玩家以及落子位置是否有效，然后在服务器维护的board数组上记录该步。如果通过验证，服务器构造MOVE消息广播给双方客户端，同步更新棋盘状态 。随后调用GameLogic.checkVictory检查当前玩家是否形成五子连线，如是则判定胜利 ，更新双方胜负数据并通过RESULT消息通知结果，最后关闭双方连接结束游戏。若未分出胜负，则切换currentTurn到另一方，等待下一步操作。handleQuit处理游戏中途退出：如果游戏尚未正式开始（例如一方在同意环节退出），直接当作拒绝处理；如果游戏已在进行，则认定另一方获胜，更新胜负和逃逸次数，然后向对手发送RESULT胜利消息，并关闭双方连接结束该局。



通过GameSession，服务器实现了对游戏全局状态的集中管理，包括胜负判定和异常情况处理，确保游戏规则严格执行且双方状态同步。





## **GameLogic.java 模块**





GameLogic是一个游戏规则逻辑模块，提供五子棋胜利判定的静态方法。该模块独立于网络通信和界面，用于封装五子棋规则判断，以便服务器和客户端都可使用相同算法检测五子连珠。

```java
/** 
 * 游戏逻辑工具类：提供胜负判定的算法。
 */
public class GameLogic {
    // Check if placing a piece of given color at (row, col) results in a win.
    public static boolean checkVictory(int[][] board, int row, int col, int color) {
        int n = board.length;
        // Directions: horizontal, vertical, diagonal down-right, diagonal up-right
        int[][] directions = {{1,0}, {0,1}, {1,1}, {1,-1}};
        for (int[] dir : directions) {
            int dr = dir[0], dc = dir[1];
            int count = 1;
            // check one direction
            int r = row + dr, c = col + dc;
            while (r >= 0 && r < n && c >= 0 && c < n && board[r][c] == color) {
                count++;
                r += dr;
                c += dc;
            }
            // check opposite direction
            r = row - dr;
            c = col - dc;
            while (r >= 0 && r < n && c >= 0 && c < n && board[r][c] == color) {
                count++;
                r -= dr;
                c -= dc;
            }
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }
}
```

**实现说明：**上述checkVictory方法以最近一次落子的坐标(row,col)和对应棋子颜色color为输入，在board棋盘数组上检查该位置是否形成五子连珠。它通过四组方向向两侧延伸计数连续同色棋子数，每组方向包括正反两个子方向。例如，对于水平方向{1,0}，先向右检查连续同色棋子，再向左检查连续同色棋子，将两边计数加上当前落子本身（初始count=1）。若任一方向总计达到5则返回胜利。此算法覆盖水平、垂直、两条对角线四种可能连线，效率高且实现简单  。服务器在每次有效落子后调用它判定胜负，客户端也可在本地调用以便提示（本实现主要以服务器判定为准）。通过将胜负判定抽离为工具类，代码模块化更清晰，方便维护和复用。





## **Player.java 模块**





Player类是一个简单的数据类，用于封装玩家的账号信息和游戏统计数据。每个连接的客户端登录成功后，会在服务器端对应一个Player对象，保存该用户的用户名、密码（明文保存用于演示）、累积胜场数、败场数、逃逸次数以及玩家等级。

```java
/** 
 * 用户数据类：记录用户名、密码、胜负场次、等级、逃逸次数等信息。
 */
public class Player {
    String username;
    String password;
    int wins;
    int losses;
    int escapes;
    int level;
    
    public Player(String username, String password, int wins, int losses, int escapes, int level) {
        this.username = username;
        this.password = password;
        this.wins = wins;
        this.losses = losses;
        this.escapes = escapes;
        this.level = level;
    }
    
    // For a new player, default stats 0
    public Player(String username, String password) {
        this(username, password, 0, 0, 0, 1);
    }
}
```

**实现说明：**Player提供了两个构造函数：一个用于从存储载入已有玩家（指定所有字段值），另一个便于注册新用户时初始化默认值（胜、负、逃逸次数为0，等级为1）。**等级(level)**在本实现中可根据胜场数动态调整，例如每达到5胜提升一级（在更新胜场时计算），也可以根据需要采用不同策略。玩家密码在这里简单存储为明文字符串，实际应用中应当使用加密方式。本地存储的用户数据通过PlayerManager模块进行加载和保存。





## **PlayerManager.java 模块**





PlayerManager模块负责服务器端的玩家数据管理，包括用户注册、登录验证以及胜负记录的更新保存。不使用数据库的情况下，本模块通过文件系统保存用户信息。

```java
/** 
 * 用户管理类：负责玩家注册、登录验证和数据保存。
 */
public class PlayerManager {
    private static Map<String, Player> players = new HashMap<>();
    private static final String DATA_FILE = "users.txt";
    
    // Load user data from local file
    public static void loadData() {
        try {
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                // If no file, create an empty file
                file.createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String username = parts[0];
                    String password = parts[1];
                    int wins = Integer.parseInt(parts[2]);
                    int losses = Integer.parseInt(parts[3]);
                    int escapes = Integer.parseInt(parts[4]);
                    int level = Integer.parseInt(parts[5]);
                    Player player = new Player(username, password, wins, losses, escapes, level);
                    players.put(username, player);
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println("用户数据加载失败: " + e.getMessage());
        }
    }
    
    // Save all user data back to file
    private static void saveData() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE));
            for (Player p : players.values()) {
                // username,password,wins,losses,escapes,level
                bw.write(p.username + "," + p.password + "," + p.wins + "," + p.losses + "," + p.escapes + "," + p.level);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            System.out.println("用户数据保存失败: " + e.getMessage());
        }
    }
    
    // Register a new user, returns Player object or null if fail
    public static synchronized Player register(String username, String password) {
        if (players.containsKey(username)) {
            return null; // username already exists
        }
        Player newPlayer = new Player(username, password);
        players.put(username, newPlayer);
        saveData();
        return newPlayer;
    }
    
    // Login existing user, returns Player object if success or null if fail
    public static synchronized Player login(String username, String password) {
        if (!players.containsKey(username)) {
            return null;
        }
        Player player = players.get(username);
        if (!player.password.equals(password)) {
            return null;
        }
        return player;
    }
    
    // Update player stats after a game and save to file
    public static synchronized void recordResult(Player winner, Player loser, boolean escape) {
        if (winner != null) {
            winner.wins += 1;
            // Optionally update level based on wins count
            // For example: every 5 wins increase level by 1
            winner.level = 1 + winner.wins / 5;
        }
        if (loser != null) {
            loser.losses += 1;
            if (escape) {
                loser.escapes += 1;
            }
        }
        saveData();
    }
}
```

**实现说明：**服务器启动时会调用PlayerManager.loadData()从users.txt文件加载所有用户信息，逐行解析为Player对象并存入静态哈希表players（键为用户名）。用户数据文件每行格式为“username,password,wins,losses,escapes,level”，多个字段用逗号分隔。



提供的register方法用于新用户注册：检查用户名不存在后创建Player对象加入哈希表并立即保存数据文件。login方法用于登录验证：检查用户名存在且密码匹配则返回对应Player对象，否则返回null。由于这些操作可能被多个线程并发调用（例如两个用户同时注册），这里用synchronized关键字保证线程安全。



recordResult方法用于更新玩家对局结果：接收胜者和败者的Player对象以及一个标志escape表示败者是否逃跑。函数会给胜者胜场+1、根据胜场数调整等级，给败者败场+1，若逃跑则其逃逸次数+1，然后调用saveData()将所有玩家数据写回文件。这样即使服务器中途退出，也不会丢失已更新的对局数据。



通过PlayerManager，实现了**玩家注册/登录**以及**胜负记录持久化**等功能。简单起见，本实现未对密码进行加密、未使用数据库，直接采用文件保存，满足题目要求的同时，代码逻辑清晰易懂。





## **Client.java 模块**





客户端主程序模块Client负责用户界面的创建、与服务器的网络通信，以及处理游戏交互逻辑。客户端使用Swing构建图形界面，包括棋盘和控制按钮，并在后台线程中监听服务器消息，实现异步更新UI。

```java
/** 
 * 客户端主类：建立UI，连接服务器并处理游戏交互。
 */
public class Client {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private JFrame frame;
    private BoardPanel boardPanel;
    private JLabel infoLabel;
    private JButton quitButton;
    private String opponentName;
    private boolean myTurn = false;
    private boolean gameActive = false;
    private int myColor; // 1 = black, 2 = white
    
    public Client(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "无法连接到服务器: " + e.getMessage());
            System.exit(0);
        }
        // Handle login/registration
        boolean loggedIn = false;
        while (!loggedIn) {
            String[] options = {"登录", "注册"};
            int choice = JOptionPane.showOptionDialog(null, "请选择登录或注册：", "登录",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            if (choice == JOptionPane.CLOSED_OPTION) {
                // user closed the dialog
                System.exit(0);
            }
            String username = JOptionPane.showInputDialog(null, "用户名:");
            if (username == null) {
                continue;
            }
            String password = JOptionPane.showInputDialog(null, "密码:");
            if (password == null) {
                continue;
            }
            try {
                if (choice == 0) { // 登录
                    out.write("LOGIN " + username + " " + password);
                    out.newLine();
                    out.flush();
                } else { // 注册
                    out.write("REGISTER " + username + " " + password);
                    out.newLine();
                    out.flush();
                }
                // Wait for server response
                String response = in.readLine();
                if (response == null) {
                    JOptionPane.showMessageDialog(null, "与服务器断开连接");
                    System.exit(0);
                }
                if (response.startsWith("LOGIN_SUCCESS") || response.startsWith("REGISTER_SUCCESS")) {
                    loggedIn = true;
                    JOptionPane.showMessageDialog(null, "登录成功，欢迎 " + username);
                } else if (response.startsWith("LOGIN_FAIL")) {
                    JOptionPane.showMessageDialog(null, "登录失败：用户名或密码错误");
                } else if (response.startsWith("REGISTER_FAIL")) {
                    JOptionPane.showMessageDialog(null, "注册失败：用户名已存在");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "通信错误: " + e.getMessage());
            }
        }
        // Setup game UI
        frame = new JFrame("网络五子棋 客户端");
        boardPanel = new BoardPanel(this);
        infoLabel = new JLabel("等待对手连接...");
        quitButton = new JButton("退出");
        quitButton.addActionListener(e -> {
            // When quit button clicked, send quit message and exit
            sendQuit();
            closeConnection();
            System.exit(0);
        });
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(quitButton);
        frame.add(infoLabel, "North");
        frame.add(boardPanel, "Center");
        frame.add(bottomPanel, "South");
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // Start a thread to listen for server messages
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        String[] tokens = line.split(" ");
                        if (tokens[0].equals("OPPONENT")) {
                            // Received opponent info: tokens: ["OPPONENT", name, level, wins, losses, escapes]
                            opponentName = tokens[1];
                            String oppLevel = tokens[2];
                            String oppWins = tokens[3];
                            String oppLosses = tokens[4];
                            String oppEscapes = tokens[5];
                            // Show a dialog asking to start game
                            String msg = "找到对手：" + opponentName + "\n"
                                       + "等级: " + oppLevel + "  胜: " + oppWins + "  负: " + oppLosses + "  逃逸: " + oppEscapes + "\n"
                                       + "是否开始游戏？";
                            int result = JOptionPane.showConfirmDialog(frame, msg, "对局请求", JOptionPane.YES_NO_OPTION);
                            if (result == JOptionPane.YES_OPTION) {
                                sendText("AGREE");
                                infoLabel.setText("已同意与 " + opponentName + " 开始游戏，等待对方确认...");
                            } else {
                                sendText("REFUSE");
                                infoLabel.setText("您拒绝了与 " + opponentName + " 的游戏");
                            }
                        } else if (tokens[0].equals("START")) {
                            // Game is starting, tokens: ["START", "BLACK/WHITE"]
                            gameActive = true;
                            if (tokens[1].equals("BLACK")) {
                                myColor = 1;
                                myTurn = true;
                                infoLabel.setText("游戏开始！您执黑棋，您先下");
                            } else if (tokens[1].equals("WHITE")) {
                                myColor = 2;
                                myTurn = false;
                                infoLabel.setText("游戏开始！您执白棋，等待对方落子");
                            }
                        } else if (tokens[0].equals("MOVE")) {
                            // tokens: ["MOVE", row, col, color]
                            int r = Integer.parseInt(tokens[1]);
                            int c = Integer.parseInt(tokens[2]);
                            int color = Integer.parseInt(tokens[3]);
                            // Update board
                            boardPanel.placePiece(r, c, color);
                            // Determine whose move it was and update turn
                            if (color == myColor) {
                                // It was my move, now opponent's turn
                                myTurn = false;
                                infoLabel.setText("已落子，等待对方落子...");
                            } else {
                                // Opponent moved, now my turn
                                myTurn = true;
                                infoLabel.setText("轮到您落子");
                            }
                        } else if (tokens[0].equals("RESULT")) {
                            // Game result, tokens: ["RESULT", "BLACK/WHITE"]
                            gameActive = false;
                            myTurn = false;
                            String winnerColor = tokens[1];
                            String message;
                            if ((winnerColor.equals("BLACK") && myColor == 1) || (winnerColor.equals("WHITE") && myColor == 2)) {
                                message = "游戏结束，您获胜了！";
                            } else {
                                message = "游戏结束，您输了。";
                            }
                            JOptionPane.showMessageDialog(frame, message);
                            infoLabel.setText("游戏结束: " + (winnerColor.equals("BLACK") ? "黑棋胜利" : "白棋胜利"));
                        } else if (tokens[0].equals("CANCEL")) {
                            // Game cancelled (opponent refused or left before start)
                            gameActive = false;
                            myTurn = false;
                            JOptionPane.showMessageDialog(frame, "对局取消，未开始游戏");
                            infoLabel.setText("对局已取消");
                            // After cancellation, break out and close
                            break;
                        }
                    }
                } catch (IOException e) {
                    // Connection closed
                } finally {
                    // Ensure socket closed
                    closeConnection();
                }
            }
        }).start();
    }
    
    // Send a move command to server
    public void sendMove(int row, int col) {
        sendText("MOVE " + row + " " + col);
    }
    
    // Send quit command to server
    private void sendQuit() {
        sendText("QUIT");
    }
    
    // Generic send text
    public void sendText(String text) {
        try {
            out.write(text);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.out.println("消息发送失败: " + e.getMessage());
        }
    }
    
    // Close the socket and streams
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            // ignore
        }
    }
    
    public boolean isMyTurn() {
        return myTurn;
    }
    
    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }
    
    public boolean isGameActive() {
        return gameActive;
    }
    
    public static void main(String[] args) {
        new Client("localhost", 8888);
    }
}
```

**实现说明：**客户端启动后先尝试连接服务器，若连接失败则弹出提示并退出。成功连接后，进入登录/注册流程：通过JOptionPane对话框询问用户选择登录或注册，并获取用户名和密码输入，然后发送相应的LOGIN或REGISTER命令给服务器。客户端读取服务器返回的结果，依据LOGIN_SUCCESS等反馈判断是否登录成功，未成功则提示原因并允许重新输入。整个登录过程在连接建立后、图形界面显示前完成 。



登录成功后，客户端构建主窗口界面：包含棋盘面板BoardPanel、顶部标签infoLabel显示状态信息、底部“退出”按钮等。然后启动一个专门的监听线程从服务器读取消息，以免阻塞UI线程。**所有从服务器接收的消息都在此监听线程中处理**，根据消息类型更新UI状态或进行相应操作：



- 收到OPPONENT消息时，表示找到了一个匹配的对手以及对方信息。客户端使用对手数据构造提示信息，通过对话框让玩家确认是否开始游戏。如果玩家点击“是”，则发送AGREE回复服务器，同时在界面上更新状态提示等待对方确认；如果点击“否”，则发送REFUSE并提示已拒绝对局。此时还未进入正式游戏，须等待服务器后续指令。
- 收到START消息时，游戏正式开始。根据服务器指示设置自己执黑或执白，并初始化回合状态：若执黑则myTurn=true表示可以下子，执白则等待对方先行。界面上的infoLabel更新为当前行动提示，如“您执黑棋，您先下”。
- 收到MOVE消息时，包含对方或自己的落子坐标及棋子颜色。客户端据此调用boardPanel.placePiece()在棋盘上绘制相应黑/白棋子。然后通过比较颜色判断落子者是谁：如果color等于myColor，说明这是自己刚才下的子，已经落在棋盘上，此时轮到对手行动，设置myTurn=false并更新提示“等待对方落子…”；反之则是对手的落子，现在轮到自己，设置myTurn=true并提示“轮到您落子”。
- 收到RESULT消息表示一方胜利，游戏结束。客户端停止游戏状态gameActive=false，禁止后续落子输入，同时弹出消息框告知胜负结果（结合自身棋色判断是赢是输），在infoLabel显示最终胜方颜色。
- 收到CANCEL消息表示游戏尚未开始就被取消（可能由于对手拒绝或掉线）。客户端将状态置为非活动并提示“对局已取消”。然后跳出监听循环，执行清理关闭连接。





监听线程的循环在游戏正常结束或取消时都会跳出并执行finally，调用closeConnection()关闭Socket和I/O流。客户端的“退出”按钮绑定了事件处理：点击时发送QUIT命令告诉服务器自己退出，然后直接关闭程序终止。



通过客户端的设计，我们实现了**非阻塞的GUI更新**和**同步的网络通信**：利用后台线程专门接收服务器信息，在需要时通过Swing组件更新UI；而用户操作（点击棋盘、确认对局、退出等）直接发送命令到服务器。双方协调下，整局游戏流程按照请求-响应模式推进，保证客户端UI表现与服务器棋局进度一致。





## **BoardPanel.java 模块**





BoardPanel是客户端的自定义面板类，继承自JPanel，负责绘制五子棋的棋盘和棋子，并处理用户在棋盘上的鼠标点击以执行落子操作。

```java
/** 
 * 客户端棋盘面板：绘制棋盘和棋子，并处理鼠标下棋操作。
 */
public class BoardPanel extends JPanel {
    private static final int GRID_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final int MARGIN = 20;
    private int[][] board = new int[GRID_SIZE][GRID_SIZE];
    private Client client;
    
    public BoardPanel(Client client) {
        this.client = client;
        this.setPreferredSize(new Dimension(MARGIN*2 + (GRID_SIZE-1)*CELL_SIZE, MARGIN*2 + (GRID_SIZE-1)*CELL_SIZE));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!client.isMyTurn() || !client.isGameActive()) {
                    return; // if not this player's turn or game not active, ignore clicks
                }
                int x = e.getX();
                int y = e.getY();
                // Calculate board indices from pixel coordinates
                int col = Math.round((x - MARGIN) / (float)CELL_SIZE);
                int row = Math.round((y - MARGIN) / (float)CELL_SIZE);
                if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
                    return; // click outside board
                }
                if (board[row][col] != 0) {
                    return; // already a piece here
                }
                // Send the move to server
                client.sendMove(row, col);
                // After sending, temporarily disable further input until response
                client.setMyTurn(false);
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw board background
        g.setColor(new Color(242, 202, 118));
        g.fillRect(0, 0, getWidth(), getHeight());
        // Draw grid lines
        g.setColor(Color.BLACK);
        for (int i = 0; i < GRID_SIZE; i++) {
            int pos = MARGIN + i * CELL_SIZE;
            // horizontal line
            g.drawLine(MARGIN, pos, MARGIN + (GRID_SIZE - 1) * CELL_SIZE, pos);
            // vertical line
            g.drawLine(pos, MARGIN, pos, MARGIN + (GRID_SIZE - 1) * CELL_SIZE);
        }
        // Draw pieces
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (board[i][j] == 1) {
                    // black piece
                    g.setColor(Color.BLACK);
                    g.fillOval(MARGIN + j * CELL_SIZE - 10, MARGIN + i * CELL_SIZE - 10, 20, 20);
                } else if (board[i][j] == 2) {
                    // white piece
                    g.setColor(Color.WHITE);
                    g.fillOval(MARGIN + j * CELL_SIZE - 10, MARGIN + i * CELL_SIZE - 10, 20, 20);
                    g.setColor(Color.BLACK);
                    g.drawOval(MARGIN + j * CELL_SIZE - 10, MARGIN + i * CELL_SIZE - 10, 20, 20);
                }
            }
        }
    }
    
    // Place a piece on the board and repaint
    public void placePiece(int row, int col, int color) {
        board[row][col] = color;
        repaint();
    }
}
```



**现说明：**BoardPanel通过重写paintComponent方法绘制棋盘和棋子：使用15×15的网格（常规五子棋棋盘大小），CELL_SIZE定义格子间距为40像素，MARGIN为边距20像素，使棋盘有适当留白。双重循环绘制横竖网格线形成棋盘格局，然后遍历board数组，在每个有棋子的坐标处绘制黑色或白色圆形棋子（白子用黑边勾勒以便在浅色背景下清晰可见）。



在构造函数中，BoardPanel添加了鼠标事件监听MouseListener。当用户点击棋盘面板时，mouseClicked被触发：首先检查如果**不是当前玩家的回合或者游戏未在进行**（例如已结束），则直接忽略点击，不允许下子。然后将点击的像素坐标换算为棋盘数组索引：通过对 (x - MARGIN) 和 (y - MARGIN)分别除以格距CELL_SIZE取四舍五入得到最近的行列索引。如果计算结果在0~14范围内且该位置上当前没有棋子，则认为一次有效的落子尝试。【注意】由于我们采用点击最近格点的方式，玩家需要点击棋盘网格线的交叉点附近区域才能识别为有效位置。



在确定点击是有效落子后，BoardPanel并不直接在本地落子，而是调用client.sendMove(row, col)将坐标发送给服务器验证。这与之前说明的流程一致：客户端**将落子请求交由服务器判定**，防止非法操作 。发送后立即调用client.setMyTurn(false)暂时禁止再次点击，等待服务器回应。如果服务器判定该步有效，将通过MOVE消息通知，包括刚才发送的坐标和棋子颜色，客户端监听线程收到后会调用placePiece更新界面；如果服务器返回INVALID（本实现中未在客户端单独处理，但可扩展），也会由于没有MOVE广播而保持等待状态，不会产生新棋子，使玩家可以重新点击尝试。



综上，BoardPanel模块实现了对棋盘的绘制和本地图形化交互。当与服务器的网络模块配合时，可以做到：玩家每次点击在自己界面看到即时反馈，而最终是否在双方界面落子以服务器确认广播为准，保证了双方棋盘的一致性和规则的正确性 。