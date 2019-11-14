import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

public class ChessNet {
   public static final int aiLevel = 4;
   public static GUI window;
   public static Net net;
   public static Scanner keyboard;
   
   public static String hostName = "128.237.167.228";
   public static int portNumber = 4444;
   
   public static boolean isClient = true;

   public static void main(String[] args) throws Exception {
      keyboard = new Scanner(System.in);
      
      
      
      window = new GUI();
      
      //If we pass any argument
      if(args.length != 0) {
         isClient = false;
         server(); //Be the server
      }
      else //Otherwise just be a client
      {
         client();
      }
      
      
   }
   
   /** The game client code */
   public static void client() throws Exception {
      net = new Net(hostName, portNumber);
      
      //Just keep grabbing the board and displaying it
      while(true) {
         Board b = net.getBoard();
         window.updateBoard(b);
         //b.draw();
         //System.out.println("Turn:\t" + b.turn);
         //System.out.println("Level:\t" + b.aiLevel);
         
         double score = aiSearch(b, b.aiLevel, b.turn);
         
         //System.out.println(score);
         net.sendDouble(score);
         
      }
   }
   
   /** The game server code */
   public static void server() throws Exception {
      Board board = new Board();
      boolean turn = true;
      boolean checkmate = false;
      
      boolean whiteAi = true;
      boolean blackAi = true;
      
      Move move = new Move(-1, -1, -1, -1);
      
      /*System.out.println("Load a previous game?");
            
      int input;
            
      do {
         input = keyboard.nextInt();
         keyboard.nextLine(); //Clear the buffer
      } while(input < 0 || input > 1); //While beyond valid range
      
      if(input == 1) {
         turn = board.load("gameBoard.txt");
      }*/
      
      net = new Net(4444);
      new Thread(net).start();
      
      System.out.println("Press enter to start the game.");
      keyboard.nextLine();
      
      //Main loop
      while(!checkmate) {
         if(board.check(turn)) {
            if(board.checkmate(turn)) {
               System.out.println(" Checkmate."); //Checkmate can only occur if in check
               checkmate = true;
               continue;
            }
            else {
               System.out.println(" Check."); //State if in check, when not in checkmate.
            }
         }

         
         if(!(turn ? whiteAi : blackAi)) { //See if AI is disabled for this turn or not
            //board.draw();
            move = input(board, turn, keyboard);
         }
         else { //AI Turn
            if(whiteAi == blackAi) { //If both players are AI, show the board on each AI turn
               //board.draw();
            }
            
            //System.out.println("Processing moves.");
            long time = System.currentTimeMillis();
            
            move = ai(board, aiLevel, turn, false);
            
            //move.print();
            //System.out.println(" is the best move.");
            
            time = System.currentTimeMillis() - time;
            //System.out.println("Completed in "+time/1000.0+" seconds.");
         }
         
         if(move.isLegal(board.board, turn)) {
            Board testBoard = board.copy();
            testBoard.makeMove(move);            
            
            if(testBoard.check(turn)) {
               System.out.println(" You cannot put yourself in check.");
            }
            else {
               board.makeMove(move);
               
               window.updateBoard(board);
               
               //Send the board to all clients
               for(int i = 0; i < net.getSocketsCount(); i++) {
                  net.sendBoard(board, i);
               }
               
               //window.board = board;
               //window.graphicsBoard.revalidate();
               
               turn = !turn;
               board.save("gameBoard.txt", turn);
            }
         }
         else
         {
            System.out.println("Illegal Move.");
         }
      }
      
      if(turn) {
         System.out.println("Black wins.");
      }
      else {
         System.out.println("White wins.");
      }
   }
   
