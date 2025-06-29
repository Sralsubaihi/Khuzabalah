package network;



import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;


public class Client {
    private static final String SERVER_IP = "10.6.196.199";
    private static final int SERVER_PORT = 8000;
    
    private JFrame startFrame, loginFrame, playersFrame, waitingRoomFrame, gameFrame;
    private DefaultListModel<String> playerListModel, waitingRoomListModel, scoreModel;
    private JList<String> playerList, waitingRoomList, scoreList;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
private JLabel waitingRoomTimerLabel;
private JLabel gameTimerLabel;

  private JLabel questionLabel;
private JPanel answerButtonsPanel;


    public Client() {
        showStartWindow();
    }
//start gui
    
    
    
    // نافذة البداية
    private void showStartWindow() {
    startFrame = new JFrame("ابدأ اللعبة");
    startFrame.setSize(800, 600); // حجم مناسب للصورة
    startFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    startFrame.setLocationRelativeTo(null); // توسيط النافذة

    // تحميل صورة الخلفية
    ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("start.png"));
    JLabel backgroundLabel = new JLabel(backgroundIcon);
    backgroundLabel.setLayout(null); // نتحكم يدويًا بالمكونات

    // النص الترحيبي "مرحبًا بك في لعبة خُزعبَلَة!"
    JLabel welcomeLabel = new JLabel("مرحبًا بك في لعبة خُزعبَلَة!", SwingConstants.CENTER);
    welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
    welcomeLabel.setForeground(Color.darkGray);
    welcomeLabel.setBounds(250, 220, 300, 30); // المكان بالمنتصف تقريبًا

    // زر "ابدأ" أسفل النص، صغير، كحلي، في المنتصف
    JButton startButton = new JButton("ابدأ");
    startButton.setFont(new Font("Arial", Font.BOLD, 16));
    startButton.setBackground(new Color(10, 30, 60)); // كحلي
    startButton.setForeground(Color.WHITE);
    startButton.setFocusPainted(false);
    startButton.setBounds(360, 280, 80, 35); // أسفل النص مباشرة، صغير ومُنتصف

    startButton.addActionListener(e -> {
        startFrame.dispose();
        showLoginWindow();
    });

    // إضافة العناصر إلى الخلفية
    backgroundLabel.add(welcomeLabel);
    backgroundLabel.add(startButton);

