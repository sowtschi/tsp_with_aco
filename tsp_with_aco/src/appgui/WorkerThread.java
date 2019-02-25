package appgui;

public class WorkerThread implements Runnable {
	private Ameise ameise;
	
	public WorkerThread(Ameise ant) {
		this.ameise = ant;
	}
	
	@Override
	public void run() {
		//System.out.println(Thread.currentThread().getName()+ " läuft los");
		ameise.laufen();
		//System.out.println(Thread.currentThread().getName()+ " fertig gelaufen");
	}

}
