package coin;

import java.util.ArrayList;

public class BotConfiguration {
	public String name;
	public String folder;
	public ArrayList<String> factions;
	public ArrayList<ArrayList<String>> actions;
	
	public BotConfiguration() {
		factions = new ArrayList<String>();
		actions = new ArrayList<ArrayList<String>>();
	}
}
