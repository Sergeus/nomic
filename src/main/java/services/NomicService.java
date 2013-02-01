package services;

import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventBus;

import com.google.inject.Inject;

public class NomicService extends EnvironmentService {

	StatefulKnowledgeSession session;
	
	@Inject
	protected NomicService(EnvironmentSharedStateAccess sharedState,
			StatefulKnowledgeSession session, EventBus e) {
		super(sharedState);
		
		this.session = session;
		e.subscribe(this);
	}
}
