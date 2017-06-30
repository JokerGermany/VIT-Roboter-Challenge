
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.utility.Delay;

/*
 * 0,0527777777778 cm/°
 */

public class BarcodeScanner
{
	float caliGrenze; // Pauschal: 0 schwarz, 1 weiß
	float samples[] = new float[1];
	//Object[] rueckgabe = new Object[2]; // Evtl nur fürs Debugging gebraucht
	EV3ColorSensor light;
	EV3LargeRegulatedMotor linkerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	EV3LargeRegulatedMotor rechterMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	long toleranzBlock;
	//int degreeBlock;
	long block; // long falls zeit ausgewählt wird
	boolean zeit;
	boolean debug;
	int zeile=0;
	boolean start;
	String dunkele=""; 
	String strichcode="";  
	
	//640 32
	//499 25
	//709 35
	
	// class Rueckgabe
	// {
	// float aktWert; //TODO: SChmittigenauigkeit: privat?
	// long timeBlock; //Wie lange braucht Robi für einen Block -
	// durchgetTachocount ersetzt

	/*
	 * nicht mehr gebraucht, da die Blöcke in der Methode erkenneStart gemesssen
	 * werden. public Rueckgabe(float aktWert, long timeBlock) {
	 * this.aktWert=aktWert; this.timeBlock=timeBlock; }
	 */
	// }
	
	public void warte(int sekunden)
	{
		this.clearLCD();
		this.drawString("Starte in "+sekunden+" Sekunden",3);
		Delay.msDelay(sekunden*1000); //Damit der Roboter nicht vom (Be)diener beeinflusst wird
		this.clearLCD();		
	}
	
	public void pruefeBeginnRichtigSteht(boolean dunkel)
	{
		float a = this.ersterScan(); 
		
		if(dunkel && (a > caliGrenze))
		{
			this.drawString("Bitte auf Schwarz stellen");
		}
		else if (!dunkel && (a < (caliGrenze-(caliGrenze/2))))
		{
			this.drawString("Bitte auf Weiß stellen");
		}
		if((!dunkel && (a < (caliGrenze-(caliGrenze/2)))) || (dunkel && (a > caliGrenze)))
		{	
			this.drawString("und ENTER drücken");
			while (Button.ENTER.isUp());
			this.warte(3);
			this.pruefeBeginnRichtigSteht(dunkel);
		}
		if(debug)
		{
			drawString("pruefeBeginnRichtigSteht bestanden");
		}
		//LENNI: Einfach schwarz erkennen; fährt bis weiß und los gehts.		
	}

