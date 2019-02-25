package appnogui;

import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Die Klasse TSPJobLoader dient zum Einlesen von TSP Jobs per JSON und Bereitstellung dessen Parametern.
 * Ein TSP Job wird somit durch eine jeweilige JSON Datei abgebildet.
 * 
 * @author WPF-VS - SS 2017
 *
 */
public class TSPJobLoader implements Serializable {
	/**
	 * Anzahl der TSP Jobs (JSON Dateien)
	 */
	private int amountJobs;
	/**
	 * Anzahl der Knoten des TSP Jobs
	 */
	private int amountNodes;
	/**
	 * Pfad zu den TSP Jobs (JSON Dateien)
	 */
	private String jobPath;
	/**
	 * Ordner mit allen TSP Jobs
	 */
	private File folderJobs;
	/**
	 * Liste aller Dateinamen der TSP Jobs
	 */
	private ArrayList<String> listOfJobFilenames;
	/**
	 * JSON Inhalt eines TSP Jobs
	 */
	private JSONObject tspJSON;
	/**
	 * Knoten eines TSP Jobs für den Graphen
	 */
	private ArrayList<JSONArray> jobNodeList;
	
	/**
	 * Konstruktor zum Anlegen eines TSPJobLoader anhand der Pfadangabe zum Ordner mit den jeweiligen
	 * JSON Dateien.
	 * 
	 * @param path - Pfad zu den JSON Dateien mit den TSP Jobs
	 */
	public TSPJobLoader(String path) {
		jobPath = path;
	}

	/**
	 * Anzahl der TSP Jobs anhand des Ordnerinhalts festlegen
	 */
	public void setAmountJobsFromFile() {
		folderJobs = new File("./" + jobPath);
		if(folderJobs.exists()) {
			amountJobs = folderJobs.listFiles().length-1;
		} else {
			System.err.println("Pfad für Jobs existiert nicht!");
		}
	}
	
	/**
	 * Dateinamen der TSP Jobs anhand des Ordnerinhalts speichern
	 */
	public void loadJobFileNames() {
		File[] listOfFiles = folderJobs.listFiles();
		listOfJobFilenames = new ArrayList<String>();
		
	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	    	  if(!listOfFiles[i].getName().equals("default.json"))
	    		  listOfJobFilenames.add(listOfFiles[i].getName());
	      }
	    }
	}
	
	/**
	 * Das JOSN der TSP Jobs parsen, um im weiteren Verlauf auf die Parameter des Jobs zugreifen
	 * zu können.
	 * 
	 * @param filename - Dateiname eines TSP Jobs
	 */
	public void parseJSONFromJob(String filename) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("./"+jobPath+"/"+filename));
			tspJSON = (JSONObject) obj;
		} catch (Exception e) {
			System.err.println("parseJSONFromJob Fehler: " + e.getMessage());
		}
	}
	
	/**
	 * Anzahl der Knoten des aktuellen TSP Jobs zurückgegeben
	 * 
	 * @return int - Anzahl der Knoten des TSP Jobs
	 */
	public int getAmountNodesFromFile() {
		this.amountNodes = Integer.parseInt(tspJSON.get("Dimension").toString());
		return this.amountNodes;
	}
	
	/**
	 * Name des aktuellen TSP Jobs zurückgegeben
	 * 
	 * @return String - Name des TSP Jobs
	 */
	public String getTourNameFromFile() {
		return tspJSON.get("Name").toString();
	}
	
	/**
	 * Gibt die Anzahl der Knoten des aktuellen TSP Jobs zurück
	 * 
	 * @return int - Anzahl der Knoten des TSP Jobs
	 */
	public int getAmountNodes() {
		return this.amountNodes;
	}
	
	/**
	 * Anzahl de Knoten des aktuellen TSP Jobs setzen
	 * 
	 * @param amountNodes - Anzahl der Knoten
	 */
	public void setAmountNodes(int amountNodes) {
		this.amountNodes = amountNodes;
	}
	
	/**
	 * Knoten mit Koordinaten des aktuellen TSP Jobs zurückgeben
	 * 
	 * @return ArrayList - Knoten mit Koordinanten des TSP Jobs
	 */
	public ArrayList<JSONArray> getNodes() {
		jobNodeList = new ArrayList<JSONArray>();
		JSONArray msg = (JSONArray) tspJSON.get("Nodes");
        Iterator<JSONObject> iterator = msg.iterator();
        while (iterator.hasNext()) {
        	JSONArray nodeElement = (JSONArray)(iterator.next().get("coords"));
        	jobNodeList.add(nodeElement);
        }
        
        return jobNodeList;
	}
	
	/**
	 * Index des aktuellen TSP Jobs zurückgeben
	 * 
	 * @return String - Index des TSP Jobs
	 */
	public String getJobIndex() {
		return tspJSON.get("Index").toString();
	}
	
	/**
	 * Dateinamen aller TSP Jobs zurückgeben.
	 * 
	 * @return ArrayList - Dateinamen aller TSP Jobs
	 */
	public ArrayList<String> getJobFileNames() {
		return listOfJobFilenames;
	}
	
	/**
	 * Anzahl aller TSP Jobs zurückgeben
	 * 
	 * @return int - Anzahl der TSP Jobs
	 */
	public int getAmountJobs() {
		return amountJobs;
	}

	/**
	 * Anzahl der TSP Jobs setzen
	 * 
	 * @param amountJobs - Anzahl der TSP Jobs
	 */
	public void setAmountJobs(int amountJobs) {
		this.amountJobs = amountJobs;
	}
}
