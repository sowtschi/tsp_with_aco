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

import java.util.Random;

public class Ameise
{
	private static Random zufall;

	private AmeiseWege wege;		// kompletter Graph bzw. mögliche Wege einer Ameise
	private int n;					// Anzahl der Städte/Knoten von Graph
	private int start;				// Startindex einer Stadt
	private int ziel;				// Zielindex einer Stadt
	private int schritte;			// Anzahl der Schritte einer Ameise
	private int[] stadtBesucht;		// Array Elemente = Anzahl der Städte anlegen, um eine Reihenfolge von Besuchen für die jeweilen Städte zu speichern
	private int stadtBesuchtIndex;	// Index für Array der besuchten Stadt

	private static int[] kuerzesteTour = null;		// kürzester Weg (Reihenfolge der Städte default auf null
	private static int kuerzesteTourLaenge = -1;	// kürzeste Weglänge default auf -1

	/**
	 * Konstruktor zum Anlegen einer Ameise
	 * 
	 * @param w Graph den die Ameise durchlaufen kann
	 */
	public Ameise(AmeiseWege w) {
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
		if (kuerzesteTour == null) {
			// Array Elemente = Anzahl der Städte anlegen, um die Reihenfolge für die kürzesteTour zu speichern
			kuerzesteTour = new int[n];
		}
	}

	/**
	 * statische Methode zum Initialisieren der Klassenvariablen
	 */
	public static void initialisieren() {
		// Zufallswert für spätere zufällige Ziele setzen
		zufall = new Random(12L);
		// kürzester Weg default auf null
		kuerzesteTour = null;
		// kürzeste Weglänge default auf -1
		kuerzesteTourLaenge = -1;
	}

	/**
	 * Gibt die Reihenfolge der Städte der aktuell kürzesten Tour zurück
	 * 
	 * @return int[] Reihenfolge der Städte
	 */
	public static int[] gibKuerzesteTour() {
		return kuerzesteTour;
	}

	/**
	 * Methode zum Laufen einer Ameise
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
				synchronized (kuerzesteTour) {
					zuruecksetzen();
				}
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

			boolean waehleZufaellig = zufall.nextDouble() < AmeiseEinstellungen.WAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL;

			double markierungen = 0.0;
			// alle Städte werden durchlaufen
			for (int i = 0; i < n; i++) {
				// falls aktuelle Stadt dem Start entspricht
				if (i == start)
					continue;
				// falls aktuelle Stadt bereits besucht
				if (istStadtBesucht(i))
					continue;
				// falls waehleZufaellig aktiv ist, erhöhe Markierungen um 1
				if (waehleZufaellig) {
					markierungen += 1.0;
				} else {
					// sonst setze Markierungen auf Intensität der Strecke Start zur aktuellen Stadt
					markierungen += wege.gibMarkierung(start, i);
				}
			}
			
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
		schritte -= AmeiseEinstellungen.SCHRITTWEITE;
	}

	/**
	 * Methode zum Merken einer besuchten Stadt/Knoten
	 * 
	 * @param stadt index
	 */
	private void merkeStadtAlsBesucht(int stadt) {
		// besuchte Stadt wird in Reihenfolge der besuchten Städte aufgenommen
		stadtBesucht[stadtBesuchtIndex] = stadt;
		// Stadtbesuchtindex wird durch Modulo Rechnung inkrementiert
		stadtBesuchtIndex = (stadtBesuchtIndex + 1) % n;
	}

	private boolean istStadtBesucht(int stadt)
	{
		for (int i = 0; i < n; i++)
		{
			if (stadtBesucht[i] == stadt)
				return true;
		}
		return false;
	}

	/**
	 * Gibt die Anzahl der besuchten Städte zurück
	 * 
	 * @return int Anzahl besuchter Städte
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
	 * Methode zum Berechnen der Markierung
	 * 
	 * @param start Stadtindex vom Start
	 * @param ziel Stadtindex vom Ziel
	 * @return double Stärke der Markierung
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
		// alle Städte durchlaufen
		for (int i = 0; i < n; i++) {
			// Wegstrecke der Reihenfolge aller besuchten Städte zurückgeben
			laenge += wege.gibEntfernung(stadtBesucht[i], stadtBesucht[(i + 1) % n]);
		}
		// prüfen, ob die Wegstrecke kürzer als die bekannteste kürzeste Wegstrecke ist
		// oder ob kuerzesteTourLaenge noch ihren Defaultwert (-1) besitzt
		if (laenge < kuerzesteTourLaenge || kuerzesteTourLaenge < 0) {
			// neue kürzeste Wegstrecke festlegen
			kuerzesteTourLaenge = laenge;
			// kürzeste Wegstrecke wird anhan der aktuellen Reihenfolge der besuchten Städte festgelegt
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
