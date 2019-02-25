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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class AmeiseZeichenpanel extends JPanel {
	public static final long serialVersionUID = 0L;

	public static final int BREITE = AmeiseWege.BREITE + 2 * AmeiseWege.ABSTAND;	// Konstante Breite für Panel
	public static final int HOEHE = AmeiseWege.HOEHE + 2 * AmeiseWege.ABSTAND;		// KOnstante Höhe für Panel

	private AmeiseWege wege;	// Graph
	private int n;				// Anzahl der Städte

	/**
	 * Konstruktor für das Panel zur Ausgabe des aktuellen Stand der TSP Optimierung.
	 * In diesem Panel wird der aktuelle Graph jeweils angepasst und neu gezeichnet.
	 */
	public AmeiseZeichenpanel() {
		super();
		// setzen der Höhe, Breite etc.
		setSize(new Dimension(BREITE, HOEHE));
		setMinimumSize(new Dimension(BREITE, HOEHE));
		setMaximumSize(new Dimension(BREITE, HOEHE));
		setPreferredSize(new Dimension(BREITE, HOEHE));
	}

	/**
	 * Methode zum Setzen des Graphs und der Anzahl der Städte
	 * 
	 * @param w Graph
	 */
	public void setzeWege(AmeiseWege w) {
		// Graph wird fürs Zeichenpanel gestzt
		wege = w;
		// Anzahl der Städte wird gestezt
		n = wege.n();
	}
	
	/**
	 * Zeichnet den Graph neu und aktualisiert das Panel.
	 * Wird von repaint aus AmeiseGUI gerufen.
	 */
	public void paintComponent(Graphics gg) {
		super.paintComponent(gg);

		Graphics2D g = (Graphics2D) gg;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, BREITE, HOEHE);

		zeichnePfad(gg);
		zeicheStaedte(gg);
	}

	private void zeichnePfad(Graphics g) {
		// prüfen, ob  kurze Wegstrecke existiert
		if (Ameise.gibKuerzesteTour() == null) {
			// wenn nicht, muss nichts gezeichnet werden
			return;
		}
		
		// alle Städte durchlaufen
		for (int i = 0; i < n; i++) {
			int start = Ameise.gibKuerzesteTour()[i];
			int ende = Ameise.gibKuerzesteTour()[(i + 1) % n];

			int x1 = wege.xKoordinate(start);
			int x2 = wege.xKoordinate(ende);
			int y1 = wege.yKoordinate(start);
			int y2 = wege.yKoordinate(ende);

			// Zeichnen der Kanten der aktuellen kürzesten Wegstrecke
			for (int j = -2; j <= 2; j++) {
				for (int k = -2; k <= 2; k++) {
					g.setColor(Color.BLACK);
					g.drawLine(x1 + j, y1 + k, x2 + j, y2 + k);
				}
			}
		}
	}

	private void zeicheStaedte(Graphics g)
	{
		int x;
		int y;
		for (int i = 0; i < n; i++)
		{
			x = wege.xKoordinate(i);
			y = wege.yKoordinate(i);
			g.setColor(Color.WHITE);
			g.fillOval(x - 10, y - 10, 20, 20);
			g.setColor(Color.BLACK);
			g.drawOval(x - 10, y - 10, 20, 20);
			// g.drawString(Integer.toString(i),x-(i < 10 ? 4 : 8),y+5);
		}
	}
}
