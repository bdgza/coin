/*
 * $Id: NotesWindow.java 7725 2011-07-31 18:51:43Z uckelman $
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package coin.fs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GameState;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;
import VASSAL.command.Command;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.Deck;
import VASSAL.counters.Decorator;
import VASSAL.counters.DynamicProperty;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Immobilized;
import VASSAL.counters.Stack;
import VASSAL.i18n.Resources;
import VASSAL.tools.FormattedString;
import VASSAL.tools.LaunchButton;
import coin.*;

/**
 * This is a copy of the NotesWindow in VASSAL, could probably still be improved upon
 */
public class FallingAIWindow extends AbstractConfigurable {
	final GameModule mod = GameModule.getGameModule();

	protected JDialog frame;
	protected LaunchButton launch;

	private JCheckBox aeduiNPBox;
	private JCheckBox arverniNPBox;
	private JCheckBox belgicNPBox;
	private JCheckBox romanNPBox;
	private GameState myGameState;
	private JPanel[] buttonPanels = new JPanel[4];
	private JComboBox<String> botCombo;

	private BotScriptEngine _botScriptEngine;

	public static final String HOT_KEY = "hotkey"; //$NON-NLS-1$
	public static final String ICON = "icon"; //$NON-NLS-1$
	public static final String BUTTON_TEXT = "buttonText"; //$NON-NLS-1$
	public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$