	BarcodeScanner(boolean zeit, boolean debug)
	{
		//Motor.A.setSpeed(50);
		//Motor.D.setSpeed(50);
		light = new EV3ColorSensor(SensorPort.S4);
		light.setCurrentMode("Red"); // hier wird Betriebsmodus gesetzt		
		this.zeit=zeit;
		this.debug=debug;
		
	}
	public static void main(String[] args)
	{	
		boolean debug = true;
		boolean zeit = true; //Zeit oder Grad zur Messung verwenden?
		BarcodeScanner myLineReader = new BarcodeScanner(zeit, debug); 
		myLineReader.calibrate();		
		//myLineReader.caliGrenze = 0.4f; TODO Sei nicht so Faul du  Penner
		//LCD.clear();
		myLineReader.erkenneStart("1010");
		
		
		//34
		//35
		//31
		//  Steueung 15
		
		
		
		
		myLineReader.drawString("Fertig");
		myLineReader.stoppe();
		while (Button.ENTER.isUp()); // TODO KILLME
		//LCD.clear();
	}
	/*public void berechneStartBlockgroesse()
	{
		int anzahlBloecke=this.berechneBlockgroesse(false);
		if(anzahlBloecke==1)
		{
			//berechneBlockgroesse(true); //TODO Nach Test einkommentieren
			this.drawString("Weiss nur Start");
		}
		else
		{
			this.drawString(+(anzahlBloecke-1)+" Bloecke weiss");
			if((anzahlBloecke-1) > 3)
			{
				//0
			}
		}	
		this.start=false;
	}*/
	
		
		/* TODO Müll entfernen wenn sicher ist, dass es müll ist
		 
		 if(debug)
		{
// Der 4. Block des Starts (weiß) beginnt hoffentlich hier
			// Rueckgabe ergebnis4 = this.erkenneFarbe(false);
			this.drawString("AktWert: " + this.erkenneFarbe(false));
			// LCD.drawString("TBlock: "+ergebnis4.timeBlock,0,2);
			// while (Button.ENTER.isUp());
		}
		else
		{
// Der 4. Block des Starts (weiß) beginnt hoffentlich hier
			this.erkenneFarbe(false);
		}
		/*
		 * TODO Ende Methode entwickeln
		 */
	
	
	public void fahre()
	{
		int geschwindigkeit = 50;	//	Festsetzen der Geschwindigkeit in "Grad/Sekunde"
		int beschleunigung = 500;	//	Verzögerung von 500 ms bis Geschwindigkeit
		linkerMotor.resetTachoCount();					//	Tacho-Reset
		linkerMotor.setSpeed(geschwindigkeit);			//	setzen der Geschwindigkeit
		linkerMotor.setAcceleration(beschleunigung);	//	setzen der Beschleunigung
		rechterMotor.resetTachoCount();					//	Tacho-Reset
		rechterMotor.setSpeed(geschwindigkeit);			//	setzen der Geschwindigkeit
		rechterMotor.setAcceleration(beschleunigung);	//	setzen der Beschleunigung
				
		linkerMotor.backward();
		rechterMotor.backward();
		//Motor.A.backward();
		//Motor.D.backward();
	}

	public void stoppe()
	{
		linkerMotor.stop();
		rechterMotor.stop();
		//Motor.A.stop();
		//Motor.D.stop();
	}
	
	/*
	 * positve Zahlen...
	 */
	public int getTachoCount()
	{
		return (linkerMotor.getTachoCount()*-1); 
		//return (Motor.A.getTachoCount()*-1); 
	}
	
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

