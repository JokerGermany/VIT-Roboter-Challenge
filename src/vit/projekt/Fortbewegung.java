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
	private int geschwindigkeit;
	private int beschleunigung;
	
	public Fortbewegung(int beschleunigung, int geschwindigkeit)
	{
		this.linkerMotor = new EV3LargeRegulatedMotor(MotorPort.A); //nicht direkt in die Klasse, sonst Exception (das Programm funktioniert trotzdem oO)
		this.rechterMotor= new EV3LargeRegulatedMotor(MotorPort.D); //nicht direkt in die Klasse, sonst Exception (das Programm funktioniert trotzdem oO)
		this.geschwindigkeit=geschwindigkeit;
		this.beschleunigung=beschleunigung;
		this.linkerMotor.synchronizeWith(new RegulatedMotor[]{rechterMotor});
	}
	
	
	public void fahreZurueckStart()
	{
		this.stoppe();
		Delay.msDelay(500);
		linkerMotor.startSynchronization();
		linkerMotor.forward();
		rechterMotor.forward();
		linkerMotor.endSynchronization();
		while(linkerMotor.getTachoCount()<=0); //Fahre zurück zum start!
		this.stoppe();
		Delay.msDelay(500);		
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
}
