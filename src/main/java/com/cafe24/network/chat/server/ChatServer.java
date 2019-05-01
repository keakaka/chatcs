package com.cafe24.network.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ChatServer {
	
	public static void main(String[] args) {
		
		ServerSocket serverSocket = null;
		List<PrintWriter> userList = new ArrayList<PrintWriter>();
		
		try {
			// 1. Create Server Socket
			serverSocket = new ServerSocket();
			   
			// 2. Bind
			serverSocket.bind( new InetSocketAddress( "0.0.0.0", 7000 ) );

			while (true) {
				// 3. Wait for connecting ( accept )
				Socket socket = serverSocket.accept();

				// 4. Delegate Processing Request
				new ChatServerThread(socket, userList).start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 5. 자원정리
			try {
				if (serverSocket != null && serverSocket.isClosed() == false) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void log(String log) {
		System.out.println("[server# " + Thread.currentThread().getId() +"] " + log);
	}
}
