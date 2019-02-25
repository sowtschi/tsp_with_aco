package appnogui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import org.json.simple.JSONArray;

/**
 * Die Klasse AmeiseWege dient zur Implementierung des Graphen, welcher von den Ameisen genutzt wird.
 * Der Graph stellt die Grundlage einer jeweiligen TSP Optimierung. Ein Graph kann entweder mit zufälligen
 * Knotenpunkten oder per Vorgabe einer von Koordinatnen für die Knoten angelegt werden. Für den ACO
 * Algorithmus liegt eine Methode zum Aktualisieren der Pheromone/Markierungen vor, sofern alle Ameisen
 * innerhalb einer ACO Iteration den Graphen durchlaufen haben.
 * 
 * @author WPF-VS - SS 2017
 *
 */
public class AmeiseWege implements Serializable {
	// Vorgaben für den Aufbau des Graphen bezüglich maximaler Breiter und Länge, sowie einem Mindestabstand zwischen den Knoten.
	/**
	 * Abstand zwischen den Knoten im Graphen.
	 */
	public static final int ABSTAND = 30;
	/**
	 * Maximale Breite des Graphen
	 */
	public static final int BREITE = 1200-2*ABSTAND;
	/**
	 * Maximale Höhe des Graphen
	 */
	public static final int HOEHE = 600-2*ABSTAND;
	/**
	 * Zufallsvariable für das Erstellen eines Graphen mit zufälligen Knoten
	 */
	private static Random zufall = new Random(42L);
	/**
	 * Array zur Speicherung der Entfernung zwischen einzelnen Knoten
	 */
	private int entfernungen[][];
	/**
	 * Array zur Speicherung der Pheromone/Markierungen der einzelnen Kanten
	 */
	private double markierungen[][];
	/**
	 * x-Koordianten für Knoten
	 */
	private int xKoordinate[];
	/**
	 * y-Koordinaten für Knoten
	 */
	private int yKoordinate[];
	/**
	 * Anzahl der Städte/Knoten
	 */
	private int n;
	/**
	 * ArrayList für alle Städte zur leichteren Weiterverarbeitung. Statt nur den Koordinaten werden
	 * die Städte als Objekte hinterlegt, sodass noch weitere Eigenschaften für Städte hinterlegt
	 * werden könnten.
	 */
	private ArrayList<Stadt> staedte = new ArrayList<Stadt>();
	/**
	 * ArrayList für aktuelle Städtetour zur leichteren Weiterverarbeitung
	 */
	private ArrayList<Stadt> staedteTour = new ArrayList<Stadt>();
	/**
	 * TSP Optimierung, welcher der Graph zugeordnet ist
	 */
	private TSPwithACO tspoptimize;
	/**
	 * ein TSP Job stellt eine Vorgabe des Graphen durch eine JSON Datei dar
	 */
	private TSPJobLoader tspjob;
	
	/**
	 * Konstruktor für das Anlegen des Graphs, sprich den möglichen Wegen für eine Ameise.
	 * 
	 * @param n - Anzahl der Städte/Knoten
	 * @param optimize - TSP Optimierung, welcher der Graph zugeordnet ist
	 * @param tspjob - ein TSP Job stellt eine Vorgabe des Graphen durch eine JSON Datei dar
	 */
	public AmeiseWege(int n, TSPwithACO optimize, TSPJobLoader tspjob) {
		// Zuweisung TSP Optimierung
		tspoptimize = optimize;
		// Anzahl der Städte setzen
		this.n = n;
		// 2-Dim. Array mit Elemente = Anzahl der Städte/Knoten für die Entfernungen anlegen
		entfernungen = new int[n][n];
		// 2-Dim. Array mit Elemente = Anzahl der Städte/Knoten für die Markierungen anlegen
		markierungen = new double[n][n];
		// Array mit Elemente = Anzahl der Städte für die x-Koordianten der Städte/Knoten anlegen
		xKoordinate = new int[n];
		// Array mit Elemente = Anzahl der Städte für die y-Koordianten der Städte/Knoten anlegen
		yKoordinate = new int[n];
		// prüfen, ob ein TSP Job vorliegt
		if (tspjob==null) {
			// Graph mit zufälligen Positionen (Einhaltung von Mindestabstand) der Knoten anlegen
			erzeugeZufaellig();
		} else {
			// Graph anhand der JSON Vorgabe anlegen
			this.tspjob = tspjob;
			generateByJSONJob();
		}
	}
	
