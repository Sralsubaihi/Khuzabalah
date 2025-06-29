package network;

/**
 *
 * @author Srals
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NewServer {
    private static List<NewClinet> connectedPlayers = new ArrayList<>();
    private static List<NewClinet> waitingRoom = new ArrayList<>();
    private static final int MAX_PLAYERS_PER_ROOM = 4;
    private static List<List<NewClinet>> gameRooms = new ArrayList<>();
private static Thread gameTimerThread; // لمراقبة مؤقت اللعبة

    private static final List<String> questions = List.of(
         "الفيلة تخاف من النمل أكثر من الفئران.",
        "الطيور تنام أثناء الطيران أحياناً.",
        "الإنسان يمكنه العيش بدون نوم لمدة شهر.",
        "السمك يضحك إذا دغدغته.",
        "الحلزون يمكنه النوم لمدة 3 سنوات متواصلة."
    );
    private static final boolean[] answers = {true, true, false, false, true};

    private static int currentQuestionIndex = 0;
    private static List<NewClinet> currentRoom;
    private static boolean questionAnswered = false;
    private static int[] scores;
    private static int answersReceived = 0;
    private static long[] answerTimes;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);


        System.out.println("Server started...");

        while (true) {
            System.out.println("Waiting for client connection...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connected to client");

            NewClinet clientThread = new NewClinet(clientSocket);
            handlePlayerConnection(clientThread);
        }
    }

    public static synchronized void handlePlayerConnection(NewClinet client) {
        connectedPlayers.add(client);
        new Thread(client).start();
        client.sendMessage("CONNECTED " + client.getUsername());
        sendConnectedPlayersListToAll();
    }

    public static synchronized void handlePlayRequest(NewClinet client) {
        for (List<NewClinet> room : gameRooms) {
            if (room.size() < MAX_PLAYERS_PER_ROOM) {
                room.add(client);
                sendWaitingRoomListToAll(room);

                if (room.size() == 2) {
                    startCountdownTimer(room);
                }
                return;
            }
        }

        List<NewClinet> newRoom = new ArrayList<>();
        newRoom.add(client);
        gameRooms.add(newRoom);
        sendWaitingRoomListToAll(newRoom);
    }

    private static void startCountdownTimer(List<NewClinet> room) {
        new Thread(() -> {
            try {
                while (room.size() > 2) {
                    Thread.sleep(1000);
                }
                for (int i = 10; i >= 0; i--) {
                    updateWaitingRoomTimer(room, i);

                    if (room.size() == MAX_PLAYERS_PER_ROOM) {
                        startGameForRoom(room);
                        return;
                    }

                    Thread.sleep(1000);
                }

                if (room.size() >= 2) {
                    startGameForRoom(room);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void updateWaitingRoomTimer(List<NewClinet> room, int secondsLeft) {
        for (NewClinet player : room) {
            player.sendMessage("TIMER: " + secondsLeft);
        }
    }

    private static void startGameForRoom(List<NewClinet> room) {
        StringBuilder sb = new StringBuilder("START_GAME_WITH: ");
        for (NewClinet player : room) {
            sb.append(player.getUsername()).append(",");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);

        String startGameMessage = sb.toString();

        for (NewClinet player : room) {
            player.sendMessage(startGameMessage);
        }

        System.out.println("تم بدء اللعبة لغرفة فيها " + room.size() + " لاعبين!");
        sendQuestionsToPlayers(room);
        startGameTimer();
        gameRooms.remove(room);
    }
    
    private static void startGameTimer() {
    gameTimerThread = new Thread(() -> {
        try {
            int timeLeft = 45;
            while (timeLeft > 0) {
                Thread.sleep(1000);
                timeLeft--;

                for (NewClinet player : currentRoom) {
                    player.sendMessage("GAME_TIMER: " + timeLeft);
                }
            }
            System.out.println("⏰ انتهى وقت اللعبة!");
            endGame();
        } catch (InterruptedException e) {
            System.out.println("تم إيقاف مؤقت اللعبة (بسبب انتهاء الأسئلة).");
        }
    });
    gameTimerThread.start();
}



    private static void sendQuestionsToPlayers(List<NewClinet> room) {
        currentRoom = room;
        scores = new int[room.size()];
        answerTimes = new long[room.size()];
        currentQuestionIndex = 0;
        sendQuestionToRoom();
    }

    private static void sendQuestionToRoom() {
        if (currentQuestionIndex >= questions.size()) {
            if (gameTimerThread != null) gameTimerThread.interrupt(); // نوقف العداد إذا خلصت الأسئلة
            endGame();
            return;
        }

        questionAnswered = false;
        answersReceived = 0;
        answerTimes = new long[currentRoom.size()];

        String q = questions.get(currentQuestionIndex);
        for (NewClinet player : currentRoom) {
            player.hasAnswered = false;
            player.sendMessage("QUESTION: " + q);
        }
    }

    public static void handleAnswer(NewClinet player, String answer) {
        if (player.hasAnswered) return;

        player.hasAnswered = true;
        boolean playerAnswer = Boolean.parseBoolean(answer);
        boolean correctAnswer = answers[currentQuestionIndex];
        int index = currentRoom.indexOf(player);

        if (index != -1 && playerAnswer == correctAnswer) {
            answerTimes[index] = System.currentTimeMillis();
        } else {
            answerTimes[index] = Long.MAX_VALUE; // تجاهل الخطأ
        }

        boolean allAnswered = true;
        for (NewClinet p : currentRoom) {
            if (!p.hasAnswered) {
                allAnswered = false;
                break;
            }
        }

        if (allAnswered) {
            int minIndex = -1;
            long minTime = Long.MAX_VALUE;

            for (int i = 0; i < answerTimes.length; i++) {
                if (answerTimes[i] < minTime) {
                    minTime = answerTimes[i];
                    minIndex = i;
                }
            }

            if (minIndex != -1) {
                scores[minIndex]++;
            }

            sendScoresToRoom();
            currentQuestionIndex++;

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    for (NewClinet p : currentRoom) {
                        p.hasAnswered = false;
                    }
                    sendQuestionToRoom();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static void sendScoresToRoom() {
        StringBuilder sb = new StringBuilder("SCORE_UPDATE: ");
        for (int i = 0; i < currentRoom.size(); i++) {
            sb.append(currentRoom.get(i).getUsername()).append(" - ").append(scores[i]).append(",");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);

        for (NewClinet player : currentRoom) {
            player.sendMessage(sb.toString());
        }
    }

    private static void endGame() {
    int max = -1;
    List<String> winners = new ArrayList<>();

    for (int i = 0; i < scores.length; i++) {
        if (scores[i] > max) {
            max = scores[i];
            winners.clear();
            winners.add(currentRoom.get(i).getUsername());
        } else if (scores[i] == max && max > 0) {
            winners.add(currentRoom.get(i).getUsername());
        }
    }

    String message;
    if (max <= 0 || winners.size() > 1) {
        message = "NO_WINNER";
    } else {
        message = "WINNER: " + winners.get(0);
    }

    for (NewClinet player : currentRoom) {
        player.sendMessage(message);
    }

    currentRoom = null;
}


    public static synchronized void sendConnectedPlayersListToAll() {
        StringBuilder sb = new StringBuilder("PLAYERS: ");
        for (NewClinet player : connectedPlayers) {
            sb.append(player.getUsername()).append(",");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        String msg = sb.toString();
        for (NewClinet player : connectedPlayers) {
            player.sendMessage(msg);
        }
    }


    private static void sendWaitingRoomListToAll(List<NewClinet> room) {
        StringBuilder sb = new StringBuilder("WAITING ROOM: ");
        for (NewClinet player : room) {
            sb.append(player.getUsername()).append(",");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        String msg = sb.toString();
        for (NewClinet player : room) {
            player.sendMessage(msg);
        }
    }




  

    public static synchronized void removePlayer(NewClinet client) {
    connectedPlayers.remove(client);



    // نحذف اللاعب من كل الغرف
    List<List<NewClinet>> roomsToRemove = new ArrayList<>();
    for (List<NewClinet> room : gameRooms) {
        room.remove(client);
        if (room.isEmpty()) {
            roomsToRemove.add(room);
        } else {
            sendWaitingRoomListToAll(room);
        }
    }
    gameRooms.removeAll(roomsToRemove);

    //  نحذف اللاعب من الغرفة الحالية (اللعبة)
    if (currentRoom != null && currentRoom.contains(client)) {
        currentRoom.remove(client);

        //  لو بقي لاعب واحد فقط، نعلنه فائز
        if (currentRoom.size() == 1) {
            NewClinet lastPlayer = currentRoom.get(0);
            lastPlayer.sendMessage("WINNER: " + lastPlayer.getUsername());
            currentRoom = null; // ننهي الغرفة
        }
    }

    // نخبر الجميع أن لاعب خرج
    notifyAllPlayers("Player left: " + client.getUsername());

    // نرسل تحديث لقائمة اللاعبين
    sendConnectedPlayersListToAll();
}



    public static void notifyAllPlayers(String message) {
        for (NewClinet player : connectedPlayers) {
            player.sendMessage(message);
        }
    }
}
