import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

/**
 * @author <a href="mailto:zmm2962@rit.edu" target="_top">Zachary Migliorini</a>
 */
public class DarwinSim {
	 
	static final String MENU = "===============MENU============\n" +
			"1 ) Starting Stats\n" +
			"2 ) Display Individuals and Points\n" +
			"3 ) Display Sorted\n" +
			"4 ) Have 1000 interactions\n" +
			"5 ) Have 10000 interactions\n" +
			"6 ) Have N interactions\n" +
			"7 ) Step through interactions \"Stop\" to quit\n" +
			"8 ) Quit\n" +
			"================================\n" +
			">";
	
	static final String USAGE = "Usage: ./project02 popSize [percentHawks] [resourceAmt] [costHawk-Hawk]";
	static int totalInteractions;
	
	
	/**
	 * Main method
	 */
	public static void main(String[] args) {
		//Set-up 
		Scanner scanner = new Scanner(System.in);
		int percentHawks = 20, percentDoves = 80, resourceAmt = 50, fightCost = 100, n = 0;
		ArrayList<Bird> theBirds;
		
		totalInteractions = 0;
		
		//Confirm valid args length
		if (args.length > 4 | args.length < 1) {
			System.err.println(USAGE);
		}
		
		//Update what values have been provided
		try {
			switch(args.length) {
			case 4:	
				fightCost = Integer.parseInt(args[3]);
			case 3:	
				resourceAmt = Integer.parseInt(args[2]);
			case 2: 
				percentHawks = Integer.parseInt(args[1]);
				percentDoves = 100 - percentHawks;
			case 1:	
				n = Integer.parseInt(args[0]);
				break;
			default:
				System.err.printf("%s%n", "Unexpected error encountered.");
				break;
			}

		} catch (NumberFormatException e) {
			//Catch any mis-formatted arguments
			System.err.println(USAGE);
		}
		
		int numHawks = n * percentHawks / 100;
		int numDoves = n * percentDoves / 100;
		
		theBirds = new ArrayList<Bird>();
		
		//Construct our list of individuals
		for (int i = 0; i < numHawks + numDoves; i++) {
			theBirds.add( i < numDoves ? new Bird(i, false) : new Bird(i, true) );
		}
		
		
		//Main loop
		while (true) {
			System.out.printf("%s", MENU);
			
			switch(scanner.nextInt()) {
			case 1:
				printStatistics(n, percentHawks, numHawks, percentDoves, numDoves, resourceAmt, fightCost);
				break;
				
			case 2:
				int deadCounter = 0;
				for (int i = 0; i < theBirds.size(); i++) {
					Bird theBird = theBirds.get(i);
					String typeOfBird = ( theBird.isDead() ? "DEAD" : (theBird.isHawk() ? "Hawk" : "Dove") );
					if (theBird.isDead()) 
						++deadCounter;
					System.out.printf("%s%d%s%d%n", "Individual[", i, "]=" + typeOfBird + ":", theBird.getResources());
				}
				System.out.printf("%s%d%n", "Living: ", theBirds.size()-deadCounter);
				break;
				
			case 3:
				ArrayList<Bird> theBirdsToSort = new ArrayList<Bird>(theBirds);
				Collections.sort(theBirdsToSort);
				Collections.reverse(theBirdsToSort);
				for (Bird theBird : theBirdsToSort) {
					String typeOfBird = ( theBird.isDead() ? "DEAD" : (theBird.isHawk() ? "Hawk" : "Dove") );						
					System.out.printf("%s%d%n", typeOfBird + ":", theBird.getResources());
				}
				break;
				
			case 4:
				simulationRun(1000, theBirds, resourceAmt, fightCost, scanner, false);
				break;
			case 5:
				simulationRun(10000, theBirds, resourceAmt, fightCost, scanner, false);
				break;
			case 6:
				System.out.printf("%s%n", "Enter the number of interactions");
				simulationRun(scanner.nextInt(), theBirds, resourceAmt, fightCost, scanner, false);
				break;
			case 7:
				simulationRun(0, theBirds, resourceAmt, fightCost, scanner, true);
				break;
			case 8: 
				System.exit(0);
			default:
				break;
			}
		}
	}
	
