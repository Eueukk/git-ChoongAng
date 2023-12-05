package miniproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;

public class ChatServerThread extends Thread {
    private Socket socket;
    private ChatServer server;
    private String nickname;
    private ChatGUI chatGUI;
    private ChatClientThread usersThread;

    public ChatServerThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;

        // 서버가 연결된 클라이언트에게 초기 닉네임 목록을 보냅니다.
        server.sendInitialNicknamesList(this);
    }

    @Override
    public void run() {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                String message = br.readLine();

                if (message == null) {
                    // 클라이언트가 연결을 끊었을 때
                    break;
                }	          
                
                System.out.println("서버가 클라이언트로부터 받는 메세지: " + message);
                
                if (message.startsWith("/inform")) {
                    // 새 클라이언트 추가 알림 메시지
                    informNewUser(message);
                
                } else if (message.startsWith("/nicklist")) {
                    // 닉네임 목록 업데이트
                    updateNicknameList(message);
                } else if (message.startsWith("/setnick")) {
                    // 초기 닉네임 설정
                    setInitialNickname(message);
                } else if (message.startsWith("/changenick")) {
                    // 닉네임 변경 메시지 처리
                    handleNicknameChangeMessage(message);
                } else {
                    // 일반 채팅 메시지
                    server.usersMessage(nickname + ": " + message, this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.exitUser(this, usersThread);
            }
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void informNewUser(String message) {
        String[] parts = message.split(" ");
        String newUsername = parts[1];
        chatGUI.appendMessage(newUsername + " 님이 입장했습니다.");
    }
    
    public void msSendToUser(String message) {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write(message + "\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateNicknameList(String message) {
        String[] nicknames = message.split("\n");
        if (nicknames.length > 1 && chatGUI != null) {
            chatGUI.updateUserList(Arrays.copyOfRange(nicknames, 1, nicknames.length));
        }
    }

    private void setInitialNickname(String message) {
        String[] parts = message.split(" ");
        String initialNickname = parts[1];
        setNickname(initialNickname);
    }

    private void handleNicknameChangeMessage(String message) {
        String[] parts = message.split(" ");
        String oldNickname = parts[1];
        String newNickname = parts[2];

        // 닉네임 업데이트
        changeNickname(newNickname);

        // 모든 클라이언트에게 닉네임 변경 메시지를 브로드캐스트
        server.changeNickname(oldNickname, newNickname);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;

        if (chatGUI != null) {
            chatGUI.setNickname(nickname);
        }

        server.sendNicknamesList(); // 닉네임 목록 업데이트
    }

    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
        server.sendNicknamesList(); // 닉네임 목록 업데이트
    }

    public String getNickname() {
        return nickname;
    }

    public void setChatGUI(ChatGUI chatGUI) {
        this.chatGUI = chatGUI;
    }
}// class


