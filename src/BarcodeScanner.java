
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.utility.Delay;

/*
 * GetTachocount oder System.nanoTime() oder beides?
 * 
 */

public class BarcodeScanner
{
	float caliGrenze; // Pauschal: 0 schwarz, 1 weiß
	float sample[] = new float[1];
	Object[] rueckgabe = new Object[2]; // Evtl nur fürs Debugging gebraucht
	EV3ColorSensor light;
	long toleranzBlock;
	//int degreeBlock;
	long block; // long falls zeit ausgewählt wird
	boolean zeit;
	int zeile=0;
	

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

	BarcodeScanner(boolean zeit)
	{
		Motor.A.setSpeed(2000);
		Motor.D.setSpeed(2000);
		light = new EV3ColorSensor(SensorPort.S4);
		light.setCurrentMode("Red"); // hier wird Betriebsmodus gesetzt		
		this.zeit=zeit;
		
	}
	public static void main(String[] args)
	{		
		boolean zeit = true; //Zeit wird zur Messung verwendet
		BarcodeScanner myLineReader = new BarcodeScanner(zeit); 
		myLineReader.calibrate();		
		//LCD.clear();
		myLineReader.fahre();
		myLineReader.erkenneStart();
		myLineReader.drawString("Fertig");
		myLineReader.stoppe();
		while (Button.ENTER.isUp()); // TODO KILLME
		//LCD.clear();
	}

	public void fahre()
	{
		Motor.A.backward();
		Motor.D.backward();
	}

	public void stoppe()
	{
		Motor.A.stop();
		Motor.D.stop();
	}
	
