package actionHandlers;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.compiler.DroolsParserException;
import org.drools.runtime.StatefulKnowledgeSession;

import services.NomicService;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import Exceptions.InvalidRuleProposalException;
import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import actions.TimeStampedAction;

import com.google.inject.Inject;

import enums.RuleChangeType;

public class RuleChangeActionHandler implements ActionHandler {
	
	final StatefulKnowledgeSession session;
	private final Logger logger = Logger.getLogger(RuleChangeActionHandler.class);
	final EnvironmentServiceProvider serviceProvider;
	
	NomicService nomicService;
	
	@Inject
	public RuleChangeActionHandler(StatefulKnowledgeSession session,
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
		return action instanceof ProposeRuleChange;
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		
		logger.info("Handling action: " + action);
		
		NomicService service = getNomicService();
		
		if (action instanceof TimeStampedAction) {
			((TimeStampedAction) action).setT(service.getTurnNumber());
		}
		
		try {
			service.ProposeRuleChange((ProposeRuleChange)action);
			
			session.insert(action);
		} catch(ClassCastException e) {
			throw new ActionHandlingException("Supplied action is the wrong class." + e.getMessage());
		} catch(InvalidRuleProposalException e) {
			throw new ActionHandlingException("It is not time to propose rule changes now.\n" 
					+ e.getMessage());
		}
		return null;
	}

}
