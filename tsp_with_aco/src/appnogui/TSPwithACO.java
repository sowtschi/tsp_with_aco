package appnogui;

import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.IntStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import mpi.MPI;

/**
 * Die Klasse TSPwithACO dient zum Anlegen einer TSP Optimierung mit ACO. über verschiedene Parameter
 * kann das Verhalten des ACO Algorithmus, sprich die Schwarmintelligenz, angepasst werden. Ebenfalls
 * können Parameter der zu optimierenden Städtetour, sprich der TSP Problematik, angepasst werden.
 * 
 * Für eine Nutzung in verteilten Systemen dient diese Klasse als Grundlage und kann für verschiedene
 * Szenarien eingesetzt werden. Objekte müssen das Interface Serializable implementiert haben, sofern
 * sie per MPI genutzt werden, daher ist das Interface zwingend erforderlich.
 * 
 * @author WPF-VS - SS 2017
 *
 */
public class TSPwithACO implements Serializable {
	/**
	 * Graph der von Ameisen durchlaufen wird
	 */
	private AmeiseWege wege;
	/**
	 * Iterationen des ACO Algorithmus
	 */
	private int amountACOCounter;
	/**
	 * Ameisen für den ACO Algorithmus
	 */
	private ArrayList<Ameise> ameisen; 
	/**
	 * Index der TSP Optimierung
	 */
	private String tspindex;
	/**
	 * kürzester Weg (Reihenfolge der Städte), default auf null
	 */
	private int[] kuerzesteTour = null; 
	/**
	 * kürzeste Tourlänge, default auf -1
	 */
	private int kuerzesteTourLaenge = -1; 
	/**
	 * Länge der ersten gefundenen Tour
	 */
	private int tourLaengeStart;
	/**
	 * Auflistung aller Optimierungsfortschritte (Entfernung, Speicherungen von Tourlängen pro ACO Iteration)
	 */
	private ArrayList<Integer> iterationDistances;
	/**
	 * verfügbare CPU Kerne
	 */
	private int processors;
	/**
	 * genutzte CPU Kerne/Anzahl Prozesse
	 */
	private int size;
	/**
	 * Größe von aufgeteilten Anteilen (bspw. Ameisen) je Prozess/CPU Kern
	 */
	private int scattersize;
	/**
	 * Speichert den Jobfilname (JSON Datei) der TSP Optimierung
	 */
	private String sJobFilename;
	/**
	 * Pfad zu den cfg Ordner (JSON Dateien)
	 */
	private final String DIRECTORY_CFGPATH = "cfg";
	/**
	 * Name der TSP Optimierung
	 */
	private String tourname;
	
	// ACO Parameter aus der tspwithacosettings.json
	private final String TSP_ACO_SETTINGS_FILE = "tspwithacosettings.json";
	private int ANZAHL_STAEDTE = 0;
	private int ANZAHL_STAEDTE_MAXIMAL = 0;
	private int ANZAHL_AMEISEN = 0; 						
	private double WAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL = 0;
	private double MARKIERUNG_ABSCHWAECHUNG = 0;
	private double MARKIERUNG_VERSTAERKUNG = 0;
	private int SCHRITTWEITE = 0;
	private int MINIMALE_ENTFERNUNG_DER_STAEDTE = 0;
	private int ACO_ITERATIONS = 0;
	private int ACO_REFRESH_RATE = 0;
	private int ACO_REFRESH_DELAY = 0;

