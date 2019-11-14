import javax.swing.*;
import javax.swing.UIManager.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class GUI extends JFrame {
   private JPanel panel;
   private BoardPanel graphicsBoard;
   private JList movesList;
   
   private Board board;
   
   private BufferedImage[] pieceImg;
   public final String[] pieceNames = {"Pawn", "Rook", "Knight", "Bishop", "Queen", "King"};
   
   public GUI() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(640, 640);
      setTitle("Chess");
      
      loadPieces();
      board = new Board();
      
      buildWindow();
      
      setVisible(true);
   }
   
   /** Updates the game board*/
   public void updateBoard(Board b) {
      board = b;
      graphicsBoard.repaint();
   }
   
   /** Selects a move from the moves list */
   public void selectMove(int index) {
      movesList.setSelectedIndex(index);
   }
   
   /** Selects moves from the moves list */
   public void selectMoves(int[] indices) {
      movesList.setSelectedIndices(indices);
   }
   
   /** Updates the moves list */
   public void updateMoves(String[] moves) {
      movesList.setListData(moves);
   }
   
   private void buildWindow() {
      panel = new JPanel();
      panel.setLayout(new BorderLayout());
      
      //Add in the moves list on the right
      JPanel movesPanel = new JPanel();
      movesPanel.setBorder(BorderFactory.createTitledBorder("Moves"));
      String[] things = new String[1];
      things[0] = "no";
      
      movesList = new JList(things);
      movesList.setLayoutOrientation(JList.VERTICAL);
      movesList.setVisibleRowCount(-1);
      movesList.setPreferredSize(new Dimension(128, 512));
      
      movesPanel.add(movesList);
      panel.add(movesPanel, BorderLayout.EAST);
      
      //Add in the game board
      JPanel boardPanel = new JPanel();
      boardPanel.setBorder(BorderFactory.createTitledBorder("Board"));
      
      graphicsBoard = new BoardPanel();
      boardPanel.add(graphicsBoard);
      panel.add(boardPanel, BorderLayout.CENTER);
      
      add(panel);
      
      pack();
   }
   
   /** Loads in the images for each piece */
   private void loadPieces() {
      pieceImg = new BufferedImage[12];
      
      try {
         for(int i = 0; i < 6; i++) {
            //System.out.println("w" + pieceNames[i] + "Small.png");
            //System.out.println("b" + pieceNames[i] + "Small.png");
            
            pieceImg[i] = ImageIO.read(new File("w" + pieceNames[i] + "Small.png"));
            pieceImg[i + 6] = ImageIO.read(new File("b" + pieceNames[i] + "Small.png"));
         }
      }
      catch (Exception e) {
      }
   }
   
   public static void main(String[] args) {
      //Set the look and feel
      try {
         for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (Exception e) {
      // If Nimbus is not available, you can set the GUI to another look and feel.
      }
      
      new GUI();
   }
   
   private class BoardPanel extends JPanel {
      public void paintComponent(Graphics g) {
         super.paintComponent(g);
         
         Color color1 = new Color(128, 72, 16); //Black
         Color color2 = new Color(255, 144, 32); //White
         
         for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 8; x++) {
               //Choose what color to use
               boolean tileColor = (x + y) % 2 > 0;
               
               //Draw the board tiles
               g.setColor(tileColor ? color1 : color2);
               g.fillRect(x * 64, y * 64, 64, 64);
               
               //Determine what piece is here
               int type = board.board[x][7 - y].type;
               
               //If there is a piece in this spot, draw it
               if(type != 0) {
                  //Choose whether to draw a black or white piece
                  type += board.board[x][7 - y].color ? 0 : 6;
                  g.drawImage(pieceImg[type - 1], x * 64, y * 64, null);
               }
            }
         }
        
         //g.drawLine(0, 0, 512, 512);
      }
      
      public BoardPanel() {
         setBackground(Color.white);
         setPreferredSize(new Dimension(512, 512));
      }
   }
}