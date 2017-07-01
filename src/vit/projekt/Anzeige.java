package vit.projekt;

import lejos.hardware.lcd.LCD;

public class Anzeige
{
	int zeile=0;
	
	public void clearLCD()
	{
		LCD.clear();
		this.zeile=0;
	}
	
	public void drawString(String str, int y)
	{
		LCD.drawString(str, 0, y);
	}
	
	public void drawString(String str)
	{
		if(this.zeile > 7)
		{
			LCD.scroll();
			LCD.drawString(str, 0, 7);
		}
		else
		{
			LCD.drawString(str, 0, this.zeile);
			this.zeile++;
		}				
	}
}
