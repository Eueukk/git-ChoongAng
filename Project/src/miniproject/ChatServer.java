package miniproject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatServer {
	
	                   //synchronized 를 사용하여 여러 쓰레드가 동시에 'usersList'를 수정하는것을 방지
	private List<ChatServerThread> usersList = Collections.synchronizedList(
			new ArrayList<>());   //유저 들어올때마다 리스트로 저장
	private List<String> nicknamesList = Collections.synchronizedList(
			new ArrayList<>()); // 닉네임 목록 리스트로 저장
	
	private ServerSocket ss = null;  // 서버 소켓
	private Socket socket;  // 유저 소켓
	private ChatServerThread user;  // 유저를 나타내는 ChatServerThread 객체
	
	public ChatServer(int port) { // 생성자
        try {
            ss = new ServerSocket(port);
            System.out.println("서버가 시작되었습니다");
            startServer();
        } catch (IOException ioe) {
            ioe.printStackTrace(); 
        }
    }
	
    public void startServer() {
        while (true) {
            try {
                Socket socket = ss.accept();
                ChatServerThread user = new ChatServerThread(socket, this);
                usersList.add(user);
                user.start(); // 각 클라이언트에 대한 스레드 시작
                sendInitialNicknamesList(user);
                informOtherUsers(user); // 수정된 부분: 다른 클라이언트에게 새 클라이언트를 알림
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void informOtherUsers(ChatServerThread newUser) {
        for (ChatServerThread user : usersList) {
            if (user != newUser) {
                user.msSendToUser("/inform " + newUser.getNickname());
            }
        }
    }
    
	public void handleClientMessage(ChatServerThread user, String message) {

		if (message.startsWith("/getnicklist")) {
	        // 클라이언트에게 초기 닉네임 목록 전송
	        sendInitialNicknamesList(user);
	    } else if (message.startsWith("/setnick")) {
	        setInitialNickname(user.getNickname());
	    } else if (message.startsWith("/changenick")) {
	        // 닉네임 변경 메시지 처리
	        handleNicknameChangeMessage(user, message);
	    } else {
	        // 채팅 메시지 처리
	        usersMessage(user.getNickname() + ": " + message,user);
	    }
	}

    public void updateUserList() {
        // 서버에서 닉네임 목록을 클라이언트로 전송
        for (ChatServerThread user : usersList) {
            user.msSendToUser("/nicklist\n" + String.join("\n", nicknamesList));
        }
    }
    
    private void handleNicknameChangeMessage(ChatServerThread user, String message) {
        String[] parts = message.split(" ");
        String oldNickname = parts[1];
        String newNickname = parts[2];

        // 유저의 닉네임 변경 처리
        user.changeNickname(newNickname);

        // 서버에 닉네임 변경 정보 전파
        changeNickname(oldNickname, newNickname);
    }
    
    public void usersMessage(String message, ChatServerThread sender) {
        for (ChatServerThread user : usersList) {
            user.msSendToUser(message);
        }
    } // usersMessage
	
    public void addNickname(String nickname) { // 새로운 닉네임 추가
        
        nicknamesList.add(nickname); // 닉네임 목록을 따로 관리
        sendNicknamesList(); // 닉네임 목록 전송
        
    } // addNickname
    
    // 클라이언트에서 닉네임 변경 요청 전송하는 로직
    public void changeNickname(String oldNickname, String newNickname) {
        for (ChatServerThread user : usersList) {
            user.msSendToUser("/changenick " + oldNickname + " " + newNickname);
        }
    }
    
    public void sendNicknamesList() { //서버에 연결된 모든 클라이언트에게 닉네임 목록을 전송
    	
    	for (ChatServerThread user : usersList) {
    	user.msSendToUser("/nicklist\n" + String.join("\n", nicknamesList));
        }
    	
    } // sendUsersList
    
    public void setInitialNickname(String nickname) {
        for (ChatServerThread user : usersList) {
            user.msSendToUser("/setnick " + nickname);
        }
    }
    public void sendInitialNicknamesList(ChatServerThread user) { //서버에 연결된 모든 클라이언트에게 닉네임 목록을 전송
        user.msSendToUser("/nicklist\n" + String.join("\n", nicknamesList));
    }
    
	public void exitUser(ChatServerThread user, ChatClientThread usersThread) { // 유저 나갈때
		
		usersList.remove(user);
		nicknamesList.remove(user.getNickname());
		System.out.println(" ' " + user.getNickname() + " ' " + "님이 나갔습니다");
		sendNicknamesList(); // 닉네임 목록 전송
		
	} // exitUser
    
	public static void main(String[] args) {
		new ChatServer(8783);
		
	} // main
	
    
} // class

