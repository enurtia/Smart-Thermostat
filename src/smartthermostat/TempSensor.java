package smartthermostat;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;

public class TempSensor
{
    I2CBus i2cBus;
    I2CDevice device;
    
    public TempSensor()
    {
        try
        {
            i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
            device = i2cBus.getDevice(0x18);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public double getTemp()
    {
        double fahrenheit = 0;
        try
        {
            //Get ambient temp (address 0x05)
            byte[] data = new byte[2];
            device.read(0x05, data, 0, 2);

            //msb, only first 5 bits are needed
            int temp = ((data[0] & 0x1F) * 0x100 + data[1]);

            double c = temp * 0.0625; //Default resolution = 0.0625
            double f = c * 1.8 + 32;

            fahrenheit = Double.parseDouble(String.format("%.2f", f));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return fahrenheit;
    }
    
    public void closeBus()
    {
        try
        {
            i2cBus.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}