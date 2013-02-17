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
import actions.Vote;

import com.google.inject.Inject;

public class VoteActionHandler implements ActionHandler {

	final StatefulKnowledgeSession session;
	private final Logger logger = Logger.getLogger(RuleChangeActionHandler.class);
	final EnvironmentServiceProvider serviceProvider;
	
	NomicService nomicService;
	
	@Inject
	public VoteActionHandler(StatefulKnowledgeSession session,
			EnvironmentServiceProvider serviceProvider) {
		super();
		this.session = session;
		this.serviceProvider = serviceProvider;
	}
	
	public NomicService getNomicService() {
		if (nomicService == null) {
			try {
				nomicService = serviceProvider.getEnvironmentService(NomicService.class);
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
		// TODO Auto-generated method stub
		return null;
	}

}