	/**
	 * Runs the interaction simulation.
	 * @param numInteractions the number of interactions to run
	 * @param theBirds list of birds
	 * @param resourceAmt amount per resource
	 * @param fightCost cost of a fight
	 * @param scanner the Scanner object
	 * @param stepMode flag to control whether the sim will run until
	 * 	the user 'stop's it, or until it hits numInteractions. 
	 */
	public static void simulationRun(int numInteractions, ArrayList<Bird> theBirds, int resourceAmt, int fightCost, Scanner scanner, boolean stepMode) {
		if (stepMode) {
			String temp = scanner.nextLine();
			while (!temp.equalsIgnoreCase("stop")) {
				
				if (!existsLivingBirds(theBirds)) {
					System.err.printf("%s%n", "Less than two living birds remaining, simulation cannot continue.");
					break;
				}
				
				interaction(getRandomBirds(theBirds), resourceAmt, fightCost);
				temp = scanner.nextLine();
			}
		
		} else {
			for (int i = 0; i < numInteractions; i++) {
				
				if (!existsLivingBirds(theBirds)) {
					System.err.printf("%s%n", "Less than two living birds remaining, simulation cannot continue.");
					break;
				}
				interaction(getRandomBirds(theBirds), resourceAmt, fightCost);
				System.out.println();
			}
		}
		
	}
	
	/**
	 * Checks for the existence of two or more living birds
	 * @param theBirds the list of birds
	 * @return true if more than 1 alive, else false
	 */
	public static boolean existsLivingBirds(ArrayList<Bird> theBirds) {
		int aliveCounter = 0;
		for (Bird bird : theBirds) {
			if (!bird.isDead())
				++aliveCounter;
			if (aliveCounter > 1) 
				return true;
		}
		return false;
	}
	
	/**
	 * Randomly picks two different, living birds
	 * @param theBirds the list of birds
	 * @return a two-Bird array
	 */
	public static Bird[] getRandomBirds(ArrayList<Bird> theBirds) {
		Random rand = new Random(System.currentTimeMillis() + totalInteractions);
		
		//Ensure we are picking different, living birds
		int firstBirdIndex = rand.nextInt(theBirds.size());
		while (theBirds.get(firstBirdIndex).isDead()) 
			firstBirdIndex = rand.nextInt(theBirds.size());
		Bird firstBird = theBirds.remove(firstBirdIndex);
		
		int secondBirdIndex = rand.nextInt(theBirds.size());
		while (theBirds.get(secondBirdIndex).isDead()) 
			secondBirdIndex = rand.nextInt(theBirds.size());
		Bird secondBird = theBirds.get(secondBirdIndex);
		
		//Re-add the bird back into the list
		theBirds.add(firstBirdIndex, firstBird);
		
		return new Bird[] {firstBird, secondBird};
	}
	
