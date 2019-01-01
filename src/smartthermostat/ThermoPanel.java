package smartthermostat;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

public class ThermoPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener
{
    TempController controller;
    TempSensor sensor;
    
    private Timer spinTimer;
    private Timer tempTimer;
    
    private Shape ring;
    private Color ringColor;
    private final int x = 400;
    private final int y = 240;
    private final int r = 120;
    private final int thickness = 25;
    private final float degreesPerTurn = 5f;
    
    private int xS; //Slider X
    private int yS; //Slider Y
    private boolean slider;
    private double alpha;
    private double ang;
    private double direction;
    
    private Point menuPoint;
    private boolean menu;
    private int option;
    private final String[] optionStrings = {"Heating", "Fan On", "Cooling", "Off"};
    private final Font optionFont = new Font("sansserif", Font.BOLD, 20);
    
    private Image img;
    private Image[] options;
    
    private final Font tempFont = new Font("sansserif", Font.BOLD, 52);
    private float tempTarget;
    private double ambientTemp;
    
    public ThermoPanel()
    {
        addMouseListener(this);
        addMouseMotionListener(this);
        
        tempTimer = new Timer(5000, this);
        tempTimer.start();
        controller = new TempController(tempTimer);
        sensor = new TempSensor();
        
        xS = x;
        yS = y;
        direction = 1;
        
        tempTarget = 70;   //Temperature, in Fahrenheit
        
        menuPoint = new Point();
        option = 4;
        
        ring = createRing(x, y, r, thickness);
        ringColor = new Color(0,0,0);
        
        spinTimer = new Timer(15, this);
        
        try
        {

            InputStream in = getClass().getClassLoader().getResourceAsStream("resources/Images/background.jpg");
            img = ImageIO.read(in);
            in.close();
            
            String[] optionPaths = new String[]
            {
                "resources/Images/noSelection.png",
                "resources/Images/heatSelected.png",
                "resources/Images/fanSelected.png",
                "resources/Images/coolSelected.png",
                "resources/Images/offSelected.png"
            };
            options = new Image[optionPaths.length];
            for(int i = 0; i < optionPaths.length; i++)
            {
                in = getClass().getClassLoader().getResourceAsStream(optionPaths[i]);
                options[i] = ImageIO.read(in);
                in.close();
            }
        }
        catch(Exception e)
        {
            System.out.println("ThermoPanel constructor");
            e.printStackTrace();
        }
    }
    
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        setBackground(Color.WHITE);
        
        Graphics2D g1  = (Graphics2D)g;
        g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        //Draw Background
        g1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g1.drawImage(img, 0, 0, this);
        g1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        
        //SliderBase
        g1.setColor(Color.BLACK);
        g1.draw(ring);
        g1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
            
        if(tempTarget > 65 && tempTarget < 85)  //Set ring color depending on temperature
        {
            ringColor = new Color(Math.abs(65-tempTarget)*(1f/20), 0, Math.abs(85-tempTarget)*(1f/20));
        }
        g1.setColor(ringColor);
        g1.fill(ring);
        g1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        
        //Slider
        drawSlider(g1);
        
        //Temperature
        g1.setColor(Color.BLACK);
        g1.setFont(tempFont);
        if(slider)
        {
            g1.drawString(String.valueOf((int)tempTarget + "°F"), x - (111 / 2),  y + (67/4));//x and y adjusted for specific height and width of font.
        }
        else
        {
            g1.drawString(String.valueOf((int)ambientTemp + "°F"), x - (111 / 2),  y + (67/4));
        }
        //Menu
        if(menu)
        {
            int tempX = (int)menuPoint.getX();
            int tempY = (int)menuPoint.getY();
            g1.drawImage(options[option], tempX - 50, tempY - 50, this); 
        }
        
        g1.setColor(Color.GREEN);
        g1.setFont(optionFont);
        g1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g1.drawString(optionStrings[option-1], 5, 475);
        g1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
    
    private Shape createRing(int x, int y, int r, int thickness)
    {
        Ellipse2D outside = new Ellipse2D.Double(x - r, y - r, 2*r, 2*r);
        int r1 = r - thickness;
        Ellipse2D inside = new Ellipse2D.Double(x - r1, y - r1, 2*r1, 2*r1);
        
        Area area = new Area(outside);
        area.subtract(new Area(inside));
        return area;
    }

