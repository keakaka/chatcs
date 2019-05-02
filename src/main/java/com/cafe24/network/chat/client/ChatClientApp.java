package com.cafe24.network.chat.client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Scanner;


public class ChatClientApp {
	private static final String SERVER_IP = "192.168.1.38";
	private static final int SERVER_PORT = 7000;
	public static void main(String[] args) {
		String name = null;
		Scanner scanner = new Scanner(System.in);

		while( true ) {
			System.out.println("대화명을 입력하세요.");
			System.out.print(">>> ");
			name = scanner.nextLine();
			
			if(name.isEmpty() == true || name.length() < 2) {
				System.out.println("아이디는 두 글자 이상 입력하셔야 합니다.");
				continue;
			}
			Socket socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
				String data = "join:" + name + "\r\n";
				pw.println(data);
				String check = br.readLine();
				
				if(!"중복된 아이디".equals(check)) {
					new ChatWindow(name, socket).show();
					break;
				}else {
					continue;
				}
				
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		scanner.close();

		
		
	}
	}