	/**
	 * Runs a single interaction between two birds, with the given 
	 * 	resource amount and cost of a fight
	 * @param theBirds the list of birds
	 * @param resourceAmt amount per resource
	 * @param fightCost the cost of a fight
	 */
	public static void interaction(Bird[] theBirds, int resourceAmt, int fightCost) {
		++totalInteractions;
		
		String firstBirdType = theBirds[0].isHawk() ? "Hawk" : "Dove";
		String secondBirdType = theBirds[1].isHawk() ? "Hawk" : "Dove";
		
		int firstBirdResChange = 0, secondBirdResChange = 0;
		
		if (theBirds[0].isHawk()) {
			if (theBirds[1].isHawk()) {
				firstBirdResChange = (resourceAmt-fightCost);
				secondBirdResChange = (-fightCost);
			} else {
				firstBirdResChange = (resourceAmt);
			}
		} else {
			if (theBirds[1].isHawk()) {
				secondBirdResChange = (resourceAmt);
			} else {
				firstBirdResChange = (resourceAmt/2);
				secondBirdResChange = (resourceAmt/2);
			}
		}
		
		theBirds[0].updateResources(firstBirdResChange);
		theBirds[1].updateResources(secondBirdResChange);
		
		//Print the summary
		System.out.printf("%s%d%n", "Encounter: ", totalInteractions);
		System.out.printf("%s%d%s%n", "Individual ", theBirds[0].getId(), ":" + firstBirdType);
		System.out.printf("%s%d%s%n", "Individual ", theBirds[1].getId(), ":" + secondBirdType);
		
		System.out.printf("%s", firstBirdType + "/" + secondBirdType + ": ");
		System.out.printf("%s%d%s", firstBirdType + ": ".concat(firstBirdResChange > 0 ? "+" : ""), firstBirdResChange, "    ");
		System.out.printf("%s%d%n", secondBirdType + ": ".concat(secondBirdResChange > 0 ? "+" : ""), secondBirdResChange);
		
		if(theBirds[0].isDead()) 
			System.out.printf("%s%n", "Hawk one has died!");
		if(theBirds[1].isDead())
			System.out.printf("%s%n", "Hawk two has died!");
		
		System.out.printf("%s%d%s%d%s", "Individual ", theBirds[0].getId(), "=", theBirds[0].getResources(), "        ");
		System.out.printf("%s%d%s%d%n", "Individual ", theBirds[1].getId(), "=", theBirds[1].getResources());
		
		
	}
	
	/**
	 * Simple auxiliary function to print the starting stats of the program
	 * @param n num of individuals
	 * @param percentHawks % hawks
	 * @param numHawks # hawks
	 * @param percentDoves % doves
	 * @param numDoves # doves
	 * @param resourceAmt amount per resource
	 * @param fightCost the cost of a fight
	 */
	public static void printStatistics(int n, int percentHawks, int numHawks, int percentDoves, int numDoves, int resourceAmt, int fightCost) {
		System.out.printf("%s%d%n", "Population size: ", n);
		System.out.printf("%s%d%s%n", "Percentage of Hawks: ", percentHawks, "%");
		System.out.printf("%s%d%n%n", "Number of Hawks: ", numHawks);

		System.out.printf("%s%d%s%n", "Percentage of Doves: ", percentDoves, "%");
		System.out.printf("%s%d%n%n", "Number of Doves: ", numDoves);

		System.out.printf("%s%d%n", "Each resource is worth: ", resourceAmt);
		System.out.printf("%s%d%n%n", "Cost of Hawk-Hawk interaction: ", fightCost);
	}
	
	
	/**
	 * Bird class to hold the information pertaining to a single individual
	 * @author <a href="mailto:zmm2962@rit.edu" target="_top">Zachary Migliorini</a>
	 */
	private static class Bird implements Comparable<Bird> {
		
		private int id;
		private int resources;
		private boolean isHawk;
		
		public Bird(int id, boolean isHawk) {
			this.id = id;
			this.resources = 0;
			this.isHawk = isHawk;
		}
		
		public int getId() {
			return this.id;
		}
		
		public int getResources() {
			return this.resources;
		}

		/**
		 * Changes the resources held by the individual
		 * @param change the change
		 */
		public void updateResources(int change) {
			this.resources += change;
		}
		
		public boolean isDead() {
			if (this.resources < 0) {
				return true;
			}
			return false;
		}
	
		public boolean isHawk() {
			return this.isHawk;
		}
		
		@Override
		public int compareTo(Bird that) {
			if (this.resources > that.getResources())
				return 1;
			else if (this.resources < that.getResources())
				return -1;
			return 0;
		}
	}
	
	
}
























