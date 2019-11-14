import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.io.*;

public class MiniMaxChess {
   public static final int aiLevel = 4;
   public static GUI window;

   public static void main(String[] args) throws FileNotFoundException {
      Scanner keyboard = new Scanner(System.in);
      Board board = new Board();
      boolean turn = true;
      boolean checkmate = false;
      
      Move move = new Move(-1, -1, -1, -1);
      
      int players;
      System.out.println("Input the number of players.");
      do {
         players = keyboard.nextInt();
         keyboard.nextLine(); //Clear the buffer
      } while(players < 0 || players > 2); //While beyond valid range
      
      boolean whiteAi = false;
      boolean blackAi = false;
      
      switch(players) { //Decide when the computer plays a move
         case 2:
            whiteAi = false; blackAi = false; //No AI
            break;
         case 1:
            System.out.println("What side do you want to play?\n - White (1)\n - Black (0)");
            
            int input;
            
            do {
               input = keyboard.nextInt();
               keyboard.nextLine(); //Clear the buffer
            } while(input < 0 || input > 1); //While beyond valid range
            
            boolean choice = (input == 1);
            whiteAi = !choice; blackAi = choice; //Play as black

            break;
         case 0:
            whiteAi = true; blackAi = true; //All AI
      }
      
      System.out.println("Load a previous game?");
            
      int input;
            
      do {
         input = keyboard.nextInt();
         keyboard.nextLine(); //Clear the buffer
      } while(input < 0 || input > 1); //While beyond valid range
      
      if(input == 1) {
         turn = board.load("gameBoard.txt");
      }
      
      window = new GUI();
      
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
            
            System.out.println("Processing moves.");
            long time = System.currentTimeMillis();
            
            move = ai(board, aiLevel, turn, false);
            
            move.print();
            System.out.println(" is the best move.");
            
            time = System.currentTimeMillis() - time;
            System.out.println("Completed in "+time/1000.0+" seconds.");
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
   public static Move ai(Board aiBoard, int recursion, boolean stepTurn, boolean display) {
      List<Move> moves = aiBoard.possibleMoves(stepTurn);
      window.updateMoves(Move.toStringArray(moves));
      
      double alpha = -1000000000;
      double beta = 1000000000;
      
      Move move = new Move(0, 0, 0, 0);
      double score = 0;
      double highScore = 0; //This initializtion doesn't matter since it gets initialized to the first move when in the loop
      
      for(int i = 0; i < moves.size(); i++) {
         window.selectMove(i);
         Board nextBoard = aiBoard.copy();
         nextBoard.makeMove(moves.get(i));

         score = aiSearch(nextBoard, recursion - 1, !stepTurn, alpha, beta);
         
         if(stepTurn) {
            alpha = Math.max(alpha, score); //If we have a better possible score, increase our alpha
         }
         else
         {
            beta = Math.min(beta, score); //If we are minimizing, lower beta when we find a lower score
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
   public static double aiSearch(Board aiBoard, int recursion, boolean stepTurn, double alpha, double beta) {
      List<Move> moves = aiBoard.possibleMoves(stepTurn);
      boolean realTurn = ((recursion % 2) == 0) ? stepTurn : !stepTurn; //Whose turn is it currently in the game?
      
      double total = 0;
      double minmax = stepTurn ? -1000000000 : 1000000000;
      double score;
      
      double localAlpha = alpha;
      double localBeta = beta;
      
      for(int i = 0; i < moves.size(); i++) { //Search for check moves first to quit while you're ahead
         if(aiBoard.board[moves.get(i).bx][moves.get(i).by].type == 6) { //Attacking a king?
            if(aiBoard.board[moves.get(i).bx][moves.get(i).by].color == realTurn) { //Attacking the side deciding this move?
               return realTurn ? -1000000000 : 1000000000; //Give this move a ridiculously bad score to prevent it from being chosen
            }
         }
      }
      
      for(int i = 0; i < moves.size(); i++) { //Assuming everything checks out, move on to the slow recursive code
         Board nextBoard = aiBoard.copy();
         nextBoard.makeMove(moves.get(i));
         
         if(recursion != 0) { //Go deeper if recursion level isn't zero
            score = aiSearch(nextBoard, recursion - 1, !stepTurn, alpha, beta);
            
            if(score == (realTurn ? -1000000000 : 1000000000)) { //Is this a check move?
               moves.remove(i--); //Remove this option from the list and set back the counter
               continue; //Go to the next move
            }
            else { //Move isn't check
               //Decide whether we do min or max
               if(stepTurn) {
                  minmax = Math.max(minmax, score);
               }
               else
               {
                  minmax = Math.min(minmax, score);
               }
            }
         }
         else {
            //Decide whether we do min or max
            if(stepTurn) {
               minmax = Math.max(minmax, nextBoard.score());
            }
            else
            {
               minmax = Math.min(minmax, nextBoard.score());
            }
         }
         
         if(stepTurn) { //If we're maximizing (above call minimizing)
            localAlpha = Math.max(localAlpha, minmax); //If we find something better than we already have, bump up our alpha
            if(localBeta < localAlpha) { //If the beta passes the alpha, we just escape
               return minmax;
            }
         }
         else //If we're minimizing (above call maximizing)
         {
            localBeta = Math.min(localBeta, minmax); //If we find something lower than we already have, update beta
            if(localBeta < localAlpha) { //If alpha and beta switch sides of the inequality, escape
               return minmax;
            }
         }
      }
      return minmax;
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