import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
 
public class SwingExample implements Runnable {

    @Override
    public void run() {
        // Create the window
        JFrame f = new JFrame("Hello, !");
        // Sets the behavior for when the window is closed
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setBounds(100, 100, 612, 484);
        // Add a layout manager so that the button is not placed on top of the label
        f.setLayout(new FlowLayout());
        // Add a label and a button
        f.add(new JLabel("Hello, world!"));
        
        ImageIcon icon = new ImageIcon("C:\\Users\\pghst\\Documents\\Java\\Chess\\Icons\\BishopBlack.png");
        
        for(int i = 0; i < 64; i++)
            f.add(new JLabel(icon));
            
        f.add(new JButton("Press me!"));
        // Arrange the components inside the window
        //f.pack();
        // By default, the window is not visible. Make it visible.
        f.setVisible(true);
    }
 
    public static void main(String[] args) {
        SwingExample se = new SwingExample();
        // Schedules the application to be run at the correct time in the event queue.
        SwingUtilities.invokeLater(se);
    }

}