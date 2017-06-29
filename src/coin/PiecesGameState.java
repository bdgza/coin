package coin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PiecesGameState {
	public ArrayList<ZoneIndex> zones;
	public ArrayList<ZonePiece> offboard;
	public Map<String, String> attributes;
	
	public PiecesGameState() {
		zones = new ArrayList<ZoneIndex>();
		offboard = new ArrayList<ZonePiece>();
		attributes = new HashMap<String, String>();
	}
	
	public PiecesGameState(ArrayList<ZoneIndex> zones) {
		this();
		
		this.zones = zones;
	}
}
