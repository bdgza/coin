package coin;

import org.json.simple.JSONObject;

public class Question {
	public static final String QUESTION_YESNO = "yesno";
	public static final String QUESTION_SINGLECHOICE = "single";
	public static final String QUESTION_MULTIPLECHOICE = "multi";
	
	private String _faction;
	private String _q;
	private String _questionType;
	private String _question;
	private String _datafile;
	private String _reply;
	private String _options;
	
	public Question(String faction, String questionType, String q, String question, String datafile, String options) {
		_faction = faction;
		_questionType = questionType;
		_q = q;
		_question = question;
		_datafile = datafile;
		_options = options;
	}
	
	public void setReply(String reply) {
		_reply = reply;
	}
	
	public String reply() {
		return _reply;
	}
	
	public String questionType() {
		return _questionType;
	}
	
	public String options() {
		return _options;
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
