package org.qianqianzhu.instrument.mutation;

public class MutationObserver {

	/** Constant <code>activeMutation=-1</code> */
	public static int activeMutation = -1;
	
	/**
	 * <p>activateMutation</p>
	 *
	 * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public static void activateMutation(Mutation mutation) {
		if (mutation != null){
			//System.out.println("Activate: m"+mutation.getId());
			activeMutation = mutation.getId();
		}
		else{
			System.out.println("Error: mutation is null");
		}
	}

	/**
	 * <p>activateMutation</p>
	 *
	 * @param id a int.
	 */
	public static void activateMutation(int id) {
		activeMutation = id;
	}

	/**
	 * <p>deactivateMutation</p>
	 */
	public static void deactivateMutation() {
		activeMutation = -1;
	}

	/**
	 * <p>deactivateMutation</p>
	 *
	 * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public static void deactivateMutation(Mutation mutation) {
		activeMutation = -1;
	}
}
