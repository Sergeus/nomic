package services;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.drools.definition.rule.Rule;
import org.drools.runtime.StatefulKnowledgeSession;

import simulations.SubScenarioSimulation;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.core.participant.Participant;
import actions.ProposeRuleChange;
import agents.NomicAgent;
import agents.ProxyAgent;

import com.google.inject.AbstractModule;

public class ScenarioService extends EnvironmentService {
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	final private EnvironmentServiceProvider serviceProvider;
	
	private NomicService superNomicService;
	
	private NomicService subNomicService;
	
	private StatefulKnowledgeSession testSession;
	
	private SubScenarioSimulation subScenarioSimulation;
	
	NomicAgent controller;
	
	ProxyAgent avatar;

	public ScenarioService(EnvironmentSharedStateAccess ss, EnvironmentServiceProvider provider,
			Participant p) {
		super(ss);
		this.serviceProvider = provider;
		
		if (p instanceof NomicAgent) {
			controller = (NomicAgent)p;
		}
	}
	
	public NomicService getSuperNomicService() {
		if (superNomicService == null) {
			try {
				superNomicService = serviceProvider.getEnvironmentService(NomicService.class);
			} catch (UnavailableServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return superNomicService;
	}
	
	public NomicService getSubNomicService() {
		if (superNomicService == null) {
			try {
				subNomicService = subScenarioSimulation.getEnvironmentService(NomicService.class);
			} catch (UnavailableServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return subNomicService;
	}
	
	public void setController(NomicAgent controller) {
		this.controller = controller;
	}
	
	public Collection<ProxyAgent> getProxyAgents() {
		Collection<ProxyAgent> proxies = superNomicService.getProxyAgents();
		
		for (ProxyAgent proxy : proxies) {
			if (proxy.GetOwnerID() == controller.getID()) {
				avatar = proxy;
				avatar.SetAvatar(true);
			}
		}
		
		return proxies;
	}
	
	public void RunQuerySimulation(ProposeRuleChange ruleChange, int timeIntoFuture) {
		testSession = getSuperNomicService().getNewStatefulKnowledgeSession();
		
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new EventBusModule());
		
		subScenarioSimulation = new SubScenarioSimulation(modules, this, ruleChange);
		subScenarioSimulation.finishTime = timeIntoFuture;
		
		subScenarioSimulation.load();
		
		subScenarioSimulation.run();
		
//		logger.info("Super service rules.");
//		
//		for (Rule rule : getSuperNomicService().getRules()) {
//			logger.info(rule.getName());
//		}
//		
//		logger.info("Sub service rules.");
//		
//		for (Rule rule : getSubNomicService().getRules()) {
//			logger.info(rule.getName());
//		}
		
		//logger.info("Super session kbase sessions: ");
		
		testSession.dispose();
		
		getSuperNomicService().refreshSession();
		
		avatar.setPreferenceLocked(false);
		
		if (avatar.getPreference() > 100)
			avatar.setPreference(100);
		else if (avatar.getPreference() < 0)
			avatar.setPreference(0);
		
		avatar.setPreferenceLocked(true);
	}
	
	public StatefulKnowledgeSession getReplacementSession() {
		return testSession;
	}
	
	public boolean IsController(NomicAgent agent) {
		return agent.getID() == controller.getID();
	}
	
	public NomicAgent getController() {
		return controller;
	}
	
	public boolean IsWinner() {
		return getSubNomicService().isGameWon();
	}
	
	public ProxyAgent getWinner() {
		if (IsWinner()) {
			return (ProxyAgent) getSubNomicService().getWinner();
		}
		
		return null;
	}
	
	public ProxyAgent getAvatar() {
		return avatar;
	}
	
	public Integer getPreference() {
		return avatar.getPreference();
	}
	
	public Map<Integer, Integer> getPointsAtEnd() {
		return getSubNomicService().getPointsMap();
	}
}
