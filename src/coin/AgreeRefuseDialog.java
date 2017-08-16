package coin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AgreeRefuseDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	public String reply = "";
	
	public AgreeRefuseDialog(Window parent, String coinTitle, Question question) {
		super(parent, "", Dialog.ModalityType.MODELESS);
		
		String title = "COINBot – " + question.category();
		this.setTitle(title);
		this.setMinimumSize(new Dimension(300, 80));
		
		if (parent != null) {
			Dimension parentSize = parent.getSize();
			Point p = parent.getLocation(); 
			setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
		}
		JPanel messagePane = new JPanel();
		messagePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		messagePane.setLayout(new BoxLayout(messagePane, BoxLayout.PAGE_AXIS));
		messagePane.setAlignmentX(LEFT_ALIGNMENT);
		messagePane.add(new JLabel("Playing Faction: " + question.askingFaction()));
		messagePane.add(new JLabel("Responding: " + question.answeringFaction()));
		messagePane.add(new JLabel(" "));
		messagePane.add(new JLabel(question.question()));
		getContentPane().add(messagePane);
		
		JPanel buttonPane = new JPanel();
		JButton allowButton = new JButton("Agree");
		buttonPane.add(allowButton);
		allowButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				reply = "agreed";
				
				setVisible(false);
			}
		});
		JButton denyButton = new JButton("Refuse");
		buttonPane.add(denyButton); 
		denyButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				reply = "refused";
				
				setVisible(false);
			}
		});
		
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
	}
}
