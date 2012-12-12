package server.trigger;

import server.helpers.PoBox;

public interface ITrigger {

	PoBox executePreCommand(PoBox poBox, String config);
	PoBox executePostCommand(PoBox poBox, String config);
	
	
}
