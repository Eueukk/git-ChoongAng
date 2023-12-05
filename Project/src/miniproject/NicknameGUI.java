package miniproject;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NicknameGUI extends JFrame { // 닉네임 입력 GUI
	
	private ChatClient chatClient;
    private JPanel namePane;
    private JTextField textField;
    private JButton actionBnt;
    private String nickname = "";

    public NicknameGUI(ChatClient chatClient) {
        
        namePanel();
        NameLabel();
        nameActionBnt();
        
        this.setVisible(true);
        this.setLocationRelativeTo(null); // GUI 화면 가운데 띄우기
        this.chatClient = chatClient;  // 추가
        
    } // NickNameGUI
    
    public void namePanel() { // 닉네임 전체 틀
    	
        this.setTitle("닉네임 입력창");
        this.setBounds(100, 100, 315, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        namePane = new JPanel();
        namePane.setLayout(null);
        namePane.setBackground(new Color(153,204,204));
        
        this.setContentPane(namePane);
        
    } // namePanel

    public void NameLabel() { // 닉네임 입력문구와 입력하는 텍스트 필드
    	
        JLabel nickName = new JLabel(" 닉네임을 입력해 주세요 ");
        nickName.setBounds(80, 35, 200, 15);
        nickName.setForeground(Color.white);
        
        namePane.add(nickName);
        textField = new JTextField();
        textField.setBounds(50, 60, 200, 20);
        namePane.add(textField);
        textField.setColumns(13);
        
        // 텍스트필드의 엔터키 처리이벤트
        textField.addActionListener(new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e) {
                enterChatGUI();
            }
        }); 
        
    } // NameLabel

    public void nameActionBnt() { // 입장 버튼 이벤트 액션
    	
        actionBnt = new JButton("입장");
        actionBnt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enterChatGUI();
            }
        });
        actionBnt.setBounds(100, 90, 100, 20);
        namePane.add(actionBnt);
        
    } //nameActionBnt

    public void enterChatGUI() {
        nickname = textField.getText();

        if (!nickname.isEmpty()) {
            setVisible(false);
            chatClient.setNickname(nickname);
            chatClient.showChatGUI();
        } else {
            JOptionPane.showMessageDialog(
                    this, "닉네임을 입력해주세요!!!", "경고", JOptionPane.WARNING_MESSAGE);
        }
    }//enterChatGUI
    
    public String getNickname() {
        return nickname;
    }

    public static void main(String[] args) {
        new NicknameGUI(new ChatClient("localhost", 8782));
    }
}