    // وضع الخلفية كـ ContentPane
    startFrame.setContentPane(backgroundLabel);
    startFrame.setVisible(true);
}


    // نافذة تسجيل الدخول
    private void showLoginWindow() {
    loginFrame = new JFrame("تسجيل الدخول");
    loginFrame.setSize(800, 600);
    loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    loginFrame.setLocationRelativeTo(null); // توسيط النافذة

    // صورة الخلفية
    ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("gamee.png"));
    JLabel backgroundLabel = new JLabel(backgroundIcon);
    backgroundLabel.setLayout(null); // نتحكم يدويًا في أماكن العناصر

    // العنوان: "أدخل اسمك" في الأعلى
    JLabel nameLabel = new JLabel("أدخل اسمك:", SwingConstants.CENTER);
    nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
    nameLabel.setForeground(Color.DARK_GRAY);
    nameLabel.setBounds(275, 170, 250, 40); // أعلى منتصف النافذة

    // حقل إدخال الاسم أسفل العنوان
    JTextField nameField = new JTextField();
    nameField.setFont(new Font("Arial", Font.PLAIN, 18));
    nameField.setBounds(250, 220, 300, 40); // أسفل العنوان مباشرة

    // زر "اتصل"
    JButton connectButton = new JButton("اتصل");
    connectButton.setFont(new Font("Arial", Font.BOLD, 18));
    connectButton.setBackground(new Color(10, 30, 60)); // كحلي
    connectButton.setForeground(Color.WHITE);
    connectButton.setFocusPainted(false);
    connectButton.setBounds(350, 300, 100, 40); // أسفل خانة الاسم

    connectButton.addActionListener(e -> {
        username = nameField.getText().trim();
        if (!username.isEmpty()) {
            loginFrame.dispose();
            connectToServer();
            showPlayersWindow();
        }
    });

    // إضافة كل شيء إلى الخلفية
    backgroundLabel.add(nameLabel);
    backgroundLabel.add(nameField);
    backgroundLabel.add(connectButton);

    loginFrame.setContentPane(backgroundLabel);
    loginFrame.setVisible(true);
}


    // الاتصال بالسيرفر
    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // إرسال اسم اللاعب للسيرفر
            out.println(username);
            
            // بدء Thread للاستماع لرسائل السيرفر
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        processMessage(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("تم قطع الاتصال بالخادم.");
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "فشل الاتصال بالخادم!", "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    // نافذة اللاعبين المتصلين
    private void showPlayersWindow() {
    playersFrame = new JFrame("اللاعبين المتصلين");
    playersFrame.setSize(800, 600);
    playersFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    playersFrame.setLocationRelativeTo(null); // توسيط النافذة

    // صورة الخلفية
    ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("gamee.png"));
    JLabel backgroundLabel = new JLabel(backgroundIcon);
    backgroundLabel.setLayout(null); // نتحكم يدويًا في أماكن العناصر

    // قائمة اللاعبين
    playerListModel = new DefaultListModel<>();
    playerList = new JList<>(playerListModel);
    playerList.setFont(new Font("Arial", Font.BOLD, 24));
    playerList.setOpaque(false); // شفافية
    playerList.setForeground(Color.WHITE);
    playerList.setBackground(new Color(0, 0, 0, 0));

    // تنسيق أسماء القائمة لتكون في المنتصف
    playerList.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 24));
            label.setOpaque(false);
            return label;
        }
    });

    JScrollPane playersScrollPane = new JScrollPane(playerList);
    playersScrollPane.setBounds(250, 140, 300, 300); // وسط الشاشة تقريبًا
    playersScrollPane.setOpaque(false);
    playersScrollPane.getViewport().setOpaque(false);
    playersScrollPane.setBorder(null);

    // زر "ابدأ اللعب"
    JButton playButton = new JButton("ابدأ اللعب");
    playButton.setFont(new Font("Arial", Font.BOLD, 18));
    playButton.setBackground(new Color(10, 30, 60)); // كحلي
    playButton.setForeground(Color.WHITE);
    playButton.setFocusPainted(false);
    playButton.setBounds(325, 460, 150, 40); // تحت القائمة

    playButton.addActionListener(e -> {
        sendPlayRequest();
        playersFrame.dispose();
        showWaitingRoom();
    });

    // إضافة العناصر للخلفية
    backgroundLabel.add(playersScrollPane);
    backgroundLabel.add(playButton);

    playersFrame.setContentPane(backgroundLabel);
    playersFrame.setVisible(true);
}



    // نافذة غرفة الانتظار
   private void showWaitingRoom() {
    waitingRoomFrame = new JFrame("غرفة الانتظار");
    waitingRoomFrame.setSize(800, 600);
    waitingRoomFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    waitingRoomFrame.setLocationRelativeTo(null);

    // صورة الخلفية
    ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("gamee.png"));
    JLabel backgroundLabel = new JLabel(backgroundIcon);
    backgroundLabel.setLayout(null); // تحكم يدوي

    // العداد العلوي
    waitingRoomTimerLabel = new JLabel(" انتظر قليلا...", SwingConstants.LEFT);
    waitingRoomTimerLabel.setFont(new Font("Arial", Font.BOLD, 18));
    waitingRoomTimerLabel.setForeground(Color.WHITE);
    waitingRoomTimerLabel.setBounds(5, 5, 200, 50); // منتصف الشاشة من الأعلى

    // قائمة الانتظار
    waitingRoomListModel = new DefaultListModel<>();
    waitingRoomList = new JList<>(waitingRoomListModel);
    waitingRoomList.setFont(new Font("Arial", Font.BOLD, 24));
    waitingRoomList.setOpaque(false);
    waitingRoomList.setForeground(Color.WHITE);
    waitingRoomList.setBackground(new Color(0, 0, 0, 0));

    // تنسيق عرض الأسماء بالمنتصف
    waitingRoomList.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground(Color.DARK_GRAY);
            label.setFont(new Font("Arial", Font.BOLD, 24));
            label.setOpaque(false);
            return label;
        }
    });

    JScrollPane waitingScrollPane = new JScrollPane(waitingRoomList);
    waitingScrollPane.setBounds(250, 140, 300, 300);
    waitingScrollPane.setOpaque(false);
    waitingScrollPane.getViewport().setOpaque(false);
    waitingScrollPane.setBorder(null);

    // إضافة المكونات
    backgroundLabel.add(waitingRoomTimerLabel);
    backgroundLabel.add(waitingScrollPane);

    waitingRoomFrame.setContentPane(backgroundLabel);
    waitingRoomFrame.setVisible(true);
}

   


    // إرسال الإجابة إلى السيرفر
    private void sendAnswer(boolean isCorrect) {
        if (out != null) {
            out.println("ANSWER: " + (isCorrect ? "true" : "false"));
        }
    }

    // بدء نافذة اللعبة
  