   /* Main method for searching the tree of moves */
   public static Move ai(Board aiBoard, int recursion, boolean stepTurn, boolean display) throws Exception {
      List<Move> moves = aiBoard.possibleMoves(stepTurn);
      window.updateMoves(Move.toStringArray(moves));
      
      Move move = new Move(0, 0, 0, 0);
      double score = 0;
      double highScore = 0; //This initializtion doesn't matter since it gets initialized to the first move when in the loop
      
      int sockets = net.getSocketsCount();
      double[] scores = new double[moves.size()];
      
      //If anyone is connected
      if(sockets != 0) {
         int[] running = new int[sockets];
         boolean[] busy = new boolean[sockets];
         int[] assigned = new int[sockets];
         int moveCnt = 0;
         
         //Loop for all the moves
         while(moveCnt < moves.size()) {
            for(int i = 0; i < sockets; i++) {
               //If this socket isn't busy
               if(!busy[i]) {
                  Board nextBoard = aiBoard.copy();
                  nextBoard.makeMove(moves.get(moveCnt));
                  //Add in turn and recursion data
                  nextBoard.turn = !stepTurn;
                  nextBoard.aiLevel = recursion - 1;
                  
                  //Assign this client this move
                  net.sendBoard(nextBoard, i);
                  busy[i] = true;
                  assigned[i] = moveCnt;
                  //running[i] = moveCnt;
                  window.selectMoves(assigned);
                  
                  System.out.println("Client " + i + " assigned to move " + moveCnt + ".");
                  
                  //Increment the move counter
                  moveCnt++;
                  
                  //If all moves have been distributed, exit the loop
                  if(moveCnt >= moves.size()) {
                     break;
                  }
               }
            }
            
            for(int i = 0; i < sockets; i++) {
               //Read in this socket
               String line = net.readLine(i);
               
               //If the client said something
               if(line != null) {
                  //The client is available
                  busy[i] = false;
                  scores[assigned[i]] = Double.parseDouble(line);
                  
                  assigned[i] = 0;
                  window.selectMoves(assigned);
                  
                  System.out.println("Client " + i + " done.\t\t" + line);
               }
            }
            
            //moveCnt++
         }
      }
         
      int moveCnt = 0;
      
      for(int i = 0; i < moves.size(); i++) {
         
         //If there are no clients, just do it locally
         if(sockets == 0) {
            window.selectMove(i);
            Board nextBoard = aiBoard.copy();
            nextBoard.makeMove(moves.get(i));
            
            score = aiSearch(nextBoard, recursion - 1, !stepTurn);
         }
         else
         {
            score = scores[moveCnt++];
         }
         
         if(score == (stepTurn ? -1000000000 : 1000000000)) { //Is this a check move?
            moves.remove(i--); //Remove this option from the list and set back the counter
            continue; //Go to the next move
         }
         else if(i == 0) { //Is this the first iteration
            move = moves.get(i);
            highScore = score; //Initialize the highScore to the score of the first move
         }
         else if((score > highScore) == stepTurn) { //Look for positive or negative numbers depending on the turn
            move = moves.get(i);
            highScore = score;
            if(display) {
               System.out.println("\nBetter move:");
            }
         }
         
         if(display) { //Show information if AI debug is enabled
            for(int indent = 4; indent > recursion; indent--) { //Show recursive indentation
               System.out.print("\t");
            } 
                       
            moves.get(i).print();
            System.out.println(" "+score);
         }
      }
      return move;
   }
   
   /* Method used to rank each move. Called upon by the ai method */
   public static double aiSearch(Board aiBoard, int recursion, boolean stepTurn) {
      List<Move> moves = aiBoard.possibleMoves(stepTurn);
      boolean realTurn = ((recursion % 2) == 0) ? stepTurn : !stepTurn; //Whose turn is it currently in the game?
      
      double total = 0;
      double score;
      
      for(int i = 0; i < moves.size(); i++) { //Search for check moves first to quit while you're ahead
         if(aiBoard.board[moves.get(i).bx][moves.get(i).by].type == 6) { //Attacking a king?
            if(aiBoard.board[moves.get(i).bx][moves.get(i).by].color == realTurn) { //Attacking the side deciding this move?
               return realTurn ? -1000000000 : 1000000000; //Give this move a ridiculously bad score to prevent it from being chosen
            }
         }
      }
      
      //GUI code specific to the client
      if(isClient && recursion == aiLevel - 1)  {
         window.updateMoves(Move.toStringArray(moves));
      }
      
      for(int i = 0; i < moves.size(); i++) { //Assuming everything checks out, move on to the slow recursive code
         Board nextBoard = aiBoard.copy();
         nextBoard.makeMove(moves.get(i));
         
         //GUI code specific to the client
         if(isClient && recursion == aiLevel - 1)  {
            window.updateBoard(nextBoard);
            window.selectMove(i);
         }
         
         if(recursion != 0) { //Go deeper if recursion level isn't zero
            score = aiSearch(nextBoard, recursion - 1, !stepTurn);
            
            if(score == (realTurn ? -1000000000 : 1000000000)) { //Is this a check move?
               moves.remove(i--); //Remove this option from the list and set back the counter
               continue; //Go to the next move
            }
            else { //Move isn't check
               total += score;
            }
         }
         else {
            total += nextBoard.score();
         }
      }
      return 10 * total / moves.size(); //Scale score to amount of moves scanned
   }
   
