import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class OBST_Parallel extends Thread {
	static int numberOfNodes;
	static double accessProbabilities[];
	static double frequencyArray[];
	static int numberOfCores = 4;
	static double sumOfFirstNnumbers[];
	static double searchCount[][];
	static Node treeNodes[][];
	int threadNum;
	int currCol;
	int start;
	int end;

	public OBST_Parallel() {

	}

	public OBST_Parallel( int num, int currCol, int start, int end ) {
		this.threadNum = num;
		this.currCol = currCol;
		this.start = start;
		this.end = end;
	}

	public static void main( String[] args ) throws InterruptedException {

		Scanner sc = new Scanner( System.in );
		System.out.println("Enter the number of nodes = ");
		numberOfNodes = sc.nextInt();

		System.out.println("Enter the number of cores = ");
		numberOfCores = sc.nextInt();

		System.out.println();
		System.out.println("==== Randomly Generating Access Probabilities ====");
		System.out.println();
		generateAccessProbabilities();

		System.out.println("==== Calculating Time for Searial OBST Algorithm ====");
		long startTime = System.currentTimeMillis();
		serialOBST();
		System.out.println(" The optimal cost for BST is = " + searchCount[ 0 ][ numberOfNodes - 1 ] );
		long endTime = System.currentTimeMillis();
		

		System.out.println("Time Taken by Serial OBST Algorithm = " + ( endTime - startTime ) + " milliseconds");
		
		System.out.println();
		System.out.println("==== Calculating Time for Parallel OBST Algorithm ====");
		startTime = System.currentTimeMillis();
		parallelOBST();
		System.out.println(" The optimal cost for BST is = " + searchCount[ 0 ][ numberOfNodes - 1 ] );
		endTime = System.currentTimeMillis();
		System.out.println("Time Taken by Parallel OBST Algorithm = " + ( endTime - startTime ) + " milliseconds");
		
		int input = -1;
		System.out.println("------------------------------------------------------------------------------------");
		System.out.println();
		System.out.println("Press 0 to view Frequency Array");
		System.out.println("Press 1 to view Access Probabilities array");
		System.out.println("Press 2 to view DP matrix used to compute the optimal BST");
		System.out.println("Press 3 to print level order traversal of OBST");
		System.out.println("Press 4 to exit");
		System.out.println();
		while( input != 4 )
		{
			input = sc.nextInt();
			switch( input )
			{
			case 0:{
				System.out.println(Arrays.toString( frequencyArray ));
				break;
			}
			case 1: {
				System.out.println( Arrays.toString( accessProbabilities ) );
				break;
			}
			case 2:{
				for(double arr[] : searchCount)
					System.out.println( Arrays.toString( arr ) );
				break;
			}
			case 3:{
				levelOrderTraversal( treeNodes[ 0 ][ numberOfNodes - 1 ] );
				break;
			}
			case 4:{
				continue;
			}
			default:{
				System.out.println("Enter a valid number");
			}
			}
		}
	}
	
	/*
		Generates random frequencies for each node which represent the 
		amount of time the node is being searched in the BST.
		Access probability is calculated by dividing frequency of each
		node with total frequency of all nodes.
	*/

	public static void generateAccessProbabilities() {
		sumOfFirstNnumbers = new double[ numberOfNodes ];
		frequencyArray = new double[ numberOfNodes ];
		accessProbabilities = new double[ numberOfNodes ];
		double sum = 0;
		for ( int index = 0; index < numberOfNodes; index ++ ) {
			accessProbabilities[ index ] = Math.floor( Math.random() * 100 );
			sum += accessProbabilities[ index ];
			frequencyArray[ index ] = accessProbabilities[ index ];
			sumOfFirstNnumbers[ index ] = sum;
		}
		for ( int index = 0; index < numberOfNodes; index ++ ) {
			accessProbabilities [index ] /= sum;
		}
	}
	
	/*
		This function finds the optimal cost for the binary search tree
		and also constructs the tree using a serial DP approach (1 thread).		
	*/	

	public static void serialOBST() {
		searchCount = new double[ numberOfNodes ][ numberOfNodes ];
		treeNodes = new Node[ numberOfNodes ][ numberOfNodes ];

		for ( int col = 0; col < numberOfNodes; col ++ ) {
			int row = 0;
			int colCopy = col;
			while ( colCopy < numberOfNodes ) {
				if ( colCopy == row ) {
					searchCount[ row ][ colCopy ] = accessProbabilities[ row ];
					treeNodes[ row ][ colCopy ] = new Node( row );
				} else {
					double sum = 0;
					if ( row == 0 )
						sum = sumOfFirstNnumbers[ colCopy ];
					else
						sum = sumOfFirstNnumbers[ colCopy ] - sumOfFirstNnumbers[ row - 1 ];
					double min = Double.MAX_VALUE;

					Node leftNode = null, rightNode = null;
					int rootValue = 0;

					for ( int k = row; k <= colCopy; k ++ ) {
						double left = 0;
						double right = 0;

						if ( k != row )
							left = searchCount[ row ][ k - 1 ];
						if ( k != colCopy )
							right = searchCount[ k + 1 ][ colCopy ];

						if ( left + right + sum < min ) {
							rootValue = k;
							min = left + right + sum;
							if ( left != 0 )
								leftNode = treeNodes[ row ][ k - 1 ];
							if ( right != 0 )
								rightNode = treeNodes[ k + 1 ][ colCopy ];
						}
					}
					searchCount[ row ][ colCopy ] = min;
					treeNodes[ row ][ colCopy ] = new Node( rootValue, leftNode, rightNode );
				}
				colCopy ++;
				row ++;
			}
		}
	}

	/*
		This method creates and starts the user specified threads to
		build the optimal search tree parallely.
	*/
	public static void parallelOBST() throws InterruptedException {
		searchCount = new double[ numberOfNodes ][ numberOfNodes ];
		treeNodes = new Node[ numberOfNodes ][ numberOfNodes ];

		OBST_Parallel threads[] = new OBST_Parallel[ numberOfCores ];
		for (int col = 0; col < numberOfNodes; col ++) {
			int parts = (int)(( numberOfNodes - col ) / numberOfCores );
			int start = 0;
			int end = col + parts - 1;
			if ( parts == 0 ) {
				threads[ 0 ] = new OBST_Parallel( 0, col, 0, numberOfNodes - 1 );
				threads[ 0 ].start();
				threads[ 0 ].join();
			} else {
				for ( int i = 0; i < numberOfCores - 1; i ++ ) {
					threads[ i ] = new OBST_Parallel( i, col, start, end );
					start += parts;
					end += parts;
					threads[ i ].start();
				}
				threads[ numberOfCores - 1 ] = new OBST_Parallel( numberOfCores - 1, col, start, numberOfNodes - 1 );
				threads[ numberOfCores - 1 ].start();
				for ( int i = 0; i < numberOfCores; i ++ )
					threads[ i ].join();
			}
		}
	}
	
	/*
		Overrided run method. Each thread works on a specific portion of data.		
	*/

	public void run() {
		int row = start;
		int colCopy = start + currCol;
		while ( colCopy <= end ) {
			if ( colCopy == row ) {
				searchCount[ row ][ colCopy ] = accessProbabilities[ row ];
				treeNodes[ row ][ colCopy ] = new Node( row );
			} else {
				double sum = 0;
				if ( row == 0 )
					sum = sumOfFirstNnumbers[ colCopy ];
				else
					sum = sumOfFirstNnumbers[ colCopy ] - sumOfFirstNnumbers[ row - 1 ];
				double min = Double.MAX_VALUE;

				Node leftNode = null, rightNode = null;
				int rootValue = 0;

				for ( int k = row; k <= colCopy; k ++ ) {
					double left = 0;
					double right = 0;

					if ( k != row )
						left = searchCount[ row ][ k - 1 ];
					if ( k != colCopy )
						right = searchCount[ k + 1 ][ colCopy ];

					if ( left + right + sum < min ) {
						rootValue = k;
						min = left + right + sum;
						if ( left != 0 )
							leftNode = treeNodes[ row ][ k - 1 ];
						if ( right != 0 )
							rightNode = treeNodes[ k + 1 ][ colCopy ];
					}
				}
				searchCount[ row ][ colCopy ] = min;
				treeNodes[ row ][ colCopy ] = new Node( rootValue, leftNode, rightNode );
			}
			colCopy ++;
			row ++;
		}
	}
	
	/*
		A test method to test the serial algorithm
	*/

	public static void testSerial() {
		double accessProbabilities2 [] = { 6, 3, 4 };
		accessProbabilities = accessProbabilities2;

		numberOfNodes = 3;
		sumOfFirstNnumbers = new double[ numberOfNodes ];
		double sum = 0;
		for (int index = 0; index < numberOfNodes; index ++) {
			sum += accessProbabilities[ index ];
			sumOfFirstNnumbers[ index ] = sum;
		}

		serialOBST();
	}

	/*
		A test method to find the OBST parallely
	*/
	public static void testParallel() throws InterruptedException {
		double accessProbabilities2[] = { 6, 3, 4 };
		accessProbabilities = accessProbabilities2;

		numberOfNodes = 3;
		sumOfFirstNnumbers = new double[ numberOfNodes ];
		double sum = 0;
		for ( int index = 0; index < numberOfNodes; index ++) {
			sum += accessProbabilities[ index ];
			sumOfFirstNnumbers[ index ] = sum;
		}

		parallelOBST();
	}

	public static void printNodeMatrix( Node treeNodes[][] ) {
		for ( int i = 0; i < treeNodes.length; i ++ ) {
			for ( int j = 0; j < treeNodes.length; j ++ ) {
				System.out.print( treeNodes[i][j] + " **** ");
			}
			System.out.println();
		}
	}

	public static void printMatrix( double mat[][] ) {
		for ( int i = 0; i < mat.length; i ++ ) {
			for ( int j = 0; j < mat.length; j ++ ) {
				System.out.print( mat[ i ][ j ] + " --- " );
			}
			System.out.println();
		}
	}

	/*
		This method prints the OBST level-wise (level order traversal)
	*/

	public static void levelOrderTraversal( Node root ) {
		Queue< Node > q = new LinkedList<>();
		q.offer( root );
		int level = 0;

		while( !q.isEmpty() )
		{
			int size = q.size();
			System.out.println("Nodes at level " + level + " are = " );
			for( int i = 0 ; i < size; i ++ )
			{
				Node curr = q.poll();
				System.out.print( "[  " + curr.val + " with access probability = " + accessProbabilities[ curr.val ] + "]   " );
				
				if( curr.left != null )
					q.offer( curr.left );
				if( curr.right != null )
					q.offer( curr.right );
			}
			level ++;
			System.out.println();
		}
	}
}

class Node {
	int val;
	Node left;
	Node right;

	public Node( int val ) {
		this.val = val;
		left = null;
		right = null;
	}

	public Node( int val, Node left, Node right ) {
		this.val = val;
		this.left = left;
		this.right = right;
	}

	public String toString() {
		String leftNode = "null";
		String rightNode = "null";
		
		if ( this.left != null )
			leftNode = "" + this.left.val;
		if ( this.right != null )
			rightNode = "" + this.right.val;
		
		return leftNode + " <------  " + val + "  ------> " + rightNode;
	}
}