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
}
