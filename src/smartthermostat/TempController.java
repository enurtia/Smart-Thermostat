package smartthermostat;

import javax.swing.Timer;

public class TempController
{
    RelayController handler;
    TempSensor sensor;
    
    private Timer timer;
    private final double tolerance = 2; //Tolerance of temperature(in degrees) after reaching tempTarget.
    private boolean toleranceActive;
    
    private String option;
    private float tempTarget;
    
    private boolean fanActive;
    private boolean coolActive;
    private boolean heatActive;
    
    public TempController(Timer tempTimer)
    {
        handler = new RelayController();
        sensor = new TempSensor();
        
        option = "off";
        timer = tempTimer;
    }
    
    public void tick()
    {
        //For cooling and heating
        double temp = sensor.getTemp();
        
        if(option.equals("heat"))
        {
            if(temp >= tempTarget && !toleranceActive)
            {
                setHeat(false);
                toleranceActive = true;
            }
            else if(toleranceActive && (tempTarget - temp) >= tolerance)
            {
                setHeat(true);
                toleranceActive = false;
            }
            
        }
        else if(option.equals("cool"))
        {
            if(temp <= tempTarget && !toleranceActive)
            {
                setCool(false);
                toleranceActive = true;
            }
            else if(toleranceActive && (temp - tempTarget) >= tolerance)
            {
                setHeat(true);
                toleranceActive = true;
            }
        }
        
        check("activePerformed");
    }
    
    public void set(int opt, float temp)
    {
        switch(opt)
        {
            case(1):
                option = "heat";
                break;
            case(2):
                option = "fan";
                break;
            case(3):
                option = "cool";
                break;
            case(4):
                option = "off";
                break;
        }
        
        tempTarget = temp;
        
        if(option.equals("fan") || option.equals("off"))
        {
            if(timer.isRunning())
            {
                timer.stop();
            }
            
            if(option.equals("fan"))
            {
                setFan(true);
            }
            else if(option.equals("off"))
            {
                disableAll();
            }
            
            check("pub void set, if option.equals");        // 
        }
        else
        {
            if(option.equals("heat"))
            {
                if(coolActive)
                {
                    setCool(false);
                }
                else if(fanActive)
                {
                    setFan(false);
                }
            }
            else if(option.equals("cool"))
            {
                if(heatActive)
                {
                    setHeat(false);
                }
                else if(fanActive)
                {
                    setFan(false);
                }
            }
            
            timer.start();
            check("pub void set, if option.equals else");   //
        }
    }       
    
    private void disableAll()
    {
        setFan(false);
        setCool(false);
        setHeat(false);
    }
    
    private void setFan(boolean active)
    {
        //gpio fan off
        if(active)
        {
            handler.fan(true);
            fanActive = true;
            check("setFan active");                         //
        }
        else
        {
            handler.fan(false);
            fanActive = false;
            check("setFan active else");                    //
        }
    }
    
    private void setCool(boolean active)
    {
        //gpio cool off
        if(active)
        {
            handler.cool(true);
            coolActive = true;
            check("setCool active");                        //
        }
        else
        {
            handler.cool(false);
            coolActive = false;
            check("setCool active else");                   //
        }
    }
    
    private void setHeat(boolean active)
    {
        //gpio heat off
        if(active)
        {
            handler.heat(true);
            heatActive = true;
            check("setHeat active");                        //
        }
        else
        {
            handler.heat(false);
            heatActive = false; 
            check("setHeat active else");                   //
        }
    }
    
    private void check(String s)    //Delete after testing  //
    {
        if((heatActive && coolActive) || (fanActive && coolActive) || (fanActive && heatActive))
        {
            System.out.println("heat && cool: " + (heatActive && coolActive));
            System.out.println("fan && cool: " + (fanActive && coolActive));
            System.out.println("fan && heat: " + (fanActive && heatActive));
            
            disableAll();
            timer.stop();
            try
            {
                throw new Exception("Two relays running at once. -- " + s);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void closeGpio()
    {
        handler.closeGpio();
    }
}