	/*
	 * positve Zahlen...
	 */
	public int getTachoCount()
	{
		return (Motor.A.getTachoCount()*-1); 
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
			LCD.drawString(str, 0, 6);
		}
		else
		{
			LCD.drawString(str, 0, this.zeile);
		}		
		this.zeile++;
	}

	/**
	 * Lichtsensor pro Abfrage einen Wert FIXME funktioniere!!!
	 */
	public float scanne()
	{
		light.fetchSample(sample, 0);
		while (sample[0] == 0)
		{
			light.fetchSample(sample, 0);
		}
		return sample[0];
	}

	/**
	 *
	 */
	public int erkenneFarbe(boolean dunkel)
	{
		int aktDegreeBlock = -this.getTachoCount();
		// //LCD.clear();
		// long timeBlock= -System.nanoTime();
		float aktWert = this.scanne();
		if (dunkel == true)
		{
			while (aktWert < caliGrenze && Button.ENTER.isUp())
			{
				aktWert = this.scanne();
				// this.fahre();
			}
			this.drawString("erkenneSchwarz");
		} else
		{
			while (aktWert > caliGrenze && Button.ENTER.isUp()) // weiß
			{
				aktWert = this.scanne();
				// this.fahre();
			}
			this.drawString("erkenneWeiss");
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
		this.drawString("AktWert: " + aktWert);
		// timeBlock += System.nanoTime(); // Wird in der Methode erkenne start
		// gemacht
		// return new Rueckgabe(aktWert, timeBlock);
		return aktDegreeBlock + this.getTachoCount();
	}

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
	 * Probleme hierbei könnte die Startlinie machen FIXME funktioniere!!! Die
	 * Entferungsberechnung fehlt noch
	 */
	public void erkenneStart()
	{
		float aktWert = this.scanne();
		this.erkenneFarbe(false);
		// while (Button.ENTER.isUp());
		// TODO END KILLME!

		// Der 1. Block des Starts (Schwarz) beginnt hoffentlich hier

		// this.erkenneFarbe(true); TODO Implement ME

		// TODO Start KILLME!
		// aktWert = (this.erkenneSchwarz())[0]; Funktioniert in Java leider
		// nicht
		// Rueckgabe ergebnis1 = this.erkenneFarbe(true);
		if(this.zeit)
		{
			block = -System.currentTimeMillis();
		}
		else
		{	
			block = -this.getTachoCount();
		}	
		this.drawString("Strecke: " + this.erkenneFarbe(true));
		// LCD.drawString("TBlock: "+ergebnis1.timeBlock,0,2);
		// while (Button.ENTER.isUp());
		// TODO END KILLME!

		// Der 2. Block des Starts (weiß) beginnt hoffentlich hier
		// this.erkenneFarbe(false); TODO Implement ME

		// TODO Start KILLME!
		// Rueckgabe ergebnis2 = this.erkenneFarbe(false);
		this.drawString("Strecke: " + this.erkenneFarbe(false));
		// LCD.drawString("TBlock: "+ergebnis2.timeBlock,0,2);
		// while (Button.ENTER.isUp());
		// TODO END KILLME!

		// Der 3. Block des Starts (schwarz) beginnt hoffentlich hier

		// this.erkenneFarbe(true); TODO Implement ME

		// TODO Start KILLME!
		// Rueckgabe ergebnis3 = this.erkenneFarbe(true);
		this.drawString("Strecke: " + this.erkenneFarbe(true));
		long Streckenanfang = block;
		if(this.zeit)
		{
			block = (block + System.currentTimeMillis())/3;
		}
		else
		{	
			block = (block + this.getTachoCount()) / 3; 
		}	
// TODO ist Integer/long gut? denk dran Nachkommastellen werden abgeschnitten
		this.drawString("Block:" + block);
		if(this.zeit)
		{
			this.drawString("GesamtStr:" + (System.currentTimeMillis() - Streckenanfang));
		}
		else
		{	
			this.drawString("GesamtStr:" + (this.getTachoCount() - Streckenanfang));
		}
		// Toleranz von einem 1/4.
		toleranzBlock = (block / 4);
		this.drawString("Toleranz:" + toleranzBlock);
		this.drawString("Toleranz:" + toleranzBlock);

		// LCD.drawString("TBlock: "+ergebnis3.timeBlock,0,2);
		// while (Button.ENTER.isUp());
		// TODO END KILLME!

		// Der 4. Block des Starts (weiß) beginnt hoffentlich hier

		// this.erkenneFarbe(true); TODO Implement ME

		// TODO Start KILLME!
		// Rueckgabe ergebnis4 = this.erkenneFarbe(false);
		this.drawString("AktWert: " + this.erkenneFarbe(false));
		// LCD.drawString("TBlock: "+ergebnis4.timeBlock,0,2);
		// while (Button.ENTER.isUp());
		// TODO END KILLME!
	}

	/**
	 * Calibriert "Hell" und "Dunkel" TODO Kontrollieren
	 */
	public void calibrate()
	{
		float sample[] = new float[light.sampleSize()]; // wird in dieser
														// Methode mehrfach
														// verwendet
		float caliHell = 2; // Da der Wert eigentliche Wert nur zwischen 0-1
							// sein kann, 2 als initialsierung genommen
		float caliDunkel = 2; // Da der Wert eigentliche Wert nur zwischen 0-1
								// sein kann, 2 als initialsierung genommen
		// Ab hier wird losgemessen
		this.drawString("Helle Fleche stellen");
		this.drawString("druecken sie ENTER");
		while (Button.ENTER.isUp())
			;
		caliHell = this.scanne();
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
		LCD.clear();
		// TODO Wenn nicht zufrieden ESC drücken und Methode neu aufrufen, sonst
		// ENTER
		// Delay.msDelay(5000); //TODO KILLME
		this.zeile=0; //ganz nach oben schreiben
		this.drawString("Dunkle Fleche stellen");
		this.drawString("druecken sie ENTER");
		while (Button.ENTER.isUp())
			;
		caliDunkel = this.scanne();
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
		this.drawString("Bitte an den Start stellen",0);
		this.drawString("druecken sie ENTER",1);
		while (Button.ENTER.isUp());
		LCD.clear();
	}

}