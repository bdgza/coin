package coin;

import org.json.simple.*;

public class BotPackage {
	public String name = "";
	public String description = "";
	public String version = "";
	public String mainEntry = "";
	public String basePath = "";
	public int[] windowSize = new int[] { 300, 150 };
	
	public String[] Actions = new String[0];
	public Faction[] Factions = new Faction[0];
	
	public BotPackage(JSONObject packageFile) {
		if (packageFile.containsKey("name"))
			name = packageFile.get("name").toString();
		if (packageFile.containsKey("description"))
			description = packageFile.get("description").toString();
		if (packageFile.containsKey("version"))
			version = packageFile.get("version").toString();
		if (packageFile.containsKey("main"))
			mainEntry = packageFile.get("main").toString();
		
		if (packageFile.containsKey("coinbot")) {
			JSONObject coinbot = (JSONObject) packageFile.get("coinbot");
			if (coinbot.containsKey("windowsize")) {
				JSONArray size = (JSONArray) coinbot.get("windowsize");
				for (int i = 0; i < 2; i++)
					try {
						windowSize[i] = Integer.parseInt(size.get(i).toString());
					}
					catch (NumberFormatException ex) {
						
					}
			}
			if (coinbot.containsKey("actions")) {
				JSONArray actions = (JSONArray) coinbot.get("actions");
				Actions = new String[actions.size()];
				for (int i = 0; i < actions.size(); i++)
					Actions[i] = actions.get(i).toString();
			}
			JSONArray factions = (JSONArray) coinbot.get("factions");
			Factions = new Faction[factions.size()];
			for (int i = 0; i < factions.size(); i++) {
				Factions[i] = new Faction((JSONObject) factions.get(i));
			}
		}
		
		if (windowSize[0] < 60) windowSize[0] = 60;
		if (windowSize[0] < 30) windowSize[1] = 30;
	}
	
	public String GetBotType() {
		if (mainEntry.substring(mainEntry.length()-3).toLowerCase().equals(".js"))
			return "JS";
		
		if (mainEntry.substring(mainEntry.length()-3).toLowerCase().equals(".py"))
			return "PY";
		
		return null;
	}
	
	public String GetReadableLabel() {
		if (description == null || description.trim().length() <= 0) {
			if (name == null || name.trim().length() <= 0) {
				return "#invalid bot package#";
			}
			
			return name;
		}
		
		return description;
	}
}
