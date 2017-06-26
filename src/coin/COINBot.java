package coin;

import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;
import VASSAL.i18n.Resources;
import VASSAL.tools.FormattedString;
import VASSAL.tools.LaunchButton;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.sun.org.glassfish.external.amx.BootAMXMBean;

public class COINBot extends AbstractConfigurable {
	final static GameModule mod = GameModule.getGameModule();
	
	protected LaunchButton toolbarButton;
	protected IAIWindow aiWindow;
	
	public static final String HOT_KEY = "hotkey"; //$NON-NLS-1$
	public static final String ICON = "icon"; //$NON-NLS-1$
	public static final String BUTTON_TEXT = "buttonText"; //$NON-NLS-1$
	public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$
	
	public COINBot() throws IOException {
		ActionListener al = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				//frame.setVisible(!frame.isShowing());
				if (aiWindow != null)
					WriteLine("TEST CLICKED");
			}
		};
		
		String gameName = mod.getGameName();
		aiWindow = AIWindowFactory.getAIWindow(gameName);
		
		toolbarButton = new LaunchButton("COINBot", TOOLTIP, BUTTON_TEXT, HOT_KEY, ICON, al); //$NON-NLS-1$
		toolbarButton.setAttribute(ICON, "/images/chart.gif"); //$NON-NLS-1$
		toolbarButton.setToolTipText("COINBot"); //$NON-NLS-1$
		toolbarButton.setEnabled(true);
		
		if (aiWindow == null) {
			WriteLine("COINBot is not compatible with this game module: " + gameName);
		} else {
			WriteLine("COINBot detected \"" + aiWindow.getModuleTitle() + "\"");
		}
		
		File f = null;
		try {
			f = new File(VASSAL.launch.Player.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			WriteLine("VASSAL=" + f.getCanonicalPath());
		} catch (URISyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		f = mod.getDataArchive().getArchive().getFile();
		String moduleFileName = f.getName();
		moduleFileName = moduleFileName.substring(0, moduleFileName.length() - 5);
		f = f.getParentFile();
		String baseBotPath = f.getCanonicalPath() + File.separator + moduleFileName + "_ext" + File.separator + "coinbot" + File.separator;
		String packagePath = baseBotPath + "package.json";
		
		WriteLine(packagePath);
		WriteLine(moduleFileName);
		WriteLine(String.valueOf(new File(packagePath).exists()));
		
		try {
			InputStreamReader packageStream = new InputStreamReader(new FileInputStream(packagePath));
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(packageStream);
			packageStream.close();
			
			WriteLine(jsonObject.get("name").toString());
		}
		catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String[] path = System.getenv("PATH").split(System.getProperty("path.separator"));
		for (int i = 0; i < path.length; i++)
			WriteLine(i + ". " + path[i]);
		
		WriteLine(System.getProperty("os.name"));
		
		RunHelloWorld(baseBotPath);
		
		//RunTest1(baseBotPath);
		//RunTest2(baseBotPath);
	}
	
	private void RunTest1(String baseBotPath) {
		WriteLine("ENTRY");
		
		final ProcessWithTimeout pwt;
		final Process p;
		try {  
			String commandline = "node " + "~/bot.js";
			WriteLine(commandline);
			
		    p = Runtime.getRuntime().exec(new String[] {"ls"}, new String[] {}, new File("/")); //commandline);
		    pwt = new ProcessWithTimeout(p);
			
//			ProcessBuilder pb = new ProcessBuilder("ls"); //, baseBotPath + "bot.js");
			
//			p = pb.start();
//			p.wait(6000);
		    
		    InputStream stdout = p.getInputStream();
		    InputStreamReader in = new InputStreamReader(stdout);
		    BufferedReader reader = new BufferedReader(in);
		    
		    InputStream stderr = p.getErrorStream();
		    InputStreamReader errin = new InputStreamReader(stderr);
		    BufferedReader errreader = new BufferedReader(errin);
		    
		    //int exitVal = 1;
		    int exitVal = pwt.waitForProcess(6000);
		    
		    p.getOutputStream().close();
		    
		    if (in.ready()) {
		    	String line = null;
		    	try {
		    		while ((line = reader.readLine()) != null) {
		    			WriteLine("XXX " + line);
		    			//processMessageLine(line, null);
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
		
		WriteLine("EXIT");
	}
	
//	private void RunTest2(String baseBotPath) {
//		try {
//			ProcessBuilder pb = new ProcessBuilder("/usr/local/bin/node", baseBotPath + "bot.js");
//			pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//			pb.redirectError(ProcessBuilder.Redirect.INHERIT);
//			try {
//				Process p = pb.start();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	
	private void RunHelloWorld(String baseBotPath) {
		WriteLine("ENTRY");
		
		final ProcessWithTimeout pwt;
		final Process p;
		try {
		    p = Runtime.getRuntime().exec(new String[] {"/usr/local/bin/node", "bot.js"}, new String[] {}, new File(baseBotPath)); //commandline);
		    pwt = new ProcessWithTimeout(p);
			
//			ProcessBuilder pb = new ProcessBuilder("ls"); //, baseBotPath + "bot.js");
			
//			p = pb.start();
//			p.wait(6000);
		    
		    InputStream stdout = p.getInputStream();
		    InputStreamReader in = new InputStreamReader(stdout);
		    BufferedReader reader = new BufferedReader(in);
		    
		    InputStream stderr = p.getErrorStream();
		    InputStreamReader errin = new InputStreamReader(stderr);
		    BufferedReader errreader = new BufferedReader(errin);
		    
		    //int exitVal = 1;
		    int exitVal = pwt.waitForProcess(6000);
		    
		    p.getOutputStream().close();
		    
		    if (in.ready()) {
		    	String line = null;
		    	try {
		    		while ((line = reader.readLine()) != null) {
		    			WriteLine(">>> " + line);
		    			//processMessageLine(line, null);
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
		
		WriteLine("EXIT");
	}
	
	private void WriteLine(String msgLine) {
		FormattedString cStr = new FormattedString(msgLine);
		final Command cc = new Chatter.DisplayText(mod.getChatter(), cStr.getLocalizedText());
		cc.execute();
		mod.sendAndLog(cc);
	}
	
	/* *** */

	public static class IconConfig implements ConfigurerFactory {
		public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
			return new IconConfigurer(key, name, ((COINBot) c).toolbarButton.getAttributeValueString(ICON));
		}
	}
	
	public void removeFrom(Buildable parent) {
		GameModule.getGameModule().getToolBar().remove(toolbarButton);
	}

	public HelpFile getHelpFile() {
		return null;
	}

	public Class<?>[] getAllowableConfigureComponents() {
		return new Class<?>[0];
	}

	public void addTo(Buildable parent) {
		GameModule.getGameModule().getToolBar().add(toolbarButton);
		toolbarButton.setAlignmentY(0.0F);		
	}

	public String[] getAttributeDescriptions() {
		return new String[] { 
				Resources.getString(Resources.BUTTON_TEXT),
				Resources.getString(Resources.TOOLTIP_TEXT),
				Resources.getString(Resources.BUTTON_ICON)
		};
	}

	public Class<?>[] getAttributeTypes() {
		return new Class<?>[] { String.class, String.class,
			IconConfig.class
		};
	}

	public String[] getAttributeNames() {
		return new String[] { BUTTON_TEXT, TOOLTIP, ICON, HOT_KEY };
	}

	public void setAttribute(String key, Object value) {
		toolbarButton.setAttribute(key, value);
	}

	public String getAttributeValueString(String key) {
		return toolbarButton.getAttributeValueString(key);
	}

}
