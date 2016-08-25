package coin;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.script.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
	
	public BotScriptEngine(GameModule module, String coinTitle) {
		_mod = module;
		_coinTitle = coinTitle;
		_listeners = new ArrayList<BotScriptListener>();
		final BotScriptEngine self = this;
		
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
		
		int oc = 1;
		for (int j = 0; j < offboard.size(); j++) {
			ZonePiece piece = offboard.get(j);
//			WriteLine(" " + (oc++) + ". " +
//				piece.name + " ( " +
//				piece.location.x + ", " +
//				piece.location.y + " )");

			if (j > 0)
				json.append(",");
			json.append("{\"name\": \"" + piece.name + "\", \"x\": " + piece.location.x
					+ ", \"y\": " + piece.location.y + "}");
		}

		json.append("],");
		json.append("\"zones\": [");

		// WriteLine("****************************************");
		for (int i = 0; i < zones.size(); i++) {
			ZoneIndex zi = zones.get(i);
			
//			 WriteLine("*** ZONE: " + zi.name + " ( " +
//			 zi.zone.getBounds().getMinX() + ", " +
//			 zi.zone.getBounds().getMinY() + " ; " +
//			 zi.zone.getBounds().getMaxX() + ", " +
//			 zi.zone.getBounds().getMaxY() + " )");

			if (i > 0)
				json.append(",");
			json.append("{\"name\": \"" + zi.name + "\", \"map\": \"" + zi.map + "\", \"x\": " + zi.offsetX + ", \"y\": " + zi.offsetY + ", \"pieces\": [");

			for (int j = 0; j < zi.pieces.size(); j++) {
				ZonePiece piece = zi.pieces.get(j);
				
//				 WriteLine(" " + (zc++) + ". " +
//				 zi.pieces.get(j).name + " ( " +
//				 zi.pieces.get(j).location.x + ", " +
//				 zi.pieces.get(j).location.y + " )");

				if (j > 0)
					json.append(",");
				json.append("{\"name\": \"" + piece.name + "\", \"x\": " + piece.location.x
						+ ", \"y\": " + piece.location.y + "}");
			}

			json.append("]}");
		}
		// WriteLine("****************************************");

		json.append("]}");
		
		return json.toString();
	}
			
	private static void WriteLine(String msgLine) {
		FormattedString cStr = new FormattedString("-<AI> " + msgLine);
		final Command cc = new Chatter.DisplayText(_mod.getChatter(), cStr.getLocalizedText());
		cc.execute();
		_mod.sendAndLog(cc);
	}
	
	private boolean ContainsJS(String action) {
		boolean containsJS = false;
		try {
			containsJS = _mod.getDataArchive().getInputStream("ai-script.js") != null;
			containsJS = containsJS && _mod.getDataArchive().getInputStream(action.replaceAll("\\s","") + ".js") != null;
		} catch (IOException e1) {}
		return containsJS;
	}
	
	private boolean ContainsPY() {
		boolean containsPY = false;
		try {
			containsPY = _mod.getDataArchive().getInputStream("ai-script.py") != null;
		} catch (IOException e1) {}
		return containsPY;
	}
	
	public void RunScript(String jsonString, String action) {
		boolean containsJS = ContainsJS(action);
		if (containsJS) {
			// try JS
			RunJS(jsonString, action);
		}
		
		if (!containsJS && ContainsPY()) {
			// try Python
			RunPython(jsonString);
		}
	}
	
	public void RunScript(Question reply) {
		boolean containsJS = ContainsJS(reply.faction());
		if (containsJS) {
			// try JS
			RunJS(null, reply.faction(), reply);
		}
		
		if (!containsJS && ContainsPY()) {
			// try Python
			RunPython(null, reply);
		}
	}
	
	private void RunJS(String jsonString, String action) {
		RunJS(jsonString, action, null);
	}
	
	private void RunJS(String jsonString, String action, Question reply) {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");

		Reader readerJsonLib = null;
		Reader readerPolyfill = null;
		Reader readerAIScript = null;
		
		try {
			readerJsonLib = new InputStreamReader(_mod.getDataArchive().getInputStream("json2.js"));
			engine.eval(readerJsonLib);
			
			readerPolyfill = new InputStreamReader(_mod.getDataArchive().getInputStream("polyfill.js"));
			engine.eval(readerPolyfill);
			
			readerPolyfill = new InputStreamReader(_mod.getDataArchive().getInputStream("ai-tools.js"));
			engine.eval(readerPolyfill);
			
			readerPolyfill = new InputStreamReader(_mod.getDataArchive().getInputStream(action.replaceAll("\\s","") + ".js"));
			engine.eval(readerPolyfill);
			
			engine.eval("inputString = '" + (reply == null ? jsonString : reply.datafile()) + "';");
			engine.eval("reply = '" + reply.toJSONReply() + "';");
			
			readerAIScript = new InputStreamReader(_mod.getDataArchive().getInputStream("ai-script.js"));
			engine.eval(readerAIScript);
			
		} catch (ScriptException e1) {
			WriteLine("JS Exception " + e1.toString());
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			WriteLine("FileNotFoundException " + e1.toString());
			e1.printStackTrace();
		} catch (IOException e1) {
			WriteLine("I/O Exception " + e1.toString());
			e1.printStackTrace();
		} finally {
			if (readerJsonLib != null) {
				try {
					readerJsonLib.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (readerPolyfill != null) {
				try {
					readerPolyfill.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (readerAIScript != null) {
				try {
					readerAIScript.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		Object output = engine.get("msg");
		if (output != null) {
			String msgs = output.toString();
			String[] msg = msgs.split("\n");
			for (int i = 0; i < msg.length; i++) {
				WriteLine(msg[i]);
			}
		}
	}
	
	private void RunPython(String jsonString) {
		RunPython(jsonString, null);
	}
	
	private void RunPython(String jsonString, Question reply) {
		final File scriptFile;
		final File dataFile;
		final File replyFile;
		String scriptFilePath = "";
		String dataFilePath = "";
		String replyFilePath = "";
		try {
	        scriptFile = File.createTempFile("ai-script-", ".py");
	        scriptFile.deleteOnExit();
	        InputStreamReader in = new InputStreamReader(_mod.getDataArchive().getInputStream("ai-script.py"));
	        FileOutputStream out = new FileOutputStream(scriptFile);
	        IOUtils.copy(in, out);
	        scriptFilePath = scriptFile.getPath();
	        
	        if (jsonString != null) {
	        	dataFile = File.createTempFile("ai-script-", ".json");
	        	dataFile.deleteOnExit();
	        	PrintWriter sout = new PrintWriter(dataFile);
	        	sout.write(jsonString);
	        	sout.close();
	        	dataFilePath = dataFile.getPath();
	        } else {
	        	dataFilePath = reply.datafile();
	        	
	        	replyFile = File.createTempFile("ai-reply-", ".json");
	        	replyFile.deleteOnExit();
	        	PrintWriter sout = new PrintWriter(replyFile);
	        	sout.write(reply.toJSONReply());
	        	sout.close();
	        	replyFilePath = replyFile.getPath();
	        }
	    }
		catch (IOException ex) {
			WriteLine("IOException on Python: " + ex.getMessage());
			return;
		}
		
		final ProcessWithTimeout pwt;
		final Process p;
		try {  
			String commandline = "python " + scriptFilePath + " " + dataFilePath + " true";
			if (reply != null) {
				commandline += " " + replyFilePath;
			}
		    p = Runtime.getRuntime().exec(commandline);
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
		    			if (line.startsWith("{")) {
		    				// this is probably a question
		    				try {
		    					JSONParser parser = new JSONParser();
		    					JSONObject jsonObject = (JSONObject) parser.parse(line);
		    					String faction = jsonObject.get("faction").toString();
		    					String questionType = jsonObject.get("type").toString();
		    					String q = jsonObject.get("q").toString();
		    					String question = jsonObject.get("question").toString();
		    					String datafile = jsonObject.get("datafile").toString();
		    					String options = jsonObject.get("options").toString();
		    					
		    					_fireBotScriptEvent(new BotScriptEvent(this, BotScriptEvent.EVENTTYPE_QUESTION, new Question(faction, questionType, q, question, datafile, options)));
		    				}
		    				catch (Exception ex)
		    				{
		    					WriteLine("JSON: " + ex.toString());
		    					WriteLine(line);
		    				}
		    			} else {
		    				WriteLine(line);
		    			}
		    		}
		    	}
		    	catch (Exception ex) {
		    		WriteLine("Exception trying to read script output");
		    	}
		    }
		    
		    if (errin.ready()) {
		    	String line = null;
		    	try {
		    		while ((line = errreader.readLine()) != null) {
		    			WriteLine(line);
		    		}
		    	}
		    	catch (Exception ex) {
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
		} finally {
			
		}
	}
}
