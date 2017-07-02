package vit.projekt;

import lejos.hardware.Button;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.utility.Delay;

public class Messung
{
	float samples[] = new float[1];
	EV3ColorSensor light;	
	Fortbewegung fort = Fortbewegung.getInstance();
	BarcodeScanner myLineReaderM;
	Anzeige anzeigen = Anzeige.getInstance();
	float caliGrenze; // Pauschal: 0 schwarz, 1 weiß
	boolean debug;
	boolean zeit;
	long block;
	float weissgleich=1; //wird bei erkenneStart kalibriert
	float schwarzgleich=1; //wird bei erkenneStart kalibriert
	
	//Gewünscht ist genau eine Instanz der Klasse Fortbewegung, da sonst die Fehlermeldung "Port Open" angezeigt wird. //TODO potenzieller Flaschenhals?
	  // Quelle: https://de.wikibooks.org/wiki/Muster:_Java:_Singleton
	  // https://javabeginners.de/Design_Patterns/Singleton_-Pattern.php
	  // Innere private Klasse, die erst beim Zugriff durch die umgebende Klasse initialisiert wird
	  private static final class InstanceHolderM {
	    // Die Initialisierung von Klassenvariablen geschieht nur einmal 
	    // und wird vom ClassLoader implizit synchronisiert
	    static final Messung INSTANCE = new Messung();
	  }

	  // Verhindere die Erzeugung des Objektes über andere Methoden
	  private Messung() 
	  {
		  light = new EV3ColorSensor(SensorPort.S4);
		  light.setCurrentMode("Red"); // hier wird Betriebsmodus gesetzt
		  myLineReaderM = new BarcodeScanner(zeit,debug);
		  //this.zeit=myLineReaderM.getZeit(); //funktioniert nicht, da es scheinbar mehrere Instanzen von BarcodeScanner gibt...
		  //this.debug=myLineReaderM.getDebug();				  
	  }
	  // Eine nicht synchronisierte Zugriffsmethode auf Klassenebene.
	  public static Messung getInstance () {
	    return InstanceHolderM.INSTANCE;
	  }
		
	public void setDebugUndZeit(boolean debug, boolean zeit)
	{
		this.debug=debug;
		this.zeit=zeit;
	}
	  
	public long getBlock()
	{
		return this.block;
	}  
	  
	
	public float getCaliGrenze() //Lennimethode
	{
		return this.caliGrenze;
	}
	
	public float ersterScan()
	{
		light.fetchSample(samples, 0);
		while (samples[0] == 0)
		{
			light.fetchSample(samples, 0);
		}
		return samples[0];
	}
	
	public float scanne(float letzterWert)
	{
		return (this.ersterScan()+letzterWert)/2;
	}	
	
	public long erkenneFarbe(String dunkel)
	{
		long aktBlock;
		if(this.zeit)
		{
			aktBlock = -System.currentTimeMillis();
		}
		else
		{	
			aktBlock = -fort.getTachoCount();
		}
		// long timeBlock= -System.nanoTime(); //verursacht astronomische Zahlen, die so genau gar nicht sein müssen. TODO Dank Mikrosekunden reich vielleicht auch int?
		float aktWert = this.ersterScan();
		if (dunkel.equals("1"))
		{
			while (aktWert < (caliGrenze*schwarzgleich) && Button.ENTER.isUp())
			{
				aktWert = this.scanne(aktWert);
			}
		} 
		else
		{
			while ((aktWert > (caliGrenze*weissgleich)) && Button.ENTER.isUp()) //Beispiel: caliGrenze*0.7 bei 1cm
			{
				aktWert = this.scanne(aktWert);
			}
			//anzeigen.drawString("erkenneWeiss");
		}
		
//		if(debug)
//		{
//			anzeigen.drawString("AktWert: " + aktWert);
//		}
		
		if(this.zeit)
		{
			return aktBlock + System.currentTimeMillis();
		}
		else
		{	
			return aktBlock + fort.getTachoCount();
		}
	}

	/**
	 * Calibriert "Hell" und "Dunkel"
	 */
	public void calibrate()
	{
		float caliHell ;
		float caliDunkel;
		// Ab hier wird losgemessen
		
		while (Button.ENTER.isUp()) 
		{
			anzeigen.clearLCD();
			anzeigen.drawString("Helle Fleche stellen");
			anzeigen.drawString("druecken sie ENTER");
			while (Button.ENTER.isUp());
			Delay.msDelay(1000);
			caliHell = this.ersterScan();
			/*
			 * While schleife wird durch die Methode scannen ersetzt
			 */
			anzeigen.drawString("HelleFläche: " + caliHell);
			while (Button.ENTER.isDown()); // verhindert das Hell und Dunkel gleichzeitig gesetzt werden
			anzeigen.clearLCD();
			anzeigen.drawString("Dunkle Fleche stellen");
			anzeigen.drawString("druecken sie ENTER");
			while (Button.ENTER.isUp());
			Delay.msDelay(1000);
			caliDunkel = this.ersterScan();
			/*
			 * While schleife wird durch die Methode scannen ersetzt
			 */
			anzeigen.drawString("Hell: " + caliHell);
			anzeigen.drawString("Dunkel: " + caliDunkel);
			caliGrenze = caliDunkel + ((caliHell - caliDunkel) / 2); // Achtung,
																		// beachtet
																		// nicht
																		// Punkt vor
																		// Strich
																		// Rechnung!
			anzeigen.drawString("Grenze: " + caliGrenze);
			while (Button.ENTER.isDown()); // verhindert das die Kalibrierung versehentlich zu früh beendet wird.
			anzeigen.drawString("");
			anzeigen.drawString("Bitte an den Start stellen");
			anzeigen.drawString("druecken sie ENTER");
			anzeigen.drawString("oder ESC");
			while (Button.ENTER.isUp() && Button.ESCAPE.isUp());
		}	
	}
	
