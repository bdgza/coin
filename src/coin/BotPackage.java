package coin;

import org.json.simple.*;

public class BotPackage {
	public String Name = "";
	public String Description = "";
	public String Version = "";
	public String Main = "";
	public String BasePath = "";
	
	public String[] Actions = new String[0];
	public Faction[] Factions = new Faction[0];
	
	public BotPackage(JSONObject packageFile) {
		if (packageFile.containsKey("name"))
			Name = packageFile.get("name").toString();
		if (packageFile.containsKey("description"))
			Description = packageFile.get("description").toString();
		if (packageFile.containsKey("version"))
			Version = packageFile.get("version").toString();
		if (packageFile.containsKey("main"))
			Main = packageFile.get("main").toString();
		
		if (packageFile.containsKey("coinbot")) {
			JSONObject coinbot = (JSONObject) packageFile.get("coinbot");
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
	}
	
	public String GetBotType() {
		if (Main.substring(Main.length()-3).toLowerCase().equals(".js"))
			return "JS";
		
		if (Main.substring(Main.length()-3).toLowerCase().equals(".py"))
			return "PY";
		
		return null;
	}
	
	public String GetReadableLabel() {
		if (Description == null || Description.trim().length() <= 0) {
			if (Name == null || Name.trim().length() <= 0) {
				return "#invalid bot package#";
			}
			
			return Name;
		}
		
		return Description;
	}
}