	/**
	 * Kontruktor zum Anlegen einer TSP Optimierung mit ACO und einem TSP Job (JSON Datei) als Grundlage.
	 * Es kann wahlweise gewählt werden, ob Ameisen generiert werden müssen. Beim Szenario Ant werden diese
	 * bereits zuvor erstellt und an die Knoten verteilt, sodass keine erneut angelegt werden müssen.
	 * 
	 * @param tspjob - TSP Job auf dessen Basis die TSP Optimierung durchgeführt wird
	 * @param ants - Flag, ob Ameisen anelegt werden sollen oder nicht
	 */
	public TSPwithACO(TSPJobLoader tspjob, boolean ants) {
		// laden der TSP und ACO Parameter für die Optimierung aus der tspwithacosettings.json
		loadACOSettings();
		// Index für die Optimierung setzen
		tspindex = tspjob.getJobIndex();
		// Namen für die Optimierung setzen
		tourname = tspjob.getTourNameFromFile();
		// kürzester Weg default auf null
		kuerzesteTour = null;
		// kürzeste Weglänge default auf -1
		kuerzesteTourLaenge = -1;
		// prüfen, ob Anzahl der Städte anhand Parameterangabe ok ist, sonst default setzen
		if(tspjob.getAmountNodesFromFile() <= 0) {
			tspjob.setAmountNodes(getANZAHL_STAEDTE());
		}
		// falls die Angabe den Maximalwert übersteigt wird der Default Maxwert genommen
		if (tspjob.getAmountNodesFromFile() > getANZAHL_STAEDTE_MAXIMAL()) {
			tspjob.setAmountNodes(getANZAHL_STAEDTE_MAXIMAL());
		}
		// neuen Graph(Ameisenwege) anlegen
		wege = new AmeiseWege(tspjob.getAmountNodes(), this, tspjob);
		// prüfen, ob Ameisen angelegt werden sollen
		if(ants) {
			// Ameisen für den Graph generieren
			this.generateAmweisen(wege);
		}
	}
	
	/**
	 * Kontruktor zum Anlegen einer TSP Optimierung mit ACO. Der Graph der Optimierung wird zufällig anhand
	 * der übergebenen Anzahl an  Knoten generiert. Es kann wahlweise gewählt werden, ob Ameisen generiert werden müssen.
	 * Beim Szenario Ant werden diese bereits zuvor erstellt und an die Knoten verteilt, sodass keine erneut angelegt
	 * werden müssen.
	 * 
	 * @param amountstaedte - Anzahl der Städte/Knoten
	 * @param index - Index der TSP Optimierung
	 * @param ants - Flag, ob Ameisen anelegt werden sollen oder nicht
	 */
	public TSPwithACO(int amountstaedte, String index, boolean ants) {
		// Index für die Optimierung setzen
		tspindex = index;
		// Namen für die Optimierung setzen
		tourname = index;
		// laden der TSP und ACO Parameter für die Optimierung aus der tspwithacosettings.json
		loadACOSettings();
		// kürzester Weg default auf null
		kuerzesteTour = null;
		// kürzeste Weglänge default auf -1
		kuerzesteTourLaenge = -1;
		// prüfen, ob Anzahl der Städte anhand Parameterangabe ok ist, sonst default setzen
		if(amountstaedte <= 0) {
			amountstaedte = getANZAHL_STAEDTE();
		}
		// falls die Angabe den Maximalwert übersteigt wird der Default Maxwert genommen
		if (amountstaedte > getANZAHL_STAEDTE_MAXIMAL()) {
			amountstaedte = getANZAHL_STAEDTE_MAXIMAL();
		}
		// neuen Graph(Ameisenwege) anlegen
		wege = new AmeiseWege(amountstaedte, this, null);
		// prüfen, ob Ameisen angelegt werden sollen
		if(ants) {
			// Ameisen für den Graph generieren
			this.generateAmweisen(wege);
		}
	}
	
