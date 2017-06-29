package coin;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GameState;
import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;
import VASSAL.command.Command;
import VASSAL.counters.*;
import VASSAL.counters.Stack;
import VASSAL.tools.FormattedString;

public abstract class AIWindow extends JDialog implements IAIWindow {
	private static final long serialVersionUID = 1L;

	protected BotPackage bot;
	final static GameModule mod = GameModule.getGameModule();
	
	protected AIWindow() {
		super(mod.getFrame());
		
		setTitle("COINBot");
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});
	}
	
	protected abstract void initGameComponents();
	public abstract String getModuleTitle();
	protected abstract PiecesGameState gatherGamePieces(ArrayList<ZoneIndex> zones);
	
	protected void WriteLine(String msgLine) {
		FormattedString cStr = new FormattedString(" - <COINBot> - " + msgLine);
		final Command cc = new Chatter.DisplayText(mod.getChatter(), cStr.getLocalizedText());
		cc.execute();
		mod.sendAndLog(cc);
	}

	public void initComponents(final BotPackage botPackage) {
		bot = botPackage;
		getContentPane().removeAll();
		
		getContentPane().setLayout(new BorderLayout(2, 2));
		
		JPanel panelActionButtons = new JPanel();
		getContentPane().add(panelActionButtons, BorderLayout.NORTH);
		
		panelActionButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		for (int i = 0; i < bot.Actions.length; i++)
        	panelActionButtons.add(makeButton(bot.Actions[i], "action:" + bot.Actions[i]));
		
		JPanel panelFactions = new JPanel();
		getContentPane().add(panelFactions);
		panelFactions.setLayout(new GridLayout(1, 0, 2, 2));
		
		for (int i = 0; i < bot.Factions.length; i++) {
			final Faction faction = bot.Factions[i];
			
			JPanel panel = new JPanel();
			panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			panelFactions.add(panel);
			
			panel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
			
			JCheckBox checkboxFactionNP = new JCheckBox(bot.Factions[i].Name + " NP");
			checkboxFactionNP.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					faction.NonPlayer = ((JCheckBox) e.getSource()).isSelected();
				}
			});
			panel.add(checkboxFactionNP);
			
			for (int j = 0; j < bot.Factions[i].Actions.length; j++)
				panel.add(makeButton(bot.Factions[i].Actions[j], bot.Factions[i].Id + ":" + bot.Factions[i].Actions[j]));
		}
			
		// game specific override
		
		initGameComponents();
		
		// location
		
		int x = getX() + ((getWidth() - 520) / 2);
		int y = getY() + ((getHeight() - 135) / 2);
		if (x < 15) x = 15;
		if (y < 15) y = 15;
		setLocation(x, y);
		setLocationRelativeTo(getOwner());
		
		getContentPane().setPreferredSize(new Dimension(bot.windowSize[0], bot.windowSize[1]));
		pack();
	}
	
	private JButton makeButton(String name, final String id) {
		JButton button = new JButton(name);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aiButtonActionPerformed(id);
			}
		});
		return button;
	}
	
	private void aiButtonActionPerformed(String id) {
		// check that a game is available, otherwise ask use to load first
		
		GameState myGameState = mod.getGameState();
		Iterator<GamePiece> pieceIterator = myGameState.getAllPieces().iterator();
		
		if (!pieceIterator.hasNext()) {
			WriteLine("Please load a game first");
			return;
		}
		
		ArrayList<ZoneIndex> zones = new ArrayList<ZoneIndex>();
		Iterator<GameComponent> zoneIterator = myGameState.getGameComponents().iterator();
		
		do {
			GameComponent comp = zoneIterator.next();
			
			if (comp instanceof Zone) {
				Zone zone = (Zone) comp;
				zones.add(new ZoneIndex(zone.getName(), zone));
			}
		} while (zoneIterator.hasNext());
		
		PiecesGameState piecesGameState = gatherGamePieces(zones);

		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"action\": \"" + id + "\", ");
		for (int i = 0; i < bot.Factions.length; i++) {
			json.append("\"np" + bot.Factions[i].Id + "\": " + bot.Factions[i].NonPlayer.toString() + ", ");
		}
		String[] attributeKeys = (String[]) piecesGameState.attributes.keySet().toArray();
		for (int i = 0; i < attributeKeys.length; i++) {
			String key = attributeKeys[i];
			String value = piecesGameState.attributes.get(key);
			json.append("\"" + key.replace("\"", "'") + "\": \"" + value.replace("\"", "'") + "\", ");
		}
//		json.append("\"winter\": " + winterCount + ", ");
		
		// TODO: bot engine -- String jsonString = _botScriptEngine.ConstructJSON(json, piecesGameState.offboard, piecesGameState.zones);
//
//		_botScriptEngine.setVerbose((e.getModifiers() & InputEvent.SHIFT_MASK) != 0);
//		_botScriptEngine.RunScript(jsonString, aiButtonName);
	}
}
