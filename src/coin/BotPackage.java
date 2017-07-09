package coin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class BotPackage {
	public String name = "";
	public String description = "";
	public String version = "";
	public String mainEntry = "";
	public String basePath = "";
	public int[] windowSize = new int[] { 300, 150 };
	public String nodeProcess = "";
	public boolean verboseOutput = false;
	public String[] supportedVersions = null;
	
	public String[] Actions = new String[0];
	public Faction[] Factions = new Faction[0];
	public String packageFile = "";
	
	public BotPackage(JSONObject packageJson) {
		if (packageJson.containsKey("name"))
			name = packageJson.get("name").toString();
		if (packageJson.containsKey("description"))
			description = packageJson.get("description").toString();
		if (packageJson.containsKey("version"))
			version = packageJson.get("version").toString();
		if (packageJson.containsKey("main"))
			mainEntry = packageJson.get("main").toString();
		
		if (packageJson.containsKey("coinbot")) {
			JSONObject coinbot = (JSONObject) packageJson.get("coinbot");
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
			if (coinbot.containsKey("supportedversions")) {
				JSONArray versions = (JSONArray) coinbot.get("supportedversions");
				supportedVersions = new String[versions.size()];
				for (int i = 0; i < versions.size(); i++)
					supportedVersions[i] = versions.get(i).toString();
			}
			
			if (coinbot.containsKey("verbose"))
				verboseOutput = coinbot.get("verbose").toString().toLowerCase() == "true";
		}
		
		if (windowSize[0] < 60) windowSize[0] = 60;
		if (windowSize[0] < 30) windowSize[1] = 30;
	}
	
	public void UpdatePackage() {
		try {
			InputStreamReader packageStream = new InputStreamReader(new FileInputStream(packageFile));
			JSONParser parser = new JSONParser();
			JSONObject packageJson = (JSONObject) parser.parse(packageStream);
			packageStream.close();
			
			if (packageJson.containsKey("coinbot")) {
				JSONObject coinbot = (JSONObject) packageJson.get("coinbot");
				
				if (coinbot.containsKey("verbose"))
					verboseOutput = coinbot.get("verbose").toString().toLowerCase() == "true";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
