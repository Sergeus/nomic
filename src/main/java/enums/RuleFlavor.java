package enums;

/**
 * Flavors are represented by values between 0 and 100, where 100 is full affinity
 * with the associated property. (Eg. a rule with COMPLEX flavor of 100 is very complex.)
 * 0 Means the rule opposes that flavor and 50 means the rule is irrelevant to that flavor.
 * @author stuart
 * 
 * Need to discuss possibility of dynamic flavors in future implementations
 *
 */
public enum RuleFlavor {
	/**
	 * Involve significant computations or make the game more difficult to follow
	 */
	COMPLEX,
	/**
	 * 'Destroys' the flow of the game, making the game unplayable/unbalanced
	 */
	DESTRUCTIVE,
	/**
	 * Makes easily understood, non-complex changes
	 */
	SIMPLE,
	/**
	 * Last resort changes that can be used to swing the game/when there is nothing else good to do
	 */
	DESPERATION,
	/**
	 * Good for everyone
	 */
	BENEFICIAL,
	/**
	 * Introduces/removes some kind of win condition
	 */
	WINCONDITION,
	/**
	 * Makes the game behave in a regular manner
	 */
	STABLE,
	/**
	 * Causes agents to get farther from winning the game
	 */
	DETRIMENTAL,
}
