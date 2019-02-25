package appnogui;

import org.json.simple.JSONObject;

import mpi.*;

/**
 * Die Klasse TSPwithACOMPIAnt dient zur Anwendung für verteilte Systeme. Als Basis dient das Framework
 * MPJ-Express (mpj-express.org). In diesem Szenario erhält jeder Knoten des Kommunikators MPI_COMM_WORLD
 * eine Menge von Ameisen, welche eine gemeinsame vorgegebene TSP Problematik optimieren.
 * 
 * Der Austausch erfolgt per MPI Bcast und Scatter, sodass zunächst die TSP Problematik allen Knoten bekannt
 * gemacht wird. Anschließend erfolgt dann per Scatter eine Verteilung der Liste von Ameisen auf alle vorhandenen
 * Knoten. Ein Gather ist nicht notwendig, da durch Bcast bereits alle Knoten auf der gleichen TSP Problematik
 * operieren. Es müssen lediglich die Amesien per Scatter verteilt werden.
 * 
 * @author WPF-VS - SS 2017
 *
 */
public class TSPwithACOMPIAnt extends TSPwithACOMPI {
	/**
	 * globaler Kommunikator mit Unterteilung in "aktiven" und "passiven" Prozessen
	 */
	public static Intracomm antComm;
	
	/**
	 * Kontruktor zum Anlegen eines TSPwithACO Szenarios mit der Verteilung von Ameisen.
	 * 
	 * @param args - MPI- und Start-Parameter
	 */
	public TSPwithACOMPIAnt(String args[]) {
		// initialisieren und beginnen von MPI
		MPI.Init(args);
		rank = MPI.COMM_WORLD.Rank();				// Rang/Nummer des jeweiligen Prozess im Kommunikator MPI_COMM_WORLD
		size = MPI.COMM_WORLD.Size();				// Gesamtanzahl aller Prozesse im Kommunikator MPI_COMM_WORLD
		processors = MPI.NUM_OF_PROCESSORS;			// Anzahl der verfügbaren CPU Kerne
		root = 0;									// Platzhaler für Wurzelprozess
		buffersize = 0;								// Buffer Größeangabe/Anzahl an TSP Optimierungen
		scattersize = 0;							// Größenangabe für Verteilung der Daten über MPI (Verhältnis Tasks zu Prozessen)
		int remainder = 0;							// Rest bei der Buffersize durch die Berechnung der scattersize
		int amountACOCounterGlobal = 0;				// globaler Zähler für die Anzahl der ACO Iterationen pro Prozess
		// Objekte müssen das Interface Serializable implementiert haben
		TSPwithACO tspaco[] = new TSPwithACO[1];	// die TSP Problematik, welche allen Knoten  per Bcast bekannt gemacht wird
		Ameise sendbuf[] = null;					// SendBuffer, wird zum Verteilen/Sammeln der Daten über MPI benötigt
		Ameise recvbuf[] = null;					// ReceiveBuffer, wird zum Verteilen/Sammeln der Daten über MPI benötigt
		
		// wird das Programm mit nur einem Prozess gestartet, so erfolgt eine Rückgabe mit der Anzahl möglicher Prozesse
		if(size==1) {
			JSONObject obj = new JSONObject();
			obj.put("available_processors",processors);
			System.out.println(obj);
			// MPI beenden
			MPI.Finalize();
			System.exit(0);
		}
		
		// Prozess root überprüft, ob ein Job vorhanden ist und legt die Buffergrößen fest
		if(rank == root) {
			switch(GraphMode.valueOf(GRAPH_MODE)) {
				case tspjob:
					TSPJobLoader tspJobs = new TSPJobLoader("tspjobs");
					tspJobs.parseJSONFromJob(ANT_TSPJOB);
					tspaco[0] = new TSPwithACO(tspJobs, false);
				break;
				case random:
					tspaco[0] = new TSPwithACO(0, null, false);
				break;
			}
		}
		
		// TSP Optimierung per Broadcast allen Prozessen bekannt machen
		MPI.COMM_WORLD.Bcast(tspaco, 0, 1, MPI.OBJECT, root);
		// Buffergrößen allen Prozessen bekannt machen
		buffersize = tspaco[0].getANZAHL_AMEISEN();
		// Kommunikator mit Unterteilung in "aktiven" und "passiven" Prozesse, damit der Fall Prozesse > Anzahl Ameisen abgedeckt ist
		antComm = MPI.COMM_WORLD.Split((rank<buffersize)?1:MPI.UNDEFINED, rank);
		
		sendbuf = new Ameise[buffersize];
		recvbuf = new Ameise[buffersize];
		
		// scattersize und Rest der buffersize berechnen
		try {
			if(buffersize % size == buffersize) {
				scattersize = 1;
				if(buffersize == 0)
					throw new Exception("Es muss mindestens eine Ameise vorhanden sein!");
			} else {
				scattersize = calcScattersize(buffersize, size);
				// ggf. besteht ein Rest, da mehr Ameisenanteile als Prozesse vorhanden sind
				remainder = calcRemainder(buffersize, size);
			}
		} catch (Exception e) {
			if(rank==root)
				System.err.println(e.getMessage());
			// MPI beenden
			MPI.Finalize();
			System.exit(0);
		}
				
		// ReceiveBuffer von allen Prozessen != root entsprechend befüllen
		if(rank != root) {
			// der ReceiveBuffer muss VOR den MPI Operationen gemäß der scattersize "gefüllt" sein
			for(int i = 0;i < scattersize;i++) {
				// damit eine "Zufallsvariable" vorhanden ist, müssen an dieser Stelle bereits Ameisenobjekte angelegt werden
				recvbuf[i] = new Ameise(tspaco[0].getWege(), tspaco[0]);	
			}
		}
				
		// Prozess root füllt SendBuffer mit Ameisen und startet TSP Optimierung
		if(rank == root) {
			for(int i = 0;i < buffersize;i++) {
				sendbuf[i] = new Ameise(tspaco[0].getWege(), tspaco[0]);
			}
			tspaco[0].setProcessors(processors);
			tspaco[0].setSize(size);
			tspaco[0].setScattersize(scattersize);
			tspaco[0].startACOMPIAnt(rank, root, buffersize, sendbuf, recvbuf, amountACOCounterGlobal, remainder);
		// es müssen nur die Prozesse im Kommunikator mit der "Farbe" 1 (aktive Prozesse) arbeiten
		} else if(rank < buffersize) {
			TSPwithACO compareTSPBuffer[] = new TSPwithACO[size];
			// einzelene Ameisen an alle Prozesse verteilen (root Prozess verteilt die Daten)
			antComm.Scatter(sendbuf, 0, scattersize, MPI.OBJECT, recvbuf, 0, scattersize, MPI.OBJECT, root);
			for(amountACOCounterGlobal=0;amountACOCounterGlobal<tspaco[0].getACO_ITERATIONS();amountACOCounterGlobal++) {
				// vorhandene Ameisen werden der Reihe nach aufgerufen
				for(int i=0;i < scattersize;i++) {
					// Ameise durchläuft Graph und setzt dabei Markierungen anhand von Wahrscheinlichkeiten
					recvbuf[i].laufen();
				}
				
				// Daten aller Prozesse sammeln (root Prozess sammelt alle Daten)
				antComm.Gather(tspaco, 0, 1, MPI.OBJECT, compareTSPBuffer, 0, 1, MPI.OBJECT, root);
				
				// vom root Prozess den aktuell besten Stand erhalten
				antComm.Bcast(tspaco, 0, 1, MPI.OBJECT, root);
				
				// synchronisiert alle Prozesse im Kommunikator
				antComm.Barrier();
				
				// prüfen, ob 100 Schritte vollzogen wurden und aktuelle Entfernung ausgeben
				if (amountACOCounterGlobal % tspaco[0].getACO_REFRESH_RATE() == 0) {
					// DEBUG Ausgabe bei jeder x Iteration
					//System.out.println("Prozess: " + rank + " " + amountACOCounterGlobal + " " + scattersize + recvbuf[0].getWege() + " " + tspaco[0].getIndex());
				}
			}
		}
		
		// synchronisiert alle Prozesse im Kommunikator
		antComm.Barrier();
		
		// Ausgabe vom root Prozess aller fertigen TSP Optimierungen
		if(rank == root) {
			if(tspaco[0].getkuerzesteTourLaenge() > 0) {
				System.out.println(tspaco[0].printStaedteTourVISJSON());
				System.out.println(tspaco[0].printTSPOptimizeResultJSON());
			} else {
				JSONObject objParams = new JSONObject();
				JSONObject optimizeResult = new JSONObject();
				objParams.put("tspjob_index", tspaco[0].getIndex());
				objParams.put("mode", (tspaco[0].getAmeisen()==null) ? Mode.ant.toString() : Mode.multitsp.toString());
				optimizeResult.put("aco_no_optimize_result", objParams);
				System.out.println(optimizeResult);
				//System.err.println("TSP Job " + tspaco[0].getIndex() + " erbrachte noch keine Optimierung!");
			}
		}
		
		// selbt angelegten Kommunikator löschen
		antComm.Free();
		
		// MPI beenden
		MPI.Finalize();
	}
	
