package vit.projekt;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;

/*
 * 0,0527777777778 cm/°
 */

public class BarcodeScanner
{	
	private long toleranzBlock;
	//int degreeBlock; //Wenn wir den Switch zwischen Zeit und degree nicht hätten würde wahrscheinlich long reichen
	private long block;
	private boolean zeit;
	private boolean debug;
	private boolean start;
	private boolean ziel=false;
	private String dunkele=""; 
	private String strichcode=""; 
	private String dunkel;
	private Fortbewegung fort = Fortbewegung.getInstance();
	private Anzeige anzeigen = Anzeige.getInstance();
	private Messung messen = Messung.getInstance();
	private Ton klang = Ton.getInstance();
	private long notfallStartPunkt;
	private long notfallPunkt=110; //sollte nur positiv sein, wenn ungesetzt
	private String notfallDunkel;
	private String notfallDunkele;
	private boolean restart;
		
	/**
	 * Konstruktor 
	 * @param zeit ob mit Zeit oder Grad gemessen wird
	 * @param debug true wenn Werte fürs Debugging mit ausgegeben werden sollen
	 */
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
	
///	public boolean getZeit()
///	{
///		return this.zeit;
///	}
	
	/**
	 *Erzeugt eine Instanz der BarcodeScanner myLineReader. Ihm muss mitgegebn werden ob Zwischenwerte
	 *ausgegebn werden sollen und ob mit Zeit oder Grad gemessen werden soll
	 */
	public static void main(String[] args)
	{	
		boolean debug = true;
		boolean zeit = false; //Zeit oder Grad zur Messung verwenden?
		BarcodeScanner myLineReader = new BarcodeScanner(zeit, debug); 
		//fort = new Fortbewegung(500,50); //funktioniert nicht mehr, da keine Konstruktoren mehr von Fortbewegung gebaut werden können
		myLineReader.scanneCode();	
		
		while (Button.ESCAPE.isUp()); // Ohne dies wird am Ende der "Gewinncode" nicht angezeigt!
	}

	/* Es werden die Vairablen Debug und Zeit weitergegeben.
	 * Anschließend wird schwarz und weiß calibireirt bzw. die Caligrenze berechnet
	 *Anschließend soll der Start erkannt werden. Der Startcodierung ist dabei variabel einstellbar(Aufgabe swsw)
	 *TODO verbessern
	 *
	 */
	public void scanneCode()
	{
		//fort= new Fortbewegung(500,50); //funktioniert nicht mehr, da keine Konstruktoren mehr von Fortbewegung gebaut werden können
		messen.setDebugUndZeit(this.debug, this.zeit);
		messen.kalibriere();
		anzeigen.warte(3,"Starte");
		restart=true;
		this.dunkel = messen.erkenneStart("1010");
		while(restart && Button.ESCAPE.isUp()) //Notfallschleife 
		{
			this.notfallStartPunkt = fort.getNegTachoCount();
			start=true;
			restart=false;
			notfallDunkel=this.dunkel;
			dunkele="";
			block = messen.getBlock();
			toleranzBlock = block / 4; // 1/4 Toleranz
			fort.setMaxSpeed(0,50);
			while(!this.ziel && !restart && Button.ESCAPE.isUp())//(i < 10 && Button.ESCAPE.isUp()) 
			{	
				this.dunkel=this.berechneBlockgroesse(this.dunkel);
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
		fort.stoppe(); //Wird warum auch immer nicht ausgeführt...
		klang.ausgebenErgebnis(strichcode);
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

	
//ab hier Rechenlogik	
	public String berechneBlockgroesse(String dunkel)
	{	
		long aktStrecke = messen.erkenneFarbe(dunkel);
		if(aktStrecke < (block-toleranzBlock)) // Fehler
		{
			fort.fahreZurueck(notfallPunkt); 
			restart=true;
			return this.notfallDunkel;			
		}	
		int anzahlBloecke = (int) (aktStrecke/block);
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
				konvertiereStrichcode(dunkel, (anzahlBloecke));
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
			konvertiereStrichcode(dunkel, anzahlBloecke);
			//berechneBlockgroesse(gegenTeilString(dunkel)); Stackoverflow-Vermeidung
		}
		return gegenTeilString(dunkel);
	}

	public void konvertiereStrichcode(String dunkel, int anzahl)
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
					//if(dunkele.length() > 3) //nicht mehr nötig, da dunkele jetzt bis zu 8 Zahlen enthalten kann
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
	//			if(anzahl > 0) //nicht mehr nötig, da dunkele jetzt bis zu 8 Zahlen enthalten kann
	//			{
	//				this.dunkel=dunkel;
	//				this.anzahlBloeckeRead=anzahl;
	//			}
	//			else
	//			{
					//this.anzahlBloeckeRead=0;
	//			}
			}
		}