	/**
	 * TODO Kann hier noch optimiert werden?
	 */
	public float scanne(float letzterWert)
	{
		return (this.ersterScan()+letzterWert)/2;
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
	/*
	 * light.fetchSample(samples, 0);
		float rueckgabeWert=0;
		//for(float sample: samples)
		for(int i = 0; i < samples.length; i++)
		{	
			while (samples[i] == 0)
			{
				light.fetchSample(samples, i);
			}
			rueckgabeWert += samples[i];
		}	
		return rueckgabeWert / samples.length;
		*/

	

	
	
	/**
	 * Calibriert "Hell" und "Dunkel" TODO Kontrollieren
	 */
	public void calibrate()
	{
		float samples[] = new float[light.sampleSize()]; // wird in dieser
														// Methode mehrfach
														// verwendet
		float caliHell = 2; // Da der Wert eigentliche Wert nur zwischen 0-1
							// sein kann, 2 als initialsierung genommen
		float caliDunkel = 2; // Da der Wert eigentliche Wert nur zwischen 0-1
								// sein kann, 2 als initialsierung genommen
		// Ab hier wird losgemessen
		
		while (Button.ENTER.isUp()) 
		{
			this.clearLCD();
			this.drawString("Helle Fleche stellen");
			this.drawString("druecken sie ENTER");
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
			this.drawString("HelleFläche: " + caliHell);
			// Delay.msDelay(5000); //TODO KILLME
			while (Button.ENTER.isDown()); // verhindert das Hell und Dunkel gleichzeitig gesetzt werden
			this.clearLCD();
			// TODO Wenn nicht zufrieden ESC drücken und Methode neu aufrufen, sonst
			// ENTER
			// Delay.msDelay(5000); //TODO KILLME
			this.drawString("Dunkle Fleche stellen");
			this.drawString("druecken sie ENTER");
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
			this.drawString("Hell: " + caliHell);
			this.drawString("Dunkel: " + caliDunkel);
			caliGrenze = caliDunkel + ((caliHell - caliDunkel) / 2); // Achtung,
																		// beachtet
																		// nicht
																		// Punkt vor
																		// Strich
																		// Rechnung!
			this.drawString("Grenze: " + caliGrenze);
			while (Button.ENTER.isDown()); // verhindert das die Kalibrierung versehentlich zu früh beendet wird.
			this.drawString("");
			this.drawString("Bitte an den Start stellen");
			this.drawString("druecken sie ENTER");
			this.drawString("oder ESC");
			while (Button.ENTER.isUp() && Button.ESCAPE.isUp());
		}
			this.warte(3);
	
	}
	
// String Methoden beginnen hier
	/**
	 *
	 */
	/*
	 * ersetzt durch erkenneFarbe
	 * 
	 * 
	 * public Rueckgabe erkenneSchwarz() { //LCD.clear(); long timeBlock=
	 * -System.nanoTime(); float aktWert = this.scanne(); while(aktWert <
	 * caliGrenze && Button.ENTER.isUp()) //schwarz { aktWert = this.scanne();
	 * //this.fahre(); LCD.drawString("erkenneSchwarz",0,0);
	 * LCD.drawString("AktWert: "+aktWert,0,1); } //Sound.beep();
	 * //Sound.beep(); timeBlock += System.nanoTime(); //Object[] rueckgabe = {
	 * aktWert, timeBlock}; //return rueckgabe; return new Rueckgabe(aktWert,
	 * timeBlock); } /**
	 * 
	 * 
	 * //public float erkenneWeiß() old public Rueckgabe erkenneWeiß() {
	 * //LCD.clear(); long timeBlock= -System.nanoTime(); float aktWert =
	 * this.scanne(); while(aktWert > caliGrenze && Button.ENTER.isUp()) //weiß
	 * { aktWert = this.scanne(); //this.fahre();
	 * LCD.drawString("erkenneWeiss",0,0);
	 * LCD.drawString("AktWert: "+aktWert,0,1); } //Sound.beep(); timeBlock +=
	 * System.nanoTime(); //Object[] rueckgabe = { aktWert, timeBlock}; //return
	 * rueckgabe; return new Rueckgabe(aktWert, timeBlock); }
	 */
	/**
	 * Soll den Start erkennen und die Abstände eines Blockes calibrieren.
	 * Probleme hierbei könnte die Startlinie machen
	 */
	public void erkenneStart(String startString) //TODO Extrapunkte: Lasse den Start beliebig sein
	{
		if (startString.length()!=4)
		{
			this.drawString("Es werden genau 4 Werte benötigt",3);
			this.drawString("ESC zum beenden",4);
			System.exit(1);
		}
		if(startString.substring(0, 1).equals("1"))
		{
			this.pruefeBeginnRichtigSteht(false);
			this.fahre();
			this.erkenneFarbe("0");
		}
		else if(startString.substring(0, 1).equals("0"))
		{
			this.pruefeBeginnRichtigSteht(true);
			this.fahre();
			this.erkenneFarbe("1");
		}	
		else
		{
			this.drawString("Nur 0 oder 1",3);
			this.drawString("ESC zum beenden",4);
			System.exit(1);
		}
		if(this.zeit)
		{
			block = -System.currentTimeMillis();
		}
		else
		{	
			block = -this.getTachoCount();
		}	
		long Streckenanfang = block;
		for(int i = 0; i < 3; i++)
		{				
			if(debug)
			{
				this.drawString("Strecke: " + this.erkenneFarbe(startString.substring(i, i+1)));
			}
			else
			{
				this.erkenneFarbe(startString.substring(i, i+1));
			}		
			if(i==2)//Nach dem 3. Durchgang (Die 0 zählt mit!) Zeitmessung stoppen
			{
				/*if(debug)
				{
					this.drawString("Block:" + block);
					if(this.zeit)
					{
						this.drawString("GesamtStr:" + (System.currentTimeMillis() - Streckenanfang));
					}
					else
					{	
						this.drawString("GesamtStr:" + (this.getTachoCount() - Streckenanfang));
					}
				}*/
				// TODO ist Integer/long gut? denk dran Nachkommastellen werden abgeschnitten
				if(this.zeit)
				{
					block = (block + System.currentTimeMillis())/3;
				}
				else
				{	
					block = (block + this.getTachoCount()) / 3; 
				}
				toleranzBlock = (block / 4);
				
				// Toleranz von einem 1/4.
				/*if(debug)
				{
					this.drawString("Toleranz:" + toleranzBlock); TODO reinnehmen
					LCD.drawString("TBlock: "+ergebnis3.timeBlock,0,2);
					while (Button.ENTER.isUp());
				}*/
			}
		}
		this.berechneBlockgroesse(startString.substring(3));
		
		/*		
		if(debug)
		{
// Der 1. Block des Starts (Schwarz) beginnt hoffentlich hier			
			this.drawString("Strecke: " + this.erkenneFarbe("1"));
			// aktWert = (this.erkenneSchwarz())[0]; Funktioniert in Java leider
			// LCD.drawString("TBlock: "+ergebnis1.timeBlock,0,2);
			// while (Button.ENTER.isUp());
// Der 2. Block des Starts (weiß) beginnt hoffentlich hier
			// Rueckgabe ergebnis2 = this.erkenneFarbe(false);
			this.drawString("Strecke: " + this.erkenneFarbe("0"));
			// LCD.drawString("TBlock: "+ergebnis2.timeBlock,0,2);
			// while (Button.ENTER.isUp());
// Der 3. Block des Starts (schwarz) beginnt hoffentlich hier
			// Rueckgabe ergebnis3 = this.erkenneFarbe(true);
			this.drawString("Strecke: " + this.erkenneFarbe("1"));
		}
		else
		{
// Der 1. Block des Starts (Schwarz) beginnt hoffentlich hier	
			this.erkenneFarbe("1");
// Der 2. Block des Starts (weiß) beginnt hoffentlich hier
			//this.drawString(""); //FIXME Ohne das hier keine erkenneWeiß auf dem Display oO
			this.erkenneFarbe("0");			
// Der 3. Block des Starts (schwarz) beginnt hoffentlich hier
			this.erkenneFarbe("1");
		}*/		
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
			aktBlock = -this.getTachoCount();
		}
		// //LCD.clear();
		// long timeBlock= -System.nanoTime();
		float aktWert = this.ersterScan();
		if (dunkel.equals("1"))
		{
			while (aktWert < (caliGrenze) && Button.ENTER.isUp())
			{
				aktWert = this.scanne(aktWert);
				// this.fahre();
			}
			//this.drawString("erkenneSchwarz");
		} 
		else
		{
			//Grenze für weiß erhöht, da sonst zu schnell schwarz erkannt wird
			while (aktWert > (caliGrenze-(caliGrenze/2)) && Button.ENTER.isUp()) // weiß
			{
				aktWert = this.scanne(aktWert);
				// this.fahre();
			}
			//this.drawString("erkenneWeiss");
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
			//this.drawString("AktWert: " + aktWert);
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
			return aktBlock + this.getTachoCount();
		}
		//return aktDegreeBlock + this.getTachoCount();
	}
	
