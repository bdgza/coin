package coin;

import coin.adp.*;
import coin.fs.*;

public class AIWindowFactory {
	public static AIWindow getAIWindow(String gameName) {
		if (gameName.equals("Andean Abyss - Final")) {
			return null; // TODO: Add game
		}
		if (gameName.equals("Cuba Libre")) {
			return null; // TODO: Add game
		}
		if (gameName.equals("A Distant Plain")) {
			return new ADPAIWindow();
		}
		if (gameName.equals("Fire in the Lake")) {
			return null; // TODO: Add game
		}
		if (gameName.equals("Liberty or Death (GMT)")) {
			return null; // TODO: Add game
		}
		if (gameName.equals("Falling Sky")) {
			return new FSAIWindow();
		}
		
		return null;
	}
}