   /** Input the user's move */
   public static Move input(Board board, boolean turn, Scanner keyboard) {
      final String pChars = "prnbqk"; //Initialize abbreviation characters
      boolean invalid = true;
      String first = ""; String last = "";
      int xPos[] = new int[2]; int yPos[] = new int[2];
      
      System.out.println("Please enter your move.");
      
      while(invalid) {
         invalid = false;
         String ui = keyboard.nextLine();
         ui = ui.toLowerCase();
      
         int colon = ui.indexOf(':');
         
         if(colon != -1) {
            first = ui.substring(0, colon);
            last = ui.substring(colon+1, ui.length());
            
            for(int i = 0; i < 2; i++) { //Cycle between both segments
               String str = (i == 0) ? first : last;
               
               invalid = true;
               if(str.charAt(1) >= '1' && str.charAt(1) <= '8') { //Coordinates
                  if(str.charAt(0) >= 'a' && str.charAt(0) <= 'h') {
                    xPos[i] = str.charAt(0) - 'a';
                    yPos[i] = str.charAt(1) - '1';
                    
                    invalid = false; 
                  }
               }
               
               else if(str.charAt(1) >= 'a' && str.charAt(1) <= 'z') { //Piece reference                  
                  int type = 0;
                  
                  for(int index = 0; index < 6; index++) { //Determine piece type
                     if(str.charAt(1) == pChars.charAt(index)) {
                        type = index + 1;
                        invalid = false;
                        break;
                     }
                  }
                  
                  if(!invalid) {
                     if (str.charAt(0) == 'b' || str.charAt(0) == 'w') {
                        if(str.charAt(2) >= '1' && str.charAt(2) <= '9') {
                           int index = board.find(type, str.charAt(0) == 'w', str.charAt(2) - '0');
                           if(index != -1) {
                              xPos[i] = index % 8;
                              yPos[i] = index / 8;
                           }
                           else {
                              System.out.println("Piece not found.");
                              invalid = true;
                           }
                        }
                        else {
                           System.out.println("Error in numeric indentifier.");
                           invalid = true;
                        }
                     }
                     else {
                        System.out.println("Error in color indentifier.");
                        invalid = true;
                     }
                  }
                  else {
                     System.out.println("No such piece of type "+str.charAt(1)+".");
                  }                  
               }
               
               if(invalid) //Inparsable
                  break;
            }
         }
         else {
            System.out.println("Positions must be separated by a colon.");
            invalid = true;
         }
      }
      
      Move move = new Move(xPos[0], yPos[0], xPos[1], yPos[1]);
      
      //Promotion logic
      if(board.board[xPos[0]][yPos[0]].type == 1) { //Is this piece a pawn?
         if((turn && (yPos[1] == 7)) || (!turn && (yPos[1] == 0))) {
            System.out.println("Please specify the piece to which you would like to promote.");
            
            do { //Get promotion type
               String ui = keyboard.nextLine();
               ui = ui.toLowerCase();
               
               char inputChar = ui.charAt(0); //Get the first character
               
               for(int i = 2; i <= 5; i++) {
                  if(inputChar == pChars.charAt(i-1)) { //Does the input match an allowed type?
                     move.type = i;
                     break; //Escape this loop
                  }
               }
               
               if(move.type == 0) {
                  System.out.println("Promotion is not allowed for pieces of type '"+inputChar+"'. (Allowed types are 'r', 'n', 'b', and 'q')");
               }
                           
            } while(move.type == 0); //Continue while the promotion type hasn't been determined.
         }
      }
      
      return move;
   }
}