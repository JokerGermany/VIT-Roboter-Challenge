package vit.projekt;

import lejos.hardware.Sound;
import java.io.File;

public class Ton
{

	//Gewünscht ist genau eine Instanz der Klasse Fortbewegung, da sonst die Fehlermeldung "Port Open" angezeigt wird. //TODO potenzieller Flaschenhals?
		 // Quelle: https://de.wikibooks.org/wiki/Muster:_Java:_Singleton
		  // https://javabeginners.de/Design_Patterns/Singleton_-Pattern.php
		  // Innere private Klasse, die erst beim Zugriff durch die umgebende Klasse initialisiert wird
		  private static final class InstanceHolderT {
			  
		    // Die Initialisierung von Klassenvariablen geschieht nur einmal 
		    // und wird vom ClassLoader implizit synchronisiert
		    static final Ton INSTANCE = new Ton();
		  }

		  // Verhindere die Erzeugung des Objektes über andere Methoden
		  private Ton() 
		  {
			  Sound.setVolume(100);
		  }
		  // Eine nicht synchronisierte Zugriffsmethode auf Klassenebene.
		  public static Ton getInstance () {
		    return InstanceHolderT.INSTANCE;
		  }
		  
		  public void ausgebenErgebnis(String strichcode)
		  {
			  while(strichcode.length()>0)
			  {
				  int zahl=Integer.parseInt(strichcode.substring(0, 1));
				  File sound = new File(zahl+".wav");
				  Sound.playSample(sound);
				  strichcode=strichcode.substring(1);
			  }
		  }
		  
		  public void ausgebenZahl(int zahl)
		  {
			  File sound = new File(zahl+".wav");
			  Sound.playSample(sound);
		  }
}
