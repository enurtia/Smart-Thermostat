package smartthermostat;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public class MainFrame extends JFrame
{
    private static final String TITLE = "Smart Thermostat";
    private static final Dimension SIZE = new Dimension(800, 480);
    private static ThermoPanel panel;
    
    public MainFrame()
    {
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                panel.close();
            }
        });
    }
    
    public static void main(String[] args)
    {
        MainFrame frame = new MainFrame();
        frame.setTitle(TITLE);
        frame.getContentPane().setPreferredSize(SIZE);
        frame.setResizable(false);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new ThermoPanel();
        frame.add(panel);

        frame.pack();
        frame.setVisible(true);
    }
}