	public FallingAIWindow() {
		frame = new FallingAIDialog();
		frame.setTitle("Falling Sky AI");
		ActionListener al = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				frame.setVisible(!frame.isShowing());
			}
		};
		launch = new LaunchButton("AI", TOOLTIP, BUTTON_TEXT, HOT_KEY, ICON, al); //$NON-NLS-1$
		launch.setAttribute(ICON, "/images/chart.gif"); //$NON-NLS-1$
		launch.setToolTipText("AI"); //$NON-NLS-1$
		launch.setEnabled(true);

		//frame.pack();

		int x = frame.getX();
		int y = frame.getY();
		x = x + ((frame.getWidth() - 520) / 2);
		y = y + ((frame.getHeight() - 135) / 2);
		if (x < 15)
			x = 15;
		if (y < 15)
			y = 15;
		frame.setSize(520, frame.getHeight()); // TODO: was 110 for 1 row of buttons!
		frame.setLocation(x, y);
	}

	protected class FallingAIDialog extends JDialog implements ActionListener {

		private static final long serialVersionUID = 1L;

		protected FallingAIDialog() {
			super(GameModule.getGameModule().getFrame());
			
			_botScriptEngine = new BotScriptEngine(mod, "Falling Sky AI");
			
			initComponents();
			setLocationRelativeTo(getOwner());
		}
		
		private JButton makeButton(String name, GridBagLayout gridbag, GridBagConstraints c) {
			JButton button = new JButton(name);
			gridbag.setConstraints(button, c);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					aiButtonActionPerformed(e);
				}
			});
			add(button);
			return button;
		}
		
		private JCheckBox makeCheckBox(String name, GridBagLayout gridbag, GridBagConstraints c) {
			JCheckBox button = new JCheckBox(name);
			button.setSelected(true);
			gridbag.setConstraints(button, c);
			add(button);
			return button;
		}

		protected void initComponents() {
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					setVisible(false);
				}
			});
			
			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			
			setFont(new Font("SansSerif", Font.PLAIN, 12));
			setLayout(gridbag);

	        c.fill = GridBagConstraints.BOTH;
	        c.weightx = 1.0;
	        c.gridwidth = GridBagConstraints.REMAINDER;
			
			botCombo = new JComboBox<String>();
			for (int i = 0; i < _botScriptEngine.BotConfigurations().size(); i++) {
				botCombo.addItem(_botScriptEngine.BotConfigurations().get(i).name);
			}
			gridbag.setConstraints(botCombo, c);
			add(botCombo);
			
			makeButton("Game State", gridbag, c);
			
			c.weightx = 0.0;
			c.gridwidth = 1;
			
			try {
				aeduiNPBox = makeCheckBox(_botScriptEngine.BotFactions().get(0), gridbag, c);
				arverniNPBox = makeCheckBox(_botScriptEngine.BotFactions().get(1), gridbag, c);
				belgicNPBox = makeCheckBox(_botScriptEngine.BotFactions().get(2), gridbag, c);
				
				c.gridwidth = GridBagConstraints.REMAINDER;
				
				romanNPBox = makeCheckBox(_botScriptEngine.BotFactions().get(3), gridbag, c);
				
				c.weightx = 0.0;
				c.gridwidth = 1;
				
				for (int i = 0; i < 4; i++) {
					buttonPanels[i] = new JPanel();
					buttonPanels[i].setLayout(new BoxLayout(buttonPanels[i], BoxLayout.Y_AXIS));
					if (i == 3)
						c.gridwidth = GridBagConstraints.REMAINDER;
					gridbag.setConstraints(buttonPanels[i], c);
					add(buttonPanels[i]);
				}
				
				botCombo.addActionListener(this);
				
				actionPerformed(null);
			}
			catch (Exception ex) {
				WriteLine("ERROR! " + ex.getMessage());
			}
		}
		
		public void actionPerformed(ActionEvent e) {			
			int bot = botCombo.getSelectedIndex();
			int rows = 0;
			_botScriptEngine.setSelectedBot(bot);
			
			for (int i = 0; i < 4; i++) {
				buttonPanels[i].removeAll();
				
				ArrayList<String> acts = _botScriptEngine.BotConfigurations().get(bot).actions.get(i);
				
				rows = Math.max(rows, acts.size());
				
				for (int j = 0; j < acts.size(); j++) {					
					JButton button = new JButton(acts.get(j));
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							aiButtonActionPerformed(e);
						}
					});
					buttonPanels[i].add(button);
				}
			}
			
			this.setSize(this.getWidth(), 110 + (25 * rows));
			
			revalidate();
			repaint();
		}
	}
	
	private void aiButtonActionPerformed(ActionEvent e) {
		String aiButtonName = ((JButton)e.getSource()).getText();
		int winterCount = 0;

		ArrayList<ZoneIndex> zones = new ArrayList<ZoneIndex>();

		myGameState = mod.getGameState();

		Collection<GameComponent> comps = myGameState.getGameComponents();

		Iterator<GameComponent> zoneIterator = comps.iterator();

		do {
			GameComponent comp = zoneIterator.next();
			
			if (comp instanceof Zone) {
				Zone zone = (Zone) comp;
				zones.add(new ZoneIndex(zone.getName(), zone));

//			} else {
//				WriteLine("Game Component: "+comp.toString());
//				WriteLine("Component: " +
//				comp.getClass().toString());
			}
		} while (zoneIterator.hasNext());

		ArrayList<ZonePiece> offboard = new ArrayList<ZonePiece>();
		Collection<GamePiece> pieces = myGameState.getAllPieces();
		Iterator<GamePiece> pieceIterator = pieces.iterator();
		
		if (!pieceIterator.hasNext()) {
			WriteLine("Please load a game first");
			return;
		}

		do {
			GamePiece piece = pieceIterator.next();

			String mapName = "?";
			if (piece.getMap() != null) {
				mapName = piece.getMap().getMapName();
			}
			
			String pieceName = piece.getName();
			Point position = piece.getPosition();
			
//			if (pieceName.indexOf(" Eligibility") > -1) {
//				WriteLine("...Piece: " + pieceName + " ( " +
//				position.x + ", " + position.y + " ) [ " +
//				piece.getClass() + " ] on MAP: " + mapName);
//			
//				WriteLine(piece.getType());
//				WriteLine("Stack" + (piece instanceof Stack));
//			}
			
			if (!(piece instanceof Deck) && !(piece instanceof Stack) && (!pieceName.equals("?")) && (pieceName.length() > 0)) {
				
				// Joel has named "Roman Ally" -> "Germanic Ally" instead in the module, so we need to check for the name of the PNG image
				if (pieceName.indexOf("Germanic Ally") != -1) {
					Object inner = Decorator.getInnermost(piece);
					if (inner != null && inner instanceof BasicPiece) {
						BasicPiece basic = (BasicPiece) inner;
						if (basic.getType().indexOf(";;;Roman Ally.png") > -1) {
							// change pieceName from Germanic Ally to Roman Ally
							pieceName = "Roman Ally";
						}
					}
				}
				
				// set the population from property
				if (pieceName.indexOf(" Pop") == 1) { // 1 Pop, 2 Pop, 3 Pop
					// population token, check for control
					int control = Integer.parseInt(((DynamicProperty)piece).getProperty("ControlValue").toString());
					switch (control) {
					case 2:
						pieceName += " (AeduiControl)";
						break;
					case 3:
						pieceName += " (ArverniControl)";
						break;
					case 4:
						pieceName += " (BelgicControl)";
						break;
					case 5:
						pieceName += " (GermanControl)";
						break;
					case 6:
						pieceName += " (RomanControl)";
						break;
					case 1:
						pieceName += " (NoControl)";
						break;
					}
				}
				
				// check for Dispersed (TribeState)
				try {
					int tribeState = Integer.parseInt(piece.getProperty("TribeState").toString());
					switch (tribeState) {
					case 3:
						pieceName += " (Dispersed)";
						break;
					case 2:
						pieceName += " (Gathering)";
						break;
					}
				}
				catch (Exception ex2) {}
				
				// check for Available Forces DummyState
				try {
					int tribeState = Integer.parseInt(piece.getProperty("DummyState").toString());
					switch (tribeState) {
					case 2:
						pieceName += " (Empty)";
						break;
					case 3:
						pieceName += " (Ally)";
						break;
					case 1:
						pieceName += " (Occupied)";
						break;
					}
				}
				catch (Exception ex3) {}
				
				boolean foundZone = false;
				
				for (int i = 0; i < zones.size(); i++) {
					ZoneIndex zi = zones.get(i);
					Zone z = zi.zone;
					
					Point piecePosition = piece.getPosition();
					piecePosition.translate(-1 * zi.offsetX, -1 * zi.offsetY);
					
					if (z.contains(piecePosition) && zi.map.equals(mapName)) {
						zi.pieces.add(new ZonePiece(pieceName, position));
						foundZone = true;
						break;
					}
				}
				
				if (!foundZone && mapName.equals("Main Map")) {
					offboard.add(new ZonePiece(pieceName, position));
				}
			} else if (piece instanceof Deck) {
				Deck deck = (Deck) piece;
				String deckName = deck.getDeckName();
				if (deckName.equals("Deck")) {
					for (int d = 0; d < deck.getPieceCount(); d++) {
						String cardObject = deck.getPieceAt(d).toString();
						if (cardObject.indexOf(" - Winter.jpg;7") > -1) winterCount++;
					}
				}
			}
		} while (pieceIterator.hasNext());

		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"npaedui\": " + aeduiNPBox.isSelected() + ", ");
		json.append("\"nparverni\": " + arverniNPBox.isSelected() + ", ");
		json.append("\"nproman\": " + romanNPBox.isSelected() + ", ");
		json.append("\"npbelgic\": " + belgicNPBox.isSelected() + ", ");
		json.append("\"winter\": " + winterCount + ", ");
		json.append("\"action\": \"" + aiButtonName + "\", ");
		
		String jsonString = _botScriptEngine.ConstructJSON(json, offboard, zones);

