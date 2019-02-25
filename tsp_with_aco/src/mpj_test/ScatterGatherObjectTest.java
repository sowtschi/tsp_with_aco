package mpj_test;

import mpi.*;

public class ScatterGatherObjectTest {
	public static void main(String args[]) {
		// initialisieren und beginnen von MPI
		MPI.Init(args);
		int rank = MPI.COMM_WORLD.Rank();						// Rang/Nummer des jeweiligen Prozess im Kommunikator MPI_COMM_WORLD
		int size = MPI.COMM_WORLD.Size();						// Gesamtanzahl aller Prozesse im Kommunikator MPI_COMM_WORLD
		int processors = MPI.NUM_OF_PROCESSORS;					// Anzahl der verfügbaren CPU Kerne
		int root = 0;											// Platzhaler für Wurzelprozess
		int buffersize = 6;										// Buffer Größeangabe
		int scattersize = buffersize/size;
		// Objekte müssen das Interface Serializable implementiert haben
		MPJObject sendbuf[] = new MPJObject[buffersize];		// SendBuffer, wird zum Verteilen/Sammeln der Daten über MPI benötigt
		MPJObject recvbuf[] = new MPJObject[buffersize];		// ReceiveBuffer, wird zum Verteilen/Sammeln der Daten über MPI benötigt
		
		// beide Buffer (Arrays) müssen VOR MPI Operationen "gefüllt" sein
		for(int i = 0;i < buffersize; i++) {
			sendbuf[i] = new MPJObject(0);
			recvbuf[i] = new MPJObject(0);
		}
		
		// Operationen für Wurzelknoten
		if (rank == root) {
			System.out.println("Es stehen " + size + " Prozesse und " + processors + " CPU Kerne zur Verfügung!");
			// SendBuffer befüllen
			for(int i = 0; i < buffersize; i++) {
				sendbuf[i] = new MPJObject(i);
			}
			System.out.println("Prozess  " + rank + " hat die Daten: ");
			// Ausgabe des Inhalts des SendBuffers
			for(int i = 0; i < buffersize; i++) {
				System.out.println(sendbuf[i].getNumber());
			}
		} else {
			System.out.println("Prozess  " + rank + " hat die Daten: ");
			// Ausgabe des Inhalts des SendBuffers
			for(int i = 0; i < buffersize; i++) {
				System.out.println(sendbuf[i].getNumber());
			}
		}
		
		// Daten an alle Prozesse verteilen (root Prozess verteilt die Daten)
		MPI.COMM_WORLD.Scatter(sendbuf, 0, scattersize, MPI.OBJECT, recvbuf, 0, scattersize, MPI.OBJECT, root);
		
		// Ausgabe welcher Prozess, welche Daten erhalten hat und Verarbeitung
		for(int i=0;i<buffersize;i++) {
			System.out.println("Prozess " + rank + " hat die Daten " + recvbuf[i] + " Verarbeitung...");
			// Verarbeitung der Daten auf dem jeweiligen Prozess
			recvbuf[i].setNumber(recvbuf[i].getNumber()*2);
		}
		
		// Ausgabe der Daten nach der Verarbeitung auf dem jeweiligen Prozess
		for(int i=0;i<buffersize;i++) {
			System.out.println("Prozess " + rank + " verdoppelt, und hat nun " + recvbuf[i].getNumber());
		}
		
		// Daten aller Prozesse sammeln (root Prozess sammelt alle Daten)
		MPI.COMM_WORLD.Gather(recvbuf, 0, scattersize, MPI.OBJECT, sendbuf, 0, scattersize, MPI.OBJECT, root);
		if (rank == root) {
	        System.out.println("Prozess " + rank + " hat nach dem Sammeln die Daten:");
	        for (int i=0; i<buffersize; i++) {
	            System.out.println(sendbuf[i].getNumber());
	        }
	    }
		
		// synchronisiert alle Prozesse im Kommunikator MPI_COMM_WORLD
		MPI.COMM_WORLD.Barrier();
		
		System.out.println("Am Ende hat Prozess  " + rank + " die Daten: ");
		// Ausgabe des Inhalts des SendBuffers
		for(int i = 0; i < buffersize; i++) {
			System.out.println(sendbuf[i].getNumber());
		}
		
		// MPI beenden
		MPI.Finalize();			
	}
}
