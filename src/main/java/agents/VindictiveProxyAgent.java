package agents;

import java.util.UUID;

public class VindictiveProxyAgent extends ProxyAgent {

	String NemesisName;
	
	public VindictiveProxyAgent(UUID id, String name) {
		super(id, name);
	}

	public String getNemesisName() {
		return NemesisName;
	}

	public void setNemesisName(String nemesisName) {
		NemesisName = nemesisName;
	}
}
