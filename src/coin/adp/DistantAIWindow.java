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
package coin.adp;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

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
import VASSAL.counters.Deck;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Stack;
import VASSAL.i18n.Resources;
import VASSAL.tools.FormattedString;
import VASSAL.tools.LaunchButton;

import coin.*;

/**
 * This is a copy of the NotesWindow in VASSAL, could probably still be improved upon
 */
public class DistantAIWindow extends AbstractConfigurable {
	final GameModule mod = GameModule.getGameModule();

	protected JDialog frame;
	protected LaunchButton launch;

	private JCheckBox coalitionNPBox;
	private JCheckBox governmentNPBox;
	private JCheckBox warlordNPBox;
	private JCheckBox talibanNPBox;
	private GameState myGameState;

	public static final String HOT_KEY = "hotkey"; //$NON-NLS-1$
	public static final String ICON = "icon"; //$NON-NLS-1$
	public static final String BUTTON_TEXT = "buttonText"; //$NON-NLS-1$
	public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$

	public DistantAIWindow() {
		frame = new DistantAIDialog();
		frame.setTitle("A Distant Plain AI");
		ActionListener al = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				frame.setVisible(!frame.isShowing());
			}
		};
		launch = new LaunchButton("AI", TOOLTIP, BUTTON_TEXT, HOT_KEY, ICON, al); //$NON-NLS-1$
		launch.setAttribute(ICON, "/images/chart.gif"); //$NON-NLS-1$
		launch.setToolTipText("AI"); //$NON-NLS-1$
		launch.setEnabled(true);

		frame.pack();

		int x = frame.getX();
		int y = frame.getY();
		x = x + ((frame.getWidth() - 480) / 2);
		y = y + ((frame.getHeight() - 120) / 2);
		if (x < 15)
			x = 15;
		if (y < 15)
			y = 15;
		frame.setSize(480, 120);
		frame.setLocation(x, y);
	}

	protected class DistantAIDialog extends JDialog {

		private static final long serialVersionUID = 1L;

		protected DistantAIDialog() {
			super(GameModule.getGameModule().getFrame());
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
			
			setFont(new Font("SansSerif", Font.PLAIN, 14));
			setLayout(gridbag);

	        c.fill = GridBagConstraints.BOTH;
	        c.weightx = 1.0;
	        c.gridwidth = GridBagConstraints.REMAINDER;
			makeButton("Game State", gridbag, c);
			
			c.weightx = 0.0;
			c.gridwidth = 1;
			
			coalitionNPBox = makeCheckBox("Coalition NP", gridbag, c);
			governmentNPBox = makeCheckBox("Government NP", gridbag, c);
			warlordNPBox = makeCheckBox("Warlords NP", gridbag, c);
			
			c.gridwidth = GridBagConstraints.REMAINDER;
			
			talibanNPBox = makeCheckBox("Taliban NP", gridbag, c);
			
			c.weightx = 0.0;
			c.gridwidth = 1;
			
			makeButton("Coalition", gridbag, c);
			makeButton("Government", gridbag, c);
			makeButton("Warlords", gridbag, c);
			
			c.gridwidth = GridBagConstraints.REMAINDER;
			
			makeButton("Taliban", gridbag, c);
		}
	}
	
	private void aiButtonActionPerformed(ActionEvent e) {
		String aiButtonName = ((JButton)e.getSource()).getText();
		int propCount = 0;

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

			// WriteLine("...Piece: " + pieceName + " ( " +
			// position.x + ", " + position.y + " ) [ " +
			// piece.getClass() + " ]");

			if (!(piece instanceof Deck) && !(piece instanceof Stack) && (!pieceName.equals("?")) && (pieceName.length() > 0)) {
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
//					if (((z.contains(position)) || ((zi.name.equals("Card Play Area")) && (position.x < 0))
//							|| ((zi.name.equals("Momentum")) && (position.x > 5050)))) {
//						zi.pieces.add(new ZonePiece(pieceName, position));
//						break;
//					}
//				}
			} else if (piece instanceof Deck) {
				// check for "Deck" deck to count Propaganda cards left inside
				Deck deck = (Deck) piece;
				String deckName = deck.getDeckName();
				if (deckName.equals("Deck")) {
					for (int d = 0; d < deck.getPieceCount(); d++) {
						String cardObject = deck.getPieceAt(d).toString();
						if (cardObject.indexOf("ADP_Cards_Printer-73.png;Propaganda") > -1) propCount++;
					}
				}
			}
		} while (pieceIterator.hasNext());

		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"npcoalition\": " + coalitionNPBox.isSelected() + ", ");
		json.append("\"npgovernment\": " + governmentNPBox.isSelected() + ", ");
		json.append("\"nptaliban\": " + talibanNPBox.isSelected() + ", ");
		json.append("\"npwarlords\": " + warlordNPBox.isSelected() + ", ");
		json.append("\"propaganda\": " + propCount + ", ");
		json.append("\"action\": \"" + aiButtonName + "\", ");
		
		BotScriptEngine engine = new BotScriptEngine(mod, "A Distant Plain AI");
		String jsonString = engine.ConstructJSON(json, offboard, zones);

//		WriteLine("JSON: " + jsonString);

		engine.RunScript(jsonString, aiButtonName);
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
			return new IconConfigurer(key, name, ((DistantAIWindow) c).launch.getAttributeValueString(ICON));
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
