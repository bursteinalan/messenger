
//package groups;

import java.io.PrintStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

/*
 * A server that delivers status messages to other users.
 */
public class Server {

	// Create a socket for the server
	private static ServerSocket serverSocket = null;
	// Create a socket for the server
	private static Socket userSocket = null;
	// Maximum number of users
	private static int maxUsersCount = 5;
	// An array of threads for users
	private static userThread[] threads = null;

	public static void main(String args[]) {

		// The default port number.
		int portNumber = 58555;
		if (args.length < 2) {
			System.out.println("Usage: java Server <portNumber>\n" + "Now using port number=" + portNumber + "\n"
					+ "Maximum user count=" + maxUsersCount);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
			maxUsersCount = Integer.valueOf(args[1]).intValue();
		}

		System.out.println("Server now using port number=" + portNumber + "\n" + "Maximum user count=" + maxUsersCount);

		userThread[] threads = new userThread[maxUsersCount];

		/*
		 * Open a server socket on the portNumber (default 8000).
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a user socket for each connection and pass it to a new user thread.
		 */
		while (true) {
			try {
				userSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxUsersCount; i++) {
					if (threads[i] == null) {
						threads[i] = new userThread(userSocket, threads);
						threads[i].start();
						break;
					}
				}
				if (i == maxUsersCount) {
					PrintStream output_stream = new PrintStream(userSocket.getOutputStream());
					output_stream.println("#busy");
					output_stream.close();
					userSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/*
 * Threads
 */
class userThread extends Thread {

	private String userName = null;
	private BufferedReader input_stream = null;
	private PrintStream output_stream = null;
	private Socket userSocket = null;
	private final userThread[] threads;
	public HashMap<String, Integer> friendList;
	// If friendList[name]==0 Friend request has been sent
	// If friendList[name]==1 Friends
	// If friendList[name]==null Nor friends
	private int maxUsersCount;

	public userThread(Socket userSocket, userThread[] threads) {
		this.userSocket = userSocket;
		this.threads = threads;
		maxUsersCount = threads.length;
		this.friendList = new HashMap<String, Integer>();
	}

	public void run() {
		int maxUsersCount = this.maxUsersCount;
		userThread[] threads = this.threads;

		try {
			/*
			 * Create input and output streams for this client. Read user name.
			 */
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
			output_stream = new PrintStream(userSocket.getOutputStream());

			String response;
			String clientMessage;
			while (true) {
				// Get sent message
				clientMessage = input_stream.readLine().trim();
				System.out.println(clientMessage);
				// If starts with #join, Send back welcome and send all other users #newuser
				if (clientMessage.startsWith("#join")) {
					String name = clientMessage.substring(6);
					response = "#welcome";
					this.userName = name;
					System.out.println(name);
					output_stream.println(response);
					for (userThread thread : threads) {
						if (thread != null && thread != this)
							thread.output_stream.println("#newuser " + name);
					}
				}

				// Send #newstatus to all friends and send back #statusposted
				else if (clientMessage.startsWith("#status")) {
					String status = clientMessage.substring(8);
					// for(String name:friendList.keySet())
					for (userThread thread : threads) {
						if (thread != null && thread.friendList.get(userName) == null) {
							// DO Nothing if it is null
						} else if (thread != null && thread != this && friendList.get(thread.userName) == 1)
							thread.output_stream.println("#newStatus " + this.userName + " " + status);
					}
					output_stream.println("#statusPosted");
				}

				// Send #friendme to correct user and update map to 0
				// Handle Already sent and already friends
				else if (clientMessage.startsWith("#friendme")) {
					String name = clientMessage.substring(10);
					System.out.println(name);
					if (friendList.get(name) == null) {
						friendList.put(name, 0); // Friend with status sent
						for (userThread thread : threads) {
							if (thread != null)
								System.out.println(thread.userName);
							if (thread != null && thread.userName.equals(name)) {
								thread.output_stream.println("#friendme " + userName);
							}
						}
					} else if (friendList.get(name) == 0) {
						output_stream.println("Already sent request to: " + name);
					} else if (friendList.get(name) == 1) {
						output_stream.println("Already friends with: " + name);
					}

				}
				// Send #okFriends to ALL users
				// Check whether friend request has been sent first
				// Handle both No friend request and Already friends
				else if (clientMessage.startsWith("#friends")) {
					String name = clientMessage.substring(9);
					for (userThread thread : threads) {
						if (thread != null && thread.userName.equals(name)) {
							System.out.println(name);
							System.out.println(thread.friendList);
							if (thread.friendList.get(userName) == null) {
								thread.output_stream.println("Friend request does not exist from user: " + name);
							} else if (thread.friendList.get(userName) == 0) {
								thread.output_stream.println("#OKfriends " + userName);
								output_stream.println("#OKfriends " + name);
								thread.friendList.put(userName, 1);
								friendList.put(name, 1);
							} else if (thread.friendList.get(userName) == 1) {
								output_stream.println("You are already friends with: " + name);
							} else {
								thread.output_stream.println("Friend request does not exist from user: " + name);
							}
						}
					}
				}
				// Send #friendRequestDenied to user
				// Remove name from friendlist
				else if (clientMessage.startsWith("#DenyFriendRequest")) {
					String name = clientMessage.substring(19);
					for (userThread thread : threads) {
						if (thread != null && thread.userName.equals(name)) {
							if (thread.friendList.get(userName) == 0) {
								thread.output_stream.println("#FriendRequestDenied " + userName);
								thread.friendList.remove(userName);
							}
						}
					}
				}

				// Send #NotFriends to both threads and remove friendship
				else if (clientMessage.startsWith("#unfriend")) {
					String name = clientMessage.substring(10);
					if (friendList.get(name) == 1) {
						friendList.remove(name);
						for (userThread thread : threads) {
							if (thread != null && thread.userName.equals(name)) {
								thread.friendList.remove(userName);
								thread.output_stream.println("#NotFriends " + userName);
								output_stream.println("#NotFriends " + name);
							}

						}
					}

				}

				// Send #Leave to all other threads and send back #Bye
				else if (clientMessage.startsWith("#Bye")) {
					System.out.println("closing thread");
					for (userThread thread : threads) {
						if (thread != null && thread != this)
							thread.output_stream.println("#Leave " + this.userName);
					}
					output_stream.println("#Bye");
					break;
				}
				
				else {
					output_stream.println("Invalid Command (Remember to use #status <status> or @<command> <user> for valid communications");
				}

			}

			/*
			 * Clean up. Set the current thread variable to null so that a new user could be
			 * accepted by the server.
			 */
			synchronized (userThread.class) {
				for (int i = 0; i < maxUsersCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, close the input stream, close the socket.
			 */
			input_stream.close();
			output_stream.close();
			userSocket.close();
		} catch (IOException e) {
		}
	}
}
