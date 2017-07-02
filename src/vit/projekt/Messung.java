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
		  //this.zeit=myLineReaderM.getZeit();
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
		// //LCD.clear();
		// long timeBlock= -System.nanoTime();
		float aktWert = this.ersterScan();
		if (dunkel.equals("1"))
		{
			while (aktWert < (caliGrenze) && Button.ENTER.isUp())
			{
				aktWert = this.scanne(aktWert);
				// fort.fahre;
			}
			//anzeigen.drawString("erkenneSchwarz");
		} 
		else
		{
			//Grenze für weiß erhöht, da sonst zu schnell schwarz erkannt wird
			//while (aktWert > (caliGrenze-(caliGrenze/2)) && Button.ENTER.isUp()) // TODO: An die Blockgröße anpassen - Wird bei 1cm nicht benötigt!
			while (aktWert > (caliGrenze) && Button.ENTER.isUp())
			{
				aktWert = this.scanne(aktWert);
				// fort.fahre;
			}
			//anzeigen.drawString("erkenneWeiss");
			//Sound.beep();
		}
		/*
		 * Crazy Schleife welche aus 2 Schleifen eine Schleife machen würde,
		 * aber relativ kompliziert ist. float wert1; float wert2;
		 * if(dunkel==true) { wert1 = aktWert; wert2 = caliGrenze;
		 * LCD.drawString("erkenneSchwarz",0,0); } else { wert1 = caliGrenze;
		 * wert2 = aktWert; LCD.drawString("erkenneWeiß",0,0); } while(wert1 <
		 * wert2) //aktWert < caliGrenze - schwarz //caliGrenze < aktWert - weiß
		 * { aktWert = this.scanne(); LCD.drawString("AktWert: "+aktWert,0,1); }
		 */
		if(debug)
		{
			//anzeigen.drawString("AktWert: " + aktWert);
		}
		// timeBlock += System.nanoTime(); // Wird in der Methode erkenne start
		// gemacht
		// return new Rueckgabe(aktWert, timeBlock);
		if(this.zeit)
		{
			return aktBlock + System.currentTimeMillis();
		}
		else
		{	
			return aktBlock + fort.getTachoCount();
		}
		//return aktDegreeBlock + this.getTachoCount();
	}

	public void calibrate()
	{
		//float samples[] = new float[light.sampleSize()]; // wird in dieser
														// Methode mehrfach
														// verwendet
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
			// while (sample[0]==0 || caliHell==2) //TODO: Nice to have: abfangen
			// wenn Hell abgefragt aber auf dunkel gestellt
			// {
			// light.fetchSample(sample, 0);
			// caliHell = sample[0];
			// //Delay.msDelay(5000);
			// }
			anzeigen.drawString("HelleFläche: " + caliHell);
			// Delay.msDelay(5000); //TODO KILLME
			while (Button.ENTER.isDown()); // verhindert das Hell und Dunkel gleichzeitig gesetzt werden
			anzeigen.clearLCD();
			// TODO Wenn nicht zufrieden ESC drücken und Methode neu aufrufen, sonst
			// ENTER
			// Delay.msDelay(5000); //TODO KILLME
			anzeigen.drawString("Dunkle Fleche stellen");
			anzeigen.drawString("druecken sie ENTER");
			while (Button.ENTER.isUp());
			Delay.msDelay(1000);
			caliDunkel = this.ersterScan();
			/*
			 * While schleife wird durch die Methode scannen ersetzt
			 */
			// while (sample[0]==0 || caliDunkel==2) //TODO: Nice to have: abfangen
			// wenn Dunkel abgefragt aber auf hell gestellt
			// {
			// light.fetchSample(sample, 0);
			// caliDunkel = sample[0];
			// //Delay.msDelay(5000);
			// }
			//LCD.clear();
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
			myLineReaderM.warte(3); //FIXME starte in 3 Sekunden
			this.pruefeBeginnRichtigSteht(dunkel);
		}
		if(debug)
		{
			anzeigen.drawString("pruefeBeginnRichtigSteht bestanden");
		}
		//LENNI: Einfach schwarz erkennen; fährt bis weiß und los gehts.		
	}
	
	public String erkenneStart(String startString)//startString = z.b. 1010
	{		
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
				else if(debug)
				{
					//anzeigen.drawString(startString.substring(i, i+1));
					anzeigen.drawString("Strecke: " +strecke);
				}
			/*if(i==3)//Nach dem 3. Durchgang (Die 0 zählt mit!) Zeitmessung stoppen
			{
				/*if(debug)
				{
					anzeigen.drawString("Block:" + block);
					if(this.zeit)
					{
						anzeigen.drawString("GesamtStr:" + (System.currentTimeMillis() - Streckenanfang));
					}
					else
					{	
						anzeigen.drawString("GesamtStr:" + (this.getTachoCount() - Streckenanfang));
					}
				}*/
				// TODO ist Integer/long gut? denk dran Nachkommastellen werden abgeschnitten
				
				
				// Toleranz von einem 1/4.
				/*if(debug)
				{
					anzeigen.drawString("Toleranz:" + toleranzBlock); TODO reinnehmen
					LCD.drawString("TBlock: "+ergebnis3.timeBlock,0,2);
					while (Button.ENTER.isUp());
				}
			}*/
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
		if(this.zeit)
		{
			block = (block + System.currentTimeMillis())/3;
		}
		else
		{	
			block = (block + fort.getTachoCount()) / 3; 
		}
		anzeigen.drawString(""+block);
		this.block=block;
		return startString.substring(3);	
	}
}
