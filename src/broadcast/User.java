
//package broadcast;

import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class User extends Thread {

	// The user socket
	private static Socket userSocket = null;
	// The output stream
	private static PrintStream output_stream = null;
	// The input stream
	private static BufferedReader input_stream = null;

	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	public static void main(String[] args) {

		// The default port.
		int portNumber = 58555;
		// The default host.
		String host = "csa1.bu.edu";

		if (args.length < 2) {
			System.out.println(
					"Usage: java User <host> <portNumber>\n" + "Now using host=" + host + ", portNumber=" + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}

		/*
		 * Open a socket on a given host and port. Open input and output streams.
		 */
		try {
			userSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			output_stream = new PrintStream(userSocket.getOutputStream());
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host " + host);
		}

		/*
		 * If everything has been initialized then we want to write some data to the
		 * socket we have opened a connection to on port portNumber.
		 */
		if (userSocket != null && output_stream != null && input_stream != null) {
			try {
				/* Create a thread to read from the server. */
				new Thread(new User()).start();
				String ClientMessage;
				// Get user name and join the social net
				System.out.print("Please Enter Your Username: ");
				ClientMessage = inputLine.readLine();
				ClientMessage = "#join " + ClientMessage;
				output_stream.println(ClientMessage);

				while (!closed) {
					ClientMessage = inputLine.readLine();
					if (ClientMessage != null) {
						output_stream.println(ClientMessage);
					}
				} // Read user input and send protocol message to server
				output_stream.close();
				input_stream.close();
				userSocket.close();
				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	/*
	 * Create a thread to read from the server.
	 */
	public void run() {
		/*
		 * Keep on reading from the socket till we receive a Bye from the server. Once
		 * we received that then we want to break.
		 */
		String responseLine;
		String[] responses;

		try {
			// Get Server Response and check if it starts with an acceptable command as per
			// the protocol
			// Display appropriate responses
			while ((responseLine = input_stream.readLine().trim()) != null) {
				if (responseLine.startsWith("#welcome")) {
					System.out.println("Connection has been established with the server");
				} else if (responseLine.startsWith("#busy")) {
					System.out.println("Server is busy. Try again later");
					break;
				} else if (responseLine.startsWith("#statusPosted")) {
					System.out.println("Status successfully posted");
				} else if (responseLine.startsWith("#newuser")) {
					responses = responseLine.split(" ");
					System.out.println("New user has joined: " + responses[1]);
				} else if (responseLine.startsWith("#newStatus")) {
					String name = responseLine.substring(11, responseLine.indexOf(' ', 11));
					String status = responseLine.substring(responseLine.indexOf(' ', 11));
					System.out.println(name + ": " + status);
				} else if (responseLine.startsWith("#Leave")) {
					responses = responseLine.split(" ");
					System.out.println(responses[1] + " has left");
				} else if (responseLine.startsWith("#Bye")) {
					System.out.println("Closing Connection");
					break;
				}

			}

			// Close the socket
			closed = true;
			output_stream.close();
			input_stream.close();
			userSocket.close();
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}
