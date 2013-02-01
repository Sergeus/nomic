package services;

import java.io.StringReader;
import java.util.Collection;

import javax.naming.spi.DirStateFactory.Result;

import org.apache.log4j.Logger;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.DroolsParserException;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.QueryResults;

import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import agents.NomicAgent;

import com.google.inject.Inject;

import enums.TurnType;
import facts.Turn;

public class NomicService extends EnvironmentService {

	private final Logger logger = Logger.getLogger(this.getClass());
	
	StatefulKnowledgeSession session;
	
	TurnType Turn = enums.TurnType.INIT;
	int TurnNumber = 0;
	
	Turn CurrentTurn;
	
	@Inject
	public NomicService(EnvironmentSharedStateAccess sharedState,
			StatefulKnowledgeSession session, EventBus e) {
		super(sharedState);
		this.session = session;
		e.subscribe(this);
	}
	
	@EventListener
	public void onIncrementTime(EndOfTimeCycle e) {
		if (Turn != TurnType.PROPOSE) {
			Turn = TurnType.PROPOSE;
		}
		CurrentTurn = new Turn(++TurnNumber, Turn);
		session.insert(CurrentTurn);
		logger.info("Next turn: " + TurnNumber + ", " + Turn);
	}
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		super.registerParticipant(req);
	}
	
	public boolean isMyTurn(NomicAgent agent) {
		return CurrentTurn.ActivePlayer == agent;
	}
	
	public QueryResults query(String query, NomicAgent agent) {
		return session.getQueryResults(query, agent);
	}
	
	public void RemoveRule(String packageName, String ruleName) {
		session.getKnowledgeBase().removeRule(packageName, ruleName);
	}
	
	public void addRule(Collection<String> imports, String ruleName,
			Collection<String> conditions, Collection<String> actions)
					throws DroolsParserException {
		String rule = "";
		
		for(String importe : imports) {
			rule += "import " + importe + " ";
		}
		
		rule += "rule \"" + ruleName + "\" ";
		
		rule += "when ";
		
		for (String condition : conditions) {
			rule += condition + " ";
		}
		
		rule += "then ";
		
		for (String action : actions) {
			rule += action + " ";
		}
		
		rule += "end";
		
		addRule(rule);
	}
	
	public void addRule(String rule) throws DroolsParserException {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		Resource myResource = ResourceFactory.newReaderResource(new StringReader(rule));
		kbuilder.add(myResource, ResourceType.DRL);
		
		if (kbuilder.hasErrors()) {
			throw new DroolsParserException("Unable to parse new rule.\n"
					+ kbuilder.getErrors().toString());
		}
		
		Collection<KnowledgePackage> packages = kbuilder.getKnowledgePackages();
		
		session.getKnowledgeBase().addKnowledgePackages(packages);
	}
}
