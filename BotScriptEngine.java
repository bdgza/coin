package coin;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.script.*;

import org.apache.commons.io.IOUtils;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.tools.FormattedString;

import org.json.simple.*;
import org.json.simple.parser.*;


public class BotScriptEngine {
	private static GameModule mod;
	private List<BotScriptListener> _listeners;
	
	public BotScriptEngine(GameModule module) {
		mod = module;
		_listeners = new ArrayList<BotScriptListener>();
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
		final Command cc = new Chatter.DisplayText(mod.getChatter(), cStr.getLocalizedText());
		cc.execute();
		mod.sendAndLog(cc);
	}
	
	private boolean ContainsJS(String action) {
		boolean containsJS = false;
		try {
			containsJS = mod.getDataArchive().getInputStream("ai-script.js") != null;
			containsJS = containsJS && mod.getDataArchive().getInputStream(action.replaceAll("\\s","") + ".js") != null;
		} catch (IOException e1) {}
		return containsJS;
	}
	
	private boolean ContainsPY() {
		boolean containsPY = false;
		try {
			containsPY = mod.getDataArchive().getInputStream("ai-script.py") != null;
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
			RunJS("", reply.faction(), reply);
		}
		
		if (!containsJS && ContainsPY()) {
			// try Python
			RunPython("", reply);
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
			readerJsonLib = new InputStreamReader(mod.getDataArchive().getInputStream("json2.js"));
			engine.eval(readerJsonLib);
			
			readerPolyfill = new InputStreamReader(mod.getDataArchive().getInputStream("polyfill.js"));
			engine.eval(readerPolyfill);
			
			readerPolyfill = new InputStreamReader(mod.getDataArchive().getInputStream("ai-tools.js"));
			engine.eval(readerPolyfill);
			
			readerPolyfill = new InputStreamReader(mod.getDataArchive().getInputStream(action.replaceAll("\\s","") + ".js"));
			engine.eval(readerPolyfill);
			
			engine.eval("inputString = '" + (reply == null ? jsonString : reply.datafile()) + "';");
			engine.eval("reply = '" + reply.toJSONReply() + "';");
			
			readerAIScript = new InputStreamReader(mod.getDataArchive().getInputStream("ai-script.js"));
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
		String dataFilePath = "";
		try {
	        scriptFile = File.createTempFile("ai-script-", ".py");
	        scriptFile.deleteOnExit();
	        InputStreamReader in = new InputStreamReader(mod.getDataArchive().getInputStream("ai-script.py"));
	        FileOutputStream out = new FileOutputStream(scriptFile);
	        IOUtils.copy(in, out);
	        
	        if (jsonString.length() > 0) {
	        	dataFile = File.createTempFile("ai-script-", ".json");
	        	dataFile.deleteOnExit();
	        	PrintWriter sout = new PrintWriter(dataFile);
	        	sout.write(jsonString);
	        	sout.close();
	        	dataFilePath = dataFile.getPath();
	        } else {
	        	dataFilePath = reply.datafile();
	        }
	    }
		catch (IOException ex) {
			WriteLine("IOException on Python: " + ex.getMessage());
			return;
		}
		
		final ProcessWithTimeout pwt;
		final Process p;
		try {  
			String commandline = "python " + scriptFile.getPath() + " " + dataFilePath + " true";
			if (reply != null) {
				commandline += " " + reply.toJSONReply();
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
		    					String q = jsonObject.get("q").toString();
		    					String question = jsonObject.get("question").toString();
		    					String datafile = jsonObject.get("datafile").toString();
		    					
		    					_fireBotScriptEvent(new BotScriptEvent(this, BotScriptEvent.EVENTTYPE_QUESTION, new Question(faction, q, question, datafile)));
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
