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

	private BotScriptEngine botScriptEngine;
	final static GameModule mod = GameModule.getGameModule();
	
	protected AIWindow() {
		super(mod.getFrame());
		
		botScriptEngine = new BotScriptEngine(mod, getModuleTitle());
		
		initComponents();
		setLocationRelativeTo(getOwner());
	}
	
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	protected abstract void initComponents();
	public abstract String getModuleTitle();
	
	private void WriteLine(String msgLine) {
		FormattedString cStr = new FormattedString("-<AI> " + msgLine);
		final Command cc = new Chatter.DisplayText(mod.getChatter(), cStr.getLocalizedText());
		cc.execute();
		mod.sendAndLog(cc);
	}

}
