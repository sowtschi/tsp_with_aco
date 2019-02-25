package mpj_test;

import mpi.*;

public class ScatterGatherTest {
	public static void main(String args[]) {
		// initialisieren und beginnen von MPI
		MPI.Init(args);
		int rank = MPI.COMM_WORLD.Rank();			// Rang/Nummer des jeweiligen Prozess im Kommunikator MPI_COMM_WORLD
		int size = MPI.COMM_WORLD.Size();			// Gesamtanzahl aller Prozesse im Kommunikator MPI_COMM_WORLD
		int processors = MPI.NUM_OF_PROCESSORS;		// Anzahl der verfügbaren CPU Kerne
		int root = 0;								// Platzhaler für Wurzelprozess
		int buffersize = 6;							// Buffer Größeangabe
		int scattersize = buffersize/size;
		int sendbuf[] = new int[buffersize];		// SendBuffer, wird zum Verteilen/Sammeln der Daten über MPI benötigt
		int recvbuf[] = new int[buffersize];		// ReceiveBuffer, wird zum Verteilen/Sammeln der Daten über MPI benötigt
		
		// Operationen für Wurzelknoten
		if (rank == root) {
			System.out.println("Es stehen " + size + " Prozesse und " + processors + " CPU Kerne zur Verfügung!");
			// SendBuffer befüllen
			for(int i = 0; i < buffersize; i++) {
				sendbuf[i] = i;
			}
			System.out.println("Prozess  " + rank + " hat die Daten: ");
			// Ausgabe des Inhalts des SendBuffers
			for(int i = 0; i < buffersize; i++) {
				System.out.println(sendbuf[i]);
			}
		} else {
			System.out.println("Prozess  " + rank + " hat die Daten: ");
			// Ausgabe des Inhalts des SendBuffers
			for(int i = 0; i < buffersize; i++) {
				System.out.println(sendbuf[i]);
			}
		}
		
		// Daten an alle Prozesse verteilen (root Prozess verteilt die Daten)
		MPI.COMM_WORLD.Scatter(sendbuf, 0, scattersize, MPI.INT, recvbuf, 0, scattersize, MPI.INT, root);
		
		// Ausgabe welcher Prozess, welche Daten erhalten hat und Verarbeitung
		for(int i=0;i<buffersize;i++) {
			System.out.println("Prozess " + rank + " hat die Daten " + recvbuf[i]);
			// Verarbeitung der Daten auf dem jeweiligen Prozess
			recvbuf[i] *= 2;
		}
		
		// Ausgabe der Daten nach der Verarbeitung auf dem jeweiligen Prozess
		for(int i=0;i<buffersize;i++) {
			System.out.println("Prozess " + rank + " verdoppelt, und hat nun " + recvbuf[i]);
		}
		
		// Daten aller Prozesse sammeln (root Prozess sammelt alle Daten)
		MPI.COMM_WORLD.Gather(recvbuf, 0, scattersize, MPI.INT, sendbuf, 0, scattersize, MPI.INT, root);
		if (rank == root) {
	        System.out.println("Prozess " + rank + " hat nach dem Sammeln die Daten:");
	        for (int i=0; i<buffersize; i++) {
	            System.out.println(sendbuf[i]);
	        }
	    }
		
		// synchronisiert alle Prozesse im Kommunikator MPI_COMM_WORLD
		MPI.COMM_WORLD.Barrier();
		
		System.out.println("Am Ende hat Prozess  " + rank + " die Daten: ");
		// Ausgabe des Inhalts des SendBuffers
		for(int i = 0; i < buffersize; i++) {
			System.out.println(sendbuf[i]);
		}
		
		// MPI beenden
		MPI.Finalize();			
	}
}
