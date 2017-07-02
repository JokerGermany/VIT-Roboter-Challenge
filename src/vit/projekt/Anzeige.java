package vit.projekt;

import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class Anzeige
{
	int zeile=0;
	
  //Gewünscht ist genau eine Instanz der Klasse Anzeige, damit die Zeile mit übernommen wird. //TODO potenzieller Flaschenhals?
  // Quelle: https://de.wikibooks.org/wiki/Muster:_Java:_Singleton
  // https://javabeginners.de/Design_Patterns/Singleton_-Pattern.php
  // Innere private Klasse, die erst beim Zugriff durch die umgebende Klasse initialisiert wird
  private static final class InstanceHolderA 
  {	  
    // Die Initialisierung von Klassenvariablen geschieht nur einmal 
    // und wird vom ClassLoader implizit synchronisiert
    static final Anzeige INSTANCE = new Anzeige();
  }

  // Verhindere die Erzeugung des Objektes über andere Methoden
  private Anzeige () {}
  // Eine nicht synchronisierte Zugriffsmethode auf Klassenebene.
  public static Anzeige getInstance () 
  {
    return InstanceHolderA.INSTANCE;
  }

  /**
	 * Wartet die übergebene Zeit in Sekunden 
	 * @param sekunden Zeit
	 * @pram text Text
	 */	  
	public void warte(int sekunden, String text)
	{
		this.clearLCD();
		this.drawString(text+" in "+sekunden+" Sekunden",3);
		Delay.msDelay(sekunden*1000); //Damit der Roboter nicht vom (Be)diener beeinflusst wird
		this.clearLCD();		
	}	  
	
	public void clearLCD()
	{
		LCD.clear();
		zeile=0;
	}
	
	public void drawString(String str, int y)
	{
		LCD.drawString(str, 0, y);
	}
	
	public void drawString(String str)
	{
		if(zeile > 7)
		{
			LCD.scroll();
			LCD.drawString(str, 0, 7);
		}
		else
		{
			LCD.drawString(str, 0, zeile);
			this.zeile++;
		}				
	}
}
