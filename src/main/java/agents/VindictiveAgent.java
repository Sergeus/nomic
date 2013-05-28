package agents;

import java.util.UUID;

import actions.ProposeRuleChange;
import enums.VoteType;

public class VindictiveAgent extends NomicAgent {
	
	String nemesisName;

	public VindictiveAgent(UUID id, String name) {
		super(id, name);
	}
	
	public void ChooseNemesis() {
		
	}
	
	@Override
	public VoteType chooseVote(ProposeRuleChange ruleChange) {
		scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
		
		logger.info("This vindictive subsimulation had a preference of: " + scenarioService.getPreference());
		
		return chooseVoteFromProbability(scenarioService.getPreference());
	}
	
	@Override
	public String getProxyRulesFile() {
		return "src/main/resources/VindictiveProxy.drl";
	}
	
	@Override
	public ProxyAgent getRepresentativeProxy() {
		VindictiveProxyAgent proxy = new VindictiveProxyAgent(uk.ac.imperial.presage2.core.util.random.Random.randomUUID(), 
				"proxy " + getName());
		proxy.setOwner(this);
		proxy.setPoints(getPoints());
		proxy.setSequentialID(getSequentialID());
		proxy.setNemesisName(nemesisName);
		
		return proxy;
	}
	
	@Override
	public String getAgentType() {
		return "VindictiveAgent";
	}

}
