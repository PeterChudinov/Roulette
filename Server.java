package roulette;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Server {
	
	//streamer and listener ids are used to get every user an unique number
	public static int streamerID;
	public static int listenerID;
	//arraylists of both streamers and listeners
	private ArrayList<Streamer> connectedStreamers;
	private ArrayList<Listener> connectedListeners;
	private int port;
	private SimpleDateFormat sdf;
	private ServerSocket serverSocket;
	@SuppressWarnings("unused")
	private Socket socket;
	private boolean online;
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	
	public Server(int port) {
		
		this.port = port;
		//to log time in server console
		sdf = new SimpleDateFormat("EEE, MMM d ''yy h:mm:ss.S a zz");
		
		//arrays of streamers and listeners
		connectedStreamers = new ArrayList<Streamer>();
		connectedListeners = new ArrayList<Listener>();
		
	}
	
	public void start() {
		online = true;
		//create socket server and wait for connection requests
		try {
			//the socket used by the server
			serverSocket = new ServerSocket(port);
			
			System.out.println(sdf.format(new Date()) + " :: success initializing server");
			
			while (online) {
				
				//accept connection
				Socket socket = serverSocket.accept();
				
				System.out.println(sdf.format(new Date()) + " :: connection accepted from " + socket.getInetAddress() + ":" + socket.getPort());
				
				//stop the loop if server is stopping
				if (!online) break;
				
				sInput = new ObjectInputStream(socket.getInputStream());
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				
				String x = (String) sInput.readObject();

				
				if (x.equals("STREAM")) {
					
					//creates a streamer object with and both streams
					Streamer streamer = new Streamer(sInput, sOutput);
					
					//adds the streamer to the streamer array
					connectedStreamers.add(streamer);
					
					//gives the streamer an id
					streamer.streamerID = ++streamerID;
					
					//starts the thread
					streamer.start();
					
				} else if (x.equals("LISTEN")) {
					
					//creates a listener object with both streams
					Listener listener = new Listener(sInput, sOutput);
					
					//adds listener to listener array
					connectedListeners.add(listener);
					
					//gives listener an id
					listener.listenerID = ++listenerID;
					
					//starts the thread
					listener.start();
				} else {
					
					//if client sends a string unequal to STREAM/LISTEN then connection terminates;
					System.out.println(sdf.format(new Date()) + " :: connected client is neither streamer/listener :c");
					
					//closing streams
					sInput.close();
					sOutput.close();
				}
			}
			
			try {
				serverSocket.close();
			} catch (Exception e) {
				System.out.println(sdf.format(new Date()) + " :: exception closing socket " + e);
			}
		} catch (IOException e) {
			System.out.println(sdf.format(new Date()) + " :: exception at Input/Output streams " + e);
		} catch (ClassNotFoundException e) {}
		
	}
	
	public static void main(String[] args) {
		//start server on port 5051 just like SOSI in Russian :D
		int portNumber = 5051;
		
		//creates a server object and starts it
		Server server = new Server(portNumber);
		server.start();
	}

	private class Streamer extends Thread {
		
		//streams and id
		private ObjectInputStream sInput;
		/*private ObjectOutputStream sOutput;*/
		public int streamerID;
		//listeners of current streamer
		ArrayList<CurrentListener> listeners;
		//bool tells that streamer is streaming
		protected boolean stream;
		//media object
		private Media media;
		
		public Streamer(ObjectInputStream sInput, ObjectOutputStream sOutput) {
			
			//transfer inputs and outputs from the server to the current streamer
			this.sInput = sInput;
			/*this.sOutput = sOutput;*/
			
		}
		
		public void run() {
			
			//stream is set to true at the connection by default
			stream = true;
			System.out.println(sdf.format(new Date()) + " :: streamer id " + Server.streamerID + " " + "just successfully connected");
			/*
			 * Streaming cycle which
			 * gets a Media object from streamer
			 * and sends it to all the listeners
			 * of current streamer
			 */
			
			while (stream) {
				
				if(!stream)
					break;
				
				//reading a Media object
				try {
					media = (Media) sInput.readObject();
				} catch (ClassNotFoundException | IOException e) {
					System.out.println(sdf.format(new Date()) + " :: streamer " + streamerID +" got an exception at streams(or classes?!) " + e);
					break;
				}
				System.out.println("uspeh");
				//broadcast the Media object to the streamers
				broadcast(media);
			}
		}
		
		public void broadcast(Media toListener) {
			for (int k = 0; k < connectedListeners.size(); k++) {
				for (int i = 0; i < listeners.size(); i++) {
					if (listeners.get(i).listenerID == connectedListeners.get(k).listenerID) {
						try {
							connectedListeners.get(k).lOutput.writeObject(toListener);
						} catch (IOException e) {
								System.out.println(e.toString());
						}
					}
				}
			}
		}
		
	}
	
	private class Listener extends Thread {
		
		//streams, id
		private ObjectInputStream lInput;
		private ObjectOutputStream lOutput;
		public int listenerID;
		//random streamer generator
		private Random random;
		//id of the streamer you are listening to
		public int streamID;
		//bool tells if listener listens or not
		protected boolean listening;
		//Media object
		private ListenerControls control;
		
		Listener(ObjectInputStream lInput, ObjectOutputStream lOutput) {
			
			this.lInput = lInput;
			this.lOutput = lOutput;
				
			//waits before streamers will connect
			while (connectedStreamers.size() > 0) {}
			
			//gets a random streamer
			int streamersAmount = connectedStreamers.size() + 1;
			streamID = connectedStreamers.get(random.nextInt() % streamersAmount).streamerID;
			
			//connects to the streamer
			connectedStreamers.get(streamID).listeners.add(new CurrentListener(listenerID, streamID));
			
		}
		
		public void run() {
			listening = true;
			
			System.out.println(sdf.format(new Date()) + " :: listener with id " + Server.listenerID + " just successfully connected");
			
			/*
			 * TODO
			 * listening features
			 */
			
			while (listening) {
				
				//get instructions from the listener's client
				try {
					control = (ListenerControls) lInput.readObject();
				} catch (ClassNotFoundException | IOException e) {
					System.out.println(sdf.format(new Date()) + " :: listener " + listenerID +" got an exception at streams(or classes?!) " + e);
				}
				
				switch (control.getType()) {
				
				case ListenerControls.NOTHING:
					break;
					
				case ListenerControls.NEXT:
					next();
					break;
					
				case ListenerControls.DISCONNECT:
					stopListening();
					break;
				}
				
			}
			
			try {
				this.finalize();
			} catch (Throwable e) {
				System.out.println(sdf.format(new Date()) + " :: listener " + listenerID + " cannot be finalized " + e);
			}
			
		}
		
		public void next() {
			int streamersAmount = connectedStreamers.size() + 1;
			connectedStreamers.get(streamID).listeners.remove(listenerID);
			streamID = connectedStreamers.get(random.nextInt() % streamersAmount).streamerID;
			connectedStreamers.get(streamID).listeners.add(new CurrentListener(listenerID, streamID));
		}
		
		public void stopListening() {
			listening = false;
		}

	}
	
	class CurrentListener {
		
		private int listenerID;
		@SuppressWarnings("unused")
		private int streamerID;
		
		CurrentListener(int listenerID, int streamerID) {
			
			this.listenerID = listenerID;
			this.streamerID = streamerID;
			
		}
	}
	
}
