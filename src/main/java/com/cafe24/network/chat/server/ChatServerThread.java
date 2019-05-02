package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ChatServerThread extends Thread{
	private Socket socket;
	private String id;
	List<PrintWriter> userList;
	HashSet<String> hset;
	StringBuilder sb = new StringBuilder();
	public ChatServerThread(Socket socket, List userList, HashSet<String> hset) {
		this.socket = socket;
		this.userList = userList;
		this.hset = hset;
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
					exit(pw);
					break;
				}

				if(data.startsWith("join:")) {
					if(hset.contains(data.substring(5,data.length()))) {
						pw.println("중복된 아이디");
						socket.close();
						Thread.interrupted();
					}else {
						pw.println("OK");
						join(data.substring(5,data.length()), pw);
					}
				}else if(data.startsWith("talk:")) {
					talk(data.substring(5,data.length()));
				}else if(data.startsWith("exit:")) {
					exit(pw);
				}
			}

		} catch (SocketException e) {
			ChatServer.log("socketException");
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
	private void join(String id, PrintWriter pw) {
		this.id = id;
		// writer pool에 저장
		addWriter(pw, id);
		String data = id + "님이 입장하였습니다.";
		broadcast(data);
		String onUser = "";
		for(String s : hset) {
			onUser += s+"&";
		}
		broadcast("onUser"+onUser);
		onUser = "";
	}
	private void talk(String data) {
		broadcast("["+this.id + "] : " + data);
	}
	private void exit(PrintWriter pw) {
		removeWriter(pw);
		
		String data = this.id + "님이 퇴장했습니다.";
		ChatServer.log(data);
		broadcast(data);
		String downUser = "";
		for(String s : hset) {
			downUser += s+"&";
		}
		broadcast("downUser"+downUser);
		downUser = "";
	}
	private void addWriter(PrintWriter pw, String id) {
		synchronized (userList) {
			userList.add(pw);
		}
		synchronized (hset) {
			hset.add(id);
		}
	}
	private void removeWriter(PrintWriter pw) {
		synchronized (userList) {
			userList.remove(pw);
			pw.close();
		}
		synchronized (hset) {
			hset.remove(id);
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
