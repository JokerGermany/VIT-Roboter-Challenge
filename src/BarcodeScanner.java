
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
	float caliGrenze; //Pauschal: 0 schwarz, 1 weiß
	float sample[] = new float[1]; 
	Object[] rueckgabe = new Object[2]; //Evtl nur fürs Debugging gebraucht
	EV3ColorSensor light;
	int degreeBlock;
	int toleranzBlock;
	
	//class Rueckgabe
	//{
		//float aktWert;  //TODO: SChmittigenauigkeit: privat?
		//long timeBlock; //Wie lange braucht Robi für einen Block - durchgetTachocount ersetzt
		
		
		/*
		 * nicht mehr gebraucht, da die Blöcke in der Methode erkenneStart gemesssen werden.
		public Rueckgabe(float aktWert, long timeBlock)
		{
			this.aktWert=aktWert;
			this.timeBlock=timeBlock;
		}
		*/
	//}
	
	BarcodeScanner()
	{
		Motor.A.setSpeed(2000);
		Motor.D.setSpeed(2000);
		light = new EV3ColorSensor(SensorPort.S4);
		light.setCurrentMode("Red"); // hier wird Betriebsmodus gesetzt		
	}
	
	public static void main(String[] args)
	{
				
		//Motor.A.getT
		BarcodeScanner myLineReader = new BarcodeScanner();
        myLineReader.calibrate();
        while (Button.ESCAPE.isUp()); //TODO KILLME
        LCD.clear();        
        myLineReader.fahre();       
        myLineReader.erkenneStart();
        LCD.drawString("Fertig",0,5);
        myLineReader.stoppe();
        while (Button.ENTER.isUp()); //TODO KILLME
        LCD.clear();        
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
	
	/**
     * Lichtsensor pro Abfrage einen Wert
     * FIXME funktioniere!!!
     */
	public float scanne()
	{
		light.fetchSample(sample, 0);
		while (sample[0]==0)
    	{
    		light.fetchSample(sample, 0);
    	}
		return sample[0];
	}
	
	/**
     * FIXME evtl macht es sinn diese Methode zu implementieren
     */
	public int erkenneFarbe(boolean dunkel)
	{
		int aktDegreeBlock = -Motor.A.getTachoCount();
		//LCD.clear();
		//long timeBlock= -System.nanoTime();
		float aktWert = this.scanne();
		if(dunkel==true)
		{
			while(aktWert < caliGrenze && Button.ENTER.isUp())
			{
				aktWert = this.scanne();
				//this.fahre();
				LCD.drawString("erkenneSchwarz",0,0);	
				LCD.drawString("AktWert: "+aktWert,0,1);
			}
		}
		else
		{
			while(aktWert > caliGrenze && Button.ENTER.isUp()) //weiß
			{
				aktWert = this.scanne();
				//this.fahre();
				LCD.drawString("erkenneWeiss",0,0);	
				LCD.drawString("AktWert: "+aktWert,0,1);
			}
		}
		//timeBlock += System.nanoTime(); // Wird in der Methode erkenne start gemacht
		//return new Rueckgabe(aktWert, timeBlock);	
		return aktDegreeBlock + Motor.A.getTachoCount();
	}
	/*ersetzt durch erkenne Fabre
	 * 
	 
	public Rueckgabe erkenneSchwarz()
	{
		LCD.clear();
		long timeBlock= -System.nanoTime();
		float aktWert = this.scanne();
		while(aktWert < caliGrenze && Button.ENTER.isUp()) //schwarz
		{
			aktWert = this.scanne();
			//this.fahre();
			LCD.drawString("erkenneSchwarz",0,0);	
			LCD.drawString("AktWert: "+aktWert,0,1);
		}
		//Sound.beep();
		//Sound.beep();
		timeBlock += System.nanoTime();
		//Object[] rueckgabe = { aktWert, timeBlock};
		//return rueckgabe;
		return new Rueckgabe(aktWert, timeBlock);
	}
	/**
     
     
	//public float erkenneWeiß() old
	public Rueckgabe erkenneWeiß()
	{
		LCD.clear();
		long timeBlock= -System.nanoTime();
		float aktWert = this.scanne();
		while(aktWert > caliGrenze && Button.ENTER.isUp()) //weiß
		{
			aktWert = this.scanne();
			//this.fahre();
			LCD.drawString("erkenneWeiss",0,0);	
			LCD.drawString("AktWert: "+aktWert,0,1);
		}	
		//Sound.beep();
		timeBlock += System.nanoTime();
		//Object[] rueckgabe = { aktWert, timeBlock};
		//return rueckgabe;
		return new Rueckgabe(aktWert, timeBlock);
	}
	*/
	/**
     * Soll den Start erkennen und die Abstände eines Blockes calibrieren. Probleme hierbei könnte die Startlinie machen
     * FIXME funktioniere!!! Die Entferungsberechnung fehlt noch
     */
	public void erkenneStart() 
    {
		float aktWert = this.scanne();
		this.erkenneFarbe(false);
		//while (Button.ENTER.isUp());
		//TODO END KILLME!
		
		//Der 1. Block des Starts (Schwarz) beginnt hoffentlich hier
		
		//this.erkenneFarbe(true); TODO Implement ME
		
		//TODO Start KILLME!
		//aktWert = (this.erkenneSchwarz())[0]; Funktioniert in Java leider nicht
		//Rueckgabe ergebnis1 = this.erkenneFarbe(true);
		degreeBlock = -Motor.A.getTachoCount();
		LCD.drawString("Strecke: "+this.erkenneFarbe(true),0,1);
		//LCD.drawString("TBlock: "+ergebnis1.timeBlock,0,2);
			//	while (Button.ENTER.isUp());
		//TODO END KILLME!
		
		//Der 2. Block des Starts (weiß) beginnt hoffentlich hier		
		//this.erkenneFarbe(false); TODO Implement ME
		
		//TODO Start KILLME!
		//Rueckgabe ergebnis2 = this.erkenneFarbe(false);				
		LCD.drawString("Strecke: "+this.erkenneFarbe(false),0,1);
		//LCD.drawString("TBlock: "+ergebnis2.timeBlock,0,2);
		//while (Button.ENTER.isUp());
		//TODO END KILLME!
		
		//Der 3. Block des Starts (schwarz) beginnt hoffentlich hier
		
		//this.erkenneFarbe(true); TODO Implement ME
				
		//TODO Start KILLME!
		//Rueckgabe ergebnis3 = this.erkenneFarbe(true);				
		LCD.drawString("Strecke: "+this.erkenneFarbe(true),0,1);
		int Streckenanfang = degreeBlock;
		degreeBlock = (degreeBlock + Motor.A.getTachoCount())/3; //TODO ist Integer gut? denk dran nachkommastellen werden abgeschnitten
		LCD.drawString("Block:"+degreeBlock,0,4);
		LCD.drawString("GesamtStr:"+(Motor.A.getTachoCount()-Streckenanfang),0,6);
		//Toleranz von einem 1/4.
		toleranzBlock = (degreeBlock/4);
		LCD.drawString("Toleranz:"+toleranzBlock,0,3);
		LCD.drawString("Toleranz:"+toleranzBlock,0,2);
		
		
		
		//LCD.drawString("TBlock: "+ergebnis3.timeBlock,0,2);
		//while (Button.ENTER.isUp());
		//TODO END KILLME!
		
		//Der 4. Block des Starts (weiß) beginnt hoffentlich hier
		
		//this.erkenneFarbe(true); TODO Implement ME
						
		//TODO Start KILLME!
		//Rueckgabe ergebnis4 = this.erkenneFarbe(false);				
		LCD.drawString("AktWert: "+this.erkenneFarbe(false),0,1);
		//LCD.drawString("TBlock: "+ergebnis4.timeBlock,0,2);
		//while (Button.ENTER.isUp());
		//TODO END KILLME!		
    }
	/**
     * Calibriert "Hell" und "Dunkel"
     * TODO Kontrollieren
     */
    public void calibrate() 
    {
    	float sample[] = new float[light.sampleSize()]; //wird in dieser Methode mehrfach verwendet
    	float caliHell=2; //Da der Wert eigentliche Wert nur zwischen 0-1 sein kann, 2 als initialsierung genommen
    	float caliDunkel=2; //Da der Wert eigentliche Wert nur zwischen 0-1 sein kann, 2 als initialsierung genommen
    	//Ab hier wird losgemessen
    	LCD.drawString("Helle Fleche stellen",0,0); 
    	LCD.drawString("druecken sie ENTER",0,1);        
        while (Button.ENTER.isUp());       	
    	caliHell = this.scanne();
        /*
         * While schleife wird durch die Methode scannen ersetzt
         */
//        while (sample[0]==0 || caliHell==2) //TODO: Nice to have: abfangen wenn Hell abgefragt aber auf dunkel gestellt
//    	{
//    		light.fetchSample(sample, 0);
//    		caliHell = sample[0];
//    		//Delay.msDelay(5000);
//    	}
    	LCD.drawString("HelleFläche: "+caliHell,0,2);
    	// Delay.msDelay(5000); //TODO KILLME
    	while (Button.ENTER.isDown()); //verhindert das Hell und Dunkel gleichzeitig gesetzt werden
    	LCD.clear();
    	//TODO Wenn nicht zufrieden ESC drücken und Methode neu aufrufen, sonst ENTER
		//Delay.msDelay(5000); //TODO KILLME
		LCD.drawString("Dunkle Fleche stellen",0,0); 
    	LCD.drawString("druecken sie ENTER",0,1);
    	while (Button.ENTER.isUp());
    	caliDunkel=this.scanne();
    	 /*
         * While schleife wird durch die Methode scannen ersetzt
         */
//     	while (sample[0]==0 || caliDunkel==2) //TODO: Nice to have: abfangen wenn Dunkel abgefragt aber auf hell gestellt
//     	{
//     		light.fetchSample(sample, 0);
//     		caliDunkel = sample[0];
//     		//Delay.msDelay(5000);
//     	}
     	LCD.clear();
     	LCD.drawString("Hell: "+caliHell,0,0);
     	LCD.drawString("Dunkel: "+caliDunkel,0,1);
     	caliGrenze=caliDunkel+((caliHell-caliDunkel)/2); //Achtung, beachtet nicht Punkt vor Strich Rechnung!
     	LCD.drawString("Grenze: "+caliGrenze,0,2);
    }

	

	

	
	    	
	    
	   
	}