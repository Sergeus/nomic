package simulations;

import java.util.HashSet;
import java.util.Set;

import org.drools.runtime.StatefulKnowledgeSession;

import plugins.StoragePlugin;
import services.NomicService;
import services.ScenarioService;
import uk.ac.imperial.presage2.core.plugin.PluginModule;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;
import EnvironmentModules.NomicRuleModule;
import EnvironmentModules.NomicRuleStorage;
import actionHandlers.ProposeRuleChangeActionHandler;
import actionHandlers.VoteActionHandler;
import agents.DestructiveAgent;
import agents.NomicAgent;
import agents.SelfishAgent;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class NomicSimulation extends InjectedSimulation {
	
	StatefulKnowledgeSession session;
	
	@Parameter(name="agents")
	public int agents;
	
	@Parameter(name="dagents")
	public int dagents;
	
	@Parameter(name="sagents")
	public int sagents;

	public NomicSimulation(Set<AbstractModule> modules) {
		super(modules);
	}
	
	@Inject
	public void setSession(StatefulKnowledgeSession session) {
		this.session = session;
	}

	@Override
	protected void addToScenario(Scenario s) {
		session.setGlobal("logger", this.logger);
		session.setGlobal("storage", this.storage);
		
		int id = 0;
		
		for (int i=0; i < agents; i++) {
			NomicAgent agent = new NomicAgent(Random.randomUUID(), "agent" + id);
			
			agent.setSequentialID(id);
			
			s.addParticipant(agent);
			session.insert(agent);
			id++;
		}
		
		for (int i=0; i < dagents; i++) {
			DestructiveAgent agent = new DestructiveAgent(Random.randomUUID(), "agent" + id);
			
			agent.setSequentialID(id);
			
			s.addParticipant(agent);
			session.insert(agent);
			id++;
		}
		
		for (int i=0; i < sagents; i++) {
			SelfishAgent agent = new SelfishAgent(Random.randomUUID(), "agent" + id);
			
			agent.setSequentialID(id);
			
			s.addParticipant(agent);
			session.insert(agent);
			id++;
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
				.setStorage(NomicRuleStorage.class));
		
		modules.add(new PluginModule()
				.addPlugin(StoragePlugin.class));
		
		modules.add(new NomicRuleModule().addClasspathDrlFile("Basic.dslr"));
		
		modules.add(NetworkModule.noNetworkModule());
		
		return modules;
	}
}
