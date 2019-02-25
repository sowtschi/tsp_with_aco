package mpj_test;

import mpi.*;

public class CommunicatorTest {
	public static void main(String args[]) throws Exception {
		MPI.Init(args);
		int me = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		System.out.println("Hi from <"+me+">");
		
		Intracomm antComm = MPI.COMM_WORLD.Split((me%2==0)?1:MPI.UNDEFINED, me);
		
		if(me==0) {
			antComm.Barrier();
		} else if (me%2==0) {
			antComm.Barrier();
		} else {
			System.out.println("Prozess " + me + " blockiert nicht, da anderer Kommunikator!");
		}
		
		antComm.Barrier();
		
		MPI.Finalize();
	}
}
