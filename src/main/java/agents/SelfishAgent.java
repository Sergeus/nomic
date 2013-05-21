package agents;

import java.util.UUID;

import org.drools.runtime.StatefulKnowledgeSession;

public class SelfishAgent extends NomicAgent {
	
	StatefulKnowledgeSession experimentSession;

	public SelfishAgent(UUID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void incrementTime() {
		experimentSession = nomicService.getNewStatefulKnowledgeSession();
		
		logger.info("Alternate knowledge session exists here.");
		
		experimentSession.insert(this);
		
		experimentSession.insert(new Test());
		
		experimentSession.fireAllRules();
		
		experimentSession.dispose();
		
		super.incrementTime();
	}
}
