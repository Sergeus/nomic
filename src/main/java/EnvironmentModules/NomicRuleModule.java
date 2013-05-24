/**
 * 	Copyright (C) 2011-2012 Sam Macbeth <sm1106 [at] imperial [dot] ac [dot] uk>
 *
 * 	This file is part of Presage2.
 *
 *     Presage2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Presage2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser Public License
 *     along with Presage2.  If not, see <http://www.gnu.org/licenses/>.
 */
package EnvironmentModules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.presage2.rules.Rules;
import uk.ac.imperial.presage2.rules.facts.AgentStateTranslator;
import uk.ac.imperial.presage2.rules.facts.StateTranslator;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

/**
 * Binds {@link RuleStorage} with a set of DRL files to initially load into the
 * engine as well as {@link StateTranslator}s and {@link AgentStateTranslator}s
 * to use for shared state.
 * 
 * @author Sam Macbeth
 * 
 */
public class NomicRuleModule extends AbstractModule {

	private ArrayList<String> ruleFiles = new ArrayList<String>();
	private Set<Class<? extends StateTranslator>> stateTranslators = new HashSet<Class<? extends StateTranslator>>();
	private Set<Class<? extends AgentStateTranslator>> agentStateTranslators = new HashSet<Class<? extends AgentStateTranslator>>();

	public NomicRuleModule() {
		super();
	}

	public NomicRuleModule addClasspathDrlFile(String fileName) {
		ruleFiles.add(fileName);
		return this;
	}

	public NomicRuleModule addStateTranslator(Class<? extends StateTranslator> clazz) {
		stateTranslators.add(clazz);
		return this;
	}

	public NomicRuleModule addAgentStateTranslator(
			Class<? extends AgentStateTranslator> clazz) {
		agentStateTranslators.add(clazz);
		return this;
	}

	@Override
	protected void configure() {
		bind(NomicRuleStorage.class).in(Singleton.class);
		bind(StatefulKnowledgeSession.class).toProvider(NomicRuleStorage.class);

		// bind rule files in MapBinder with key to match order in which files
		// have been added.
		MapBinder<Integer, String> rulesBinder = MapBinder.newMapBinder(
				binder(), Integer.class, String.class, Rules.class);
		for (int i = 0; i < ruleFiles.size(); i++) {
			rulesBinder.addBinding(i).toInstance(ruleFiles.get(i));
		}

		Multibinder<StateTranslator> translatorBinder = Multibinder
				.newSetBinder(binder(), StateTranslator.class);
		for (Class<? extends StateTranslator> clazz : stateTranslators) {
			translatorBinder.addBinding().to(clazz);
		}
		Multibinder<AgentStateTranslator> agentTranslatorBinder = Multibinder
				.newSetBinder(binder(), AgentStateTranslator.class);
		for (Class<? extends AgentStateTranslator> clazz : agentStateTranslators) {
			agentTranslatorBinder.addBinding().to(clazz);
		}
	}

	@Provides
	KnowledgeBase getDroolsKnowledgeBase(NomicRuleStorage rules) {
		return rules.getKbase();
	}

}