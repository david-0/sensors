package org.myprojects.testWs2811B;

import java.io.IOException;

import com.github.mbelling.ws281x.Color;
import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class App 
{
	public static void main(String[] args) throws UnsupportedBusNumberException, IOException, InterruptedException
    {
		Ws281xLedStrip ws281xLedStrip = new Ws281xLedStrip(8, 18, 800000, 10, 255, 0, false, LedStripType.WS2811_STRIP_GBR, true);
		ws281xLedStrip.setStrip(Color.GREEN);
		ws281xLedStrip.render();
		Thread.sleep(5000);
		ws281xLedStrip.setStrip(Color.WHITE);
		ws281xLedStrip.render();
		Thread.sleep(5000);
		ws281xLedStrip.setStrip(Color.RED);
		ws281xLedStrip.render();
		Thread.sleep(5000);
		ws281xLedStrip.setStrip(Color.BLUE);
		ws281xLedStrip.render();
		Thread.sleep(5000);
    }
}
