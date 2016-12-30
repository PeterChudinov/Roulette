package roulette;

public class ListenerControls {

	public static final int NOTHING = 0, NEXT = 1, DISCONNECT = 2;
	private int type;
	
	//constructor
	ListenerControls(int type) {
		this.type = type;
	}
	
	//getter
	public int getType() {
		return type;
	}
	
}
