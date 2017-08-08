package coin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang.NullArgumentException;
import org.json.simple.JSONObject;

public class Question {
	public static final String QUESTION_YESNO = "yesno";
	public static final String QUESTION_SINGLECHOICE = "single";
	public static final String QUESTION_MULTIPLECHOICE = "multi";
	
	private String _askingFaction;
	private String _answeringFaction;
	private String _q;
	private String _questionType;
	private String _category;
	private String _question;
	private String _datafile;
	private String _reply;
	private String _options;
	private String _jsonString;
	
	public Question(JSONObject jsonObject, String gamedata) {
		if (jsonObject == null) throw new NullArgumentException("jsonObject");
		if (gamedata == null) gamedata = "";
		
		_askingFaction = (jsonObject.containsKey("requestingFactionId")) ? jsonObject.get("requestingFactionId").toString() : "";
		_answeringFaction = (jsonObject.containsKey("respondingFactionId")) ? jsonObject.get("respondingFactionId").toString() : "";
		_questionType = (jsonObject.containsKey("type")) ? jsonObject.get("type").toString() : "";
		_q = (jsonObject.containsKey("q")) ? jsonObject.get("q").toString() : "";
		_question = (jsonObject.containsKey("question")) ? jsonObject.get("question").toString() : "?";
		_category = (jsonObject.containsKey("category")) ? jsonObject.get("category").toString() : "";
		_datafile = (jsonObject.containsKey("datafile")) ? jsonObject.get("datafile").toString() : "";
		_options = (jsonObject.containsKey("options")) ? jsonObject.get("options").toString() : "";
		
		if (_datafile.length() == 0 && gamedata != null && gamedata.length() > 0)
			_datafile = gamedata;
		
		_jsonString = "";
		
		/*
		 * TODO: what is this manifest?
		 * InputStream is;
		try {
			is = new FileInputStream("manifest.mf");
			BufferedReader buf = new BufferedReader(new InputStreamReader(is));
			String line = buf.readLine(); StringBuilder sb = new StringBuilder();
			while (line != null) {
				sb.append(line).append("\n");
				line = buf.readLine();
			}
			_jsonString = sb.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
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
	
	public String askingFaction() {
		return _askingFaction;
	}
	
	public String answeringFaction() {
		return _answeringFaction;
	}
	
	public String q() {
		return _q;
	}
	
	public String question() {
		return _question;
	}
	
	public String category() {
		return _category;
	}
	
	public String datafile() {
		return _datafile;
	}
	
	public String toJSONReply() {
		JSONObject json = new JSONObject();
		json.put("q", q());
		json.put("requestingFactionId", _askingFaction);
		json.put("respondingFactionId", _answeringFaction);
		json.put("options", options());
		json.put("reply", reply());
		return json.toJSONString();
	}
	
	public String jsonData() {
		return _jsonString;
	}
}
