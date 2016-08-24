package coin;

import java.awt.Shape;
import java.util.ArrayList;

import org.apache.commons.io.input.NullReader;

import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;

public class ZoneIndex {
	public String name;
	public ArrayList<ZonePiece> pieces;
	public Zone zone;
	public String map;
	public int offsetX;
	public int offsetY;
	
	public ZoneIndex(String name, Zone zone) {
		this.name = name;
		this.zone = zone;
		pieces = new ArrayList<ZonePiece>();
		this.map = "";
		if (zone.getMap() != null)
			this.map = zone.getMap().getMapName();
		this.offsetX = zone.getBoard().bounds().x;
		this.offsetY = zone.getBoard().bounds().y;
	}
}
