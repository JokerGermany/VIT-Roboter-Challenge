package vit.projekt;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

/*
 * Beinhaltet alles, was für die Fortbewegung wichtig ist.
 */
public class Fortbewegung
{
	EV3LargeRegulatedMotor linkerMotor; 
	EV3LargeRegulatedMotor rechterMotor; 
	private int geschwindigkeit=50;
	private int beschleunigung=500;
	
	//Gewünscht ist genau eine Instanz der Klasse Fortbewegung, da sonst die Fehlermeldung "Port Open" angezeigt wird. //TODO potenzieller Flaschenhals?
	 // Quelle: https://de.wikibooks.org/wiki/Muster:_Java:_Singleton
	  // https://javabeginners.de/Design_Patterns/Singleton_-Pattern.php
	  // Innere private Klasse, die erst beim Zugriff durch die umgebende Klasse initialisiert wird
	  private static final class InstanceHolderF {
	    // Die Initialisierung von Klassenvariablen geschieht nur einmal 
	    // und wird vom ClassLoader implizit synchronisiert
	    static final Fortbewegung INSTANCE = new Fortbewegung();
	  }

	  // Verhindere die Erzeugung des Objektes über andere Methoden
	  private Fortbewegung() 
	  {
		  this.linkerMotor = new EV3LargeRegulatedMotor(MotorPort.A); //nicht direkt in die Klasse, sonst Exception (das Programm funktioniert trotzdem oO)
		  this.rechterMotor= new EV3LargeRegulatedMotor(MotorPort.D); //nicht direkt in die Klasse, sonst Exception (das Programm funktioniert trotzdem oO)
		  this.linkerMotor.synchronizeWith(new RegulatedMotor[]{rechterMotor});	  
	  }
	  // Eine nicht synchronisierte Zugriffsmethode auf Klassenebene.
	  public static Fortbewegung getInstance () {
	    return InstanceHolderF.INSTANCE;
	  }
	
	
	
	public Fortbewegung(int beschleunigung, int geschwindigkeit)
	{
		
	}
	
	
	public void fahreZurueck(long zielTacho)
	{
		this.stoppe();
		Delay.msDelay(500);
		linkerMotor.startSynchronization();
		linkerMotor.forward();
		rechterMotor.forward();
		linkerMotor.endSynchronization();
		while(this.getNegTachoCount()<zielTacho); //evtl. <=
		this.stoppe();
		Delay.msDelay(500);		
		this.fahre();
	}



	public void fahre()
	{
		linkerMotor.startSynchronization();
		//int geschwindigkeit = 50;	//	Festsetzen der Geschwindigkeit in "Grad/Sekunde"
		//int beschleunigung = 500;	//	Verzögerung von 500 ms bis Geschwindigkeit
		 //Sicherstellen, dass die Motoren syncron fahren
		//linkerMotor.resetTachoCount();					//	Tacho-Reset unnötig, da meines wissen am Anfang des Programms sowieso 0, könnte Probleme ergeben
		linkerMotor.setSpeed(geschwindigkeit);			//	setzen der Geschwindigkeit
		linkerMotor.setAcceleration(beschleunigung);	//	setzen der Beschleunigung
		//rechterMotor.resetTachoCount();				//	Tacho-Reset unnötig, da meines wissen am Anfang des Programms sowieso 0, könnte Probleme ergeben
		rechterMotor.setSpeed(geschwindigkeit);			//	setzen der Geschwindigkeit
		rechterMotor.setAcceleration(beschleunigung);	//	setzen der Beschleunigung
		linkerMotor.backward();
		rechterMotor.backward();
		linkerMotor.endSynchronization(); //hier beginnen die Motoren los zu fahren
		//Motor.A.backward();
		//Motor.D.backward();
	}

	public void stoppe()
	{
		linkerMotor.startSynchronization(); //Sicherstellen, dass die Motoren gleichzeitig stoppen
		linkerMotor.flt();
		rechterMotor.flt();
		linkerMotor.endSynchronization(); //hier stoppen die Motoren
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
	public int getNegTachoCount()
	{
		return linkerMotor.getTachoCount(); 
		//return (Motor.A.getTachoCount()*-1); 
	}
}
