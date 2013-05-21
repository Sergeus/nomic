package simulations;

import java.util.HashSet;
import java.util.Set;

import org.drools.runtime.StatefulKnowledgeSession;

import services.NomicService;
import services.ScenarioService;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
import uk.ac.imperial.presage2.util.network.NetworkModule;
import actionHandlers.ProposeRuleChangeActionHandler;
import actionHandlers.VoteActionHandler;
import agents.ProxyAgent;
import agents.Test;

import com.google.inject.AbstractModule;

public class SubScenarioSimulation extends NomicSimulation {
	
	ScenarioService scenarioService;
	
	public SubScenarioSimulation(Set<AbstractModule> modules, ScenarioService scenarioService) {
		super(modules);
		this.scenarioService = scenarioService;
	}

	@Override
	protected void addToScenario(Scenario s) {
		for (ProxyAgent proxy : scenarioService.getProxyAgents()) {
			s.addParticipant(proxy);
			scenarioService.getReplacementSession().insert(proxy);
		}
	}
	
	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new AbstractEnvironmentModule()
				.addParticipantGlobalEnvironmentService(NomicService.class)
				.addParticipantEnvironmentService(ScenarioService.class)
				.addActionHandler(ProposeRuleChangeActionHandler.class)
				.addActionHandler(VoteActionHandler.class)
				.setStorage(RuleStorage.class));
		
		modules.add(new RuleModule());
		
		modules.add(NetworkModule.noNetworkModule());
		
		return modules;
	}
	
	@Override
	@EventListener
	public void onNewTimeCycle(EndOfTimeCycle e) {
		session.insert(new Test());
		super.onNewTimeCycle(e);
	}
	
	public void OverrideKnowledgeSession(StatefulKnowledgeSession session) {
		this.session = session;
		
		try {
			AbstractEnvironment env = (AbstractEnvironment) getScenario().getEnvironment();
			NomicService nomicService = env.getEnvironmentService(NomicService.class);
			nomicService.OverrideKnowledgeSession(session);
		} catch (UnavailableServiceException e) {
			logger.warn("Unable to get nomic service for subsimulation.", e);
		}
	}
}
