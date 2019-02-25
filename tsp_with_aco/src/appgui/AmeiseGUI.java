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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AmeiseGUI extends JFrame implements WindowListener, ActionListener, Runnable {
	public static final long serialVersionUID = 0L;

	private Thread thread;				// Thread für GUI bzw. ACO Algorithmus
	private JButton start;
	private JTextField staedte;
	private AmeiseZeichenpanel panel;	// Panel zur Darstellung des Graphen

	private int schrittZaehler;			// Iterationen des ACO Algorithmus

	private AmeiseWege wege;			// Graph der von Ameisen durchlaufen wird

	/**
	 * Konstruktor für das GUI
	 */
	public AmeiseGUI() {
		// Setzen der Titelleiste, super Auruf aufgrund von Vererbung von JFrame
		super("Das Problem des Handlungsreisenden");
		// Panel für GUI anlegen
		erstelleLayout();
		// Paramter für das GUI setzen
		setSize(1280, 720);
		setVisible(true);
		// EventListener für das Fenster hinzufügen
		// per this da AmeiseGUI das Interface WindowListener implementiert
		addWindowListener(this);
		// Thread für GUI anlegen und dieses übergeben, damit GUI während der Berechnung
		// nicht einfriert (while(true) Statement vorhanden etc.)
		thread = new Thread(this);
	}

	/**
	 * Methode zum Starten des ACO Algorithmus
	 */
	private void starten() {
		// Schrittzähler der Ameisen zurücksetzen
		schrittZaehler = 0;

		// Anzahl der Städte zurücksetzen
		int anzahlStaedte = 0;
		try {
			// Anzahl der Städte anhand Parameterangabe im GUI setzen
			anzahlStaedte = Integer.parseInt(staedte.getText());
		} catch (Throwable t) {
			// falls kein Integer Wert angegeben wurde, wird der Default Wert übernommen
			anzahlStaedte = AmeiseEinstellungen.ANZAHL_STAEDTE;
		}
		
		// falls die Angabe den Maximalwert übersteigt wird der Default Maxwert genommen
		if (anzahlStaedte > AmeiseEinstellungen.ANZAHL_STAEDTE_MAXIMAL) {
			anzahlStaedte = AmeiseEinstellungen.ANZAHL_STAEDTE_MAXIMAL;
		}
		// Angabe wird für GUI Title Angabe übernommen
		staedte.setText(Integer.toString(anzahlStaedte));

		// kritischer Abschnitt bezüglich GUI und ACO Algorithmus
		synchronized (this) {
			// Klassenvariablen für jede Ameise setzen
			Ameise.initialisieren();
			// neuen Graph(Ameisenwege) anlegen
			wege = new AmeiseWege(anzahlStaedte, AmeiseEinstellungen.ANZAHL_AMEISEN);
			// Zeichenpanel für Graph wird Graph übergeben
			panel.setzeWege(wege);
		}
	}

	/**
	 * runnable des GUI Threads mit ACO Algorithmus
	 */
	public void run() {
		// ACO Algorithmus läuft endlos, da Optimum für TSP nicht bekannt
		while (true) {
			// kritischer Abschnitt bezüglich GUI und ACO Algorithmus
			synchronized (this) {
				// Aufruf einer Iteration des ACO Algorithmus
				wege.zeitSchritt();
				schrittZaehler++;
				// prüfen, ob 100 Schritte vollzogen wurden
				if (schrittZaehler % 100 == 0) {
					// pausieren, um aktuelles Ergebnis der letzten 100 Iterationen zu checken
					/*try {
						System.out.println("TH-Name: " + Thread.currentThread().getName());
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
					// GUI Title aktualisieren
					setTitle(schrittZaehler + " Schritte gerechnet mit " + AmeiseEinstellungen.ANZAHL_AMEISEN + " Ameisen und " + staedte.getText() + " Staedten");
					// Graph neu zeichnen
					// ruft die Methode paintComponent in AmeiseZeichenpanel
					repaint();
				}
			}
		}
	}
	
	/**
	 * legt die Inhalte des GUIs fest. Panel für TSP Graph, Button zum Starten etc.
	 */
	private void erstelleLayout() {
		// anlegen des Hauptpanel
		JPanel hauptPanel = new JPanel();
		// Panel für Graph anlegen
		panel = new AmeiseZeichenpanel();
		hauptPanel.add(panel, BorderLayout.CENTER);

		// Panel für Buttons/Menüführung unten anlegen
		JPanel kommandoPanel = new JPanel();
		kommandoPanel.setLayout(new GridLayout(1, 10));

		// Labels, Inputext und Button für Startparameter anlegen
		kommandoPanel.add(new JLabel("Staedte:"));
		staedte = (JTextField) kommandoPanel.add(new JTextField("25"));
		start = (JButton) kommandoPanel.add(new JButton("start"));
		// Start Button mit ActionListener versehen
		start.addActionListener(this);
		
		// Menüführungspanel ins GUI einbinden
		hauptPanel.add(kommandoPanel, BorderLayout.SOUTH);
		getContentPane().add(hauptPanel);
	}

	/**
	 * Methode wird ausgeführt wenn Startbutton gedrückt wurde
	 */
	public void actionPerformed(ActionEvent e) {
		// prüfen, ob Startbutton gedrückt wurde
		if (e.getSource() == start) {
			// ACO Algorithmus starten
			starten();
			// prüfen, ob Thread bereits läuft
			if (!thread.isAlive()) {
				// Thread starten (run Methode wird ausgeführt)
				thread.setName("GUI Thread");
				thread.start();
			}
		}
	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void windowClosed(WindowEvent e)
	{
	}

	/**
	 * Methode wird aufgerufen, wenn Fenster geschlossen wird
	 */
	public void windowClosing(WindowEvent e) {
		dispose();
		// Programm beenden
		System.exit(0);
	}

	public void windowDeactivated(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowOpened(WindowEvent e)
	{
	}

	/**
	 * Main Methode zum Starten der Applikation
	 * @param argumente - Startparameter für Applikation
	 */
	public static void main(String[] argumente)
	{
		// Starten der Applikation
		new AmeiseGUI();
		System.out.println("TH-Name: "  + Thread.currentThread().getName());
	}
}
