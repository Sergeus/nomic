package simulations;

import java.util.Set;

import agents.NomicAgent;
import agents.ProxyAgent;

import com.google.inject.AbstractModule;

import services.ScenarioService;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Scenario;

public class SubScenarioSimulation extends InjectedSimulation {
	
	ScenarioService scenarioService;
	
	public SubScenarioSimulation(Set<AbstractModule> modules) {
		super(modules);
	}

	@Override
	protected void addToScenario(Scenario s) {
		for (ProxyAgent proxy : scenarioService.getProxyAgents()) {
			s.addParticipant(proxy);
		}
	}

	@Override
	protected Set<AbstractModule> getModules() {
		// TODO Auto-generated method stub
		return null;
	}

}
