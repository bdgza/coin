package coin;

import org.json.simple.JSONObject;

public class Question {
	private String _faction;
	private String _q;
	private String _question;
	private String _datafile;
	private String _reply;
	
	public Question(String faction, String q, String question, String datafile) {
		_faction = faction;
		_q = q;
		_question = question;
		_datafile = datafile;
	}
	
	public void setReply(String reply) {
		_reply = reply;
	}
	
	public String reply() {
		return _reply;
	}
	
	public String faction() {
		return _faction;
	}
	
	public String q() {
		return _q;
	}
	
	public String question() {
		return _question;
	}
	
	public String datafile() {
		return _datafile;
	}
	
	public String toJSONReply() {
		JSONObject json = new JSONObject();
		json.put("q", q());
		json.put("faction", faction());
		json.put("reply", reply());
		return json.toJSONString();
	}
}
