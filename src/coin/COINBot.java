package coin;

import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.JDialog;

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

public class COINBot extends AbstractConfigurable {
	final static GameModule mod = GameModule.getGameModule();

	protected LaunchButton toolbarButton;
	protected AIWindow aiWindow;
	protected BotPackage botPackage;

	public static final String HOT_KEY = "hotkey"; //$NON-NLS-1$
	public static final String ICON = "icon"; //$NON-NLS-1$
	public static final String BUTTON_TEXT = "buttonText"; //$NON-NLS-1$
	public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$

	public COINBot() throws IOException {
		ActionListener al = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (aiWindow != null) {
					JDialog diag = (JDialog)aiWindow;
					diag.setVisible(!diag.isShowing());
				}
			}
		};

		String gameName = mod.getGameName();
		aiWindow = AIWindowFactory.getAIWindow(gameName);

		toolbarButton = new LaunchButton("COINBot", TOOLTIP, BUTTON_TEXT, HOT_KEY, ICON, al); //$NON-NLS-1$
		toolbarButton.setAttribute(ICON, "/images/chart.gif"); //$NON-NLS-1$
		toolbarButton.setToolTipText("COINBot"); //$NON-NLS-1$
		toolbarButton.setEnabled(true);

		if (aiWindow == null) {
			WriteLine(" - COINBot is not compatible with this game module: " + gameName);
			return;
		} else {
			WriteLine(" - COINBot detected game \"" + aiWindow.getModuleTitle() + "\"");
		}
		
		LoadBotPackage();

		if (botPackage == null || botPackage.GetBotType() == null) {
			WriteLine("Invalid COIN bot package.json file, COINBot will not work.");
			return;
		}
		
		// display Bot information
		
		WriteLine(" - COINBot bot loaded \"" + botPackage.GetReadableLabel() + "\" v" + botPackage.version);
//		for (int i = 0; i < botPackage.Factions.length; i++)
//			WriteLine(" - COINBot faction \"" + botPackage.Factions[i].Name + "\"");
		
		// check Bot supports this module version
		
		if (botPackage.supportedVersions != null && botPackage.supportedVersions.length > 0) {
			String version = mod.getGameVersion();
			Boolean found = false;
			for (int i = 0; i < botPackage.supportedVersions.length; i++)
				if (botPackage.supportedVersions[i].equals(version))
					found = true;
			
			if (!found) {
				WriteLine(" - COINBot Warning: The module version '" + version + "' is not supported by this bot. The bot may not work properly. It is recommended you use a supported version of the module and matching savegames.");
				WriteLine(" - COINBot bot script supported versions:");
				for (int i = 0; i < botPackage.supportedVersions.length; i++)
					WriteLine("     - Module version " + botPackage.supportedVersions[i]);
			}
		}
		
		// detect Node if bot type is JS

		if (botPackage.GetBotType().equals("JS")) {
			DetectNodeLocation();
		}
		
		// load components for COINBot AIWindow
		
		aiWindow.initComponents(botPackage);
	}
	
	@SuppressWarnings("unused")
	private String join(String[] array) {
		String s = "";
		for (int i = 0; i < array.length; i++) {
			if (s.length() > 0)
				s += ",";
			s += array[i];
		}
		return s;
	}

	private void DetectNodeLocation() {
		File temp;
		try {
			temp = File.createTempFile("coinbot-node", ".js");
			FileWriter fw = new FileWriter(temp);
			fw.write("console.log(\"node-test-confirm\");");
			fw.close();
		} catch (IOException ex) {
			WriteLine(" - ERROR trying to detect path to Node; COINBot will likely not function");
			botPackage.nodeProcess = "";
			return;
		}

		// try: node

		botPackage.nodeProcess = "node";

		try {
			final Process p = Runtime.getRuntime().exec(new String[] { botPackage.nodeProcess, temp.getName() }, new String[] {}, temp.getParentFile());
			final ProcessWithTimeout pwt = new ProcessWithTimeout(p);

			InputStream stdout = p.getInputStream();
			InputStreamReader in = new InputStreamReader(stdout);
			BufferedReader reader = new BufferedReader(in);

			pwt.waitForProcess(3000);

			p.getOutputStream().close();

			if (in.ready()) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.equals("node-test-confirm"))
						return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// try: /usr/local/bin/node

		botPackage.nodeProcess = "/usr/local/bin/node";

		try {
			final Process p = Runtime.getRuntime().exec(new String[] { botPackage.nodeProcess, temp.getName() }, new String[] {}, temp.getParentFile());
			final ProcessWithTimeout pwt = new ProcessWithTimeout(p);

			InputStream stdout = p.getInputStream();
			InputStreamReader in = new InputStreamReader(stdout);
			BufferedReader reader = new BufferedReader(in);

			pwt.waitForProcess(3000);

			p.getOutputStream().close();

			if (in.ready()) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.equals("node-test-confirm"))
						return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		botPackage.nodeProcess = "";
		WriteLine(" - Could not find path to node; COINBot will likely not function for this JavaScript bot");
	}

	private void LoadBotPackage() {
		File f = null;
		f = mod.getDataArchive().getArchive().getFile();
		String moduleFileName = f.getName();
		moduleFileName = moduleFileName.substring(0, moduleFileName.length() - 5);
		f = f.getParentFile();
		String baseBotPath = f.getAbsolutePath() + File.separator + moduleFileName + "_ext" + File.separator + "coinbot" + File.separator;
		String packagePath = baseBotPath + "package.json";

		try {
			InputStreamReader packageStream = new InputStreamReader(new FileInputStream(packagePath));
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(packageStream);
			packageStream.close();

			botPackage = new BotPackage(jsonObject);
			botPackage.basePath = baseBotPath;
			botPackage.packageFile = packagePath;
		} catch (Exception ex) {
			ex.printStackTrace();
			botPackage = null;
			WriteLine(" - ERROR loading bot package file; COINBot will not function");
		}
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
		return new String[] { Resources.getString(Resources.BUTTON_TEXT), Resources.getString(Resources.TOOLTIP_TEXT),
				Resources.getString(Resources.BUTTON_ICON) };
	}

	public Class<?>[] getAttributeTypes() {
		return new Class<?>[] { String.class, String.class, IconConfig.class };
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
