import java.util.Scanner;

public class OBST_Parallel
{
	static int numberOfNodes;
	static double accessProbabilities[];
	static int numberOfCores;
	
	public static void main(String[] args)
	{
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the number of nodes = ");
		numberOfNodes = sc.nextInt();
		
		System.out.println("Enter the number of cores = ");
		numberOfCores = sc.nextInt();
		
		System.out.println();
		System.out.println("==== Randomly Generating Access Probabilities ====");
		
		generateAccessProbabilities();
		
		long startTime = System.currentTimeMillis();
		serialOBST();
		long endTime = System.currentTimeMillis();
		
		System.out.println("Time Taken by Serial OBST Algorithm = " + (endTime - startTime));
		
		startTime = System.currentTimeMillis();
		parallelOBST();
		endTime = System.currentTimeMillis();
		System.out.println("Time Taken by Parallel OBST Algorithm = " + (endTime - startTime));
		
	}
	public static void generateAccessProbabilities()
	{
		int sum = 0;
		for(int index = 0 ; index < numberOfNodes; index++)
		{
			accessProbabilities[index] = Math.random();
			sum += accessProbabilities[index];
		}
		for(int index = 0 ; index < numberOfNodes; index++)
		{
			accessProbabilities[index] /= sum;
		}
	}
	
	public static void serialOBST()
	{
		
	}
	
	public static void parallelOBST()
	{
		
	}

}
