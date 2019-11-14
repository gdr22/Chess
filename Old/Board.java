import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class Board {
   final int[] pieceRanks = {1, 5, 3, 3, 9, 11}; //Point Values for pieces

   Piece[][] board = new Piece[8][8];
   
   /** Load the board from an external file */
   public boolean load(String filename) throws FileNotFoundException {
      boolean turn = false;
      
      Scanner file = new Scanner(new File(filename));
      
      turn = file.nextBoolean(); //Load what turn the game is at
      file.nextLine(); //Go to the next line
      
      for(int y = 0; y < 8; y++) {
         for(int x = 0; x < 8; x++) { //Load each element of the board
            board[x][y] = new Piece(file.nextInt(), file.nextBoolean(), file.nextInt()); //Determine each piece's properties
            file.nextLine(); //Go to the next line
         }
      }
      
      file.close();
      
      return turn;
   }
   
   /** Save the board to an external file */
   public void save(String filename, boolean turn) throws FileNotFoundException {
      PrintWriter file = new PrintWriter(new File(filename));
      
      file.println(turn); //Store the current turn
      
      for(int y = 0; y < 8; y++) {
         for(int x = 0; x < 8; x++) { //Save each element of the board
            file.print(board[x][y].type+" ");
            file.print(board[x][y].color+" ");
            file.println(board[x][y].number);
         }
      }
      
      file.close();
   }
   
   /** Returns if a side is in check */
   public boolean check(boolean turn) {
      boolean inCheck = false;
      
      List<Move> moves = possibleMoves(!turn);
      
      //In check?
      for(int i = 0; i < moves.size(); i++) {
         if(board[moves.get(i).bx][moves.get(i).by].type == 6) { //Attack on a king?
            if(board[moves.get(i).bx][moves.get(i).by].color == turn) { //Which side is the king on?
               inCheck = true;
               moves.get(i).print();
               break;
            }
         }
      }
      
      return inCheck;
   }
   
   /** Determines if a side is in checkmate */
   public boolean checkmate(boolean turn) {
      boolean inCheckmate = true;
      
      List<Move> moves = possibleMoves(turn);
      
      //In check?
      for(int i = 0; i < moves.size(); i++) {
         inCheckmate = false; //Assume move won't result in check
         Board testBoard = copy();
         
         testBoard.makeMove(moves.get(i));
         
         List<Move> moves2 = testBoard.possibleMoves(!turn);
         
         for(int j = 0; j < moves2.size(); j++) {
            if(testBoard.board[moves2.get(j).bx][moves2.get(j).by].type == 6) { //Attack on a king?
               if(testBoard.board[moves2.get(j).bx][moves2.get(j).by].color == turn) { //Which side is the king on?               
                  inCheckmate = true;
                  break;
               }
            }
         }
         
         if(!inCheckmate) {
            break; //Break when a move is found that doesn't result in checkmate.
         }
      }
      return inCheckmate;
   }
   
   public void makeMove(Move move) {
      board[move.bx][move.by] = board[move.ax][move.ay];
      board[move.ax][move.ay] = new Piece(0, false, 0);
      
      if(move.type != 0) { //Promotion
         board[move.bx][move.by].type = move.type;
      }
   }
   
   public int score() {
      int score = 0;
      
      for(int y = 0; y < 8; y++) {
         for(int x = 0; x < 8; x++) {
            if(board[x][y].type != 0) {
               score += pieceRanks[board[x][y].type - 1] * (board[x][y].color ? 1 : -1);
            }
         }
      }
      
      return score;
   }
   
   public List<Move> possibleMoves(boolean turn) {
      List<Move> moves = new ArrayList<Move>();
      
      for(int y = 0; y < 8; y++) {
         for(int x = 0; x < 8; x++) {
            if(board[x][y].color == turn) {
               switch(board[x][y].type) {
                  case 0: //Empty
                     break;
                  case 1: //Pawn
                     if(board[x][y+(turn ? 1 : -1)].type == 0) { //Move forward 1 space
                        if(y == (turn ? 6 : 1)) { //Promotion?
                           for(int i = 2; i <= 5; i++) {
                              moves.add(new Move(x, y, x, y+(turn ? 1 : -1), i)); //Add all possible promotional moves
                           }
                        }
                        else {
                           moves.add(new Move(x, y, x, y+(turn ? 1 : -1)));
                           if(y == (turn ? 1 : 6)) {
                              if(board[x][y+(turn ? 2 : -2)].type == 0) { //Move forward 2 spaces
                                 moves.add(new Move(x, y, x, y+(turn ? 2 : -2))); //No promotion check since it's impossible
                              }
                           }
                        }
                     }
                     
                     if(x > 0) {
                        if((board[x-1][y+(turn ? 1 : -1)].type != 0) && (board[x-1][y+(turn ? 1 : -1)].color != turn)) { //Attack left
                           if(y == (turn ? 6 : 1)) { //Promotion?
                              for(int i = 2; i <= 5; i++) {
                                 moves.add(new Move(x, y, x-1, y+(turn ? 1 : -1), i)); //Add all possible promotional moves
                              }
                           }
                           else {
                              moves.add(new Move(x, y, x-1, y+(turn ? 1 : -1)));
                           }
                        }
                     }
                     if(x < 7) {
                        if((board[x+1][y+(turn ? 1 : -1)].type != 0) && (board[x+1][y+(turn ? 1 : -1)].color != turn)) { //Attack right
                           if(y == (turn ? 6 : 1)) { //Promotion?
                              for(int i = 2; i <= 5; i++) {
                                 moves.add(new Move(x, y, x+1, y+(turn ? 1 : -1), i)); //Add all possible promotional moves
                              }
                           }
                           else {
                              moves.add(new Move(x, y, x+1, y+(turn ? 1 : -1)));
                           }
                        }
                     }
                     
                     
                     break;
                     
                  case 2: //Rook
                     for(int dir = -1; dir <= 1; dir += 2) { //Loop at -1 and 1
                        for(int a = x + dir; (dir == 1) ? (a < 8) : (a >= 0); a += dir) { //Horizantal
                           if(board[a][y].type == 0) {
                              moves.add(new Move(x, y, a, y)); //Empty space
                           }
                           else { //Obstruction
                              if(board[a][y].color != turn) {
                                 moves.add(new Move(x, y, a, y)); //Add attack move if possible
                              }
                              break; //Escape loop
                           }
                        }
                        for(int a = y + dir; (dir == 1) ? (a < 8) : (a >= 0); a += dir) { //Veritcal
                           if(board[x][a].type == 0) {
                              moves.add(new Move(x, y, x, a)); //Empty space
                           }
                           else { //Obstruction
                              if(board[x][a].color != turn) {
                                 moves.add(new Move(x, y, x, a)); //Add attack move if possible
                              }
                              break; //Escape loop
                           }
                        }
                     }
                     
                     break;
                  case 3: //Knight
                     for(int h = 1; h <= 2; h++) { //Height of L shape
                        int w = 3 - h;             //Width of L shape
                        
                        for(int b = y - h; b <= y + h; b += (2*h)) {
                           for(int a = x - w; a <= x + w; a += (2*w)) {
                              if(((a >= 0) && (a < 8)) && ((b >= 0) && (b < 8))) { //Point on the board?
                                 if((board[a][b].type == 0) || (board[a][b].color != turn)) { //If open or opponent
                                    moves.add(new Move(x, y, a, b));
                                 }
                              }
                           }
                        }
                     }
                     break;
                  case 4: //Bishop
                     for(int dirY = -1; dirY <= 1; dirY += 2) { //Loop at -1 and                  
                        for(int dirX = -1; dirX <= 1; dirX += 2) { //Loop at -1 and    
                           int a = x + dirX; int b = y + dirY;
                           while(((a >= 0) && (a < 8)) && ((b >= 0) && (b < 8))) { //Scan
                              if(board[a][b].type == 0) {
                                 moves.add(new Move(x, y, a, b)); //Empty space
                              }
                              else { //Obstruction
                                 if(board[a][b].color != turn) {
                                    moves.add(new Move(x, y, a, b)); //Add attack move if possible
                                 }
                                 break; //Escape loop
                              }
                              
                              a += dirX; b += dirY; //Increment amounts
                           }
                        }
                     }
                     
                     break;
                  case 5: //Queen
                     for(int dir = -1; dir <= 1; dir += 2) { //Loop at -1 and 1
                        for(int a = x + dir; (dir == 1) ? (a < 8) : (a >= 0); a += dir) { //Horizantal
                           if(board[a][y].type == 0) {
                              moves.add(new Move(x, y, a, y)); //Empty space
                           }
                           else { //Obstruction
                              if(board[a][y].color != turn) {
                                 moves.add(new Move(x, y, a, y)); //Add attack move if possible
                              }
                              break; //Escape loop
                           }
                        }
                        for(int a = y + dir; (dir == 1) ? (a < 8) : (a >= 0); a += dir) { //Veritcal
                           if(board[x][a].type == 0) {
                              moves.add(new Move(x, y, x, a)); //Empty space
                           }
                           else { //Obstruction
                              if(board[x][a].color != turn) {
                                 moves.add(new Move(x, y, x, a)); //Add attack move if possible
                              }
                              break; //Escape loop
                           }
                        }
                     }
                     
                     //Diagonal
                     for(int dirY = -1; dirY <= 1; dirY += 2) { //Loop at -1 and                  
                        for(int dirX = -1; dirX <= 1; dirX += 2) { //Loop at -1 and    
                           int a = x + dirX; int b = y + dirY;
                           while(((a >= 0) && (a < 8)) && ((b >= 0) && (b < 8))) { //Scan
                              if(board[a][b].type == 0) {
                                 moves.add(new Move(x, y, a, b)); //Empty space
                              }
                              else { //Obstruction
                                 if(board[a][b].color != turn) {
                                    moves.add(new Move(x, y, a, b)); //Add attack move if possible
                                 }
                                 break; //Escape loop
                              }
                              
                              a += dirX; b += dirY; //Increment amounts
                           }
                        }
                     }                  
                     
                     break;
                  case 6: //King
                     for(int b = y-1; b <= y+1; b++) {
                        for(int a = x-1; a <= x+1; a++) {
                           if(((a >= 0) && (a < 8)) && ((b >= 0) && (b < 8))) { //Point on the board?
                              if((board[a][b].type == 0) || (board[a][b].color != turn)) { //If open or opponent
                                 moves.add(new Move(x, y, a, b));
                              }
                           }
                        }
                     }
                     break;
               }
            }
         }
      }
      
      return moves;
   }
   
      
   /** Finds a piece of this type */
   public int find(int type, boolean color, int number) {
      Piece piece;
      int pos = -1; //Default -1 if piece isn't found
      
      for(int y = 0; y < 8; y++) {
         for(int x = 0; x < 8; x++) {
            piece = board[x][y];
            if(piece.type == type && piece.color == color && piece.number == number) {
               pos = y*8+x;
               y = 8; //Break out of outer loop
               break;
            }   
         }
      }
      
      return pos;
   }
   
   /** Draw the board */
   public void draw() {
      final String pChars = "PRNBQK"; //Initialize abbreviation characters
      
      System.out.print("     ");
      for(int x = 0; x < 8; x++) {
         System.out.print((char)('a'+x)+"     ");
      }
      
      System.out.print("\n");
      for(int y = 7; y >= 0; y--) {      
         System.out.print("  +");
         for(int x = 0; x < 8; x++) {
            System.out.print("-----+");
         }
         System.out.print("\n"+(y+1)+" |"); //Label the letters
         for(int x = 0; x < 8; x++) {            
            //Draw the piece
            if(board[x][y].type != 0) {
               //Determine the text representation
               String label = (board[x][y].color ? "W" : "B");                   //Color
               label += pChars.substring(board[x][y].type-1, board[x][y].type);  //Type
               label += board[x][y].number;                                      //Number
               
               System.out.print(" "+label+" |");
            }
            else {
               System.out.print("     |");
            }
         }
         System.out.print("\n  |");
         for(int x = 0; x < 8; x++) {
            System.out.print("     |");
         }
         System.out.print("\n");
      }
      
      System.out.print("  +");
      for(int x = 0; x < 8; x++) {
         System.out.print("-----+");
      }
      
      System.out.print("\n");
   }
   
   /** Construct a board */
   public Board(Piece[][] contents) {
      board = contents;
   }
   
   /** Copies the board into a new object */
   public Board copy() {
      Board cloneBoard = new Board();
      
      for(int y = 0; y < 8; y++) {
         for(int x = 0; x < 8; x++) {
            cloneBoard.board[x][y] = board[x][y];
         }
      }
      
      return cloneBoard;
   }
   
   /** Initialize the board */
   public Board() {
      final String order = "23456432";
      for(int y = 0; y < 8; y++) {
         for(int x = 0; x < 8; x++) {
            board[x][y] = new Piece(0, false, 0);
         }
      }
      //Pawns
      for(int x = 0; x < 8; x++) {
            board[x][6].type = 1; board[x][1].type = 1;
            board[x][6].color = false; board[x][1].color = true;
            board[x][6].number = x+1; board[x][1].number = x+1;
      }
      //Ouside Rows
      for(int x = 0; x < 8; x++) {
            board[x][7].type = order.charAt(x)-48; board[x][0].type = order.charAt(x)-48;
            board[x][7].color = false; board[x][0].color = true;
            board[x][7].number = x+1; board[x][0].number = x+1;
      }
   }
}