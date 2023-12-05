package miniproject;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class ChatGUI extends JFrame { // 채팅 GUI
	
    private ChatClient chatClient;
    private ChatClientThread chatClientThread;
    private JPanel chatPane, eastPanel, eastSPanel, southPanel, southCPanel; //패널
    private JList<String> userList;
    private JTextArea textArea;
    private JTextField userMessage;
    private DefaultListModel<String> userListModel;
    private JButton sendBnt, changeNicknameButton, exitButton; //버튼
    private String nickname;
    private Socket socket;
    private JScrollPane textAreaScrollPane, userListScrollPane;

    public ChatGUI(ChatClient chatClient, String nickname) {
    	this.chatClient = chatClient;
    	this.nickname = nickname;
    	this.socket = chatClient.getSocket(); // ChatClient로부터 socket 받아오기
    	
        chatPanel();

        this.chatClientThread = new ChatClientThread(socket, chatClient);
        userListModel = new DefaultListModel<>();
        userList.setModel(userListModel); //JList에 DefaultListModel연 JList는 DefaultListModel 에 저장된 데이터를 표시
        
        // 서버에서 닉네임 목록을 받아와 초기화
        chatClientThread.msSendToServer("/getnicklist"); 
        
    }

    public void chatPanel() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(0, 0, 500, 700);
        this.setTitle("채팅 프로그램");
        this.setLocationRelativeTo(null); // GUI 화면 가운데 띄우기

        textArea = new JTextArea();
        chatPane = new JPanel(new BorderLayout()); // 채팅 창의 기본 패널

        userMessage = new JTextField(); // 채팅 입력 장치
        userListModel = new DefaultListModel<>(); // 유저 목록 리모콘
        userList = new JList<>(userListModel); // 유저들을 리스트에 저장
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // JList의 선택 모드를 "단일 선택"으로 설정

        // 보내기 버튼 
        sendBnt = new JButton("보내기");
        sendBnt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(nickname);
            }
        });
        
        // 나가기 버튼 
        exitButton = new JButton("나가기");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        // 닉네임 변경 버튼 
        changeNicknameButton = new JButton("닉네임 변경");
        changeNicknameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeNickname();
            }
        });
        
        userMessage.addActionListener(new ActionListener() { // 채팅입력장치 액션기능 추가
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(nickname);
            }
        });
        
// 레이아웃 // 자식 패널일수록 위로  
        
        // 사용자 목록 패널
        userListScrollPane = new JScrollPane(userList);
        userListScrollPane.setPreferredSize(new Dimension(150, 0)); // 세로 크기는 자동 조절
        
        // 채팅 텍스트 로그 패널
        textAreaScrollPane = new JScrollPane(textArea);
        textAreaScrollPane.setPreferredSize(new Dimension(350, 0)); // 세로 크기는 자동 조절
        
        // SouthCenter(남중) :  message 텍스트(west), 채팅 입력 장치(center)
        southCPanel = new JPanel(new BorderLayout()); // 사우스센터 패널 생성
        southCPanel.add(new JLabel("  message  "), BorderLayout.WEST); // message 텍스트
        southCPanel.add(userMessage, BorderLayout.CENTER); // 채팅 입력장치
        
        // South(남) 패널 : SouthCenter패널(center), 보내기(east)버튼
        southPanel = new JPanel(new BorderLayout());  // 사우스 패널 생성
        southPanel.add(southCPanel, BorderLayout.CENTER); //사우스 패널에 사우스센터 패널 추가
        southPanel.add(sendBnt, BorderLayout.EAST); // 보내기 버튼
        
        // EastSouth(동남) 패널 : 닉네임 변경(center), 나가기 버튼(south)
        eastSPanel = new JPanel(new BorderLayout()); // 이스트사우스 패널 생성
        eastSPanel.add(changeNicknameButton, BorderLayout.CENTER); // 닉네임 변경
        eastSPanel.add(exitButton, BorderLayout.SOUTH); // 나가기
       
        // East(동) 패널 : 유저목록(center), EastSouth패널(south)
        eastPanel = new JPanel(new BorderLayout()); // 이스트 패널 생성
        eastPanel.add(userListScrollPane, BorderLayout.CENTER); // 유저목록
        eastPanel.add(eastSPanel, BorderLayout.SOUTH); // 이스트 패널에 이스트사우스 패널 추가
        
        // 부모 패널 레이아웃 : 채팅 텍스트 로그 영역(center), East 패널(east), South 패널(south)
        chatPane.add(textAreaScrollPane, BorderLayout.CENTER); // 채팅 텍스트 로그
        chatPane.add(eastPanel, BorderLayout.EAST); // 부모패널에 이스트패널 추가
        chatPane.add(southPanel, BorderLayout.SOUTH); // 부모패널에 사우스패널 추가
        
        this.setContentPane(chatPane); // JFrame에 채팅 패널 설정
    }

    private void sendMessage(String nickname) {
        String message = userMessage.getText();
        if (!message.isEmpty()) {
            chatClientThread.msSendToServer(message);
            appendMessage(nickname + "(나)" + " : " + message);
            userMessage.setText("");
        }
    }

    public void appendMessage(String message) {
        textArea.append(message + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public void updateUserList(String[] nicknames) {  // 유저 목록 업데이트
        userListModel.clear(); // 기존 목록 지우기
        for (String nickname : nicknames) {
            userListModel.addElement(nickname); // 새로운 목록 업데이트
        }
    }
    
    public void updateNickname(String oldNickname, String newNickname) { //닉네임 변경 관리
        textArea.setText(textArea.getText().replaceAll(oldNickname, newNickname));
    }
    
    private void changeNickname() { // 닉네임 변경 입력창
        String newNickname = JOptionPane.showInputDialog("새 닉네임을 입력하세요:");
        if (newNickname != null && !newNickname.isEmpty()) {
            chatClient.msSendToServer("/changenick " + nickname + " " + newNickname);
            setNickname(newNickname);
        }
    }
    public void setNickname(String newNickname) { // 닉네임 설정 시 서버로 알리는 메소드
        chatClientThread.setNickname(nickname);
        this.nickname = newNickname;
    }
}
