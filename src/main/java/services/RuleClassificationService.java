package services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;

import com.google.inject.Inject;

import enums.RuleFlavor;
import facts.RuleDefinition;

public class RuleClassificationService extends EnvironmentService {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private final StatefulKnowledgeSession session;
	
	private Map<String, RuleDefinition> RulePool = new HashMap<String, RuleDefinition>();
	
	@Inject
	public RuleClassificationService(EnvironmentSharedStateAccess ss,
			StatefulKnowledgeSession session) {
		super(ss);
		this.session = session;
	}
	
	public RuleFlavor getFlavor(String ruleName) {
		return RulePool.get(ruleName).getPrevailingFlavor();
	}
	
	public Map<RuleFlavor, Integer> getFlavors(String ruleName) {
		return RulePool.get(ruleName).getFlavors();
	}
	
	public Collection<RuleDefinition> getAllRulesWithFlavor(RuleFlavor flavorType) {
		Collection<RuleDefinition> rules = new ArrayList<RuleDefinition>();
		for (String name : RulePool.keySet()) {
			RuleDefinition currentRule = RulePool.get(name);
			if (currentRule.getFlavorAmount(flavorType) > 0) {
				rules.add(currentRule);
			}
		}
		
		return rules;
	}
	
	public String getRuleBody(String ruleName) {
		return RulePool.get(ruleName).getRuleContent();
	}
}
