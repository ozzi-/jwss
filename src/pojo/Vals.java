package pojo;

import java.util.ArrayList;

public class Vals {

	private ArrayList<Val> vals;

	public Vals() {
		vals = new ArrayList<Val>();
	}
	
	public Vals addVal(int i) {
		vals.add(new Val(i));
		return this;
	}
	
	public Vals addVal(String s) {
		vals.add(new Val(s));
		return this;
	}
	
	public ArrayList<Val> getVals(){
		return vals;
	}
}