	public void pruefeBeginnRichtigSteht(boolean dunkel)
	{
		float a = this.ersterScan(); 
		
		if(dunkel && (a > caliGrenze))
		{
			anzeigen.drawString("Bitte auf Schwarz stellen");
		}
		else if ((!dunkel) && (a < (caliGrenze-(caliGrenze/2))))
		{
			anzeigen.drawString("Bitte auf Weiß stellen");
		}
		if((!dunkel && (a < (caliGrenze-(caliGrenze/2)))) || (dunkel && (a > caliGrenze)))
		{	
			anzeigen.drawString("und ENTER drücken");
			while (Button.ENTER.isUp());
			anzeigen.warte(3,"Pruefe");
			this.pruefeBeginnRichtigSteht(dunkel);
		}
		if(debug)
		{
			anzeigen.drawString("pruefeBeginnRichtigSteht bestanden");
		}		
	}
	
	/**
	 * Soll den Start erkennen und die Abstände eines Blockes calibrieren.
	 */
	public String erkenneStart(String startString)//startString = z.b. 1010
	{		
		long schwarz=0;
		long schwarz2=0;		
		long weiss=0;
		if (startString.length()!=4)
		{
			anzeigen.drawString("Es werden genau 4 Werte benötigt",3);
			anzeigen.drawString("ESC zum beenden",4);
			System.exit(1);
		}
		if(startString.substring(0, 1).equals("1"))
		{
			this.pruefeBeginnRichtigSteht(false);
		}
		else if(startString.substring(0, 1).equals("0"))
		{
			this.pruefeBeginnRichtigSteht(true);
		}
		else
		{
			anzeigen.drawString("Nur 0 oder 1",3);
			anzeigen.drawString("ESC zum beenden",4);
			System.exit(1);
		}
		long block=0; //block MUSS auf jedenfall gesetzt werden!
		
			if(startString.substring(0, 1).equals("1"))
			{	
				fort.fahre();
				this.erkenneFarbe("0");
			}
			else if(startString.substring(0, 1).equals("0"))
			{
				fort.fahre();
				this.erkenneFarbe("1");
			}	
			else
			{
				anzeigen.drawString("Nur 0 oder 1",3);
				anzeigen.drawString("ESC zum beenden",4);
				System.exit(1);
			}
			boolean restart=true;
			while(restart && Button.ESCAPE.isUp())
			{	
				restart=false;
			if(this.zeit)
			{
				block = -System.currentTimeMillis();
			}
			else
			{	
				block = -fort.getTachoCount();
			}
			for(int i = 0; i < 3; i++)
			{	
				long strecke = this.erkenneFarbe(startString.substring(i, i+1));
				if(strecke==0)
				{
					restart=true;
					i=3; //fliege aus der Schleife
					if(debug)
					{
						anzeigen.drawString("Restart Start");
					}
				}
				else if(startString.substring(i, i+1).equals("1"))
				{
					if (schwarz==0)
					{	
						schwarz=strecke;
					}
					else
					{
						schwarz=(schwarz+strecke)/2;
						schwarz2=strecke;
					}		
				}
				else
				{					
						weiss=strecke;	
				}
				if(debug)
				{
					//anzeigen.drawString(startString.substring(i, i+1));
					anzeigen.drawString("Strecke: " +strecke);
				}
			}
			if(restart)
			{
				fort.fahreZurueck(0);
				if(startString.substring(0, 1).equals("1"))
				{	
					this.erkenneFarbe("0");
				}
				else
				{
					this.erkenneFarbe("1");
				}	
			}
		}	
		// TODO ist Integer/long gut? denk dran Nachkommastellen werden abgeschnitten
		if(this.zeit)
		{
			block = (block + System.currentTimeMillis())/3;
		}
		else
		{	
			block = (block + fort.getTachoCount()) / 3; 
		}
		
		if(schwarz<weiss && schwarz2 <weiss) //< wegen Messungenauigkeiten
		{
			this.schwarzgleich = (float) schwarz / (float) weiss; //TODO herausfinden ob Cast to float Nebenwirkungen hat
			//float schwarzgleich2 = (float) schwarz2 / (float) weiss; zu hohe Nebenwirkungen, Werte zu "scharf"
			if(debug)
			{
				anzeigen.drawString("W:"+weiss+" S:"+schwarz);
				anzeigen.drawString("SF:"+schwarzgleich);
				//anzeigen.drawString("SF:"+schwarzgleich2);
			}
		}
		else
		{
			this.weissgleich = (float) weiss / (float) schwarz; //TODO herausfinden ob Cast to float Nebenwirkungen hat
			if(debug)
			{
				anzeigen.drawString("W:"+weiss+" S:"+schwarz);
				anzeigen.drawString(" WF:"+weissgleich);
			}
			
		}
		anzeigen.drawString(""+block);
		this.block=block;
		return startString.substring(3);	
	}
}
