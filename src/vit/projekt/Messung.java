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
	
	/**
	 * Scannt den ersten Wert
	 * @return 1 Scannwert (nicht gemittelt!)
	 */
	public float ersterScan()
	{
		light.fetchSample(samples, 0);
		while (samples[0] == 0)
		{
			light.fetchSample(samples, 0);
		}
		return samples[0];
	}
	
	/**
	 * Ruft die Methode ersterScan auf und fügt dem vorhandenen Scannwert einen Wert hinzu
	 * @param letzter Wert
	 * @ return Durchschnitt aus gescannten neuen Wert und alten Wert
	 */
	public float scanne(float letzterWert)
	{
		return (this.ersterScan()+letzterWert)/2;
	}	
	
	/**
	 * In der Variablen aktBlock wird die Zeit/Gradzahl gespeichert, die der Roboter hierfür benötgt hat
	 * Anschließend wird der aktuelle Farbwert gemessen
	 * was passiert weiter //TODO
	 * @param dunkel true = schwarz
	 * @return
	 */
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
			while ((aktWert > (caliGrenze*weissgleich)) && Button.ENTER.isUp()) //Beispiel: caliGrenze*0.7 bei 1cm.
			{
				aktWert = this.scanne(aktWert);
				// fort.fahre;
			}
			//anzeigen.drawString("erkenneWeiss");
			//Sound.beep();
		}
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

	/**
	 * Calibriert "Hell" und "Dunkel" 
	 * Fordert den User auf ihn auf eine helle Fläche zu stellen
	 * Wenn Enter gedrückt wird, wird nun der Farbwert der Fläche genommen. 
	 * Wenn Enter losgelassen wird folgt die Aufforderung für schwarz und es folgt das Gleiche Prozedere
	 * 
	 * Es folgt die Aufforderung ihn an den Start zu stellen, wenn schwarz und weiß gemessen worden sind
	 */
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
	
	/**
	 * Wenn der Start mit schwarz beginnt müsste der Scanner auf weiß stehen.
	 * Er Scannt den aktuellen Wert. Wenn er einen flaschen Wert erhält fordert er seinen Herren auf
	 * ihn auf die richtige Stelle. Durch Enter wird nach einer kurzen Wartezeit erneut geprüft
	 * @param dunkel ob er auf weiß oder schwarz stehen muss
	 */
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
		//LENNI: Einfach schwarz erkennen; fährt bis weiß und los gehts.		
	}
	
	/**
	 * Wenn die erste Linie des Starts schwarz ist muss der Roboter auf weiß stehen [Wird geprüft]
	 * Wenn eine der Strecken falsch mit der Länge 0 zurück gegeben wird, fährt der Roboter zurück und versucht es erneut.
	 * Hier ist drauf zu achten, dass der Roboter am Anfang möglichst gerade steht.
	 * Im Attribut block wird nun die Zeit vom ersten Block mit der Zeit bis zum erkennen von dem 3. Blocken addiert
	 * Anschließend wird eine Tolleranz von 25% daraus errechnet 
	 * Weiterhin wird hier berechnet, ob der Messraum für weiß oder Schwarz vergrößert werden muss.
	 * Pauschal kann man sagen: 
	 * Um so größer die Blöcke sind, umso mehr weiß wird gemessen, sodass schwarzgleich angepasst werden muss.
	 * Um so kleiner die Blocke sind, umso mehr wird schwarz gemessen, sodass weissgleich angepasst werden muss.
	 * 
	 * @param Der übergebende String muss genau die Länge vier besitzen, da der Start aus vier Flächen besteht. 
	 * Er muss aus den Zeichen 0 (weiß) oder 1 (schwarz) bestehen.
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
			while (Button.ESCAPE.isUp());
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
			while (Button.ESCAPE.isUp());
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
				long streckeGefahren = this.erkenneFarbe(startString.substring(i, i+1));
				if(streckeGefahren==0)
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
						schwarz=streckeGefahren;
					}
					else
					{
						schwarz=(schwarz+streckeGefahren)/2;
						schwarz2=streckeGefahren;
					}		
				}
				else
				{					
						weiss=streckeGefahren;	
				}
				if(debug)
				{
					//anzeigen.drawString(startString.substring(i, i+1));
					anzeigen.drawString("Strecke: " +streckeGefahren);
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
			//float schwarzgleich2 = (float) schwarz2 / (float) weiss; zu hohe Nebenwirkungen
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
