package appgui;
// Zusatzmaterial zum Buch 
// "Algorithmen kompakt und verstaendlich"

// Das in diesem Werk enthaltene Programm-Material 
// ist mit keiner Verpflichtung oder Garantie 
// irgendeiner Art verbunden. 

// Der Autor uebernimmt infolgedessen keine Verantwortung 
// und wird keine daraus folgende oder sonstige Haftung 
// uebernehmen, die auf irgendeine Art aus der Benutzung 
// dieses Programm-Materials oder Teilen davon entsteht.

// Verlag und Autor weisen darauf hin, dass keine Pruefung
// vorgenommen wurde, ob die Verwertung der beschriebenen
// Algorithmen und Verfahren mit Schutzrechten Dritter 
// kollidiert.

// Verlag und Autor schliessen insofern jegliche Haftung aus.

// ISBN 978-3-658-05617-9 Springer Vieweg 
// (c) Springer Fachmedien Wiesbaden

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AmeiseWege {
	public static final int ABSTAND = 30;
	public static final int BREITE = 1200-2*ABSTAND;
	public static final int HOEHE = 600-2*ABSTAND;
	
	private static Random zufall = new Random(42L);

	private ArrayList<Ameise> ameisen;
	private int entfernungen[][];
	private double markierungen[][];
	private int xKoordinate[];			// x-Koordianten für Knoten
	private int yKoordinate[];			// y-Koordinaten für Knoten
	private int n;						// Anzahl der Städte/Knoten
	
	/**
	 * Konstruktor für das Anlegen des Graphs (Ameisenwege)
	 * 
	 * @param n - Anzahl der Knoten
	 * @param anzahlAmeisen - Anzahl der Ameisen
	 */
	public AmeiseWege(int n, int anzahlAmeisen) {
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
		// Graph mit zufälligen Positionen (Einhaltung von Mindestabstand) der Knoten anlegen
		erzeugeZufaellig();
		// ArryList für Ameisten wird angelegt
		ameisen = new ArrayList<Ameise>();
		// ArrayList wird entsprechend des Anzahl Ameisen Paramter gefüllt
		for (int i = 0; i < anzahlAmeisen; i++) {
			// Ameise wird angelegt und erhält den kompletten zufüllig erzeugten Graph
			ameisen.add(new Ameise(this));
		}
	}
	
	/**
	 * Methode zum Aufruf einer Iteration des ACO Algorithmus
	 */
	public void zeitSchritt() {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		// vorhandene Ameisen werden der Reihe nach aufgerufen
		for (Ameise ameise : ameisen) {
			// Ameise durchläuft Graph und setzt dabei Markierungen anhand von Wahrscheinlichkeiten
			// für verteile Systeme auslagern, sodass mehrere Ameisen gleichzeitg laufen können
			//ameise.laufen();
			Runnable worker = new WorkerThread(ameise);
			executor.execute(worker);
		}
		executor.shutdown();
		while(!executor.isTerminated()) {
			//System.out.println("es laufen noch welche...");
		}
		//System.out.println("Alle Ameisen haben ihre Runde gedreht!");
				
		// alle Städte bzw. Kanten und deren Markierungen werden durchlaufen
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				// Markierungen werden um einen Defaultwert (0.99) abgeschwächt
				markierungen[i][j] *= AmeiseEinstellungen.MARKIERUNG_ABSCHWAECHUNG;
			}
		}
		
		// aktuell vorhandene kürzeste Wegstrecke aller Ameisen wird geprüft
		if (Ameise.gibKuerzesteTour() != null) {
			// alle Städte der Tour werden durchlaufen
			for (int i = 0; i < n; i++) {
				// Start der kürzesten Wegstrecke wird gesetzt
				int start = Ameise.gibKuerzesteTour()[i];
				// Ziel der kürzesten Wegstrecke wird gesetzt
				int ende = Ameise.gibKuerzesteTour()[(i + 1) % n];
				// maximale Intensität vom Startknoten zu einem verbundenem Knoten i
				double maxStart = maximaleMarkierung(start);
				// maximale Intensität vom Endknoten zu einem verbundenem Knoten i
				double maxEnde = maximaleMarkierung(ende);
				// festlegen der absoulten maximalen Intensität durch Vergelich von Start > Ende Beziehung
				double max = maxStart > maxEnde ? maxStart : maxEnde;
				// Intensität wird für weitere Ameisen festgelegt
				// Hinweg
				markierungen[start][ende] = max + AmeiseEinstellungen.MARKIERUNG_VERSTAERKUNG;
				// Rückweg
				markierungen[ende][start] = max + AmeiseEinstellungen.MARKIERUNG_VERSTAERKUNG;
			}
		}
		// ACO Iteration abgeschlossen
	}

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
					if (entfernung < AmeiseEinstellungen.MINIMALE_ENTFERNUNG_DER_STAEDTE) {
						ok = false;
					}
				}
			}
		}
	}

	/**
	 * Methode zum Messen der Entfernung zwischen zwei Knoten
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
	 * Gibt die Entfernung zwischen einem Start- und Zielknoten zurück
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

	public int xKoordinate(int stadt)
	{
		return xKoordinate[stadt];
	}

	public int yKoordinate(int stadt)
	{
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
}
