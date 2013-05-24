package enums;

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
}
