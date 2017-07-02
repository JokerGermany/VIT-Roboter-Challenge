package vit.projekt;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;

/*
 * 0,0527777777778 cm/°
 */

public class BarcodeScanner
{
	
	long toleranzBlock;
	//int degreeBlock; //Wenn wir den Switch zwischen Zeit und degree nicht hätten würde wahrscheinlich long reichen
	long block;
	boolean zeit;
	boolean debug;
	boolean start;
	boolean ziel=false;
	String dunkele=""; 
	String strichcode="";  
	int anzahlBloeckeRead;
	String dunkel;
	Fortbewegung fort = Fortbewegung.getInstance();
	Anzeige anzeigen = Anzeige.getInstance();
	Messung messen = Messung.getInstance();
	long notfallStartPunkt;
	long notfallPunkt=110; //sollte nur positiv sein, wenn ungesetzt
	String notfallDunkel;
	String notfallDunkele;
	boolean restart;
		
	BarcodeScanner(boolean zeit, boolean debug)
	{
		this.zeit=zeit;
		this.debug=debug;
		this.start=true;	
	}
	
	public long getNotfallPunkt()
	{
		return this.notfallPunkt;
	}
	public long getNotfallStartPunkt()
	{
		return this.notfallStartPunkt;
	}	
	
//	public boolean getDebug() // funktioniert nicht, vermutlich da mehrere Instanzen...
//	{
//		return this.debug;
//	}
	public boolean getZeit()
	{
		return this.zeit;
	}
	
	
	public void scanneCode()
	{
		//fort= new Fortbewegung(500,50);
		messen.setDebugUndZeit(this.debug, this.zeit);
		messen.calibrate();
		anzeigen.warte(3,"Starte");
		restart=true;
		this.dunkel = messen.erkenneStart("1010");
		//restart = false; // TODO rausnehmen!
		while(restart && Button.ESCAPE.isUp()) //TODO 
		{
			this.notfallStartPunkt = fort.getNegTachoCount();
			start=true;
			restart=false;
			String notfallDunkel=this.dunkel;
			dunkele="";
			block = messen.getBlock();
			toleranzBlock = block / 4; // 1/4 Toleranz
			//Fortbewegung fort = new Fortbewegung(500,50);
			while(!this.ziel && !restart && Button.ESCAPE.isUp())//(i < 10 && Button.ESCAPE.isUp()) 
			{	
				//myLineReader.dunkel=myLineReader.berechneBlockgroesse(myLineReader.dunkel);
				//anzeigen.drawString(""+this.erkenneFarbe(dunkel));
	      //dunkel=gegenTeilString(dunkel);
	//			this.dunkel=this.gegenTeilString(dunkel);
//				if(this.anzahlBloeckeRead!=0)
//				{
//					//this.drawString("Blocke "+myLineReader.anzahlBloeckeRead);
//					this.convertiereStrichcode(this.dunkel, this.anzahlBloeckeRead);
//				}
//				else
//				{
					this.dunkel=this.berechneBlockgroesse(this.dunkel); //TODO Variante 1
					//myLineReader.drawString(myLineReader.dunkel);
//				}
				if(restart && notfallPunkt==110) // => Fallback NotfallStartPunkt
				{
					dunkele="";
					strichcode="";
				}
				else
				{
					restart=false;
				}
			}
		}	
		fort.stoppe();
	}

	
	public static void main(String[] args)
	{	
		boolean debug = true;
		boolean zeit = false; //Zeit oder Grad zur Messung verwenden?
		BarcodeScanner myLineReader = new BarcodeScanner(zeit, debug); 
		//fort = new Fortbewegung(500,50);
		myLineReader.scanneCode();
		
				
		//myLineReader.caliGrenze = 0.4f; TODO Sei nicht so Faul du  Penner
		//LCD.clear();
		//myLineReader.antiRecursion();
		//anzeigen.drawString(""+this.suche(block, startString.substring(3).equals("1"))); //TODO Variante 2
		
		
		//34
		//35
		//31
		//  Steueung 15
		
		
		
		//fort.stoppe(); TODO rausnehmen, wenn nicht mehr benötigt.
		//myLineReader.drawString("Fertig");
		
		while (Button.ESCAPE.isUp()); // TODO KILLME
		//LCD.clear();
	}
	/*public void berechneStartBlockgroesse()
	{
		int anzahlBloecke=this.berechneBlockgroesse(false);
		if(anzahlBloecke==1)
		{
			//berechneBlockgroesse(true); //TODO Nach Test einkommentieren
			anzeigen.drawString("Weiss nur Start");
		}
		else
		{
			anzeigen.drawString(+(anzahlBloecke-1)+" Bloecke weiss");
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
			anzeigen.drawString("AktWert: " + this.erkenneFarbe(false));
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
	
	
// String Methoden beginnen hier
	
	//public void antiRecursion()
	//{
//		String dunkel = this.erkenneStart("1010");
//		int i = 0;
//		while(i < 10 && Button.ESCAPE.isUp()) //(this.ziel!=true && Button.ESCAPE.isUp())
//		{	
//			dunkel=this.berechneBlockgroesse(dunkel);
////		anzeigen.drawString(""+this.erkenneFarbe(dunkel));
////		dunkel=gegenTeilString(dunkel);
////			this.dunkel=this.gegenTeilString(dunkel);
////			/*if(this.anzahlBloeckeRead!=0)
////			{
////				anzeigen.drawString("Blocke "+this.anzahlBloeckeRead);
////				this.anzahlBloeckeRead=this.convertiereStrichcode(this.dunkel, this.anzahlBloeckeRead);
////			}
////			else
////			{
//				//test=this.berechneBlockgroesse(test); //TODO Variante 1
////				anzeigen.drawString(""+test);
////			}*/
//			i++;
//		}
	//}
	