	/**
	 * Methode zur Aktualsierung der Pheromone/Markierungen nachdem alle Ameisen gelaufen sind. Stellt einen
	 * wesentlichen Anteil einer Iteration des ACO Algorithmus dar.
	 */
	public void updatePheromone() {
		// alle Städte bzw. Kanten und deren Markierungen werden bezüglich der Markierung aktualisiert
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				// Markierungen werden um einen Defaultwert (0.99) abgeschwächt
				markierungen[i][j] *= tspoptimize.getMARKIERUNG_ABSCHWAECHUNG();
			}
		}
		
		// aktuell vorhandene kürzeste Wegstrecke aller Ameisen wird geprüft
		if (tspoptimize.getKuerzesteTour() != null) {
			// alle Städte der Tour werden durchlaufen
			for (int i = 0; i < n; i++) {
				// Start der kürzesten Wegstrecke wird gesetzt
				int start = tspoptimize.getKuerzesteTour()[i];
				// Ziel der kürzesten Wegstrecke wird gesetzt
				int ende = tspoptimize.getKuerzesteTour()[(i + 1) % n];
				// maximale Intensität vom Startknoten zu einem verbundenem Knoten i
				double maxStart = maximaleMarkierung(start);
				// maximale Intensität vom Endknoten zu einem verbundenem Knoten i
				double maxEnde = maximaleMarkierung(ende);
				// festlegen der absoulten maximalen Intensität durch Vergelich von Start > Ende Beziehung
				double max = maxStart > maxEnde ? maxStart : maxEnde;
				// Intensität wird für weitere Ameisen festgelegt
				// Hinweg
				markierungen[start][ende] = max + tspoptimize.getMARKIERUNG_VERSTAERKUNG();
				// Rückweg
				markierungen[ende][start] = max + tspoptimize.getMARKIERUNG_VERSTAERKUNG();
			}
		}
	}

	
	/**
	 * Methode überprüft, ob der Graph zufällig oder anhand eines TSP Jobs erzeugt wurde.
	 * 
	 * @return boolean - true wenn der Graph zufällig erstellt wurde
	 */
	public boolean checkGraphModeRandom() {
		if(tspjob==null)
			return true;
		
		return false;
	}
	
	/**
	 * Methode um den Graph mit zufülligen Positionen (Einhaltung von Mindestabstand) der Knoten anlegen
	 */
	public void erzeugeZufaellig() {
		// Initialisierung aller Entfernungen/Markierungen zwischen Knoten
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				// Default Markierungswert ist 1.0
				markierungen[i][j] = 1.0;
				// Default Entfernung ist 0
				entfernungen[i][j] = 0;
			}
		}
		
		// Anlegen des Stadtplans/der Knoten in einem zufälligen "Muster" auf dem Panel
		// Anlegen des 1. Knotens
		xKoordinate[0] = ABSTAND + zufall.nextInt(BREITE);
		yKoordinate[0] = ABSTAND + zufall.nextInt(HOEHE);

		boolean ok;
		int index = 0;
		// Schleife läuft so lange bis alle Städte/Knoten Koordianten erhalten haben
		while (true) {
			index++;
			// prüfen, ob alle Städte/Knoten durchlaufen sind und Endlosschleife verlassen
			if (index >= n)
				break;
			ok = false;
			// Schleife läuft so lange bis die neu anzulegenden Koordianten der minimalen Entfernung entsprechen
			while (!ok) {
				// nächster Stadt/Knoten zufällige Koordinaten zuweisen
				xKoordinate[index] = ABSTAND + zufall.nextInt(BREITE);
				yKoordinate[index] = ABSTAND + zufall.nextInt(HOEHE);
				// Annahme, das Koordinanten OK sind
				ok = true;
				
				// prüfen, ob die Entfernung zu jedem bereits vorhandenem Knoten in Ordnung ist
				for (int i = 0; i < index; i++) {
					// Entfernung zwischen zwei Knoten messen
					int entfernung = entfernung(i, index);
					
					// Entfernungen
					entfernungen[index][i] = entfernung;
					entfernungen[i][index] = entfernung;
					
					// prüfen, ob die Minimale Default Entfernung eingehalten wird
					if (entfernung < tspoptimize.getMINIMALE_ENTFERNUNG_DER_STAEDTE()) {
						ok = false;
					}
				}
			}
		}
	}
	
	/**
	 * Methode um den Graph mit vorgegebenen Positionen (Einhaltung von Mindestabstand) der Knoten anhand eines
	 * TSP Jobs (JSON Datei) anzulegen.
	 */
	public void generateByJSONJob() {
		// Initialisierung aller Entfernungen/Markierungen zwischen Knoten
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				// Default Markierungswert ist 1.0
				markierungen[i][j] = 1.0;
				// Default Entfernung ist 0
				entfernungen[i][j] = 0;
			}
		}
		
		int index = 0;
		
		// Anlegen des Stadtplans/der Knoten anhand des JSON Jobs
		for(JSONArray nodelement:tspjob.getNodes()) {
			xKoordinate[index] = Integer.parseInt(nodelement.get(0).toString());
			yKoordinate[index] = Integer.parseInt(nodelement.get(1).toString());
			if(index>=1) {
				for (int i = 0; i < index; i++) {
					// Entfernung zwischen zwei Knoten messen
					int entfernung = entfernung(i, index);
					
					// Entfernungen
					entfernungen[index][i] = entfernung;
					entfernungen[i][index] = entfernung;
				}
			}
			index++;
		}
	}

	/**
	 * Methode zum Messen der Entfernung zwischen zwei Knoten, sowie der Rückgabe der erfolgten Messung
	 * 
	 * @param i Indexangabe 1. Knoten
	 * @param j Indexangabe 2. Knoten
	 * @return Entfernung der zwei Knoten
	 */
	public int entfernung(int i, int j)	{
		// Berechnung der Entfernung zwischen zwei Knoten
		double xDifferenz = xKoordinate[i] - xKoordinate[j];
		double yDifferenz = yKoordinate[i] - yKoordinate[j];
		double entfernung = Math.sqrt(xDifferenz * xDifferenz + yDifferenz
		    * yDifferenz);
		return (int) entfernung;
	}

	/**
	 * Gibt die höchste Intensität einer Kante von aktueller Stadt zu einer Stadt i zurück
	 * 
	 * @param stadt Stadtindex
	 * @return double Intensität
	 */
	public double maximaleMarkierung(int stadt) {
		double ergebnis = -1.0;
		// alle Städte werden durchlaufen und maximale Intensität/Kante mit höchster Gewichtung ermitteln
		for (int i = 0; i < n; i++) {
			// die aktuelle Stadt überspringen
			if (i == stadt)
				continue;
			// prüfen, ob Intensität/Gewichtung der Kante größer als aktuelles Ergebnis
			// oder Ergebnis den Defaultwert (-1) hat
			if (markierungen[stadt][i] > ergebnis || ergebnis < 0.0) {
				// setze Ergebnis auf neue höhere Intensität
				ergebnis = markierungen[stadt][i];
			}
		}
		return ergebnis;
	}

	/**
	 * Gibt die Entfernung zwischen einem Start- und Zielknoten zurück, ohne erneut zu messen
	 * 
	 * @param start Stadtindex
	 * @param ziel Stadtindex
	 * @return int Entfernung
	 */
	public int gibEntfernung(int start, int ziel) {
		return entfernungen[start][ziel];
	}

	/**
	 * Gibt die Intensität (Gewichtung einer Kante) zurück
	 * 
	 * @param start Stadtindex
	 * @param ziel Stadtindex
	 * @return double Intensität
	 */
	public double gibMarkierung(int start, int ziel) {
		// Intensität der aktuellen Kante zurückgeben
		return markierungen[start][ziel];
	}

	/**
	 * Intensität einer Markierung/Kante erhöhen
	 * 
	 * @param start Stadtindex
	 * @param ziel Stadtindex
	 * @param wert Intensität der Markierung
	 */
	public void erhoeheMarkierung(int start, int ziel, double wert) {
		// Intensität der Markierung/Kante wird erhöht
		markierungen[start][ziel] += wert;
	}

	/**
	 * Gibt die x-Koordinate einer Stadt anhand ihres Index zurück.
	 * 
	 * @param stadt int, Index der Stadt
	 * @return int x-Koordinate der Stadt
	 */
	public int xKoordinate(int stadt) {
		return xKoordinate[stadt];
	}

	/**
	 * Gibt die y-Koordinate einer Stadt anhand ihres Index zurück.
	 * 
	 * @param stadt int, Index der Stadt
	 * @return int y-Koordinate der Stadt
	 */
	public int yKoordinate(int stadt) {
		return yKoordinate[stadt];
	}

	/**
	 * Gibt die Anzahl der Städte/Knoten eines Graphen zurück
	 * 
	 * @return Anzahl der Städte/Knoten
	 */
	public int n() {
		return n;
	}
	
	/**
	 * Gibt eine ArrayList mit allen Städten und deren Koordinaten zurück, allerdings ohne Berücksichtigung
	 * einer kürzesten Tour.
	 * 
	 * @return ArrayList - alle Städte der aktuellen Tour mit ihren Koordinaten
	 */
	public ArrayList<Stadt> getStaedte() {
		staedte.clear();
		// Städte mit deeren Koordinaten zusammenstellen
		for(int i = 0; i < xKoordinate.length; i++) {
			staedte.add(new Stadt(xKoordinate[i], yKoordinate[i], i));
		}
		
		return staedte;
	}
	
	/**
	 * Gibt eine ArrayList mit allen Städten, deren Nachbarstädten und deren Koordinaten als aktuell
	 * kürzeste Tour zurück.
	 * 
	 * @return ArrayList - alle Städte der aktuellen Tour mit ihren Koordinaten
	 */
	public ArrayList<Stadt> getStaedteTour() {
		if (tspoptimize.getKuerzesteTour() == null) {
			return null;
		}
		
		staedteTour.clear();
		
		// Städtetour anhand aktuell vorliegender kürzester Tour zusammenstellen
		for (int i = 0; i < n; i++) {
			int start = tspoptimize.getKuerzesteTour()[i];
			int ende = tspoptimize.getKuerzesteTour()[(i + 1) % n];
			Stadt currentStadt = new Stadt(xKoordinate[start], yKoordinate[start], start);
			currentStadt.setNachbarStadt(new Stadt(xKoordinate[ende], yKoordinate[ende], ende));
			staedteTour.add(currentStadt);
		}
		
		return staedteTour;
	}
}
