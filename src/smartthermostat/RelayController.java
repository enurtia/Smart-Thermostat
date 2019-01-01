package smartthermostat;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class RelayController
{
    private GpioPinDigitalOutput coolPin, heatPin, fanPin;
    private GpioController gpio;
    
    static 
    {
         System.setProperty("pi4j.linking", "dynamic");
    }
    
    public RelayController()
    {
        gpio = GpioFactory.getInstance();
        
        coolPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "Cool", PinState.HIGH);  //Relays off by default
        heatPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, "Heat", PinState.HIGH);
        fanPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "Fan", PinState.HIGH);
    }
    
    public void cool(boolean active)
    {
        if(active)
        {
            coolPin.low();
        }
        else
        {
            coolPin.high();
        }
    }
    
    public void heat(boolean active)
    {
        if(active)
        {
            heatPin.low();
        }
        else
        {
            heatPin.high();
        }
    }
    
    public void fan(boolean active)
    {
        if(active)
        {
            fanPin.low();
        }
        else
        {
            fanPin.high();
        }
    }
    
    public void closeGpio()
    {
        gpio.shutdown();
        gpio.unprovisionPin(coolPin);
        gpio.unprovisionPin(heatPin);
        gpio.unprovisionPin(fanPin);
    }
}