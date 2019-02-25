package appnogui;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Die Klasse Stadt dient zur erweiterten Hinterlegung von Informationen für einen Knoten im Graph, statt lediglich den
 * Koordinaten. Aktuell wird die Klasse jedoch lediglich zum Zusammenstellen einer bereits ermittelten Städtetour genutzt
 * und nicht explizit beim ACO Algorithmus.
 * 
 * @author WPF-VS - SS 2017
 *
 */
public class Stadt implements Serializable {
	/**
	 * x-Koordinate der Stadt
	 */
	private int x;
	/**
	 * y-Koordinate der Stadt
	 */
	private int y;
	/**
	 * Nachbarstädte dieser Stadt
	 */
	private ArrayList<Stadt> nachbarstaedte = new ArrayList<Stadt>();
	/**
	 * Index der Stadt
	 */
	private int index;
	
	/**
	 * Standard Konstruktor zum Anlegen einer Stadt.
	 * 
	 * @param x - x-Koordinate der Stadt
	 * @param y - y-Koordinate der Stadt
	 * @param index - Index der Stadt
	 */
	public Stadt(int x, int y, int index) {
		this.x = x;
		this.y = y;
		this.index = index;
	}
	
	/**
	 * Methode gibt die x-Koordinate einer Stadt zurück
	 * 
	 * @return int - x-Koordinate
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Methode gibt die y-Koordinate einer Stadt zurück
	 * 
	 * @return int - y-Koordinate
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Methode gibt den Index einer Stadt zurück
	 * 
	 * @return int - Index einer Stadt
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Methode hinterlegt eine zustätzliche Nachbarstadt zu einer Stadt an
	 * 
	 * @param stadt - Nachbarstadt der Stadt
	 */
	public void setNachbarStadt(Stadt stadt) {
		nachbarstaedte.add(stadt);
	}
	
	/**
	 * Methode gibt die Nachbarstädte einer Stadt zurück
	 * 
	 * @return ArrayList - Alle Nachbarstädte der Stadt
	 */
	public ArrayList<Stadt> getNachbarStadt() {
		return nachbarstaedte;
	}
}
