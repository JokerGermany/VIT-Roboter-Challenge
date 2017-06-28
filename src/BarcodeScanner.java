
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.utility.Delay;

public class BarcodeScanner
{
	float caliGrenze;
	
	public static void main(String[] args)
	{
		BarcodeScanner myLineReader = new BarcodeScanner();
        myLineReader.calibrate();
        while (Button.ESCAPE.isUp()); //TODO KILLME
    }
	
	/**
     * Calibriert "Hell" und "Dunkel"
     * TODO Kontrollieren
     */
    public void calibrate() 
    {
    	EV3ColorSensor light = new EV3ColorSensor(SensorPort.S4);
    	float sample[] = new float[light.sampleSize()]; //wird in dieser Methode mehrfach verwendet
    	float caliHell=2; //Da der Wert eigentliche Wert nur zwischen 0-1 sein kann, 2 als initialsierung genommen
    	float caliDunkel=2; //Da der Wert eigentliche Wert nur zwischen 0-1 sein kann, 2 als initialsierung genommen
    	light.setCurrentMode("Red"); // hier wird Betriebsmodus gesetzt
    	//Ab hier wird losgemessen
    	LCD.drawString("Helle Fleche stellen",0,0); 
    	LCD.drawString("druecken sie ENTER",0,1);        
        while (Button.ENTER.isUp());       	
    	while (sample[0]==0 || caliHell==2) //TODO: Nice to have: abfangen wenn Hell abgefragt aber auf dunkel gestellt
    	{
    		light.fetchSample(sample, 0);
    		caliHell = sample[0];
    		//Delay.msDelay(5000);
    	}
    	LCD.drawString("HelleFläche: "+caliHell,0,2);
    	// Delay.msDelay(5000); //TODO KILLME
    	while (Button.ENTER.isDown()); //verhindert das Hell und Dunkel gleichzeitig gesetzt werden
    	LCD.clear();
    	//TODO Wenn nicht zufrieden ESC drücken und Methode neu aufrufen, sonst ENTER
		//Delay.msDelay(5000); //TODO KILLME
		LCD.drawString("Dunkle Fleche stellen",0,0); 
    	LCD.drawString("druecken sie ENTER",0,1);
    	while (Button.ENTER.isUp());
     	while (sample[0]==0 || caliDunkel==2) //TODO: Nice to have: abfangen wenn Dunkel abgefragt aber auf hell gestellt
     	{
     		light.fetchSample(sample, 0);
     		caliDunkel = sample[0];
     		//Delay.msDelay(5000);
     	}
     	LCD.clear();
     	LCD.drawString("Hell: "+caliHell,0,0);
     	LCD.drawString("Dunkel: "+caliDunkel,0,1);
     	caliGrenze=caliDunkel+(caliHell-caliDunkel/2);
     	LCD.drawString("Grenze: "+caliGrenze,0,2);
//			
//		LCD.clear();
//
//        // wait for unpressed ENTER-Key and measure threhold
//        while (Button.ENTER.isDown());
//        do { 
//            //threshold = light.getRedMode();
//        	light.setCurrentMode("Red"); // hier wird Betriebsmodus gesetzt
//    		float sample[] = new float[light.sampleSize()];
//    		while (sample[0]==0) {
//    			light.fetchSample(sample, 0);
//    			threshold = sample[0];
//    			System.out.println(sample[0]);
//    			//Delay.msDelay(5000);
//    		}
//            LCD.drawString("threshold="+threshold,0,0);
//            LCD.refresh();
//        } while (!Button.ENTER.isPressed());
//
//        // wait for unpressed ENTER-Key and return
//        while (Button.ENTER.isPressed());
//
//        LCD.drawString("press ENTER!",0,1);
//        LCD.refresh();
//
//        while (!Button.ENTER.isPressed());
    }

	

	

	
	    	
	    
	   
	}