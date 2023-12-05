package miniproject;

import java.io.IOException;
import java.net.Socket;

public class ChatClient {
	
	// class 멤버변수
	
	private String nickname;
	private Socket socket;
	private ChatClientThread chatClientThread;
	private NicknameGUI nicknameGUI;
	private ChatGUI chatGUI;
	private ChatServer server;
	
	public ChatClient(String serverAddress, int serverPort) {
		
		
		try {
				
			socket = new Socket(serverAddress, serverPort);
			System.out.println("서버에 연결되었습니다");

			// 닉네임 입력 GuI 띄우기
			nicknameGUI = new NicknameGUI(this);
			nickname = nicknameGUI.getNickname();
			
			// 클라이언트 쓰레드 시작
            chatClientThread = new ChatClientThread(socket, this);
            chatClientThread.start();
            
            // 서버에 초기 닉네임 목록 요청
            msSendToServer("/getnicklist");
            
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	} // ChatClient
	
    public void handleNicklistMessage(String[] nicknames) {
        chatGUI.updateUserList(nicknames); // 유저 목록 업데이트
    }
    public void handleChangeNickMessage(String oldNickname, String newNickname) {
        chatGUI.updateNickname(oldNickname, newNickname); // 닉네임 변경 업데이트
    }
	
	public void msSendToServer(String message) {
	    if (chatClientThread != null) {
	        chatClientThread.msSendToServer(message);
	    }
	}
	
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    
    public String getNickname() {
        return nickname;
    }
    
	public ChatGUI getChatGUI() {
		return chatGUI;
	}
	public ChatServer getChatServer() {
		return server;
	}
    public Socket getSocket() {
        return socket;
    }
	
    public void showChatGUI() {
    	if (socket.isConnected()) { // 소켓이 연결된 상태에서만 ChatGUI를 생성
            // 닉네임 설정 후 ChatGUI 생성
    		chatGUI = new ChatGUI(this, nickname);
    		chatClientThread.setNickname(nickname);
            // ChatClientThread에 ChatGUI 설정
            chatClientThread.setChatGUI(chatGUI);
            // ChatGUI를 보여줌
            chatGUI.setVisible(true);
	        // 클라이언트가 접속하면 닉네임을 서버에 전송
            msSendToServer("/nickname " + nickname);
            chatGUI.appendMessage(nickname + "님이 입장하였습니다.");
            
        } else { //socket 이 연결이 안되어 있으면 ChatGUI생성 안 함
            System.out.println("서버와 연결이 끊어져 ChatGUI를 생성할 수 없습니다.");
        }
    }
	
	public static void main(String[] args) {

		new ChatClient("localhost", 8783);
		new ChatClient("localhost", 8783); // 여러 클라이언트 실행
		
	} // main

} // class