	public int dunkeleAuswerten(String volldunkele)
	{	
	 final String[] Muster = {
	            "0000", "1000", "0100", "0010", "0001", //Zahlen 0-4
	            "1100", "0110", "0011", //Zahlen 5-7 
	            "1001", "1011", //Zahlen 8-9
	            "0101" // Ziel  (10)
	        };

	        for (int i=0; i<Muster.length; i++)
	        {	
	            if (volldunkele.equals(Muster))
	            {	
	                return i;
	            }
	        }
	        return 110; //Fehler - TODO fahre zurück zum Start...
	}   
	/*
	 * Wenn voll, dann hier leeren
	 */
	public void dunkeleUebertragen(String volldunkele)
	{
		int strichcodeZahl = dunkeleAuswerten(volldunkele); //FIXME Wahrscheinlich der  Fehler
		if( strichcodeZahl < 10 )
		{
			strichcode += strichcodeZahl;
		}
		else if (strichcodeZahl == 10)
		{
			Sound.beep();
			this.stoppe();
			this.drawString("Die Zahl lautet", 3);
			this.drawString(strichcode, 4);
			Sound.beep();
		}
		else
		{
			//Fehler - TODO fahre zurück zum Start...
			this.drawString("Verzeihe mir Meister"); 
			this.stoppe();
			while (Button.ESCAPE.isUp());
			System.exit(1);
		}
	}
	public void dunkeleLeer(String dunkel, int anzahl)
	{
		if (anzahl>3) // Mindestens 4 boolean Werte welche nur hell (dunkel=false) sein können
		{
			strichcode+="0";
			if((anzahl -= 4) != 0)
			{
				this.convertiereStrichcode(dunkel, anzahl);
			}				
		}
		else
		{	
			for(int i=0; i < anzahl;i++)
			{
				dunkele+=dunkel;
			}
		}
	}
	
