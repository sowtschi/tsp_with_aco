package appnogui;

import java.io.Serializable;
import java.util.Random;

/**
 * Die Klasse Ameise dient zum Anlegen von Ameisen für den ACO Algorithmus. 
 * 
 * @author WPF-VS - SS 2017
 *
 */
public class Ameise implements Serializable {
	/**
	 * Zufallsvariable für zufülliges Ziel der Ameise etc.
	 */
	private static Random zufall;
	/**
	 * kompletter Graph bzw. mögliche Wege einer Ameise
	 */
	private AmeiseWege wege;
	/**
	 * Anzahl der Städte/Knoten vom Graphen
	 */
	private int n;
	/**
	 * Startindex einer Stadt aus der Städtetour
	 */
	private int start;
	/**
	 * Zielindex einer Stadt aus der Städtetour
	 */
	private int ziel;
	/**
	 * Anzahl der Schritte einer Ameise
	 */
	private int schritte;
	/**
	 * Array zur Speicherung der Reihenfolge von Besuchen für die jeweilen Städte. Die Größte entspricht der Anzahl
	 * an Städten bzw. Knoten im Graphen.
	 */
	private int[] stadtBesucht;
	/**
	 * Index für Array der besuchten Stadt {@link #stadtBesucht}
	 */
	private int stadtBesuchtIndex;
	/**
	 * Optimierung an der die Ameise beteiligt ist
	 */
	private TSPwithACO tspoptimize;

	/**
	 * Konstruktor zum Anlegen einer Ameise
	 * 
	 * @param w - Graph, welchen die Ameise durchlaufen kann
	 * @param optimize - TSP Optimierung, an welcher die Ameise beteiligt ist
	 */
	public Ameise(AmeiseWege w, TSPwithACO optimize) {
		// Zufallswert für spätere zufällige Ziele setzen
		zufall = new Random(12L);
		// Zuweisung TSP Optimierung
		tspoptimize = optimize;
		// Graph wird zugewiesen
		wege = w;
		// Anzahl der Städte/Knoten zuweisen
		n = wege.n();
		// Schritte der Ameise auf 0 setzen
		schritte = 0;
		// nächstes zufälliges Ziel (Knoten) der Ameise bestimmen
		ziel = zufall.nextInt(n);
		// Start = Ziel setzen, damit Startstadt als erstes markiert wird
		start = ziel;
		// Array Elemente = Anzahl der Städte anlegen, um eine Reihenfolge von Besuchen für die
		// jeweilen Städte zu setzen (Realisierung durch int = -1 noch nichts besucht (default))
		stadtBesucht = new int[n];
		// alle Städte auf nicht besucht setzen
		for (int i = 0; i < n; i++) {
			// Stadt nicht besucht default = -1
			stadtBesucht[i] = -1;
		}
		// Index der besuchten Stadt auf 0 setzen
		stadtBesuchtIndex = 0;
		// prüfen, ob Ameise auf der kürzesten Streckte unterwegs ist (default = null, also nicht)
		if (tspoptimize.getKuerzesteTour() == null) {
			// Array Elemente = Anzahl der Städte anlegen, um die Reihenfolge für die kürzesteTour zu speichern
			tspoptimize.setKuerzesteTour(new int[n]);
		}
	}

	public TSPwithACO getTSPOptimize() {
		return tspoptimize;
	}
	
	/**
	 * Methode zum Laufen einer Ameise. Beim Durchquerem des Graphen setzt eine Ameise auf den einzelnen
	 * Wegen/Kanten entsprechende Markierungen für nachfolgende Ameisen, was als Grundlage für den ACO
	 * Algorithmus dient.
	 */
	public void laufen() {
		// prüfen, ob die Ameise noch keinen Schritt gelaufen ist
		if (schritte <= 0) {
			// Ameise ist noch nicht gelaufen
			// Ameise ordnet Ziel (default start = ziel) als besuchte Stadt ein
			merkeStadtAlsBesucht(ziel);
			
			// prüfen, ob alle Städte bereits besucht wurden
			if (gibAnzahlBesuchterStaedte() == n) {
				// anhand durchlaufener Reihenfolge feststellen, ob neue kürzeste Strecke gefunden wurde
				// in jedem Fall Reihenfolge der Städte wieder auf default setzen
				zuruecksetzen();
			}

			// prüfen, ob start nicht dem Ziel entspricht (Ameise unterwegs in Städten/Knoten)
			if (start != ziel) {
				// Intensität der Markierung wird berechnet
				double markierungsIntensitaet = berechneMarkierung(start, ziel);
				// Gewichtung einer Kante des Graph wird erhöht (Intensität der Markierung)
				// Rückweg
				wege.erhoeheMarkierung(ziel, start, markierungsIntensitaet);
				// Hinweg
				wege.erhoeheMarkierung(start, ziel, markierungsIntensitaet);
			}

			// Start wird gleich Ziel gestzt
			start = ziel;

			// Fallunterscheidung, ob Ameise einer Markierung folgen soll, oder selbst eine weitere setzen soll
			boolean waehleZufaellig = zufall.nextDouble() < tspoptimize.getWAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL();
			double markierungen = 0.0;
			
			// alle Städte werden durchlaufen
			for (int i = 0; i < n; i++) {
				// falls aktuelle Stadt dem Start entspricht
				if (i == start)
					continue;
				// falls aktuelle Stadt bereits besucht
				if (istStadtBesucht(i))
					continue;
				// falls wähle Zufaellig aktiv ist, erhöhe Markierungen um 1
				if (waehleZufaellig) {
					markierungen += 1.0;
				} else {
					// sonst setze Markierungen auf Intensität der Strecke Start zur aktuellen Stadt
					markierungen += wege.gibMarkierung(start, i);
				}
			}
			
			// Fallunterscheidung, ob Ameise einer Markierung folgen soll, oder selbst eine weitere setzen soll
			// Wahrscheinlichkeit wird anhand zuvor bestehender Markierungen aller Kanten bestimmt
			double wahrscheinlichkeit = zufall.nextDouble() * markierungen;
			double kumulierteMarkierungen = 0.0;
			
			// Städte werden so lange durchlaufen, bis wahrscheinlichkeit <= kumulierteMarkierungen
			for (ziel = 0; ziel < n; ziel++) {
				if (ziel == start)
					continue;
				if (istStadtBesucht(ziel))
					continue;
				if (waehleZufaellig)
					kumulierteMarkierungen += 1.0;
				else
					kumulierteMarkierungen += wege.gibMarkierung(start, ziel);
				if (wahrscheinlichkeit <= kumulierteMarkierungen)
					break;
			}
			// prüfen, ob mit dem aktuellen Ziel alle Städte durchlaufen wurden
			if (ziel == n) {
				// Ziel um eine Stadt zurücksetzen
				ziel = n - 1;
			}
			// Schritte werden auf Entfnerung von Start zum aktuellen Ziel gesetzt
			schritte = wege.gibEntfernung(start, ziel);
		}

		// wenn die Ameise schon gelaufen ist, wird deren Schrittanzahl
		// um eine Default Schrittweite reduziert (default = 10)
		schritte -= tspoptimize.getSCHRITTWEITE();
	}