	/*
	 * Wenn voll, dann hier leeren
	 */
	public void dunkeleUebertragen(String volldunkele)
	{
		while (dunkele.length()>3)
		{	
			int strichcodeZahl=110; //Case Error...
			if (dunkele.length() > 4)
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
				this.strichcode += strichcodeZahl; //hinten?
				this.notfallPunkt=fort.getNegTachoCount();
				this.notfallDunkel=gegenTeilString(dunkel);
				this.notfallDunkele=dunkele;
				anzeigen.drawString(this.strichcode);
				//klang.ausgebenZahl(strichcodeZahl); //kostet scheinbar zu viel Performance...
			}
			else if (strichcodeZahl == 10)
			{
				Sound.beep();
				anzeigen.clearLCD();
				anzeigen.drawString("Die Zahl lautet", 3);
				anzeigen.drawString(this.strichcode, 4);
				LCD.drawString(this.strichcode,0, 5);
				this.ziel=true;
				Sound.beep();
				anzeigen.drawString("Fertig",7);		
			}
			else
			{
				//Fehler - fahre zurück zum Notfallpunkt...
				anzeigen.drawString("Verzeihe mir Meister"); 
				notfallDunkel=this.dunkel;
				this.notfallDunkele=dunkele;
				fort.fahreZurueck(this.notfallPunkt);
				this.restart=true;
			}
			//anzeigen.drawString(this.strichcode);
		}	
	}

	public int dunkeleAuswerten(String volldunkele)
	{	
		int r;
		boolean[] stellen=new boolean[4];
		char[] nulleins=volldunkele.toCharArray();
		for (int i = 0; i < nulleins.length; i++) {
			stellen[i]=nulleins[i]=='1';
		}
		if (stellen[0]) {// a+ schwarz
			if (stellen[1]) {// b+
				r = 5;
			} else {// b-
				if (stellen[2]) {// c+
					if (stellen[3]) {// d+
						r = 9;
					} else {
						r = 110; //anfang
					}
				} else {// c-
					if (stellen[3]) {// d+
						r = 8;
					} else {// d-
						r = 1;
					}
				}
			}
		} else {// a-
			if (stellen[1]) {// b+
				if (stellen[2]) {// c+
					r = 6;
				} else {// c-
					if (stellen[3]) {// d+
						r = 10;//ende
					} else {// d-
						r = 2;
					}
				}
			} else {// b-
				if (stellen[2]) {// c+
					if (stellen[3]){//d+
						r = 7;
					}else{//d-
						r = 3;
					}
				} else {// c-
					if (stellen[3]) {// d+
						r = 4;
					} else {// d-
						r = 0;
					}
				}
			}
		}
		return r;
		/*
		 * 
		 * Alte nicht so effiziente Methode
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
	        return 110; //Fehler - TODO fahre zurück zum Start...*/
	}   
	public void dunkeleLeer(String dunkel, int anzahl)
	{
		if (anzahl>3) // Mindestens 4 boolean Werte welche nur hell (dunkel=false) sein können
		{
			strichcode+="0";
			if((anzahl -= 4) != 0)
			{
				this.konvertiereStrichcode(dunkel, anzahl);
			}				
		}
		else
		{	
			while(anzahl>0)
			for(int i=0; i < anzahl;i++)
			{
				dunkele+=dunkel;
				anzahl--;
			}			
		}
	}	
}	