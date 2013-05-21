package services;

import java.util.ArrayList;
import java.util.Collection;

import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.participant.Participant;
import actions.ProposeRuleChange;
import agents.NomicAgent;
import agents.ProxyAgent;

public class ScenarioService extends EnvironmentService {
	
	final private EnvironmentServiceProvider serviceProvider;
	
	private NomicService nomicService;
	
	private StatefulKnowledgeSession testSession;
	
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
	
	public void RunQuerySimulation(ProposeRuleChange ruleChange) {
		testSession = nomicService.getNewStatefulKnowledgeSession();
		
		
	}
}
