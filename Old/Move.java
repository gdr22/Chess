public class Move {
   int ax; int ay;
   int bx; int by;
   int type;   
   
   public Move(int a, int b, int c, int d) {
      ax = a;
      ay = b;
      bx = c;
      by = d;
      
      type = 0;
   }
   
   public Move(int a, int b, int c, int d, int e) {
      ax = a;
      ay = b;
      bx = c;
      by = d;
      
      type = e;
   }
   
   public void print() {
      final String pChars = "prnbqk"; //Initialize abbreviation characters
      
      System.out.print((char)(ax+'a'));
      System.out.print((char)(ay+'1'));
      System.out.print((char)(bx+'a'));
      System.out.print((char)(by+'1'));
      
      if(type != 0) {
         System.out.print(":"+pChars.charAt(type));
      }
      
   }
   
   /** Check if a move is legal */
   public boolean isLegal(Piece[][] board, boolean turn) {
      boolean legal = false;
      
      if(turn == board[ax][ay].color) {
         if(board[bx][by].type == 0 || board[bx][by].color != turn) {
            switch(board[ax][ay].type) {
               case 1: //Pawn
                  if(ax == bx) {
                     if((by - ay) == (board[ax][ay].color ? 1 : -1)) { //Single jump
                        legal = (board[bx][by].type == 0); //Legal if target is empty
                     }
                     else if (ay == (board[ax][ay].color ? 1 : 6)) {
                        if ((by - ay) == (board[ax][ay].color ? 2 : -2)) { //Double Jump
                           if(board[ax][ay + (turn ? 1 : -1)].type == 0) { //Is path clear?
                              legal = (board[bx][by].type == 0); //Legal if target is empty
                           }
                        }
                     }
                  }
                  else if ((Math.abs(bx - ax) == 1) && ((by - ay) == (board[ax][ay].color ? 1 : -1))) { //Attack
                     legal = (board[bx][by].type != 0); //Legal if target isn't empty
                  }
                  break;
               //Promotions are not tested here, that happens in the input method of the Chess class
                              
               case 2: //Rook
                  if(ax == bx) { //Vertical
                     legal = true;
                     boolean direction = ay < by;
                     for(int i = ay + (direction ? 1 : -1); direction ? (i < by) : (i > by); i += direction ? 1 : -1) {
                        if(board[ax][i].type != 0) {
                           legal = false;
                           break;
                        }
                     }
                  }
                  else if(ay == by) { //Horizantal
                     legal = true;
                     boolean direction = ax < bx;
                     for(int i = ax + (direction ? 1 : -1); direction ? (i < bx) : (i > bx); i += direction ? 1 : -1) {
                        if(board[i][ay].type != 0) {
                           legal = false;
                           break;
                        }
                     }
                  }
                  break;
               case 3: //Knight
                  int xDist = Math.abs(bx-ax);
                  int yDist = Math.abs(by-ay);
                  
                  legal = ((xDist == 2) && (yDist == 1)) || ((xDist == 1) && (yDist == 2));
                  break;
               case 4: //Bishop
                  if(Math.abs(ax - bx) == Math.abs(ay - by)) { //Diagonal
                     legal = true;
                     boolean xDir = ax < bx;
                     boolean yDir = ay < by;
                     
                     int distance = Math.abs(ax - bx);
                     
                     for(int i = 1; i < distance; i++) {
                        if(board[ax+(i*(xDir ? 1 : -1))][ay+(i*(yDir ? 1 : -1))].type != 0) {
                           legal = false;
                           break;
                        }
                     }
                  }
                  break;
               case 5: //Queen (Same code from Rook and Bishop)
                  if(ax == bx) { //Vertical
                     legal = true;
                     boolean direction = ay < by;
                     for(int i = ay + (direction ? 1 : -1); direction ? (i < by) : (i > by); i += direction ? 1 : -1) {
                        if(board[ax][i].type != 0) {
                           legal = false;
                           break;
                        }
                     }
                  }
                  else if(ay == by) { //Horizantal
                     legal = true;
                     boolean direction = ax < bx;
                     for(int i = ax + (direction ? 1 : -1); direction ? (i < bx) : (i > bx); i += direction ? 1 : -1) {
                        if(board[i][ay].type != 0) {
                           legal = false;
                           break;
                        }
                     }
                  }
                  else if(Math.abs(ax - bx) == Math.abs(ay - by)) { //Diagonal
                     legal = true;
                     boolean xDir = ax < bx;
                     boolean yDir = ay < by;
                     
                     int distance = Math.abs(ax - bx);
                     
                     for(int i = 1; i < distance; i++) {
                        if(board[ax+(i*(xDir ? 1 : -1))][ay+(i*(yDir ? 1 : -1))].type != 0) {
                           legal = false;
                           break;
                        }
                     }
                  }
                  break;
               case 6: //King
                  legal = (Math.abs(bx-ax) <= 1) && (Math.abs(by-ay) <= 1); //Is the move within one step on any axis?
            }
         }
         else if ((ax == bx) && (ay == by)) {
            System.out.println("Coordinates are the same position.");
         }
         else {
            System.out.println("Cannot take your own pieces.");
         }
      }
      else {
         System.out.println("Not "+(!turn ? "white's" : "black's")+" turn.");
      }
      return legal;
   }
}