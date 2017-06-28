package coin;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

import javax.swing.*;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GameState;
import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;
import VASSAL.command.Command;
import VASSAL.counters.*;
import VASSAL.counters.Stack;
import VASSAL.tools.FormattedString;

public abstract class AIWindow extends JDialog implements ActionListener, IAIWindow {
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
		
	public void actionPerformed(ActionEvent e) {
		
	}
	
	protected abstract void initGameComponents();
	public abstract String getModuleTitle();
	
	protected void WriteLine(String msgLine) {
		FormattedString cStr = new FormattedString(" - <COINBot> - " + msgLine);
		final Command cc = new Chatter.DisplayText(mod.getChatter(), cStr.getLocalizedText());
		cc.execute();
		mod.sendAndLog(cc);
	}

	public void initComponents(BotPackage bot) {
		getContentPane().removeAll();
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		
		setFont(new Font("SansSerif", Font.PLAIN, 12));
		setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
		
        makeButton("Game State", gridbag, c);
		
		c.weightx = 0.0;
		c.gridwidth = 1;
		
		try {
			JCheckBox aeduiNPBox = makeCheckBox("a", gridbag, c);
			JCheckBox arverniNPBox = makeCheckBox("b", gridbag, c);
			JCheckBox belgicNPBox = makeCheckBox("c", gridbag, c);
			
			c.gridwidth = GridBagConstraints.REMAINDER;
			
			JCheckBox romanNPBox = makeCheckBox("d", gridbag, c);
			
			c.weightx = 0.0;
			c.gridwidth = 1;
			
			JPanel[] buttonPanels = new JPanel[4];
			
			for (int i = 0; i < 4; i++) {
				buttonPanels[i] = new JPanel();
				buttonPanels[i].setLayout(new BoxLayout(buttonPanels[i], BoxLayout.Y_AXIS));
				if (i == 3)
					c.gridwidth = GridBagConstraints.REMAINDER;
				gridbag.setConstraints(buttonPanels[i], c);
				add(buttonPanels[i]);
			}
			
			// TODO: botCombo.addActionListener(this);
			
			// TODO: actionPerformed(null);
		}
		catch (Exception ex) {
			WriteLine("ERROR! " + ex.getMessage());
		}
		
		// game specific override
		
		initGameComponents();
		
		// location
		
		int x = getX();
		int y = getY();
		x = x + ((getWidth() - 520) / 2);
		y = y + ((getHeight() - 135) / 2);
		if (x < 15)
			x = 15;
		if (y < 15)
			y = 15;
		setSize(520, getHeight()); // TODO: was 110 for 1 row of buttons!
		setLocation(x, y);
		
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
	
	private void aiButtonActionPerformed(ActionEvent e) {
		String aiButtonName = ((JButton)e.getSource()).getText();
		int winterCount = 0;

		ArrayList<ZoneIndex> zones = new ArrayList<ZoneIndex>();

		GameState myGameState = mod.getGameState();

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
//		json.append("{");
//		json.append("\"npaedui\": " + aeduiNPBox.isSelected() + ", ");
//		json.append("\"nparverni\": " + arverniNPBox.isSelected() + ", ");
//		json.append("\"nproman\": " + romanNPBox.isSelected() + ", ");
//		json.append("\"npbelgic\": " + belgicNPBox.isSelected() + ", ");
//		json.append("\"winter\": " + winterCount + ", ");
//		json.append("\"action\": \"" + aiButtonName + "\", ");
//		
//		String jsonString = _botScriptEngine.ConstructJSON(json, offboard, zones);
//
//		_botScriptEngine.setVerbose((e.getModifiers() & InputEvent.SHIFT_MASK) != 0);
//		_botScriptEngine.RunScript(jsonString, aiButtonName);
	}
}
