package coin;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.script.*;
import org.apache.commons.io.IOUtils;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.tools.FormattedString;
import org.json.simple.*;
import org.json.simple.parser.*;

public class BotScriptEngine {
	private static GameModule _mod;
	private static String _coinTitle;
	private List<BotScriptListener> _listeners;
	
	private BotPackage _botPackage;
		
	public BotScriptEngine(GameModule module, BotPackage botPackage, String coinTitle) {
		_mod = module;
		_coinTitle = coinTitle;
		_botPackage = botPackage;
		_listeners = new ArrayList<BotScriptListener>();
		final BotScriptEngine self = this;
		
		// respond to question triggers
		
		this.addBotScriptListener(new BotScriptListener() {
			
			public void botScriptEvent(BotScriptEvent event) {
				if (event.eventType() == BotScriptEvent.EVENTTYPE_QUESTION) {
					Question question = (Question) event.eventData();
					WriteLine("# Please answer the question in the dialog: " + question.question());
					
					if (question.questionType().equals(Question.QUESTION_YESNO)) {
						YesNoDialog dlg = new YesNoDialog(_mod.getFrame(), _coinTitle, question);
						final YesNoDialog myDlg = dlg;
						final Question myQuestion = question;
						dlg.addWindowListener(new WindowAdapter() {
							public void windowDeactivated(WindowEvent e) {
								if (myDlg.reply.length() > 0) {
									myQuestion.setReply(myDlg.reply);
									self.RunScript(myQuestion);
								}
							}
						});
						dlg.setVisible(true);
					} else if (question.questionType().equals(Question.QUESTION_SINGLECHOICE)) {
						ListDialog dlg = new ListDialog(_mod.getFrame(), _coinTitle, question);
						final ListDialog myDlg = dlg;
						final Question myQuestion = question;
						dlg.addWindowListener(new WindowAdapter() {
							public void windowDeactivated(WindowEvent e) {
								if (myDlg.reply.length() > 0) {
									myQuestion.setReply(myDlg.reply);
									self.RunScript(myQuestion);
								}
							}
						});
						dlg.setVisible(true);
					} else if (question.questionType().equals(Question.QUESTION_MULTIPLECHOICE)) {
						MultipleDialog dlg = new MultipleDialog(_mod.getFrame(), _coinTitle, question);
						final MultipleDialog myDlg = dlg;
						final Question myQuestion = question;
						dlg.addWindowListener(new WindowAdapter() {
							public void windowDeactivated(WindowEvent e) {
								if (myDlg.reply.length() > 0) {
									myQuestion.setReply(myDlg.reply);
									self.RunScript(myQuestion);
								}
							}
						});
						dlg.setVisible(true);
					}
				}
			}
		});
	}
	
	public synchronized void addBotScriptListener(BotScriptListener l) {
		_listeners.add(l);
	}
	
	public synchronized void removeBotScriptListener(BotScriptListener l) {
		_listeners.remove(l);
	}
	
	private synchronized void _fireBotScriptEvent(BotScriptEvent event) {
		Iterator<BotScriptListener> listeners = _listeners.iterator();
		while (listeners.hasNext())
			(listeners.next()).botScriptEvent(event);
	}
	
	public String ConstructJSON(StringBuilder json, ArrayList<ZonePiece> offboard, ArrayList<ZoneIndex> zones) {
		json.append("\"offboard\": [");
		
		for (int j = 0; j < offboard.size(); j++) {
			ZonePiece piece = offboard.get(j);

			if (j > 0)
				json.append(",");
			json.append("{\"name\": \"" + piece.name + "\", \"x\": " + piece.location.x
					+ ", \"y\": " + piece.location.y + "}");
		}

		json.append("],");
		json.append("\"zones\": [");

		for (int i = 0; i < zones.size(); i++) {
			ZoneIndex zi = zones.get(i);
			
			if (i > 0)
				json.append(",");
			json.append("{\"name\": \"" + zi.name + "\", \"map\": \"" + zi.map + "\", \"x\": " + zi.offsetX + ", \"y\": " + zi.offsetY + ", \"pieces\": [");

			for (int j = 0; j < zi.pieces.size(); j++) {
				ZonePiece piece = zi.pieces.get(j);
				
				if (j > 0)
					json.append(",");
				json.append("{\"name\": \"" + piece.name + "\", \"x\": " + piece.location.x
						+ ", \"y\": " + piece.location.y + "}");
			}

			json.append("]}");
		}

		json.append("]}");
		
		return json.toString();
	}
	