	/*
	 * ersetzt durch erkenneFarbe
	 * 
	 * 
	 * public Rueckgabe erkenneSchwarz() { //LCD.clear(); long timeBlock=
	 * -System.nanoTime(); float aktWert = this.scanne(); while(aktWert <
	 * caliGrenze && Button.ENTER.isUp()) //schwarz { aktWert = this.scanne();
	 * //fort.fahre; LCD.drawString("erkenneSchwarz",0,0);
	 * LCD.drawString("AktWert: "+aktWert,0,1); } //Sound.beep();
	 * //Sound.beep(); timeBlock += System.nanoTime(); //Object[] rueckgabe = {
	 * aktWert, timeBlock}; //return rueckgabe; return new Rueckgabe(aktWert,
	 * timeBlock); } /**
	 * 
	 * 
	 * //public float erkenneWeiß() old public Rueckgabe erkenneWeiß() {
	 * //LCD.clear(); long timeBlock= -System.nanoTime(); float aktWert =
	 * this.scanne(); while(aktWert > caliGrenze && Button.ENTER.isUp()) //weiß
	 * { aktWert = this.scanne(); //fort.fahre;
	 * LCD.drawString("erkenneWeiss",0,0);
	 * LCD.drawString("AktWert: "+aktWert,0,1); } //Sound.beep(); timeBlock +=
	 * System.nanoTime(); //Object[] rueckgabe = { aktWert, timeBlock}; //return
	 * rueckgabe; return new Rueckgabe(aktWert, timeBlock); }
	 */
	/**
	 * Soll den Start erkennen und die Abstände eines Blockes calibrieren.
	 * Probleme hierbei könnte die Startlinie machen
	 */
	
