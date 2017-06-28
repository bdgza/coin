package coin;

import coin.adp.*;
import coin.fs.*;

public class AIWindowFactory {
	public static AIWindow getAIWindow(String gameName) {
		if (gameName == "Andean Abyss - Final") {
			return null; // TODO: Add game
		}
		if (gameName == "Cuba Libre") {
			return null; // TODO: Add game
		}
		if (gameName.equals("A Distant Plain")) {
			return new ADPAIWindow();
		}
		if (gameName == "Fire in the Lake") {
			return null; // TODO: Add game
		}
		if (gameName == "Liberty or Death (GMT)") {
			return null; // TODO: Add game
		}
		if (gameName == "Falling Sky") {
			return new FSAIWindow();
		}
		
		return null;
	}
}
