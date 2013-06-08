package simulations;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.drools.compiler.DroolsParserException;
import org.drools.runtime.StatefulKnowledgeSession;

import plugins.StoragePlugin;
import services.NomicService;
import services.RuleClassificationService;
import services.RuleStringRepository;
import services.ScenarioService;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.plugin.PluginModule;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;
import actionHandlers.ProposeRuleChangeActionHandler;
import actionHandlers.VoteActionHandler;
import agents.DestructiveAgent;
import agents.HarmoniousAgent;
import agents.NomicAgent;
import agents.SelfishAgent;
import agents.VindictiveAgent;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

/**
 * Default simulation for simulating a game of Nomic.
 * @author Stuart
 *
 */
public class NomicSimulation extends InjectedSimulation {
	
	StatefulKnowledgeSession session;
	
	private Logger logger = Logger.getLogger(getClass());
	
	/**
	 * Instances of <code>NomicAgent</code>, random voters, do not make proposals.
	 */
	@Parameter(name="agents")
	public int agents;
	
	/**
	 * Instances of <code>DestructiveAgent</code>, attempt to undermine the game.
	 */
	@Parameter(name="dagents")
	public int dagents;
	
	/**
	 * Instances of <code>SelfishAgent</code>, attempt to win for themselves.
	 */
	@Parameter(name="sagents")
	public int sagents;
	
	/**
	 * Instances of <code>VindictiveAgent</code>, attempt to prevent a
	 * specific other agent from succeeding at the game.
	 */
	@Parameter(name="vagents")
	public int vagents;
	
	@Parameter(name="hagents")
	public int hagents;

	public NomicSimulation(Set<AbstractModule> modules) {
		super(modules);
	}
	
	@Inject
	public void setSession(StatefulKnowledgeSession session) {
		this.session = session;
	}

	@Override
	protected void addToScenario(Scenario s) {
		// Adds the basic rules of Nomic to the simulation.
		try {
			NomicService nomicService = getEnvironmentService(NomicService.class);
			nomicService.AddRuleFile("src/main/resources/Basic.dslr");
		} catch (UnavailableServiceException e) {
			logger.warn("All is lost", e);
		} catch (DroolsParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Sets relevant rule globals.
		session.setGlobal("logger", this.logger);
		session.setGlobal("storage", this.storage);
		
		// Instantiate agents
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
		
		for (int i=0; i < vagents; i++) {
			VindictiveAgent agent = new VindictiveAgent(Random.randomUUID(), "agent" + id);
			
			agent.setSequentialID(id);
			
			s.addParticipant(agent);
			session.insert(agent);
			id++;
		}
		
		for (int i=0; i < hagents; i++) {
			HarmoniousAgent agent = new HarmoniousAgent(Random.randomUUID(), "agent" + id);
			
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
				.addParticipantGlobalEnvironmentService(RuleClassificationService.class)
				.addParticipantEnvironmentService(ScenarioService.class)
				.addActionHandler(ProposeRuleChangeActionHandler.class)
				.addActionHandler(VoteActionHandler.class)
				.setStorage(RuleStorage.class));
		
		modules.add(new PluginModule()
				.addPlugin(StoragePlugin.class));
		
		modules.add(new RuleModule());
		
		modules.add(NetworkModule.noNetworkModule());
		
		return modules;
	}
	
	public <T extends EnvironmentService> T getEnvironmentService(Class<T> serviceType) 
			throws UnavailableServiceException {
		AbstractEnvironment env = (AbstractEnvironment) getScenario().getEnvironment();
		
		return env.getEnvironmentService(serviceType);
	}
}
