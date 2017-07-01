package coin;

import org.json.simple.*;

public class Faction {
	public String Id;
	public String Name;
	public String[] Actions;
	public Boolean NonPlayerSelected = false;
	public Boolean NonPlayerFixed = false;
	
	public Faction() {
		
	}
	
	public Faction(JSONObject factionObject) {
		if (factionObject.containsKey("id"))
			Id = factionObject.get("id").toString();
		if (factionObject.containsKey("name"))
			Name = factionObject.get("name").toString();
		if (factionObject.containsKey("actions")) {
			JSONArray actions = (JSONArray) factionObject.get("actions");
			Actions = new String[actions.size()];
			for (int i = 0; i < actions.size(); i++)
				Actions[i] = actions.get(i).toString();
		}
		if (factionObject.containsKey("np")) {
			NonPlayerFixed = Boolean.parseBoolean(factionObject.get("np").toString());
			NonPlayerSelected = NonPlayerFixed;
		}
	}
}