		/*		
		if(debug)
		{
// Der 1. Block des Starts (Schwarz) beginnt hoffentlich hier			
			anzeigen.drawString("Strecke: " + this.erkenneFarbe("1"));
			// aktWert = (this.erkenneSchwarz())[0]; Funktioniert in Java leider
			// LCD.drawString("TBlock: "+ergebnis1.timeBlock,0,2);
			// while (Button.ENTER.isUp());
// Der 2. Block des Starts (weiß) beginnt hoffentlich hier
			// Rueckgabe ergebnis2 = this.erkenneFarbe(false);
			anzeigen.drawString("Strecke: " + this.erkenneFarbe("0"));
			// LCD.drawString("TBlock: "+ergebnis2.timeBlock,0,2);
			// while (Button.ENTER.isUp());
// Der 3. Block des Starts (schwarz) beginnt hoffentlich hier
			// Rueckgabe ergebnis3 = this.erkenneFarbe(true);
			anzeigen.drawString("Strecke: " + this.erkenneFarbe("1"));
		}
		else
		{
// Der 1. Block des Starts (Schwarz) beginnt hoffentlich hier	
			this.erkenneFarbe("1");
// Der 2. Block des Starts (weiß) beginnt hoffentlich hier
			//anzeigen.drawString(""); //FIXME Ohne das hier keine erkenneWeiß auf dem Display oO
			this.erkenneFarbe("0");			
// Der 3. Block des Starts (schwarz) beginnt hoffentlich hier
			this.erkenneFarbe("1");
		}*/		
		
	
	
	
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
	            if (volldunkele.equals(Muster[i]))
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
		while (dunkele.length()>3)
		{	
			int strichcodeZahl=110; //Case Error...
			if (dunkele.length() > 3)
			{
				strichcodeZahl=dunkeleAuswerten(dunkele.substring(0,4));
				dunkele=dunkele.substring(4);
			}
			else
			{
				strichcodeZahl=dunkeleAuswerten(dunkele);
				dunkele="";
			}
		//int strichcodeZahl = dunkeleAuswerten(volldunkele);
			if( strichcodeZahl < 10 )
			{
				strichcode += " "+strichcodeZahl; //hinten?
				this.notfallPunkt=fort.getNegTachoCount();
				this.notfallDunkel=gegenTeilString(dunkel);
				this.notfallDunkele=dunkele;
				anzeigen.drawString(strichcode);
			}
			else if (strichcodeZahl == 10)
			{
				Sound.beep();
				anzeigen.drawString("Die Zahl lautet", 3);
				anzeigen.drawString(strichcode, 4);
				this.ziel=true; // TODO herausbekommen, warum es nicht funktioniert
				Sound.beep();
				
			}
			else
			{
				//Fehler - fahre zurück zum Notfallpunkt...
				anzeigen.drawString("Verzeihe mir Meister"); 
				this.dunkel=notfallDunkel;
				this.notfallDunkele=dunkele;
				fort.fahreZurueck(this.notfallPunkt);
				this.restart=true;
			}
			anzeigen.drawString(strichcode);
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
			while(anzahl>0)
			//for(int i=0; i < anzahl;i++)
			{
				dunkele+=dunkel;
				anzahl--;
			}
			this.anzahlBloeckeRead=anzahl; // sollte 0 sein...
			
		}
	}
	
	public void convertiereStrichcode(String dunkel, int anzahl)
	{		
		//anzeigen.drawString("F:"+dunkel+" A:"+anzahl);
		if(dunkele.isEmpty()) // Wenn strichcode leer ist
		{
			if(debug)
			{
				//anzeigen.drawString("Leer");
			}
				dunkeleLeer(dunkel, anzahl);		
		}
		else
		{
			while((anzahl > 0))// && (dunkele.length() < 4))
			{	
				//if(dunkele.length() > 3)
				//{
				//	anzeigen.drawString(dunkele);
				//	dunkeleUebertragen(dunkele);
				//	dunkele="";
				//}
				dunkele+=dunkel;//1000
				anzahl--;				
			}	
			if(dunkele.length() > 3)
			{
				anzeigen.drawString(dunkele);
				dunkeleUebertragen(dunkele);
			}
//			if(anzahl > 0)
//			{
//				this.dunkel=dunkel;
//				this.anzahlBloeckeRead=anzahl;
//			}
//			else
//			{
				this.anzahlBloeckeRead=0;
//			}
		}
	}
	
	public String gegenTeilString(String dunkel)
	{
		if(dunkel.equals("1"))
		{
			return "0";
		}
		else
		{
			return "1";
		}
	}
			
	
	public String berechneBlockgroesse(String dunkel)
	{	
		/*Fahr zu Anfang weiß
Strecke entspricht x
finde n, für das gilt:
nBlockgröße < x < nBlockgröße + Toleranz
Sag wie viele Blöcke dieselbe Farbe hatten
Miss den nächsten Block (andere Farbe) genau so
Finde Ende*/
		long aktStrecke = messen.erkenneFarbe(dunkel);
		if(aktStrecke < (block-toleranzBlock)) // Fehler
		{
			fort.fahreZurueck(notfallPunkt); 
			restart=true;
			return this.notfallDunkel;			
		}	
		//FIXME Hier ist irgendwo im Fehlerfall ein devided by Zero...
		int anzahlBloecke = (int) (aktStrecke/block);
		//float rest = aktStrecke % this.block;
		if(aktStrecke % block>=toleranzBlock)
		{
			if(this.debug)
			{
				anzeigen.drawString(aktStrecke % this.block+"Ueber="+anzahlBloecke+"F"+this.dunkel);
			}
			anzahlBloecke++;			
		}
		//else if(aktStrecke % this.block<=(anzahlBloecke*toleranzBlock))
		else
		{
			if(this.debug)
			{
				anzeigen.drawString(aktStrecke % this.block+"Inner="+anzahlBloecke+"F"+this.dunkel);
			}
		}	
		//anzeigen.drawString(""+block);
		if(this.start)
		{
			if(anzahlBloecke>1)
			{
				if(this.debug)
				{
					anzeigen.drawString((anzahlBloecke-1)+" mehr als Start");
				}	
				anzahlBloecke--;
				//"Overhead" weitergeben
				convertiereStrichcode(dunkel, (anzahlBloecke-1));
				this.start=false;
			}	
			else if(anzahlBloecke<1)
			{
				fort.fahreZurueck(notfallStartPunkt);
				this.dunkel=this.notfallDunkel;	
				this.start=true;
			}
			else //Block ist 1 lang
			{
				this.notfallPunkt=fort.getNegTachoCount();
				this.notfallDunkel=gegenTeilString(dunkel);
				if(this.debug)
				{
					anzeigen.drawString("Weiss nur Start");
				}				
				this.start=false;
			}
			
			//berechneBlockgroesse(gegenTeilString(dunkel));//Stackoverflow-Vermeidung
		}
		else
		{
			convertiereStrichcode(dunkel, anzahlBloecke);
			//berechneBlockgroesse(gegenTeilString(dunkel)); Stackoverflow-Vermeidung
		}
		return gegenTeilString(dunkel);
	}	
}	