//		WriteLine("JSON: " + jsonString);

//		WriteLine("SHIFT? " + (e.getModifiers() & InputEvent.SHIFT_MASK));
		_botScriptEngine.setVerbose((e.getModifiers() & InputEvent.SHIFT_MASK) != 0);
		_botScriptEngine.RunScript(jsonString, aiButtonName);
		
//		_botScriptEngine.removeBotScriptListener(listener);
	}
		
	private void WriteLine(String msgLine) {
		FormattedString cStr = new FormattedString("-<AI> " + msgLine);
		final Command cc = new Chatter.DisplayText(mod.getChatter(), cStr.getLocalizedText());
		cc.execute();
		mod.sendAndLog(cc);
	}

	public String[] getAttributeNames() {
		return new String[] { BUTTON_TEXT, TOOLTIP, ICON, HOT_KEY };
	}

	public void setAttribute(String name, Object value) {
		launch.setAttribute(name, value);
	}

	public String getAttributeValueString(String name) {
		return launch.getAttributeValueString(name);
	}

	public String[] getAttributeDescriptions() {
		return new String[] { Resources.getString(Resources.BUTTON_TEXT), Resources.getString(Resources.TOOLTIP_TEXT),
				Resources.getString(
						Resources.BUTTON_ICON)/*
												 * ,
												 * Resources.getString(Resources
												 * .HOTKEY_LABEL)
												 */
		};
	}

	public Class<?>[] getAttributeTypes() {
		return new Class<?>[] { String.class, String.class,
				IconConfig.class/*
								 * , NamedKeyStroke.class
								 */
		};
	}

	public static class IconConfig implements ConfigurerFactory {
		public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
			return new IconConfigurer(key, name, ((FallingAIWindow) c).launch.getAttributeValueString(ICON));
		}
	}

	public Configurable[] getConfigureComponents() {
		return new Configurable[0];
	}

	public Class<?>[] getAllowableConfigureComponents() {
		return new Class<?>[0];
	}

	public static String getConfigureTypeName() {
		return "AI Window";
	}

	/**
	 * Expects to be added to a {@link VASSAL.build.GameModule}. Adds a button
	 * to the controls window toolbar to show the window containing the notes
	 */
	public void addTo(Buildable b) {
		GameModule.getGameModule().getToolBar().add(launch);
		launch.setAlignmentY(0.0F);
	}

	public void removeFrom(Buildable b) {
		GameModule.getGameModule().getToolBar().remove(launch);
	}

	public HelpFile getHelpFile() {
		return null;
	}
}