	public void convertiereStrichcode(String dunkel, int anzahl)
	{
		
		if(dunkele.isEmpty()) // Wenn strichcode leer ist
		{
			dunkeleLeer(dunkel, anzahl);	
		}
		else
		{
			while((anzahl > 0) && (dunkele.length() < 4))
			{		
				dunkele+=dunkel;
				anzahl--;
			}	
			if(dunkele.length() == 4)
			{
				dunkeleUebertragen(dunkele);
			}
			if(anzahl > 0)
			{
				convertiereStrichcode(dunkel, anzahl);
			}
		}			
	}
			
	
	public void berechneBlockgroesse(String dunkel)
	{	
		/*Fahr zu Anfang weiß
Strecke entspricht x
finde n, für das gilt:
nBlockgröße < x < nBlockgröße + Toleranz
Sag wie viele Blöcke dieselbe Farbe hatten
Miss den nächsten Block (andere Farbe) genau so
Finde Ende*/
		long aktStrecke = this.erkenneFarbe(dunkel);
		
		int anzahlBloecke = (int) (aktStrecke/this.block);
		//float rest = aktStrecke % this.block;
		
		if(aktStrecke % this.block>=(anzahlBloecke*toleranzBlock))
		{
			if(this.debug)
			{
				this.drawString("Tolleranz überschritten");
			}
			anzahlBloecke++;			
		}
		//else if(aktStrecke % this.block<=(anzahlBloecke*toleranzBlock))
		else
		{
			if(this.debug)
			{
				this.drawString("Innerhalb Tolleranz");
			}
		}	
		if(this.start)
		{
			if(anzahlBloecke!=1)
			{
				if(this.debug)
				{
					this.drawString((anzahlBloecke-1)+" mehr als Start");
				}	
				anzahlBloecke--;	
				convertiereStrichcode("0", (anzahlBloecke-1));//"Overhead" weißer Blöcke weitergeben 
			}	
			else
			{
				if(this.debug)
				{
					this.drawString("Weiss nur Start");
				}				
			}
			this.start=false;
			berechneBlockgroesse("1"); //Nach Startweiß muss schwarz kommen
		}
		else
		{
			convertiereStrichcode(dunkel, anzahlBloecke);
			if(dunkel.equals("1"))
			{
				berechneBlockgroesse("0");
			}
			else
			{
				berechneBlockgroesse("1");
			}
		}
	}		
	
	

}