	private static void WriteLine(String msgLine) {
		WriteLine(msgLine, false);
	}
	private static void WriteLine(String msgLine, boolean raw) {
		FormattedString cStr = new FormattedString((raw ? "" : " - <COINBot> - ") + msgLine);
		final Command cc = new Chatter.DisplayText(_mod.getChatter(), cStr.getLocalizedText());
		cc.execute();
		_mod.sendAndLog(cc);
	}
	
	public void RunScript(String jsonString, String action) {
		RunJS(jsonString, action, null);
	}
	
	public void RunScript(Question reply) {
		if (reply == null) {
			WriteLine(" - ERROR: there is no valid reply to continue the bot script execution; Abort");
			return;
		}
		
		String jsonData = reply.jsonData();
		if (jsonData == null || jsonData.trim().length() <= 0) {
			WriteLine(" - ERROR: did not get or unable to read valid gamestate from bot script; Abort");
			return;
		}
		
		RunJS(reply.jsonData(), reply.faction(), reply);
	}
	
	private void RunJS(String jsonString, String action, Question reply) {
		// refresh certain package settings, such as verbosity
		
		_botPackage.UpdatePackage();
		
		// write temp JSON file with gamestate
		
		File temp;
		try {
			temp = File.createTempFile("coinbot-", ".json");
			FileWriter fw = new FileWriter(temp);
			fw.write(jsonString);
			fw.close();
		} catch (IOException ex) {
			WriteLine(" - ERROR trying to write JSON temp file in RunJS(); Abort");
			return;
		}
		
		// run the bot script JS
		
		final ProcessWithTimeout pwt;
		final Process p;
		try {
			if (_botPackage.verboseOutput) WriteLine("GameState: " + temp.getAbsolutePath(), true);
			
			p = Runtime.getRuntime().exec(new String[] { _botPackage.nodeProcess, _botPackage.mainEntry, "\"" + temp.getAbsolutePath() + "\"", reply == null ? "" : reply.toJSONReply() }, new String[] {}, new File(_botPackage.basePath));
			pwt = new ProcessWithTimeout(p);

			InputStream stdout = p.getInputStream();
			InputStreamReader in = new InputStreamReader(stdout);
			BufferedReader reader = new BufferedReader(in);

			InputStream stderr = p.getErrorStream();
			InputStreamReader errin = new InputStreamReader(stderr);
			BufferedReader errreader = new BufferedReader(errin);

			int exitVal = pwt.waitForProcess(6000);

			p.getOutputStream().close();

			if (in.ready()) {
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						processMessageLine(line, null);
					}
				} catch (Exception ex) {
					WriteLine("Exception trying to read script output");
				}
			}

			if (errin.ready()) {
				String line = null;
				try {
					while ((line = errreader.readLine()) != null) {
						WriteLine(line);
					}
				} catch (Exception ex) {
					WriteLine("Exception trying to read script errors");
				}
			}

			if (exitVal != 0) {
				if (exitVal == Integer.MIN_VALUE) {
					WriteLine("AI Script Failed due to a Timeout");
					p.destroy();
				} else {
					WriteLine("AI Script Terminated Abnormally (" + exitVal + ")");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			WriteLine("RunJS() EXCEPTION: " + e.getClass().getSimpleName() + " -- " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			WriteLine(sw.toString());
		} finally {
			
		}
	}
	
	private void processMessageLine(String line, ScriptEngine engine) {
		if (line.startsWith("{")) {
			// this is probably a question
			try {
				JSONParser parser = new JSONParser();
				JSONObject jsonObject = (JSONObject) parser.parse(line);
				String faction = jsonObject.get("faction").toString();
				String questionType = jsonObject.get("type").toString();
				String q = jsonObject.get("q").toString();
				String question = (jsonObject.containsKey("question")) ? jsonObject.get("question").toString() : "?";
				String datafile = (jsonObject.containsKey("datafile")) ? jsonObject.get("datafile").toString() : "";
				String options = (jsonObject.containsKey("options")) ? jsonObject.get("options").toString() : "";
				String gamedata = engine != null ? engine.get("gamedata").toString() : "";
				if (datafile.length() == 0 && gamedata.length() > 0)
					datafile = gamedata;
				
				_fireBotScriptEvent(new BotScriptEvent(this, BotScriptEvent.EVENTTYPE_QUESTION, new Question(faction, questionType, q, question, datafile, options)));
			}
			catch (Exception ex)
			{
				WriteLine("JSON: " + ex.toString());
				WriteLine(line);
			}
		} else if (line.startsWith("M*")) {
			WriteLine(line.substring(2));
		} else if (_botPackage.verboseOutput) {
			WriteLine(line, true);
		}
	}
}
