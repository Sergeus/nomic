package simulations;

import java.util.HashSet;
import java.util.Set;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.io.ResourceFactory;
import org.drools.rule.Rule;
import org.drools.runtime.StatefulKnowledgeSession;

import agents.NomicAgent;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;

public class BasicSimulation extends InjectedSimulation {
	
	StatefulKnowledgeSession session;
	
	@Parameter(name="agents")
	public int agents;

	public BasicSimulation(Set<AbstractModule> modules) {
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
		
		for (int i=0; i < agents; i++) {
			NomicAgent agent = new NomicAgent(Random.randomUUID(), "agent" + i);
			
			agent.setSequentialID(i);
			
			s.addParticipant(agent);
			session.insert(agent);
		}
	}

	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new AbstractEnvironmentModule()
				.setStorage(RuleStorage.class));
		
		modules.add(new RuleModule().addClasspathDrlFile("Basic.dslr"));
		
		modules.add(NetworkModule.noNetworkModule());
		
		return modules;
	}

}
