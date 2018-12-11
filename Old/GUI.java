import javax.swing.*;
import java.awt.*;

public class GUI {
   public static void main(String[] args) {
      JFrame frame = new JFrame("Chess GUI");
      
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(512, 384);
      frame.setVisible(true);
      
      frame.add();
   }
}