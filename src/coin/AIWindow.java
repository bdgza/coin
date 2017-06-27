package coin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.tools.FormattedString;

public abstract class AIWindow extends JDialog implements ActionListener, IAIWindow {
	private static final long serialVersionUID = 1L;

	protected BotPackage bot;
	final static GameModule mod = GameModule.getGameModule();
	
	protected AIWindow() {
		super(mod.getFrame());
	}
		
	public void actionPerformed(ActionEvent e) {
		
	}
	
	protected abstract void initComponents(BotPackage bot);
	public abstract String getModuleTitle();
	
	protected void WriteLine(String msgLine) {
		FormattedString cStr = new FormattedString("<COINBot> - " + msgLine);
		final Command cc = new Chatter.DisplayText(mod.getChatter(), cStr.getLocalizedText());
		cc.execute();
		mod.sendAndLog(cc);
	}

}
