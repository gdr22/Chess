import java.net.*;
import java.io.*;
import java.util.*;

public class Net implements Runnable {
   public static String hostName = "172.19.14.255";
   public static int portNumber = 4444;
   
   private ServerSocket serverSocket;
   
   private Socket socket;
   private PrintWriter out;
   private BufferedReader in;
   
   private ArrayList<Socket> sockets;
   private ArrayList<PrintWriter> outs;
   private ArrayList<BufferedReader> ins;
   
   /** Create the client network interface object for the chess program */
   public Net(String host, int port) throws Exception {
      socket = new Socket(host, port);
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
   }
   
   /** Create the server network interface object for the chess program */
   public Net(int port) throws Exception {
      serverSocket = new ServerSocket(port);
      
      sockets = new ArrayList<Socket>(); 
      outs = new ArrayList<PrintWriter>();
      ins = new ArrayList<BufferedReader>();
      //this.start(); //Start the socket accepting subroutine
      //socket = serverSocket.accept();
      //out = new PrintWriter(socket.getOutputStream(), true);
      //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
   }
   
   /** Just an accessor of the readLine method */
   public String readLine(int i) throws Exception {
      return ins.get(i).readLine();
   }
   
   /** Sends a double to the server */
   public void sendDouble(double d) {
      out.println(d);
   }
   
   /** Returns the number of connected sockets */
   public int getSocketsCount() {
      return sockets.size();
   }
   
   /** Background thread that keeps track of all Sockets */
   public void run() {
      //Just add every opened socket to a list
      while(true) {
         try {
            
            //System.out.println("Waiting for new clients.");
            
            Socket newSocket = serverSocket.accept();
            sockets.add(newSocket);
            outs.add(new PrintWriter(newSocket.getOutputStream(), true));
            ins.add(new BufferedReader(new InputStreamReader(newSocket.getInputStream())));
            System.out.println("New client.");
         }
         catch (Exception e) {
            System.out.println("Failed to connect.");
         }
      }
   }
   
   /** Receives the chess board over the network */
   public Board getBoard() throws Exception {
      Board board = new Board();
      String line;
      int cnt = 0;
      while((line = in.readLine()) != null) {
         //If we're getting the grid
         if(cnt < 64) {
            //Determine the board coordinates for this line
            int x = cnt % 8;
            int y = cnt / 8;
            
            //Grab the byte and convert it back into a Piece
            byte b = Byte.parseByte(line);
            Piece p = new Piece(b);
            
            //Add the piece to the board
            board.board[x][y] = p;
         }
         else if(cnt == 64) { //If we're getting the last part of the message
            byte b = Byte.parseByte(line);
            //System.out.println(b);
            
            //If the final byte is positive, turn = true
            board.turn = b >= 0;
            //Set the aiLevel to the absolute value of the final byte
            board.aiLevel = (b >= 0) ? b : b * -1;
            
            break; //Escape when we have the whole board
         }
         
         cnt++;
      }
      
      return board;
   }
   
   /** Send the board to a client */
   public void sendBoard(Board board, int socket) {
      //Loop through each piece on the board
      for(int y = 0; y < 8; y++) {
         for(int x = 0; x < 8; x++) {
            Piece p = board.board[x][y];
            
            outs.get(socket).println(p.toByte());
            
            //Piece q = new Piece(p.toByte());
            
            //out.println(p.type + " " + p.color + " " + p.number);
         }
      }
      
      //Send the turn and aiLevel (negative if turn is false)
      byte b = (byte)(board.aiLevel * (board.turn ? 1 : -1));
      //System.out.println(b);
      outs.get(socket).println(b);
   }
   
   public static void main(String[] arg) throws Exception {
      //If there are no parameters, just make a client
      if(arg.length == 0) {
         Net net = new Net(hostName, portNumber);
         
         GUI window = new GUI();
         
         while(true) {
            Board b = net.getBoard();
            window.updateBoard(b);
         }
      }
      else //Make a server
      {
         Net net = new Net(portNumber);
      
         //net.sendBoard(new Board());
      }
      //while(true);
   }
}