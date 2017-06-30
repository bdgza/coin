package coin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
	private String _jsonString;
	
	public Question(String faction, String questionType, String q, String question, String datafile, String options) {
		_faction = faction;
		_questionType = questionType;
		_q = q;
		_question = question;
		_datafile = datafile;
		_options = options;
		
		InputStream is;
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
		}
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
		json.put("options", options());
		json.put("reply", reply());
		return json.toJSONString();
	}
	
	public String jsonData() {
		return _jsonString;
	}
}
