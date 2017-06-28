
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;

/*
 * GetTachocount oder System.nanoTime() oder beides?
 * 
 */


public class BarcodeScanner
{
	
	float caliGrenze; //Pauschal: 0 schwarz, 1 weiß
	float sample[];
	float aktWert;
	long timeBlock; //Wie lange braucht Robi für einen Block
	Object[] rueckgabe = new Object[2]; //Evtl nur fürs Debugging gebraucht
	
	public static void main(String[] args)
	{
		Motor.A.setSpeed(250);
		Motor.D.setSpeed(250);
		EV3ColorSensor light = new EV3ColorSensor(SensorPort.S4);
		light.setCurrentMode("Red"); // hier wird Betriebsmodus gesetzt
		//Motor.A.getT
		BarcodeScanner myLineReader = new BarcodeScanner();
        myLineReader.calibrate();
        while (Button.ESCAPE.isUp()); //TODO KILLME
        LCD.clear();        
               
        myLineReader.erkenneStart();
        while (Button.ENTER.isUp()); //TODO KILLME
        LCD.clear();        
    }
	
	public void fahre()
	{
		Motor.A.backward();
        Motor.D.backward();
	}
	
	/**
     * Lichtsensor pro Abfrage einen Wert
     * FIXME funktioniere!!!
     */
	public float scanne()
	{
		while (sample[0]==0)
    	{
    		light.fetchSample(sample, 0);
    	}
		return sample[0];
	}
	
	/**
     * FIXME evtl macht es sinn diese Methode zu implementieren
     */
	//public float erkenneSchwarz()
	public Object[] erkenneSchwarz()
	{
		timeBlock= -System.nanoTime();
		while(aktWert < caliGrenze) //schwarz
		{
			aktWert = this.scanne();
			this.fahre();
		}
		timeBlock += System.nanoTime();
		//Object[] rueckgabe = { aktWert, timeBlock};
		//return rueckgabe;
		return new Object[]{aktWert, timeBlock};
	}
	/**
     * FIXME evtl macht es sinn diese Methode zu implementieren
     */
	//public float erkenneWeiß() old
	public Object[] erkenneWeiß()
	{
		timeBlock= -System.nanoTime();
		while(aktWert > caliGrenze) //weiß
		{
			aktWert = this.scanne();
			this.fahre();
		}		
		timeBlock += System.nanoTime();
		//Object[] rueckgabe = { aktWert, timeBlock};
		//return rueckgabe;
		return new Object[]{aktWert, timeBlock};
	}
	
	/**
     * Soll den Start erkennen und die Abstände eines Blockes calibrieren. Probleme hierbei könnte die Startlinie machen
     * FIXME funktioniere!!! Die Entferungsberechnung fehlt noch
     */
	public void erkenneStart() 
    {
		aktWert = this.scanne();
		//this.erkenneWeiß(); TODO Implement ME
		
		//TODO Start KILLME!
		aktWert = this.erkenneWeiß();
		LCD.drawString("AktWert: "+aktWert,0,1);
		while (Button.ENTER.isDown());
		//TODO END KILLME!
		
		//Der 1. Block des Starts (Schwarz) beginnt hoffentlich hier
		
		//this.erkenneSchwarz(); TODO Implement ME
		
		//TODO Start KILLME!
		//aktWert = (this.erkenneSchwarz())[0]; Funktioniert in Java leider nicht
		
		
				LCD.drawString("AktWert: "+aktWert,0,2);
				while (Button.ENTER.isDown());
		//TODO END KILLME!
		
		//Der 2. Block des Starts (weiß) beginnt hoffentlich hier		
		//this.erkenneWeiß(); TODO Implement ME
		
		//TODO Start KILLME!
		aktWert = this.erkenneWeiß();
		LCD.drawString("AktWert: "+aktWert,0,3);
		while (Button.ENTER.isDown());
		//TODO END KILLME!
		
		//Der 3. Block des Starts (schwarz) beginnt hoffentlich hier
		
		//this.erkenneSchwarz(); TODO Implement ME
				
		//TODO Start KILLME!
		aktWert = this.erkenneSchwarz();
		LCD.drawString("AktWert: "+aktWert,0,4);
		while (Button.ENTER.isDown());
		//TODO END KILLME!
		
		//Der 4. Block des Starts (schwarz) beginnt hoffentlich hier
		
		//this.erkenneSchwarz(); TODO Implement ME
						
		//TODO Start KILLME!
		aktWert = this.erkenneSchwarz();
		LCD.drawString("AktWert: "+aktWert,0,5);
		while (Button.ENTER.isDown());
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
     	caliGrenze=caliDunkel+(caliHell-caliDunkel/2);
     	LCD.drawString("Grenze: "+caliGrenze,0,2);
    }

	

	

	
	    	
	    
	   
	}