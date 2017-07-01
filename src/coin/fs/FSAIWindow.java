package coin.fs;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import VASSAL.build.module.GameState;
import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.Deck;
import VASSAL.counters.Decorator;
import VASSAL.counters.DynamicProperty;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Stack;
import coin.*;

public class FSAIWindow extends AIWindow {
	private static final long serialVersionUID = 1L;
	
	protected void initGameComponents() {
		
	}
	
	protected PiecesGameState gatherGamePieces(GameState myGameState, ArrayList<ZoneIndex> zones) {
		PiecesGameState gameState = new PiecesGameState();
		gameState.zones = zones;
		int winterCount = 0;
		
		Collection<GamePiece> pieces = myGameState.getAllPieces();
		Iterator<GamePiece> pieceIterator = pieces.iterator();
		
		do {
			GamePiece piece = pieceIterator.next();

			String mapName = "?";
			if (piece.getMap() != null) {
				mapName = piece.getMap().getMapName();
			}
			
			String pieceName = piece.getName();
			Point position = piece.getPosition();
			
			if (!(piece instanceof Deck) && !(piece instanceof Stack) && (!pieceName.equals("?")) && (pieceName.length() > 0)) {
				
				// Joel has named "Roman Ally" -> "Germanic Ally" instead in the module, so we need to check for the name of the PNG image
				if (pieceName.indexOf("Germanic Ally") != -1) {
					Object inner = Decorator.getInnermost(piece);
					if (inner != null && inner instanceof BasicPiece) {
						BasicPiece basic = (BasicPiece) inner;
						if (basic.getType().indexOf(";;;Roman Ally.png") > -1) {
							// change pieceName from Germanic Ally to Roman Ally
							pieceName = "Roman Ally";
						}
					}
				}
				
				// set the population from property
				if (pieceName.indexOf(" Pop") == 1) { // 1 Pop, 2 Pop, 3 Pop
					// population token, check for control
					int control = Integer.parseInt(((DynamicProperty)piece).getProperty("ControlValue").toString());
					switch (control) {
					case 2:
						pieceName += " (AeduiControl)";
						break;
					case 3:
						pieceName += " (ArverniControl)";
						break;
					case 4:
						pieceName += " (BelgicControl)";
						break;
					case 5:
						pieceName += " (GermanControl)";
						break;
					case 6:
						pieceName += " (RomanControl)";
						break;
					case 1:
						pieceName += " (NoControl)";
						break;
					}
				}
				
				// check for Dispersed (TribeState)
				try {
					int tribeState = Integer.parseInt(piece.getProperty("TribeState").toString());
					switch (tribeState) {
					case 3:
						pieceName += " (Dispersed)";
						break;
					case 2:
						pieceName += " (Gathering)";
						break;
					}
				}
				catch (Exception ex2) {}
				
				// check for Available Forces DummyState
				try {
					int tribeState = Integer.parseInt(piece.getProperty("DummyState").toString());
					switch (tribeState) {
					case 2:
						pieceName += " (Empty)";
						break;
					case 3:
						pieceName += " (Ally)";
						break;
					case 1:
						pieceName += " (Occupied)";
						break;
					}
				}
				catch (Exception ex3) {}
				
				boolean foundZone = false;
				
				for (int i = 0; i < zones.size(); i++) {
					ZoneIndex zi = zones.get(i);
					Zone z = zi.zone;
					
					Point piecePosition = piece.getPosition();
					piecePosition.translate(-1 * zi.offsetX, -1 * zi.offsetY);
					
					if (z.contains(piecePosition) && zi.map.equals(mapName)) {
						zi.pieces.add(new ZonePiece(pieceName, position));
						foundZone = true;
						break;
					}
				}
				
				if (!foundZone && mapName.equals("Main Map")) {
					gameState.offboard.add(new ZonePiece(pieceName, position));
				}
			} else if (piece instanceof Deck) {
				Deck deck = (Deck) piece;
				String deckName = deck.getDeckName();
				if (deckName.equals("Deck")) {
					for (int d = 0; d < deck.getPieceCount(); d++) {
						String cardObject = deck.getPieceAt(d).toString();
						if (cardObject.indexOf(" - Winter.jpg;7") > -1) winterCount++;
					}
				}
			}
		} while (pieceIterator.hasNext());
		
		gameState.attributes.put("wintercount", String.valueOf(winterCount));
		
		return gameState;
	}

	public String getModuleTitle() {
		return "Falling Sky";
	}

	protected void UpdateUIState() {
		
	}
}
