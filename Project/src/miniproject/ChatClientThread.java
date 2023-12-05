package miniproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

public class ChatClientThread extends Thread{ // 유저가 서버로부터 다른 유저의 메시지를 수신하는 쓰레드  
	
	private ChatClient chatClient; 
	private Socket socket;
	private ChatGUI chatGUI; 
	private String nickname;
	private ChatServer server;
	private ChatServerThread user;
	
	public ChatClientThread(Socket socket, ChatClient chatClient) {
	    this.socket = socket;
	    this.chatClient = chatClient;
	    this.chatGUI = chatClient.getChatGUI(); // ChatGUI 초기화
	    this.server = chatClient.getChatServer();
	    
    }
    
	@Override
	public void run() {
		//run 은 쓰레드가 실행될때 호출되는 특수한 메소드이기 때문에 명칭을 변경하지 않음
		
		BufferedReader br = null;
		
		try {
			
			br = new BufferedReader(
            		new InputStreamReader(
            				socket.getInputStream()));
			

			String message;
			
			while ((message = br.readLine()) != null) { // 한줄씩 읽어오기 
                if (message.startsWith("/nicklist")) {      
                    updateNicknameList(message);
                    handleServerMessage(message);
                } else {
                        chatGUI.appendMessage(message);
                }
            }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (server != null) {
	            server.exitUser(user, this);
	        }
	        try {
	            if (br != null) {
	                br.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	} // run
	
	public void msSendToServer(String message) { // 유저가 서버로 보내는 메소드
	    try {
	        if (socket.isClosed()) {
	            return;
	        }
	        BufferedWriter bw = null;
	        bw = new BufferedWriter(
	        		new OutputStreamWriter(
	        				socket.getOutputStream()));
	        bw.write(message + "\n");
	        bw.flush();
	        System.out.println("Sent to server: " + message); // 로그 추가
	    } catch (IOException ioe) {
	        ioe.printStackTrace();
	        System.err.println("서버로 메시지를 보내는 중 오류가 발생했습니다: " + ioe.getMessage());
	    }
	}
	
    public void setChatGUI(ChatGUI chatGUI) {
        this.chatGUI = chatGUI;
    }

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
		msSendToServer("/setnick " + nickname);
	}
	// 서버에 닉네임 변경 요청 전송
    public void changeNickname(String newNickname) {
        msSendToServer("/changenick " + chatClient.getNickname() + " " + newNickname);
    }
	
    private void updateNicknameList(String message) { // 서버로부터 받은 닉네임 목록을 ChatGUI에 업데이트
        String[] parts = message.split(" ");
        if (parts.length > 1) {
            String[] nicknames = parts[1].split("\n");
            chatGUI.updateUserList(nicknames);
        }
    }
    
    private void handleServerMessage(String message) {
        if (message != null && message.startsWith("/nicklist")) {
            handleNicklistMessage(message);
        } else if (message != null && message.startsWith("/changenick")) {
            handleChangeNickMessage(message);
        } else {
            chatGUI.appendMessage(message);
        }
    }
	
    private void handleNicklistMessage(String message) {
        String[] parts = message.split("\n");
        if (parts.length > 1) {
            String[] nicknames = parts[1].split("\n");
            chatGUI.updateUserList(nicknames);
        }
    }
	
    private void handleChangeNickMessage(String message) {
        String[] parts = message.split(" ");
        if (parts.length > 2) {
            String oldNickname = parts[1];
            String newNickname = parts[2];
            chatGUI.updateNickname(oldNickname, newNickname);
        }
    }
} // class