	/**
	 * Methode zur Berechnung der Scattersize, Anteil der Verteilung für jeden Knoten.
	 * 
	 * @param buffersize - Gesamtgröße des Buffers
	 * @param size - Anzahl der Prozesse
	 * @return int - die Scattersize, Anteile für jenden Prozess der Verteilung
	 * @throws Exception - sofern Buffersize oder Size nicht korrekt angegeben wurde
	 */
	public static int calcScattersize(int buffersize, int size) throws Exception {
		if(size == 0)
			throw new Exception("Es muss mindestens ein Prozess vorhanden sein!");
		
		if(buffersize == 0)
			throw new Exception("Es muss mindestens eine Ameise vorhanden sein!");
		
		return buffersize/size;
	}
	
	/**
	 * Methode berechnet den Rest der Buffersize, welcher bspw. bei einer ungeraden
	 * Anzahl von Prozessen auftreten kann.
	 * 
	 * @param buffersize - Gesamtgröße des Buffers
	 * @param size - Anzahl der Prozesse
	 * @return int - Rest des Buffers, welcher von einem Prozess berechnet werden muss
	 * @throws Exception - sofern Buffersize oder Size nicht korrekt angegeben wurde
	 */
	public static int calcRemainder(int buffersize, int size) throws Exception {
		if(size == 0)
			throw new Exception("Es muss mindestens ein Prozess vorhanden sein!");
		
		if(buffersize == 0)
			throw new Exception("Es muss mindestens eine Ameise vorhanden sein!");
		
		return buffersize % size;
	}
}
