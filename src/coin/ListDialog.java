package coin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ListDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private String selected = "";
	public String reply = "";
	
	public ListDialog(Window parent, String coinTitle, Question question) {
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
		getContentPane().add(messagePane, BorderLayout.NORTH);
		
		JPanel choicePane = new JPanel();
		String[] opts = question.options().split(";");
		ButtonGroup group = new ButtonGroup();
		choicePane.setLayout(new GridLayout(opts.length, 1));
		for (int i = 0; i < opts.length; i++) {
			final String o = opts[i];
			JRadioButton choice = new JRadioButton(o);
			choice.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selected = o;
				}
			});
			group.add(choice);
			choicePane.add(choice);
		}
		
		getContentPane().add(choicePane, BorderLayout.CENTER);
		
		JPanel buttonPane = new JPanel();
		JButton yesButton = new JButton("Choose");
		buttonPane.add(yesButton);
		yesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reply = selected;
				
				setVisible(false);
			}
		});
		JButton noButton = new JButton("Cancel");
		buttonPane.add(noButton); 
		noButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				reply = "Cancel";
				
				setVisible(false);
			}
		});
		
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
	}
}