package appnogui;

import org.json.simple.JSONObject;

import mpi.*;

/**
 * Die Klasse TSPwithACOMPI dient zur Anwendung für verteilte Systeme. Als Basis dient das
 * Framework MPJ-Express (mpj-express.org). In diesem Szenario erhölt jeder Knoten des
 * Kommunikators MPI_COMM_WORLD eine individuelle TSP Problematik zur Optimierung zugewiesen.
 * 
 * Der Austausch erfolgt per MPI Scatter und Gather, sodass zunächst eine Liste von TSP
 * Optimierungen auf alle vorhandenen Knoten verteilt wird und zum Ende hin wieder gesammelt
 * wird.
 * 
 * @author WPF-VS - SS 2017
 *
 */
public class TSPwithACOMPIMultiTSP extends TSPwithACOMPI {
	/**
	 * globaler Kommunikator mit Unterteilung in "aktiven" und "passiven" Prozessen
	 */
	public static Intracomm tspComm;
	
	public TSPwithACOMPIMultiTSP(String args[]) {
		// initialisieren und beginnen von MPI
		MPI.Init(args);
		rank = MPI.COMM_WORLD.Rank();					// Rang/Nummer des jeweiligen Prozess im Kommunikator MPI_COMM_WORLD
		size = MPI.COMM_WORLD.Size();					// Gesamtanzahl aller Prozesse im Kommunikator MPI_COMM_WORLD
		processors = MPI.NUM_OF_PROCESSORS;				// Anzahl der verfügbaren CPU Kerne
		root = 0;										// Platzhaler für Wurzelprozess
		buffersize = 0;									// Buffer Größeangabe/Anzahl an TSP Optimierungen
		scattersize = 0;								// Größenangabe für Verteilung der Daten über MPI (Verhältnis Tasks zu Prozessen)
		int remainder = 0;								// Rest bei der Buffersize durch die Berechnung der scattersize
		// Objekte müssen das Interface Serializable implementiert haben
		TSPwithACO sendbuf[] = null;					// SendBuffer, wird zum Verteilen/Sammeln der Daten über MPI benötigt
		TSPwithACO recvbuf[] = null;					// ReceiveBuffer, wird zum Verteilen/Sammeln der Daten über MPI benötigt
		TSPJobLoader tspJobs[] = new TSPJobLoader[1];	// TSPJobLoader bestimmt die Buffergrößen und lädt die Parameter der TSP Jobs
		
		// wird das Programm mit nur einem Prozess gestartet, so erfolgt eine Rückgabe mit der Anzahl möglicher Prozesse
		if(size==1) {
			JSONObject obj = new JSONObject();
			obj.put("available_processors",processors);
			System.out.println(obj);
			// MPI beenden
			MPI.Finalize();
			System.exit(0);
		}
		
		// Prozess root überprüft welche Jobs vorhanden sind und legt die Buffergrößen fest
		if (rank==root) {
			tspJobs[0] = new TSPJobLoader("tspjobs");
			tspJobs[0].setAmountJobsFromFile();
		}
				
		// TSP Job Objekt mit neuen Parametern per Broadcast allen Prozessen bekannt machen
		MPI.COMM_WORLD.Bcast(tspJobs, 0, 1, MPI.OBJECT, root);
		// Buffergrößen allen Prozessen bekannt machen
		buffersize = tspJobs[0].getAmountJobs();
		// Kommunikator mit Unterteilung in "aktiven" und "passiven" Prozesse, damit der Fall Prozesse > Anzahl TSP Optimierungen abgedeckt ist
		tspComm = MPI.COMM_WORLD.Split((rank<buffersize)?1:MPI.UNDEFINED, rank);
		
		sendbuf = new TSPwithACO[buffersize];
		recvbuf = new TSPwithACO[buffersize];
		
		// scattersize berechnen
		try {
			if(buffersize % size == buffersize) {
				scattersize = 1;
				if(buffersize == 0)
					throw new Exception("Es muss mindestens ein Job vorhanden sein!");
			} else {
				scattersize = calcScattersize(buffersize, size);
				// ggf. besteht ein Rest, da mehr TSP Jobs als Prozesse vorhanden sind
				remainder = calcRemainder(buffersize, size);
			}
		} catch (Exception e) {
			if(rank == root)
				System.err.println(e.getMessage());
			// MPI beenden
			MPI.Finalize();
			System.exit(0);
		}

		// ReceiveBuffer von allen Prozessen != root entsprechend befüllen
		if(rank != root) {
			// der ReceiveBuffer muss VOR den MPI Operationen gemäß der scattersize "gefüllt" sein
			for(Integer i = 0;i < scattersize; i++) {
				recvbuf[i] = new TSPwithACO(0, i.toString(), true);
			}
		}
		
		// SendBuffer vom root Prozess mit TSP Jobs befüllen
		if (rank == root) {
			tspJobs[0].loadJobFileNames();
			Integer TSPCounter = 0;
			// TSP Jobs durchlaufen
			for(String JobFile:tspJobs[0].getJobFileNames()) {
				// JSON File mit TSP Job parsen, sodass JSON-Object mit allen Parametern bereitsteht
				tspJobs[0].parseJSONFromJob(JobFile);
				switch(GraphMode.valueOf(GRAPH_MODE)) {
					// Fall für bestehende TSP Jobs
					case tspjob:
						sendbuf[TSPCounter] = new TSPwithACO(tspJobs[0], true);
					break;
					// Fall das der Graph für die Ameisenwege jeweils zufällig generiert wird
					case random:
						sendbuf[TSPCounter] = new TSPwithACO(0, tspJobs[0].getJobIndex(), true);
					break;
				}
				sendbuf[TSPCounter].setProcessors(processors);
				sendbuf[TSPCounter].setSize(size);
				sendbuf[TSPCounter].setScattersize(scattersize);
				sendbuf[TSPCounter].setsJobFilename(JobFile);
				TSPCounter++;
			}
		}

		// es müssen nur die Prozesse im Kommunikator mit der "Farbe" 1 (aktive Prozesse) arbeiten
		if(rank < buffersize) {
			// einzelene TSP Optimierungen an alle Prozesse verteilen (root Prozess verteilt die Daten)
			tspComm.Scatter(sendbuf, 0, scattersize, MPI.OBJECT, recvbuf, 0, scattersize, MPI.OBJECT, root);
			
			// Verarbeitung der zugewiesenen TSP Optimierungen auf den jeweiligen Prozessen
			for(int i=0;i < scattersize;i++) {
				// Verarbeitung der Daten auf dem jeweiligen Prozess
				recvbuf[i].startACO();
			}
			
			// Prozess root bearbeitet die noch offenen Anteile
			if(rank==root) {
				// Rest der TSP Optimierungen mit einbeziehen, welche nicht verteilt werden konnten (ungerade Prozess Anzahl)
				if(remainder>0) {
					for(int i=(buffersize-remainder);i < buffersize;i++) {
						// Ameise durchläuft Graph und setzt dabei Markierungen anhand von Wahrscheinlichkeiten
						sendbuf[i].startACO();
					}
				}
			}
			
			// prüfen, ob Ausgabe der Ergebnisse asynchron erfolgen soll
			if(MultiTSPSynOpt.isAsyn(MULTI_TSP_SYN_OPT)) {
				if(recvbuf[0].getkuerzesteTourLaenge() > 0) {
					System.out.println(recvbuf[0].printStaedteTourVISJSON());
					// pausieren, da mehrere stdout nicht zu schnell aufeinander folgen dürfen
					try {
						Thread.sleep(recvbuf[0].getACO_REFRESH_DELAY());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(recvbuf[0].printTSPOptimizeResultJSON());
				} else {
					JSONObject objParams = new JSONObject();
					JSONObject optimizeResult = new JSONObject();
					objParams.put("tspjob_index", recvbuf[0].getIndex());
					objParams.put("mode", (recvbuf[0].getAmeisen()==null) ? Mode.ant.toString() : Mode.multitsp.toString());
					optimizeResult.put("aco_no_optimize_result", objParams);
					System.out.println(optimizeResult);
				}
			}
			
			// optimierte TSP Probleme aller Prozesse sammeln (root Prozess sammelt alle Daten)
			tspComm.Gather(recvbuf, 0, scattersize, MPI.OBJECT, sendbuf, 0, scattersize, MPI.OBJECT, root);
		}
		
		// synchronisiert alle Prozesse im Kommunikator
		tspComm.Barrier();
		
		// Ausgabe vom root Prozess aller fertigen TSP Optimierungen
		if(rank == root) {
			// prüfen, ob Ausgabe der Ergebnisse synchron erfolgen soll
			if(MultiTSPSynOpt.isSsyn(MULTI_TSP_SYN_OPT)) {
				// Ausgabe des Inhalts des SendBuffers, Ergebnisse der TSP Optimierungen
				for(int i = 0; i < buffersize; i++) {
					if(sendbuf[i].getkuerzesteTourLaenge() > 0) {
						System.out.println(sendbuf[i].printStaedteTourVISJSON());
						// pausieren, da mehrere stdout nicht zu schnell aufeinander folgen dürfen
						try {
							Thread.sleep(sendbuf[i].getACO_REFRESH_DELAY());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println(sendbuf[i].printTSPOptimizeResultJSON());
					} else {
						JSONObject objParams = new JSONObject();
						JSONObject optimizeResult = new JSONObject();
						objParams.put("tspjob_index", sendbuf[i].getIndex());
						objParams.put("mode", (sendbuf[i].getAmeisen()==null) ? Mode.ant.toString() : Mode.multitsp.toString());
						optimizeResult.put("aco_no_optimize_result", objParams);
						System.out.println(optimizeResult);
						//System.err.println("TSP Job " + sendbuf[i].getIndex() + " erbrachte noch keine Optimierung!");
					}
				}
			}
			// Ausgabe der Reste unabhängig von aysn oder syn Option
			if(remainder>0) {
				for(int i=(buffersize-remainder);i < buffersize;i++) {
					if(sendbuf[i].getkuerzesteTourLaenge() > 0) {
						System.out.println(sendbuf[i].printStaedteTourVISJSON());
						// pausieren, da mehrere stdout nicht zu schnell aufeinander folgen dürfen
						try {
							Thread.sleep(sendbuf[i].getACO_REFRESH_DELAY());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println(sendbuf[i].printTSPOptimizeResultJSON());
					} else {
						JSONObject objParams = new JSONObject();
						JSONObject optimizeResult = new JSONObject();
						objParams.put("tspjob_index", sendbuf[i].getIndex());
						objParams.put("mode", (sendbuf[i].getAmeisen()==null) ? Mode.ant.toString() : Mode.multitsp.toString());
						optimizeResult.put("aco_no_optimize_result", objParams);
						System.out.println(optimizeResult);
						//System.err.println("TSP Job " + sendbuf[i].getIndex() + " erbrachte noch keine Optimierung!");
					}
				}
			}
		}
			
		// selbt angelegten Kommunikator löschen
		tspComm.Free();
		
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
			throw new Exception("Es muss mindestens ein Job vorhanden sein!");
		
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
			throw new Exception("Es muss mindestens ein Job vorhanden sein!");
		
		return buffersize % size;
	}
}
