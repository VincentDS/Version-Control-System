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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A simple echo server.
 * 
 * Once connected to a client, opens a session for that client where
 * strings are echoed back until the client closes the connection.
 * 
 * Once the client has closed the connection, the server becomes
 * available again for other clients.
 * 
 * This is a single-threaded server that serves only a single client
 * at a time.
 * 
 * This server must be explicitly terminated by the user.
 * 
 * Illustrates the use of TCP/IP sockets.
 */
public class EchoServer {
	
	private final ServerSocket serverSocket;
	
	/**
	 * Construct a new EchoServer that listens on the given port.
	 */
	public EchoServer(int port) throws IOException {
		InetSocketAddress serverAddress = new InetSocketAddress(port);
		this.serverSocket = new ServerSocket();
		serverSocket.bind(serverAddress);
	}
	 
	private HashMap<String, String> logindata = new HashMap<String, String>();
	private String currentuser = ""; //moet een waarde zijn anders geeft de eerste else-if tak een nullpointerexception
	private String password = "";
	private WorkingDirectory directory;

	public void initializeMap() {
		logindata.put("anonymous", "guest");
		logindata.put("vincent", "test");
	}
	
	public String processInput(String input) {
		String response = "200";
		
		if (input.startsWith("USER")) {
			currentuser = input.substring(5);
		}
		else if (input.startsWith("PASS")) {
			password = input.substring(5);
			if (password.equals(logindata.get(currentuser))) {
				directory = new WorkingDirectory("./src");
				response = "230 Succesful login";
			}
			else {
				response = "500 Username and password don't match";
			}
		}
		else if (password.equals(logindata.get(currentuser))) {
			if (input.startsWith("CWD")) {
				String todirectory = input.substring(4);
				try {
					directory.changeWorkingDir(todirectory);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (input.equals("LIST")) {
				String listfiles = "";
				for(int i = 0; i < directory.listFiles().length; i++) {
				    listfiles += " " + directory.listFiles()[i];
				}
				response = "200" + listfiles;
			}
			else if (input.startsWith("PUT")) {
				
			}
			else if (input.startsWith("GET")) {

			}
			else {
				response = "202 this command doesn't exist";
			}
		}
		else {
			response = "500 Please login first";
		}
		
		return response;
	}
	
	/**
	 * Block and wait until a client arrives.
	 * 
	 * Once a client arrives, listen for client requests and
	 * send back echoed replies.
	 * 
	 * Client and server sockets communicate via input and output streams,
	 * as shown schematically below:
	 * 
	 * <pre>
     *   Client                             Server
     *    cs = new Socket(addr,port)        ss = socket.accept()
     *       cs.in <-------------------------- ss.out
     *       cs.out -------------------------> ss.in
     * </pre>
	 * 
	 * @param port the port on which to listen for client connections.
	 * @throws IOException when unable to setup a connection or
	 *         unable to communicate with the client. 
	 */
	public void acceptClient() throws IOException {
		
		Socket clientSocket = serverSocket.accept(); // blocks
		
		try {
			// get raw input and output streams
			InputStream rawInput = clientSocket.getInputStream();
			OutputStream rawOutput = clientSocket.getOutputStream();
			
			// wrap streams in Readers and Writers to read and write
			// text Strings rather than individual bytes 
			BufferedReader input = new BufferedReader(
					new InputStreamReader(rawInput));
			PrintWriter output = new PrintWriter(rawOutput);
			
			while (true) {
				// read string from client
				String clientInput = input.readLine();
				
				// log the string on the local console
				System.out.println("Server: client sent '" + clientInput + "'");
				
				// send back the string to the client
				output.println(processInput(clientInput));
				
				// make sure the output is sent to the client before closing
				// the stream
				output.flush();				
			}
		
		} catch (IOException e) {
			
		} finally {
			// tear down communication
			System.err.println("Server: closing client connection");
			clientSocket.close();
		}
	}
	
	/**
	 * Usage: java EchoServer port
	 * 
	 * Where port is the port on which the server should listen
	 * for requests.
	 * 
	 * Example:
	 *   java EchoServer 6789
	 *   
	 * @throws IOException when unable to setup connection or communicate
	 *         with the client. 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: java EchoServer port");
			return;
		}
		int port = Integer.parseInt(args[0]);
		
		System.out.println("Server: waiting for clients on port "+port);
		EchoServer server = new EchoServer(port);
		server.initializeMap();

		while (true) {
			server.acceptClient();
		}
		
		// ServerSockets are automatically closed for us by OS
		// when the program exits
	}

}