private void startGameWindow(String[] players) {
    gameFrame = new JFrame("🎮 اللعبة");
    gameFrame.setSize(800, 600);
    gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    gameFrame.setLocationRelativeTo(null);

    // ====== خلفية اللعبة ======
    ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("gamee.png"));
    JLabel backgroundLabel = new JLabel(backgroundIcon);
    backgroundLabel.setLayout(null); // تصميم يدوي

    // ====== عداد الوقت أعلى يسار ======
    gameTimerLabel = new JLabel("الوقت المتبقي: 45 ثانية");
    gameTimerLabel.setFont(new Font("Arial", Font.BOLD, 16));
    gameTimerLabel.setForeground(Color.WHITE);
    gameTimerLabel.setBounds(10, 10, 250, 40); // أعلى اليسار

    // ====== قائمة السكور أعلى يمين ======
    scoreModel = new DefaultListModel<>();
    scoreList = new JList<>(scoreModel);
    scoreList.setFont(new Font("Arial", Font.BOLD, 18));
    scoreList.setForeground(Color.WHITE);
    scoreList.setOpaque(false);
    scoreList.setBackground(new Color(0, 0, 0, 0));
    scoreList.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 18));
            label.setOpaque(false);
            return label;
        }
    });

    JScrollPane scoreScrollPane = new JScrollPane(scoreList);
    scoreScrollPane.setBounds(580, 10, 200, 200);
    scoreScrollPane.setOpaque(false);
    scoreScrollPane.getViewport().setOpaque(false);
    scoreScrollPane.setBorder(null);

    for (String player : players) {
        scoreModel.addElement(player + " - 0");
    }

    // ====== السؤال في المنتصف ======
    questionLabel = new JLabel("بانتظار السؤال...", SwingConstants.CENTER);
    questionLabel.setFont(new Font("Arial", Font.BOLD, 22));
    questionLabel.setForeground(Color.DARK_GRAY);
    questionLabel.setBounds(100, 230, 600, 40);

    // ====== أزرار الإجابة ======
    JButton trueButton = new JButton("✔ صح");
    styleButton(trueButton, new Color(255, 215, 0));
    trueButton.setBounds(260, 290, 80, 40);

    JButton falseButton = new JButton("✖ خطأ");
    styleButton(falseButton, new Color(176, 127, 255));
    falseButton.setBounds(410, 290, 80, 40);

    trueButton.addActionListener(e -> {
        sendAnswer(true);
        disableAnswerButtons();
    });

    falseButton.addActionListener(e -> {
        sendAnswer(false);
        disableAnswerButtons();
    });

    // ====== زر الخروج ======
    JButton leaveButton = new JButton("خروج");
    leaveButton.setFont(new Font("Arial", Font.BOLD, 16));
    leaveButton.setBackground(Color.DARK_GRAY);
    leaveButton.setForeground(Color.WHITE);
    leaveButton.setFocusPainted(false);
    leaveButton.setBounds(350, 360, 100, 40);

    leaveButton.addActionListener(e -> sendLeaveRequest());

    // ====== إضافات إلى الخلفية ======
    backgroundLabel.add(gameTimerLabel);
    backgroundLabel.add(scoreScrollPane);
    backgroundLabel.add(questionLabel);
    backgroundLabel.add(trueButton);
    backgroundLabel.add(falseButton);
    backgroundLabel.add(leaveButton);

    if (waitingRoomFrame != null) waitingRoomFrame.dispose();

    gameFrame.setContentPane(backgroundLabel);
    gameFrame.setVisible(true);
}




    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

  
  
    // تحديث النقاط
    private void updateScores(String[] scores) {
        SwingUtilities.invokeLater(() -> {
            scoreModel.clear();
            for (String s : scores) {
                scoreModel.addElement(s);
            }
        });
    }

    //end gui 
    
    
    
    
    
    
    
    
    // معالجة الرسائل الواردة من السيرفر
    private void processMessage(String message) {
    System.out.println("Received from server: " + message);

    if (message.startsWith("PLAYERS:")) {
        String players = message.substring(8).trim();
        updatePlayersList(players);
    } else if (message.startsWith("WAITING ROOM:")) {
        String players = message.substring(13).trim();
        updateWaitingRoomList(players);
    } else if (message.startsWith("START_GAME_WITH:")) {
        String playersStr = message.substring("START_GAME_WITH:".length()).trim();
        String[] players = playersStr.split(",");
        startGameWindow(players);
    } else if (message.startsWith("QUESTION:")) {
        String question = message.substring("QUESTION:".length()).trim();
        SwingUtilities.invokeLater(() -> {
            questionLabel.setText(question);
            enableAnswerButtons();
        });
    } else if (message.startsWith("SCORE_UPDATE:")) {
        String[] scores = message.substring("SCORE_UPDATE:".length()).trim().split(",");
        updateScores(scores);




    } else if (message.startsWith("WINNER:")) {
    String winner = message.substring(7).trim();
    SwingUtilities.invokeLater(() -> {
        if(gameFrame != null) gameFrame.dispose();
        JFrame winnerFrame = new JFrame("الفائز!");
        winnerFrame.setSize(800, 600);
        winnerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        winnerFrame.setLocationRelativeTo(null);

        // الخلفية
        ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("gamee.png"));
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setLayout(null);

        // نص الفائز
        JLabel winnerLabel = new JLabel("الفائز هو: " + winner, SwingConstants.CENTER);
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 32));
        winnerLabel.setForeground(new Color(255, 215, 0)); // ذهبي
        winnerLabel.setBounds(200, 200, 400, 50);

        // زر الخروج
        JButton leaveButton = new JButton("خروج");
        leaveButton.setFont(new Font("Arial", Font.BOLD, 16));
        leaveButton.setBackground(new Color(30, 30, 60)); // كحلي غامق
        leaveButton.setForeground(Color.WHITE);
        leaveButton.setFocusPainted(false);
        leaveButton.setBounds(350, 300, 100, 40);
        leaveButton.addActionListener(e -> sendLeaveRequest());

        backgroundLabel.add(winnerLabel);
        backgroundLabel.add(leaveButton);

        winnerFrame.setContentPane(backgroundLabel);
        winnerFrame.setVisible(true);
    });
} else if (message.startsWith("WINNER:")) {
    String winner = message.substring(7).trim();
    SwingUtilities.invokeLater(() -> {
        if (gameFrame != null) gameFrame.dispose();

        JFrame winnerFrame = new JFrame("الفائز!");
        winnerFrame.setSize(800, 600);
        winnerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        winnerFrame.setLocationRelativeTo(null);

        // خلفية الفائز
        ImageIcon winnerIcon = new ImageIcon(getClass().getResource("winner.png"));
        JLabel winnerLabel = new JLabel(winnerIcon);
        winnerLabel.setLayout(null);

        // اسم الفائز فوق الصورة
        JLabel nameLabel = new JLabel("الفائز هو: " + winner, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 32));
        nameLabel.setForeground(new Color(255, 215, 0)); // ذهبي
        nameLabel.setBounds(200, 200, 400, 50);

        // زر الخروج
        JButton leaveButton = new JButton("خروج");
        leaveButton.setFont(new Font("Arial", Font.BOLD, 16));
        leaveButton.setBackground(new Color(30, 30, 60)); // كحلي غامق
        leaveButton.setForeground(Color.WHITE);
        leaveButton.setFocusPainted(false);
        leaveButton.setBounds(350, 300, 100, 40);
        leaveButton.addActionListener(e -> sendLeaveRequest());

        winnerLabel.add(nameLabel);
        winnerLabel.add(leaveButton);

        winnerFrame.setContentPane(winnerLabel);
        winnerFrame.setVisible(true);
    });
} else if (message.equals("NO_WINNER")) {
    SwingUtilities.invokeLater(() -> {
        if (gameFrame != null) gameFrame.dispose();

        JFrame noWinnerFrame = new JFrame("لا يوجد فائز");
        noWinnerFrame.setSize(800, 600);
        noWinnerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        noWinnerFrame.setLocationRelativeTo(null);

        // خلفية "لا يوجد فائز"
        ImageIcon noWinnerIcon = new ImageIcon(getClass().getResource("nowin.png"));
        JLabel noWinnerLabel = new JLabel(noWinnerIcon);
        noWinnerLabel.setLayout(null);

        noWinnerFrame.setContentPane(noWinnerLabel);
        noWinnerFrame.setVisible(true);
    });

}
else if (message.startsWith("TIMER:")) {
    int seconds = Integer.parseInt(message.substring("TIMER:".length()).trim());
    startWaitingRoomTimer(seconds);
}
 else if (message.startsWith("GAME_TIMER:")) {
    int secondsLeft = Integer.parseInt(message.substring("GAME_TIMER:".length()).trim());
    SwingUtilities.invokeLater(() -> 
        gameTimerLabel.setText("الوقت المتبقي: " + secondsLeft + " ثانية"));
    } else if (message.startsWith("Player left:")) {
    String playerName = message.substring("Player left:".length()).trim();
    SwingUtilities.invokeLater(() -> {
        JOptionPane.showMessageDialog(null, "📢 اللاعب " + playerName + " خرج من اللعبة.", "لاعب غادر", JOptionPane.INFORMATION_MESSAGE);

        // نحذف اسم اللاعب من نافذة السكور
        for (int i = 0; i < scoreModel.size(); i++) {
            String item = scoreModel.getElementAt(i);
            if (item.startsWith(playerName + " ")) { // مثلا "Ahmad - 3"
                scoreModel.remove(i);
                break;
            }
        }
    });
}
    }

    private void enableAnswerButtons() {
    for (Component comp : answerButtonsPanel.getComponents()) {
        comp.setEnabled(true);
    }
}

