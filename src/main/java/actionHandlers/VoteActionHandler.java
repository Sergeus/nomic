package actionHandlers;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import services.NomicService;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import actions.TimeStampedAction;
import actions.Vote;

import com.google.inject.Inject;

public class VoteActionHandler implements ActionHandler {

	StatefulKnowledgeSession session;
	private final Logger logger = Logger.getLogger(ProposeRuleChangeActionHandler.class);
	final EnvironmentServiceProvider serviceProvider;
	
	NomicService nomicService;
	
	@Inject
	public VoteActionHandler(EnvironmentServiceProvider serviceProvider) {
		super();
		this.serviceProvider = serviceProvider;
	}
	
	public NomicService getNomicService() {
		if (nomicService == null) {
			try {
				nomicService = serviceProvider.getEnvironmentService(NomicService.class);
				session = nomicService.getActiveStatefulKnowledgeSession();
			} catch (UnavailableServiceException e) {
				logger.warn("Unable to get NomicService.");
			}
		}
		return nomicService;
	}
	
	@Override
	public boolean canHandle(Action action) {
		return action instanceof Vote;
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		logger.info("Handling action " + action);
		
		NomicService service = getNomicService();
		
		if (action instanceof TimeStampedAction) {
			((TimeStampedAction) action).setT(service.getTurnNumber());
		}
		
		try {
			Vote vote = (Vote)action;
			service.Vote(vote);
			session.insert(vote);
		} catch(ClassCastException e) {
			throw new ActionHandlingException("Supplied action is the wrong class." + e.getMessage());
		}
		
		return null;
	}

}