	public AmeiseWege getWege() {
		return wege;
	}
	
	/**
	 * Methode zum Merken einer besuchten Stadt/Knoten
	 * 
	 * @param stadt - Index der Stadt
	 */
	private void merkeStadtAlsBesucht(int stadt) {
		// besuchte Stadt wird in Reihenfolge der besuchten Städte aufgenommen
		stadtBesucht[stadtBesuchtIndex] = stadt;
		// Stadtbesuchtindex wird durch Modulo Rechnung inkrementiert
		stadtBesuchtIndex = (stadtBesuchtIndex + 1) % n;
	}

	/**
	 * Methode zum Prüfen, ob die angegebene Stadt bereits besucht wurde.
	 * 
	 * @param stadt - Index der Stadt
	 * @return boolean - wurde besucht oder nicht
	 */
	private boolean istStadtBesucht(int stadt) {
		for (int i = 0; i < n; i++)
		{
			if (stadtBesucht[i] == stadt)
				return true;
		}
		return false;
	}

	/**
	 * Methode gibt die Anzahl der besuchten Städte zurück.
	 * 
	 * @return int - Anzahl besuchter Städte
	 */
	private int gibAnzahlBesuchterStaedte() {
		int ergebnis = 0;
		// zählen, wie viele Städtedie Ameise bereits besucht hat
		for (int i = 0; i < n; i++) {
			// prüfen, ob Stadt in der Reihenfolge besuchter Städte vorhanden ist
			if (stadtBesucht[i] >= 0)
				ergebnis++;
		}
		return ergebnis;
	}

	/**
	 * Methode zum Berechnen der Markierung.
	 * 
	 * @param start - Stadtindex vom Start
	 * @param ziel - Stadtindex vom Ziel
	 * @return double - Stärke der Markierung
	 */
	private double berechneMarkierung(int start, int ziel) {
		// Markierung entspricht der Berechnung 1.0/Entfernung von Start und Ziel
		return 1.0 / wege.gibEntfernung(start, ziel);
	}

	/**
	 * Methode setzt eine neue kürzeste Wegstrecke, sofern noch gar keine existiert (default)
	 * oder die aktuelle Reihenfolge der besuchten Städte einem neuen Optimum entspricht.
	 * Anschließend wird das neue Optimum bestimmt und die Reihenfolge der besuchten Städte
	 * gelöscht.
	 */
	private void zuruecksetzen() {
		int laenge = 0;
		int[] kuerzesteTour = null;
		// alle Städte durchlaufen
		for (int i = 0; i < n; i++) {
			// Wegstrecke der Reihenfolge aller besuchten Städte zurückgeben
			laenge += wege.gibEntfernung(stadtBesucht[i], stadtBesucht[(i + 1) % n]);
		}
		// prüfen, ob die Wegstrecke kürzer als die bekannteste kürzeste Wegstrecke ist
		// oder ob kuerzesteTourLaenge noch ihren Defaultwert (-1) besitzt
		if (laenge < tspoptimize.getkuerzesteTourLaenge() || tspoptimize.getkuerzesteTourLaenge() < 0) {
			// neue kürzeste Wegstrecke festlegen
			tspoptimize.setkuerzesteTourLaenge(laenge);
			kuerzesteTour = tspoptimize.getKuerzesteTour();
			// kürzeste Wegstrecke wird anhand der aktuellen Reihenfolge der besuchten Städte festgelegt
			for (int i = 0; i < n; i++) {
				kuerzesteTour[i] = stadtBesucht[i];
			}
		}

		// die Reihenfolge der aktuell besuchten Städte wird komplett verworfen
		for (int i = 0; i < n; i++) {
			stadtBesucht[i] = -1;
		}
	}
}
