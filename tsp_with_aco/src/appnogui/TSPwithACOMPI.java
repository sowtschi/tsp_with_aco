package appnogui;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Die Klasse TSPwithACOMPI dient als Oberklasse für alle TSPwithACO Szenarien. Über eine JSON Datei werden zunächst
 * globale Einstellungen, welche zum Teil für alle oder nur spezielle Szenarien gelten, entnommen. Des Weiteren wird
 * festgelegt, welches Szenario überhaupt durchgeführt werden soll.
 * 
 * Diese Klasse wird als Einstiegspunkt (main class) der Applikation benutzt. Der Aufruf muss also per MPJ Express
 * mit entsprechenden Parametern erfolgen.
 * 
 * @author WPF-VS - SS 2017
 *
 */
public class TSPwithACOMPI {
	/**
	 * Rang/Nummer des jeweiligen Prozess im Kommunikator MPI_COMM_WORLD
	 */
	protected int rank;
	/**
	 * Gesamtanzahl aller Prozesse im Kommunikator MPI_COMM_WORLD
	 */
	protected int size;
	/**
	 * Anzahl der verfügbaren CPU Kerne
	 */
	protected int processors;
	/**
	 * Platzhaler für Wurzelprozess
	 */
	protected int root;
	/**
	 * Buffer Größeangabe/Anzahl an TSP Optimierungen
	 */
	protected int buffersize;
	/**
	 * Größenangabe für Verteilung der Daten über MPI (bspw. Verhältnis Tasks zu Prozessen)
	 */
	protected int scattersize;
	/**
	 * legt fest, wie der Graph für die TSP Optimierung aufgebaut wird
	 */
	protected final String GRAPH_MODE;
	/**
	 * legt fest, welcher TSP Job im "Ant" Szenario durchgeführt werden soll
	 */
	protected final String ANT_TSPJOB;
	/**
	 * legt fest, ob die Ergebnisse von Multi-TSP synchron oder asynchron zurückgegeben werden sollen
	 */
	protected final String MULTI_TSP_SYN_OPT;
	/**
	 * Dateiname der globalen Einstellungen
	 */
	private final String FILE_SETTINGS = "settings.json";
	/**
	 * Dateiname des Standard TSP Job im "Ant" Szenario
	 */
	private final String FILE_DEFAULT_TSPJOB = "default.json";
	/**
	 * Pfad zu den TSP Jobs (JSON Dateien)
	 */
	private final String DIRECTORY_JOBPATH = "tspjobs";
	/**
	 * Pfad zu den cfg Ordner (JSON Dateien)
	 */
	private final String DIRECTORY_CFGPATH = "cfg";
	
	/**
	 * Der Standardkonstruktor lädt die globalen Einstellungen aus der JSON Datei.
	 */
	public TSPwithACOMPI() {
		String sGraphMode = GraphMode.valueOf("random").toString();
		String sAntTSPJob = FILE_DEFAULT_TSPJOB;
		String sMultiTSPOpt = MultiTSPSynOpt.valueOf("syn").toString();
		// Überprüfung aller notwendigen Dateien
		if(!checkEssentialFiles()) {
			System.err.println("Nicht alle notwendigen Dateien zum Programmstart vorhanden!");
			System.exit(0);
		}
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("./"+DIRECTORY_CFGPATH+"/"+FILE_SETTINGS));
			JSONObject tspJSON = (JSONObject) obj;
			sGraphMode = GraphMode.valueOf(tspJSON.get("GRAPH_MODE").toString()).toString();
			sAntTSPJob = tspJSON.get("ANT_TSPJOB").toString();
			sMultiTSPOpt = MultiTSPSynOpt.valueOf(tspJSON.get("MULTI_TSP_SYN_OPT").toString()).toString();
		} catch (Exception e) {
			if(rank==root)
				System.err.println("TSPwithACOMPI() Fehler: " + e.getMessage());
			// Programm beenden, da die settings.json nicht korrekt gefüllt ist
			System.exit(0);
		}
		this.GRAPH_MODE = sGraphMode;
		this.ANT_TSPJOB = sAntTSPJob;
		this.MULTI_TSP_SYN_OPT = sMultiTSPOpt;
	}
	
	/**
	 * Der Konstruktor nimmt die Startparameterliste von MPJ-Express entgegen, erstellt das entsprechende Szenario als
	 * Objekt und übergibt diesem die jeweiligen MPI Parameter von MPJ-Express.
	 * 
	 * @param args - Stirng Array mit MPI Parametern und "Java" Startparametern
	 */
	public TSPwithACOMPI(String args[]) {
		this();
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("./"+DIRECTORY_CFGPATH+"/"+FILE_SETTINGS));
			JSONObject tspJSON = (JSONObject) obj;
			switch(Mode.valueOf(tspJSON.get("MODE").toString())) {
				case ant:
					new TSPwithACOMPIAnt(args);
				break;
				case multitsp:
					new TSPwithACOMPIMultiTSP(args);
				break;
				default:
					System.err.println("Kein mögliches Szenario gewählt!");
				break;
			}
		} catch (Exception e) {
			if(rank == root)
				//System.err.println("TSPwithACOMPI(String args[]) Fehler: " + e.getMessage());
				e.printStackTrace();
		}
	}
	
	/**
	 * Die Methode überprüft, ob zum Programmstart alle notwendingen Dateien (settings.json etc.) vorliegen.
	 * 
	 * @return boolean - false, sofern nicht alle Dateien vorliegen
	 */
	public boolean checkEssentialFiles() {
		// Überprüfung für "tspjobs" Ordner
		File folderJobs = new File("./" + DIRECTORY_JOBPATH);
		if(folderJobs.exists()) {
			if(folderJobs.listFiles().length == 0) {
				System.err.println("Die Datei default.json liegt nicht vor!");
				return false;
			}
			
			File[] listOfFiles = folderJobs.listFiles();
			boolean defaultJSON = false;
			
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	  if(listOfFiles[i].getName().equals("default.json"))
		    		  defaultJSON = true;
		      }
		    }
		    
		    if(!defaultJSON) {
		    	System.err.println("Die Datei default.json liegt nicht vor!");
				return false;
		    }	
		} else {
			System.err.println("Pfad für Jobs (tsp) existiert nicht!");
		}
		
		// Überprüfung für "cfg" Ordner
		folderJobs = new File("./" + DIRECTORY_CFGPATH);
		if(folderJobs.exists()) {
			if(folderJobs.listFiles().length == 0) {
				System.err.println("Es liegen keine settings Dateien vor!");
				return false;
			}
			
			File[] listOfFiles = folderJobs.listFiles();
			boolean defaultJSONsetting = false;
			boolean defaultJSONtspwithaco = false;
			
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	  if(listOfFiles[i].getName().equals("settings.json"))
		    		  defaultJSONsetting = true;
		    	  
		    	  if(listOfFiles[i].getName().equals("tspwithacosettings.json"))
		    		  defaultJSONtspwithaco = true;
		      }
		    }
		    
		    if(!defaultJSONsetting || !defaultJSONtspwithaco) {
		    	System.err.println("Es liegen keine settings Dateien oder nicht alle vor!");
				return false;
		    }	
		} else {
			System.err.println("Pfad für settings (cfg) existiert nicht!");
		}
		
		return true;
	}
	
	/**
	 * Starten der Applikation
	 * 
	 * @param args - Stirng Array mit MPI Parametern und "Java" Startparametern
	 */
	public static void main(String args[]) {
		new TSPwithACOMPI(args);
	}
}