    private void drawSlider(Graphics2D g1)
    {
        g1.setColor(new Color(0,0,0,(int)alpha));
        g1.fillOval(xS - (thickness/2), yS - (thickness/2), thickness+1, thickness+1);
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(spinTimer.isRunning())
        {
            if(alpha > 0)
            {
                alpha -= 3;
            }
            else
            {
                spinTimer.stop();
                tempTimer.start();
                controller.set(option, tempTarget);
            }
            double r2 = r - (thickness/2);
            
            ang -= 0.09 * direction;    
            xS = (int) ((r2 * Math.cos(ang)) + x);
            yS = (int) ((r2 * Math.sin(ang)) + y);
        }
        else if(tempTimer.isRunning())
        {
            ambientTemp = sensor.getTemp();
            controller.tick((int)ambientTemp);
        }
        
        repaint();
    }
    
    @Override
    public void mouseDragged(MouseEvent e)
    {
        //To clean up code later, create a slider object
        //which holds all info about slider, such as xS, xY,
        //ang, etc.
        if(slider)
        {
            double x1 = e.getX();
            double y1 = e.getY();
            double deltaAng = ang;
            if((x1-x) != 0)
            {
                ang = Math.atan((y1-y) / (x1-x));
                if((x1-x) < 0)
                {
                    ang -= Math.PI;
                }
                deltaAng -= ang;

                //Discard outlier changes in temp (due to arctangent's domain)
                double deltaT = ((degreesPerTurn / (Math.PI * 2)) * deltaAng);
                if(Math.abs(deltaT) > (degreesPerTurn / 3.0))
                {
                    deltaT = 0;
                }
                tempTarget -= deltaT;

                direction = (deltaT > 0) ? 1 : -1;

                double r2 = r - (thickness/2);
                xS = (int) ((r2 * Math.cos(ang)) + x);
                yS = (int) ((r2 * Math.sin(ang)) + y);

                repaint();
            }
        }
        else if(menu)
        {
            double x1 = menuPoint.getX();
            double y1 = menuPoint.getY();
            int x2 = e.getX();
            int y2 = e.getY();
            
            double theta = 0;
            
            if((x2-x1) != 0)
            {
                theta = Math.atan((y1-y2) / (x2-x1));
                
                if((x2-x1) < 0)
                {
                    theta += Math.PI;
                }
                else if((x2-x1) > 0)
                {
                    theta += (2*Math.PI);
                }
            }
            else if((y1-y2) > 0)
            {
                theta += (Math.PI/2);
            }
            else if((y1-y2) < 0)
            {
                theta += (3*Math.PI/2);
            }
            theta %= (2*Math.PI);
            
            int newOption = 0;
            if(theta >= (Math.PI/4) && theta < (3*Math.PI/4))
            {
                newOption = 2;
            }
            else if(theta >= (3*Math.PI/4) && theta < (5*Math.PI/4))
            {
                newOption = 3;
            }
            else if(theta >= (5*Math.PI/4) && theta < (7*Math.PI/4))
            {
                newOption = 4;
            }
            else
            {
                newOption = 1;
            }
            
            if(newOption != option)
            {
                option = newOption;
                repaint();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        if(e.getClickCount() == 5)
        {
            System.exit(0);
        }
        else if(ring.contains(e.getPoint()))
        {
            if(spinTimer.isRunning())
            {
                spinTimer.stop();
            }
            
            slider = true;
            alpha = 201;
        }
        else
        {
            menu = true;
            menuPoint = e.getPoint();
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if(slider)
        {
            slider = false;
            tempTimer.stop();
            spinTimer.start();
        }
        
        if(menu)
        {
            menu = false;
            repaint();
            controller.set(option, tempTarget);
            System.out.println("Temp set " + option + " heat, fan, cool, off");
        }
    }

    public void close()
    {
        sensor.closeBus();
        controller.close();
    }
    
    @Override
    public void mouseEntered(MouseEvent e)
    {
        
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        
    }
    
        @Override
    public void mouseMoved(MouseEvent e)
    {
        
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        
    }
}