	/**
	 * Methode dient zum Parsen bzw. Laden der TSP und ACO Parameter für die Optimierung aus der tspwithacosettings.json
	 */
	public void loadACOSettings() {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("./"+DIRECTORY_CFGPATH+"/"+TSP_ACO_SETTINGS_FILE));
			JSONObject tspJSON = (JSONObject) obj;
			ANZAHL_STAEDTE = Integer.parseInt(tspJSON.get("ANZAHL_STAEDTE").toString());
			//System.out.println(ANZAHL_STAEDTE);
			ANZAHL_STAEDTE_MAXIMAL = Integer.parseInt(tspJSON.get("ANZAHL_STAEDTE_MAXIMAL").toString());
			//System.out.println(ANZAHL_STAEDTE_MAXIMAL);
			ANZAHL_AMEISEN = Integer.parseInt(tspJSON.get("ANZAHL_AMEISEN").toString());
			//System.out.println(ANZAHL_AMEISEN);
			WAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL = (double)(tspJSON.get("WAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL"));
			//System.out.println(WAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL);
			MARKIERUNG_ABSCHWAECHUNG = (double)(tspJSON.get("MARKIERUNG_ABSCHWAECHUNG"));
			//System.out.println(MARKIERUNG_ABSCHWAECHUNG);
			MARKIERUNG_VERSTAERKUNG = (double)(tspJSON.get("MARKIERUNG_VERSTAERKUNG"));
			//System.out.println(MARKIERUNG_VERSTAERKUNG);
			SCHRITTWEITE = Integer.parseInt(tspJSON.get("SCHRITTWEITE").toString());
			//System.out.println(SCHRITTWEITE);
			MINIMALE_ENTFERNUNG_DER_STAEDTE = Integer.parseInt(tspJSON.get("SCHRITTWEITE").toString());
			//System.out.println(MINIMALE_ENTFERNUNG_DER_STAEDTE);
			ACO_ITERATIONS = Integer.parseInt(tspJSON.get("ACO_ITERATIONS").toString());
			ACO_REFRESH_RATE = Integer.parseInt(tspJSON.get("ACO_REFRESH_RATE").toString());
			ACO_REFRESH_DELAY = Integer.parseInt(tspJSON.get("ACO_REFRESH_DELAY").toString());
			if(getIndex()==null)
				setIndex(tspJSON.get("id").toString());
		} catch (Exception e) {
			System.err.println("loadACOSettings Fehler: " + e.getMessage());
		}
	}
	
	/**
	 * Gibt den JobFilename der TSP Optimierung zurück.
	 * 
	 * @return String - JobFilename der TSP Optimierung
	 */
	public String getsJobFilename() {
		return sJobFilename;
	}

	/**
	 * Setzt den JobFilename der TSP Optimierung.
	 * 
	 * @param sJobFilename - JobFilename der TSP Optimierung
	 */
	public void setsJobFilename(String sJobFilename) {
		this.sJobFilename = sJobFilename;
	}
	
	/**
	 * Gibt die Iterationen des ACO Algorithmus zurück
	 * 
	 * @return int - Anzahl ACO Iterationen
	 */
	public int getAmountACOCounter() {
		return this.amountACOCounter;
	}
	
	/**
	 * Anzahl verfügbarer CPU Kerne erhalten
	 * 
	 * @return int - Anzahl CPU Kerne
	 */
	public int getProcessors() {
		return processors;
	}

	/**
	 * Anzahl verfügbarer CPU Kerne setzen
	 * 
	 * @param processors - Anzahl CPU Kerne
	 */
	public void setProcessors(int processors) {
		this.processors = processors;
	}
	
	/**
	 * Anzahl der Prozesse/verfügbarer CPU Kerne erhalten
	 * 
	 * @return int - Anzahl Prozesse/CPU Kerne
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Anzahl der Prozesse/verfügbarern CPU Kerne setzen
	 * 
	 * @param size - Anzahl Prozesse/CPU Kerne
	 */
	public void setSize(int size) {
		this.size = size;
	}
	
	/**
	 * Größe von aufgeteilten Anteilen (bspw. Ameisen) je Prozess/CPU Kern erhalten
	 * 
	 * @return int - Größe von aufgeteilten Anteilen (bspw. Ameisen)
	 */
	public int getScattersize() {
		return scattersize;
	}

	/**
	 * Größe von aufgeteilten Anteilen (bspw. Ameisen) je Prozess/CPU Kern setzen
	 * 
	 * @param scattersize - Größe von aufgeteilten Anteilen (bspw. Ameisen)
	 */
	public void setScattersize(int scattersize) {
		this.scattersize = scattersize;
	}
	
	public int getACO_ITERATIONS() {
		return ACO_ITERATIONS;
	}

	public void setACO_ITERATIONS(int aCO_ITERATIONS) {
		ACO_ITERATIONS = aCO_ITERATIONS;
	}
	
	public int getANZAHL_STAEDTE() {
		return ANZAHL_STAEDTE;
	}

	public void setANZAHL_STAEDTE(int aNZAHL_STAEDTE) {
		ANZAHL_STAEDTE = aNZAHL_STAEDTE;
	}

	public int getANZAHL_STAEDTE_MAXIMAL() {
		return ANZAHL_STAEDTE_MAXIMAL;
	}

	public void setANZAHL_STAEDTE_MAXIMAL(int aNZAHL_STAEDTE_MAXIMAL) {
		ANZAHL_STAEDTE_MAXIMAL = aNZAHL_STAEDTE_MAXIMAL;
	}

	public int getANZAHL_AMEISEN() {
		return ANZAHL_AMEISEN;
	}

	public void setANZAHL_AMEISEN(int aNZAHL_AMEISEN) {
		ANZAHL_AMEISEN = aNZAHL_AMEISEN;
	}

	public double getWAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL() {
		return WAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL;
	}

	public void setWAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL(double wAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL) {
		WAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL = wAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL;
	}

	public double getMARKIERUNG_ABSCHWAECHUNG() {
		return MARKIERUNG_ABSCHWAECHUNG;
	}

	public void setMARKIERUNG_ABSCHWAECHUNG(double mARKIERUNG_ABSCHWAECHUNG) {
		MARKIERUNG_ABSCHWAECHUNG = mARKIERUNG_ABSCHWAECHUNG;
	}

	public double getMARKIERUNG_VERSTAERKUNG() {
		return MARKIERUNG_VERSTAERKUNG;
	}

	public void setMARKIERUNG_VERSTAERKUNG(double mARKIERUNG_VERSTAERKUNG) {
		MARKIERUNG_VERSTAERKUNG = mARKIERUNG_VERSTAERKUNG;
	}

	public int getSCHRITTWEITE() {
		return SCHRITTWEITE;
	}

	public void setSCHRITTWEITE(int sCHRITTWEITE) {
		SCHRITTWEITE = sCHRITTWEITE;
	}

	public int getMINIMALE_ENTFERNUNG_DER_STAEDTE() {
		return MINIMALE_ENTFERNUNG_DER_STAEDTE;
	}

	public void setMINIMALE_ENTFERNUNG_DER_STAEDTE(int mINIMALE_ENTFERNUNG_DER_STAEDTE) {
		MINIMALE_ENTFERNUNG_DER_STAEDTE = mINIMALE_ENTFERNUNG_DER_STAEDTE;
	}
		
	public int getACO_REFRESH_RATE() {
		return ACO_REFRESH_RATE;
	}

	public void setACO_REFRESH_RATE(int aCO_REFRESH_RATE) {
		ACO_REFRESH_RATE = aCO_REFRESH_RATE;
	}
	
	public int getACO_REFRESH_DELAY() {
		return ACO_REFRESH_DELAY;
	}

	public void setACO_REFRESH_DELAY(int aCO_REFRESH_DELAY) {
		ACO_REFRESH_DELAY = aCO_REFRESH_DELAY;
	}
	
	/**
	 * Gibt die kürzeste Tourlänge für die TSP Optimierung zurück
	 * 
	 * @return int - Länge der kürzesten Tour
	 */
	public int getkuerzesteTourLaenge() {
		return kuerzesteTourLaenge;
	}
	
	/**
	 * Setzt die kürzeste Tourlänge für die TSP Optimierung
	 * 
	 * @param laenge - int Länge der kürzesten Tour
	 */
	public void setkuerzesteTourLaenge(int laenge) {
		kuerzesteTourLaenge = laenge;
	}
	
	/**
	 * Gibt die Reihenfolge der Städte der aktuell kürzesten Tour zurück
	 * 
	 * @return int[] - Reihenfolge der Städte
	 */
	public int[] getKuerzesteTour() {
		return kuerzesteTour;
	}
	
	/**
	 * Setzt die Reihenfolge der Städte der aktuell kürzesten Tour
	 * 
	 * @param tour int[] - Array mit den Indizes der Städte der Tour
	 */
	public void setKuerzesteTour(int[] tour) {
		kuerzesteTour = tour;
	}
	
	/**
	 * Gibt den Index der TSP Optimierung zurück
	 * 
	 * @return int - Index der TSP Optimierung
	 */
	public String getIndex() {
		return tspindex;
	}
	
	/**
	 * Setzen des Index der TSP Optimierung
	 * 
	 * @param index - Index der TSP Optimierung
	 */
	public void setIndex(String index) {
		this.tspindex = index;
	}
	
	/**
	 * Legt die Ameisen für den Graph der TSP Optimierung an
	 * 
	 * @param weg - AmeiseWege Objekt, Graph für der TSP Optimierung
	 */
	public void generateAmweisen(AmeiseWege weg) {
		// ArryList für Ameisten wird angelegt
		ameisen = new ArrayList<Ameise>();
		// ArrayList wird entsprechend des Anzahl Ameisen Paramter gefüllt
		for (int i = 0; i < getANZAHL_AMEISEN(); i++) {
			// Ameise wird angelegt und erhölt den kompletten zufällig erzeugten Graph
			ameisen.add(new Ameise(wege, this));
		}
	}
	
	/**
	 * Gibt die Ameisen der TSP Optimierung zurück
	 * 
	 * @return ArrayList - Ameisen der TSP Optimierung
	 */
	public ArrayList<Ameise> getAmeisen() {
		return ameisen;
	}
	
	/**
	 * Methode zum Starten des ACO Algorithmus ohne Verteilung der Ameisen. Wird bspw. beim Szenario
	 * "MultiTSP" verwendet, da keine Verteilung der Ameisen notwendig ist.
	 */
	public void startACO() {
		// Anlegen von ArrayList zur Speicherungen von Tourlängen pro ACO Iteration
		iterationDistances = new ArrayList<Integer>();
		// Durchlauf der Iterationen des ACO Algorithmus
		for(amountACOCounter=0;amountACOCounter<getACO_ITERATIONS();amountACOCounter++) {
			// vorhandene Ameisen werden der Reihe nach aufgerufen
			for (Ameise ameise : ameisen) {
				// Ameise durchläuft Graph und setzt dabei Markierungen anhand von Wahrscheinlichkeiten
				ameise.laufen();
			}
			
			// Graph bezüglich Markierungen der Ameisen aktualisieren
			wege.updatePheromone();
			
			// prüfen, ob x Schritte vollzogen wurden und aktuelle Entfernung ausgeben
			if (amountACOCounter % getACO_REFRESH_RATE() == 0) {
				boolean newJSONOuput = false;
				if(this.getkuerzesteTourLaenge() > 0) {
					if(tourLaengeStart == 0)
						tourLaengeStart = getkuerzesteTourLaenge();
					if(!iterationDistances.contains(getkuerzesteTourLaenge())) {
						newJSONOuput = true;
						System.out.println(printStaedteTourVISJSON());
						iterationDistances.add(getkuerzesteTourLaenge());
					}
				}
				
				// nur pausieren, wenn zuvor ein JSONOutput erfolgte
				if(newJSONOuput) {
					try {
						Thread.sleep(getACO_REFRESH_DELAY());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		// falls Anzahl der Iterationen zu gering war
		if(tourLaengeStart == 0)
			tourLaengeStart = getkuerzesteTourLaenge();
	}
	
	/**
	 * Methode zum Starten des ACO Algorithmus mit Verteilung der Ameisen. Wird bspw. beim Szenario
	 * "Ant" verwendet.
	 * 
	 * @param rank - Index der Prozesse
	 * @param root - root Prozess, i.d.R. 0
	 * @param buffersize - Größe des Buffers
	 * @param sendbuf - SendBuffer, wird zum Verteilen/Sammeln der Daten über MPI benötigt
	 * @param recvbuf - ReceiveBuffer, wird zum Verteilen/Sammeln der Daten über MPI benötigt
	 * @param amountACOCounterGlobal - globaler Zähler der ACO Iterationen, welcher von allen Prozessen verwndet wird
	 * @param remainder - Rest des Buffers, welcher von einem Prozess berechnet werden muss
	 */
	public void startACOMPIAnt(int rank, int root, int buffersize, Ameise[] sendbuf, Ameise[] recvbuf, int amountACOCounterGlobal, int remainder) {
		// Anlegen von ArrayList zur Speicherungen von Tourlängen pro ACO Iteration
		iterationDistances = new ArrayList<Integer>();
		// Buffer für Austausch und Vergleich von guten Pfaden anlegen
		TSPwithACO compareTSPBuffer[] = new TSPwithACO[size];
		TSPwithACO tspaco[] = new TSPwithACO[1];
		tspaco[0] = this;
		// einzelene Ameisen an alle Prozesse verteilen (root Prozess verteilt die Daten)
		TSPwithACOMPIAnt.antComm.Scatter(sendbuf, 0, scattersize, MPI.OBJECT, recvbuf, 0, scattersize, MPI.OBJECT, root);
		// globalen ACO Counter zuweisen
		amountACOCounter = amountACOCounterGlobal;
		// Durchlauf der Iterationen des ACO Algorithmus
		for(amountACOCounter=0;amountACOCounter<getACO_ITERATIONS();amountACOCounter++) {
			// vorhandene Ameisen werden der Reihe nach aufgerufen
			for(int i=0;i < scattersize;i++) {
				// Ameise durchläuft Graph und setzt dabei Markierungen anhand von Wahrscheinlichkeiten
				recvbuf[i].laufen();
			}
			// Rest der Ameisen mit einbeziehen, welche nicht verteilt werden konnten (ungerade Prozess Anzahl)
			if(remainder>0) {
				for(int i=(buffersize-remainder);i < buffersize;i++) {
					// Ameise durchläuft Graph und setzt dabei Markierungen anhand von Wahrscheinlichkeiten
					sendbuf[i].laufen();
				}
			}
			
			// Daten aller Prozesse sammeln (root Prozess sammelt alle Daten)
			TSPwithACOMPIAnt.antComm.Gather(tspaco, 0, 1, MPI.OBJECT, compareTSPBuffer, 0, 1, MPI.OBJECT, root);
			
			// prüfen, welcher Ameisen Anteil den aktuell besten Pfad gefunden hat
			for(int i=0;i < size;i++) {
				if(compareTSPBuffer[i]!=null){
					if(compareTSPBuffer[i].getkuerzesteTourLaenge() > 0 && this.getkuerzesteTourLaenge() > 0 && this.getkuerzesteTourLaenge() != compareTSPBuffer[i].getkuerzesteTourLaenge()) {
						if(compareTSPBuffer[i].getkuerzesteTourLaenge() < this.getkuerzesteTourLaenge()) {
							System.out.println("Prozess != root hat bessere Tour gefunden!");
							this.setkuerzesteTourLaenge(compareTSPBuffer[i].getkuerzesteTourLaenge());
							this.setWege(compareTSPBuffer[i].getWege());
							this.setKuerzesteTour(compareTSPBuffer[i].getKuerzesteTour());
						}
					}
				}
			}
			
			// aktuell besten Stand allen Prozessen bekannt machen
			TSPwithACOMPIAnt.antComm.Bcast(tspaco, 0, 1, MPI.OBJECT, root);
			
			// synchronisiert alle Prozesse im Kommunikator
			TSPwithACOMPIAnt.antComm.Barrier();
			
			// Graph bezüglich Markierungen der Ameisen aktualisieren
			wege.updatePheromone();
			
			// prüfen, ob x Schritte vollzogen wurden und aktuelle Entfernung ausgeben
			if (amountACOCounter % getACO_REFRESH_RATE() == 0) {
				boolean newJSONOuput = false;
				if(this.getkuerzesteTourLaenge() > 0) {
					if(tourLaengeStart == 0)
						tourLaengeStart = getkuerzesteTourLaenge();
					if(!iterationDistances.contains(getkuerzesteTourLaenge())) {
						newJSONOuput = true;
						System.out.println(printStaedteTourVISJSON());
						iterationDistances.add(getkuerzesteTourLaenge());
					}
				}
				
				// nur pausieren, wenn zuvor ein JSONOutput erfolgte
				if(newJSONOuput) {
					try {
						Thread.sleep(getACO_REFRESH_DELAY());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		// falls Anzahl der Iterationen zu gering war
		if(tourLaengeStart == 0)
			tourLaengeStart = getkuerzesteTourLaenge();
	}
	
	/**
	 * Methode gibt den Graph der TSP Optimierung zurück
	 * 
	 * @return AmeiseWege - Graph der TSP Optimierung
	 */
	public AmeiseWege getWege() {
		return wege;
	}
	
	/**
	 * Methode zum Setzen des Graph der TSP Optimierung
	 * 
	 * @param wege - Graph der TSP Optimierung
	 */
	public void setWege(AmeiseWege wege) {
		this.wege = wege;
	}
	
	/**
	 * Methode gibt die aktuelle Städtetour der TSP Optimierung auf der Konsole aus
	 */
	public void printStaedteTour() {
		for(Stadt stadt : wege.getStaedteTour()) {
			System.out.println("von Stadt-Nr: " + stadt.getIndex() + " liegt bei (" + stadt.getX() + ";" + stadt.getY() + ") nach " +
					"Stadt-Nr: " + stadt.getNachbarStadt().get(0).getIndex() + " liegt bei (" + stadt.getNachbarStadt().get(0).getX() + ";" + stadt.getNachbarStadt().get(0).getY() + ")");
		}
	}
	
	/**
	 * Methode gibt die aktuelle Staedtetour mit Koordinaten auf der Konsole aus
	 */
	public void printStaedteTourCoords() {
		for(Stadt stadt : wege.getStaedteTour()) {
			System.out.println("[" + stadt.getX() + "," + stadt.getY() + "]");
		}
	}
	
	/**
	 * Methode gibt alle Angaben für den Graphen (Knoten, Kanten etc.) als JSON Object zur Übergabe
	 * an das Front-End zurück.
	 * 
	 * @return JSONObject - Graph mit allen notwendigen Parametern
	 */
	public JSONObject printStaedteTourVISJSON() {
		JSONObject obj = new JSONObject();
		JSONArray listNodes = new JSONArray();
		JSONArray listEdges = new JSONArray();
		JSONObject objParams = new JSONObject();
		JSONObject graph = new JSONObject();
		JSONObject objWrapper = new JSONObject();
		int iNodeCounter = 0;
		int nexttolastIndex = wege.getStaedteTour().size()-1;
		int amountNodes = wege.getStaedteTour().size();
		
		JSONArray fixedNodes = new JSONArray();
		JSONObject fixedX = new JSONObject();
		JSONObject fixedY = new JSONObject();
		fixedX.put("x", true);
		fixedY.put("y", true);
		fixedNodes.add(fixedX);
		fixedNodes.add(fixedY);
		JSONArray colorStart = new JSONArray();
		JSONObject backgroundColorStart = new JSONObject();
		backgroundColorStart.put("background", "#33ff33");
		colorStart.add(backgroundColorStart);
		JSONArray colorEnd = new JSONArray();
		JSONObject backgroundColorEnd = new JSONObject();
		backgroundColorEnd.put("background", "#ff3333");
		colorEnd.add(backgroundColorEnd);
		objParams.put("tourlength", getkuerzesteTourLaenge());
		objParams.put("iteration", amountACOCounter);
		objParams.put("tspjob_index", getIndex());
		objParams.put("graph_mode", (wege.checkGraphModeRandom()) ? GraphMode.random.toString() : GraphMode.tspjob.toString());
		objParams.put("mode", (ameisen==null) ? Mode.ant.toString() : Mode.multitsp.toString());
		objParams.put("tourname", tourname);
		if(amountACOCounter==getACO_ITERATIONS())
			objParams.put("progress", "finished");
		else
			objParams.put("progress", "working");
		graph.put("params", objParams);
		
		for(Stadt stadt : wege.getStaedteTour()) {
			obj = new JSONObject();
			obj.put("id",stadt.getIndex());
			obj.put("label",iNodeCounter);
			obj.put("x", stadt.getX());
			obj.put("y", stadt.getY());
			obj.put("fixed", fixedNodes);
			obj.put("physics", false);
			if(iNodeCounter==0)
				obj.put("color", "#33ff33");
			if(iNodeCounter==nexttolastIndex)
				obj.put("color", "#ff3333");
			listNodes.add(obj);
			iNodeCounter++;
		}
		graph.put("nodes", listNodes);
		
		for(Stadt stadt : wege.getStaedteTour()) {
			obj = new JSONObject();
			obj.put("from",stadt.getIndex());
			obj.put("to",stadt.getNachbarStadt().get(0).getIndex());
			obj.put("color", "#000000");
			obj.put("width", 3);
			listEdges.add(obj);
			/*for(int i = 0;i<amountNodes;i++) {
				if(i!=stadt.getNachbaStadt().getIndex() && i!=stadt.getIndex()) {
					obj = new JSONObject();
					obj.put("from",stadt.getIndex());
					obj.put("to",i);
					obj.put("color", "#D8D8D8");
					listEdges.add(obj);
				}
			}*/
		}
		graph.put("edges", listEdges);
		objWrapper.put("aco_iteration", graph);
		
		return objWrapper;
	}
	
	/**
	 * Methode gibt "Analyse" Ergebnisse der TSP Optimierung als JSON Object an das Front-End zurück.
	 * 
	 * @return JSONObject - "Analyse" Ergebenisse der TSP Optimierung
	 */
	public JSONObject printTSPOptimizeResultJSON() {
		JSONObject objParams = new JSONObject();
		JSONObject optimizeResult = new JSONObject();
		objParams.put("tourlength_end", getkuerzesteTourLaenge());
		objParams.put("tourlength_start", tourLaengeStart);
		objParams.put("length_difference", tourLaengeStart-getkuerzesteTourLaenge());
		
		IntStream differences = IntStream.range(0, iterationDistances.size() - 1).map(i -> iterationDistances.get(i + 1) - iterationDistances.get(i));
		if(iterationDistances.size()>1)
			objParams.put("average_optimize", round(Math.abs(differences.average().getAsDouble())/iterationDistances.size(), 2));
		else
			objParams.put("average_optimize", 0);
		objParams.put("available_cpu_cores", getProcessors());
		objParams.put("used_cpu_cores", getSize());
		objParams.put("scattersize", getScattersize());
		objParams.put("amount_city", getANZAHL_STAEDTE());
		objParams.put("amount_ants", getANZAHL_AMEISEN());
		objParams.put("probability_random_choice", getWAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL());
		objParams.put("marking_weakening", getMARKIERUNG_ABSCHWAECHUNG());
		objParams.put("marking_gain", getMARKIERUNG_ABSCHWAECHUNG());
		objParams.put("increment", getSCHRITTWEITE());
		objParams.put("iterations", getACO_ITERATIONS());
		objParams.put("tspjob_index", getIndex());
		objParams.put("tourname", tourname);
		objParams.put("mode", (ameisen==null) ? Mode.ant.toString() : Mode.multitsp.toString());
		optimizeResult.put("aco_final", objParams);
		
		return optimizeResult;
	}
	
	/**
	 * Methode zum Runden von Fließkommazahlen.
	 * 
	 * @param value - Fließkommazahl, welche gerundet werden sollen
	 * @param places - auf wie viele Nachkommastellen gerundet werden soll
	 * @return dobule - das gerundete Ergebnis
	 */
	public double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	/**
	 * Methode gibt die aktuelle Städtetour mit Koordinaten als JSON Object zurück.
	 * 
	 * @return JSONObject - aktuelle Städtetour mit Koordinaten
	 */
	public JSONObject printStaedteTourCoordsJSON() {
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		int iNodeCounter = 0;
		for(Stadt stadt : wege.getStaedteTour()) {
			list = new JSONArray();
			list.add(stadt.getX());
			list.add(stadt.getY());
			obj.put("node_"+stadt.getIndex(),list);
		}
		
		return obj;
	}
	
	/**
	 * Main Methode zum Starten einer TSP Optimierung ohne MPI
	 * 
	 * @param args - Standard main Parameter
	 */
	public static void main(String[] args) {
		// Testdaten
		//TSPwithACO aco1 = new TSPwithACO(25, 1);
		//TSPwithACO aco2 = new TSPwithACO(3, 2);
		//TSPwithACO aco3 = new TSPwithACO(20, 3);
		
		//aco1.startACO();
		//aco2.startACO();
		//aco3.startACO();
		//System.out.println(aco1.getkuerzesteTourLaenge());
		//System.out.println(aco2.getkuerzesteTourLaenge());
		//System.out.println(aco3.getkuerzesteTourLaenge());
		//aco3.printStaedteTour();
		
		TSPJobLoader jobs = new TSPJobLoader("tspjobs");
		jobs.setAmountJobsFromFile();
		jobs.loadJobFileNames();
		for(String filename:jobs.getJobFileNames()) {
			if(filename.equals("default.json")) {
				jobs.parseJSONFromJob(filename);
				TSPwithACO aco4 = new TSPwithACO(jobs, true);
				aco4.startACO();
				System.out.println(aco4.getkuerzesteTourLaenge());
			}
		}
	}
}
