package coin.adp;

import coin.*;

public class ADPAIWindow extends AIWindow {
	private static final long serialVersionUID = 1L;

	@Override
	protected void initComponents(BotPackage bot) {
		this.bot = bot;
		
		
	}

	@Override
	public String getModuleTitle() {
		return "A Distant Plain";
	}

}
