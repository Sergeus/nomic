package services;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.drools.runtime.StatefulKnowledgeSession;

import simulations.SubScenarioSimulation;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.core.participant.Participant;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import actions.ProposeRuleChange;
import agents.NomicAgent;
import agents.ProxyAgent;

import com.google.inject.AbstractModule;

public class ScenarioService extends EnvironmentService {
	
	final private EnvironmentServiceProvider serviceProvider;
	
	private NomicService nomicService;
	
	private StatefulKnowledgeSession testSession;
	
	private SubScenarioSimulation subScenarioSimulation;
	
	NomicAgent controller;

	public ScenarioService(EnvironmentSharedStateAccess ss, EnvironmentServiceProvider provider,
			Participant p) {
		super(ss);
		this.serviceProvider = provider;
		
		if (p instanceof NomicAgent) {
			controller = (NomicAgent)p;
		}
	}
	
	public NomicService getNomicService() {
		if (nomicService == null) {
			try {
				nomicService = serviceProvider.getEnvironmentService(NomicService.class);
			} catch (UnavailableServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return nomicService;
	}
	
	public void setController(NomicAgent controller) {
		this.controller = controller;
	}
	
	public Collection<ProxyAgent> getProxyAgents() {
		return nomicService.getProxyAgents();
	}
	
	public void RunQuerySimulation(ProposeRuleChange ruleChange, int timeIntoFuture) {
		testSession = getNomicService().getNewStatefulKnowledgeSession();
		
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new EventBusModule());
		
		subScenarioSimulation = new SubScenarioSimulation(modules, this);
		subScenarioSimulation.finishTime = timeIntoFuture;
		
		subScenarioSimulation.load();
		
		subScenarioSimulation.run();
	}
	
	public StatefulKnowledgeSession getReplacementSession() {
		return testSession;
	}
}
