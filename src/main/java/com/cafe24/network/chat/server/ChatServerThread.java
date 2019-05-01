package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ChatServerThread extends Thread{
	private Socket socket;
	private String id;
	List<PrintWriter> userList;

	public ChatServerThread(Socket socket, List userList) {
		this.socket = socket;
		this.userList = userList;
	}

	@Override
	public void run() {

		try {
			// 4. I/O Stream 받아오기
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			while(true) {
				// 5. 데이터 읽기
				String data = br.readLine();
				if(data == null) {
					ChatServer.log("클라이언트 연결 종료");
					//doquit(userList);
					break;
				}

				String[] tokens = data.split(":");
				if("join".equals(tokens[0])) {
					join(tokens[1], pw);
				}else if("talk".equals(tokens[0])) {
					talk(tokens[1]);
				}else if("exit".equals(tokens[0])) {
					exit(pw);
				}

			}

		} catch (SocketException e) {
			ChatServer.log("sudden closed by client");
		} catch (IOException e) {
			e.printStackTrace();	
		} finally {
			try {
				if(socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void exit(PrintWriter pw) {
		removeWriter(pw);

		String data = this.id + "님이 퇴장했습니다.";
		broadcast(data);
	}

	private void removeWriter(PrintWriter pw) {
		synchronized (userList) {
			userList.remove(pw);
		}
	}

	private void talk(String data) {
		broadcast(this.id + ":" + data);
	}

	private void join(String id, PrintWriter pw) {
		this.id = id;

		String data = id + "님이 입장하였습니다.";
		broadcast(data);

		// writer pool에 저장
		addWriter(pw);
	}

	private void addWriter(PrintWriter pw) {
		synchronized (userList) {
			userList.add(pw);
		}
	}

	private void broadcast(String data) {
		synchronized (userList) {
			for(PrintWriter pw : userList) {
				pw.println(data);
				pw.flush();
			}
		}
	}
}
