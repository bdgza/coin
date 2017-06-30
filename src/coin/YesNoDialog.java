package coin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class YesNoDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	public String reply = "";
	
	public YesNoDialog(Window parent, String coinTitle, Question question) {
		super(parent, "", Dialog.ModalityType.MODELESS);
		
		String title = coinTitle + " – " + question.faction();
		this.setTitle(title);
		this.setMinimumSize(new Dimension(300, 80));
		
		if (parent != null) {
			Dimension parentSize = parent.getSize();
			Point p = parent.getLocation(); 
			setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
		}
		JPanel messagePane = new JPanel();
		messagePane.add(new JLabel(question.question()));
		getContentPane().add(messagePane);
		
		JPanel buttonPane = new JPanel();
		JButton yesButton = new JButton("Yes");
		buttonPane.add(yesButton);
		yesButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				reply = "Yes";
				
				setVisible(false);
			}
		});
		JButton noButton = new JButton("No");
		buttonPane.add(noButton); 
		noButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				reply = "No";
				
				setVisible(false);
			}
		});
		
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
	}
}