private void disableAnswerButtons() {
    for (Component comp : answerButtonsPanel.getComponents()) {
        comp.setEnabled(false);
    }
}


    // تحديث قائمة اللاعبين المتصلين
    private void updatePlayersList(String players) {
        SwingUtilities.invokeLater(() -> {
            playerListModel.clear();
            String[] playerNames = players.split(",");
            for (String player : playerNames) {
                player = player.trim();
                if (!player.isEmpty()) {
                    playerListModel.addElement(player);
                }
            }
        });
    }

    // تحديث قائمة غرفة الانتظار
    private void updateWaitingRoomList(String players) {
        SwingUtilities.invokeLater(() -> {
            waitingRoomListModel.clear();
            String[] playerNames = players.split(",");
            for (String player : playerNames) {
                player = player.trim();
                if (!player.isEmpty()) {
                    waitingRoomListModel.addElement(player);
                }
            }
        });
    }

    // إرسال رسالة مغادرة للسيرفر
private void sendLeaveRequest() {
    if (out != null) {
        out.println("LEAVE");
    }
    // إغلاق جميع النوافذ المفتوحة بعد الإرسال
    if (playersFrame != null) playersFrame.dispose();
    if (waitingRoomFrame != null) waitingRoomFrame.dispose();
    if (gameFrame != null) gameFrame.dispose();
    JOptionPane.showMessageDialog(null, "لقد غادرت اللعبة.", "مغادرة", JOptionPane.INFORMATION_MESSAGE);
    System.exit(0);
}

    // دالة تضيف زر خروج تحت يسار أي نافذة
private void addLeaveButton(JFrame frame) {
    JButton leaveButton = new JButton("خروج");
    leaveButton.setBackground(Color.RED);
    leaveButton.setForeground(Color.WHITE);
    leaveButton.setFont(new Font("Arial", Font.BOLD, 14));
    leaveButton.setPreferredSize(new Dimension(80, 30)); // حجم الزر

    leaveButton.addActionListener(e -> sendLeaveRequest());

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // يسار
    bottomPanel.add(leaveButton);
    frame.add(bottomPanel, BorderLayout.SOUTH);
}

    
    private void startWaitingRoomTimer(int seconds) {
    new Thread(() -> {
        try {
            for (int i = seconds; i >= 0; i--) {
                final int timeLeft = i;
                SwingUtilities.invokeLater(() -> 
                    waitingRoomTimerLabel.setText("استعد تبقى" + timeLeft + " ثانية")
                );
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }).start();
}

    // إرسال طلب اللعب إلى السيرفر
    private void sendPlayRequest() {
        if (out != null) {
            out.println("play");
        } else {
            System.err.println("فشل في إرسال طلب اللعب: out غير مهيأ.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}
