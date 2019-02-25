package mpj_test;

import java.io.Serializable;

public class MPJObject implements Serializable {
	private int number;
	
	public MPJObject(int number) {
		this.number = number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public int getNumber() {
		return number;
	}
}
