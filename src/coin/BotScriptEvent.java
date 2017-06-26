package coin;

import java.util.EventObject;

public class BotScriptEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	public static final String EVENTTYPE_QUESTION = "QUESTION";
	
	private String _eventType;
	private Object _eventData;
	
	public BotScriptEvent(Object source, String eventType, Object eventData) {
		super(source);
		this._eventType = eventType;
		this._eventData = eventData;
	}

	public String eventType() {
		return _eventType;
	}
	
	public Object eventData() {
		return _eventData;
	}
}
