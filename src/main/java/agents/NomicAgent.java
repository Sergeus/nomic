package agents;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.drools.definition.rule.Rule;

import services.NomicService;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
import actions.ProposeRuleModification;

public class NomicAgent extends AbstractParticipant {

	String ReverseOrderRule = "import agents.NomicAgent "
			+ "import facts.* "
			+ "global org.apache.log4j.Logger logger "
			+ "rule \"Whose backwards turn is it\" "
			+ "when"
			+ "	$agent : NomicAgent($ID : sequentialID)"
			+ "	$turn : Turn((2 - (number % 3)) == ($ID) && activePlayer != $agent)"
			+ "then"
			+ "	logger.info(\"It's this guy's turn: \" + $agent.getName());"
			+ "	modify ($turn) {"
			+ "		setActivePlayer($agent)"
			+ "	};"
			+ "end";
	
	private int SequentialID;
	
	NomicService nomicService;

	public NomicAgent(UUID id, String name) {
		super(id, name);
	}

	@Override
	protected void processInput(Input arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		Set<ParticipantSharedState> ss = super.getSharedState();
		ss.add(new ParticipantSharedState("test", getName(), getID()));
		return ss;
	}
	
	@Override
	public void initialise() {
		super.initialise();
		
		try {
			this.nomicService = getEnvironmentService(NomicService.class);
		} catch (UnavailableServiceException e) {
			logger.warn("Couldn't get Nomic Environment Service.", e);
		}
	}
	
	@Override
	public void incrementTime() {
		if (nomicService.isMyTurn(this) && nomicService.getTurnNumber() > 5) {
			logger.info("It's my turn!");
			
			Collection<Rule> rules= nomicService.getRules();
			
			String oldRuleName = "Whose turn is it";
			boolean success = false;
			for (Rule rule : rules) {
				if (rule.getName().compareTo(oldRuleName) == 0) {
					ProposeRuleModification ruleMod = 
							new ProposeRuleModification(this, 
									ReverseOrderRule, oldRuleName, rule.getPackageName());
					
					logger.info("Modifying turn order!");
					
					try {
						environment.act(ruleMod, getID(), authkey);
						success = true;
					} catch (ActionHandlingException e) {
						logger.info("Failed to modify rule.", e);
					}
				}
			}
			
			if (!success) {
				logger.info("No rule changes from me.");
			}
		}
		else {
			logger.info("I've decided to do nothing this turn.");
		}
		super.incrementTime();
	}
	
	public int UnitTestTest() {
		return 2;
	}
	
	public int getSequentialID() {
		return SequentialID;
	}

	public void setSequentialID(int sequentialID) {
		SequentialID = sequentialID;
	}
}
