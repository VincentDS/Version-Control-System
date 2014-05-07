/*
Copyright (c) 2013, Tom Van Cutsem, Vrije Universiteit Brussel
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Vrije Universiteit Brussel nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class EchoClient {
	
	private static final String PROMPT = "> ";
	private WorkingDirectory directory;
	private Repository repository;

	public EchoClient() throws IOException {
		directory = new WorkingDirectory("/Users/vincentdeschutter/Documents/Test");
		run();
	}
	
	public void run() throws IOException {
		BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
		String input;
		String answer;	
		do {
			System.out.print(PROMPT);
			input = consoleInput.readLine();
			answer = processInput(input);
			if (answer != "") {
				System.out.println(answer);
			}
		}
		while (!input.equals("quit"));
	}
	
	public String processInput(String input) throws IOException {
		String answer = "";
		if (input.equals("ls")) {
			if (directory.listFiles().length != 0) {
				answer = directory.listFiles()[0];
				for(int i = 1; i < directory.listFiles().length; i++) {
					answer += "\n" + directory.listFiles()[i];
				}
			}
		}
		else if (input.startsWith("cd")) {
			String todirectory;
			if (input.length() >= 3) {
				todirectory = input.substring(3);
				directory.changeWorkingDir(todirectory);
			}
			else {
				directory.setWorkingDir("/Users/");
			}
		}
		else if (input.equals("pwd")) {
			answer = directory.getWorkingDir();
		}
		else if (input.startsWith("vcs")) {
			if (input.length() >= 4) {
				if ((repository == null) && (directory.exists(".vcs"))) {
					repository = new Repository(directory);
				}
				answer = processVcs(input.substring(4));
			}
		}
		return answer;
	}
	
	public String processVcs(String command) throws IOException {
		String answer = "";
		if (command.equals("init")) {
			if (repository == null) {
				repository = new Repository(directory);
				answer = "you initialized a new repository.";
			}
			else {
				answer = "There exist already a repository in this project";
			}
		}
		else if (command.startsWith("add")) {
			String filename = command.substring(4);
			if (repository != null) {
				repository.add(filename);
			}
			else {
				answer = "Please create a repository first.";
			}
		}
		else if (command.startsWith("commit")) {
			String message = "";
			if (command.length() > 6) {
				if (command.substring(7,9).equals("-m")) {
					message = command.substring(10);
				}
			}
			if (repository != null) {
				repository.commit(message);
			}
			else {
			answer = "Please create a repository first.";
			}
		}
		return answer;
	}
	
	
	public void connectToServer(InetAddress ip, int port) throws UnknownHostException, IOException {
		
		InetSocketAddress serverAddress = new InetSocketAddress(ip, port);		
		Socket socket = new Socket();
		socket.connect(serverAddress);
		
		try {
			// get raw input and output streams
			InputStream rawInput = socket.getInputStream();
			OutputStream rawOutput = socket.getOutputStream();
			
			// wrap streams in Readers and Writers to read and write
			// text Strings rather than individual bytes 
			BufferedReader input = new BufferedReader(
					new InputStreamReader(rawInput));
			PrintWriter output = new PrintWriter(rawOutput);
			
			BufferedReader consoleInput = new BufferedReader(
					new InputStreamReader(System.in));
			
			String message;
			do {
				
				System.out.print(PROMPT);
				message = consoleInput.readLine();

				// send message to the server
				output.println(message);
				
				// make sure the output is sent to the server before waiting
				// for a reply
				output.flush();
				
				System.out.println("Client: sent '" + message + "'");
				
				// wait for and read the reply of the server
				String serverReply = input.readLine();
				
				// log the string on the local console
				System.out.println("Client: server replied '" +
				                   serverReply + "'");
				
			} while (!message.equals(""));

		} finally {
			// tear down communication
			socket.close();
		}
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: java EchoClient ip port");
			return;
		}
		
		InetAddress ip = InetAddress.getByName(args[0]); //interne voorstelling van een IP-adres
		int port = Integer.parseInt(args[1]);
		
		//System.out.println("Client: connecting to server at "+ip+":"+port);
		
		EchoClient client = new EchoClient();
		//client.connectToServer(ip, port);
		
		//System.out.println("Client: terminating");
	}

}
