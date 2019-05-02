package com.cafe24.network.chat.client;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import com.cafe24.network.chat.server.ChatServer;

public class ChatWindow {
	private String id;
	private Socket socket;
	private Frame frame;
	private Panel pannel;
	private Panel pannel2;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private TextArea textArea2;

	public ChatWindow(String id, Socket socket) {
		this.id = id;
		this.socket = socket;
		frame = new Frame();
		pannel = new Panel();
		pannel2 = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 100);
		textArea2 = new TextArea();
		
		new ChatClientReceiveThread(socket).start();
	}
	
	public void show() {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent actionEvent ) {
				if(!textField.getText().equals("")) {
			    	sendMessage();
			    }
			}
		});

		// Textfield
		textField.setColumns(80);
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    if(!textField.getText().equals("")) {
			    	sendMessage();
			    }
			}
			
		});
		textArea2.setEditable(false);
		// Pannel2
		pannel2.setBackground(Color.LIGHT_GRAY);
		pannel2.add(BorderLayout.NORTH, textArea2);
		frame.add(BorderLayout.EAST, pannel2);
		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				finish();
			}
		});
		frame.setVisible(true);
		frame.pack();
	}
	private void finish() {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			String data = "exit\r\n";
			pw.println(data);
			pw.close();
			System.exit(0);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private void sendMessage() {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			String text = textField.getText();
			String data = "talk:" + text + "\r\n";
			pw.println(data);

			textField.setText("");
			textField.requestFocus();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class ChatClientReceiveThread extends Thread{
		Socket socket = null;
		
		ChatClientReceiveThread(Socket socket){
			this.socket = socket;
		}

		public void run() {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				while(true) {
					String data = br.readLine();
					if(data.startsWith("onUser")) {
						textArea2.setText("");
						String[] user = data.substring(6, data.length()).split("&");
						for(String s : user) {
							textArea2.append(s);
							textArea2.append("\n");
						}
						
					}else if(data.startsWith("downUser")){
						textArea2.setText("");
						String[] user = data.substring(8, data.length()).split("&");
						for(String s : user) {
							textArea2.append(s);
							textArea2.append("\n");
						}
					}else {
						textArea.append(data);
						textArea.append("\n");
					}
				}
			} catch (SocketException e ) {
				ChatServer.log("서버와의 연결이 종료됐습니다.");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}