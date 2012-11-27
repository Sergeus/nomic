package simulations;

import java.util.HashSet;
import java.util.Set;

import agents.NomicAgent;

import com.google.inject.AbstractModule;

import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;

public class BasicSimulation extends InjectedSimulation {
	
	@Parameter(name="agents")
	public int agents;

	public BasicSimulation(Set<AbstractModule> modules) {
		super(modules);
	}

	@Override
	protected void addToScenario(Scenario s) {
		for (int i=0; i < agents; i++) {
			s.addParticipant(new NomicAgent(Random.randomUUID(), "agent" + i));
		}
	}

	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new AbstractEnvironmentModule());
		
		modules.add(NetworkModule.noNetworkModule());
		
		return modules;
	}

}
