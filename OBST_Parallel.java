import java.util.Scanner;

public class OBST_Parallel extends Thread {
	static int numberOfNodes;
	static double accessProbabilities[];
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

	public OBST_Parallel(int num, int currCol, int start, int end) {
		this.threadNum = num;
		this.currCol = currCol;
		this.start = start;
		this.end = end;
	}

	public static void main(String[] args) throws InterruptedException {

		Scanner sc = new Scanner(System.in);
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

		long endTime = System.currentTimeMillis();

		System.out.println("Time Taken by Serial OBST Algorithm = " + (endTime - startTime) + " milliseconds");
		printMatrix(searchCount);
		System.out.println();
		System.out.println("==== Calculating Time for Parallel OBST Algorithm ====");
		startTime = System.currentTimeMillis();
		parallelOBST();

		endTime = System.currentTimeMillis();
		System.out.println("Time Taken by Parallel OBST Algorithm = " + (endTime - startTime) + " milliseconds");
		printMatrix(searchCount);
	}

	public static void generateAccessProbabilities() {
		sumOfFirstNnumbers = new double[numberOfNodes];
		accessProbabilities = new double[numberOfNodes];
		double sum = 0;
		for (int index = 0; index < numberOfNodes; index++) {
			accessProbabilities[index] = Math.floor(Math.random() * 100);
			sum += accessProbabilities[index];
			sumOfFirstNnumbers[index] = sum;
		}
		for (int index = 0; index < numberOfNodes; index++) {
			accessProbabilities[index] /= sum;
		}
	}

	public static void serialOBST() {
		searchCount = new double[numberOfNodes][numberOfNodes];
		treeNodes = new Node[numberOfNodes][numberOfNodes];

		for (int col = 0; col < numberOfNodes; col++) {
			int row = 0;
			int colCopy = col;
			while (colCopy < numberOfNodes) {
				if (colCopy == row) {
					searchCount[row][colCopy] = accessProbabilities[row];
					treeNodes[row][colCopy] = new Node(row);
				} else {
					double sum = 0;
					if (row == 0)
						sum = sumOfFirstNnumbers[colCopy];
					else
						sum = sumOfFirstNnumbers[colCopy] - sumOfFirstNnumbers[row - 1];
					double min = Double.MAX_VALUE;

					Node leftNode = null, rightNode = null;
					int rootValue = 0;

					for (int k = row; k <= colCopy; k++) {
						double left = 0;
						double right = 0;

						if (k != row)
							left = searchCount[row][k - 1];
						if (k != colCopy)
							right = searchCount[k + 1][colCopy];

						if (left + right + sum < min) {
							rootValue = k;
							min = left + right + sum;
							if (left != 0)
								leftNode = treeNodes[row][k - 1];
							if (right != 0)
								rightNode = treeNodes[k + 1][colCopy];
						}
					}
					searchCount[row][colCopy] = min;
					treeNodes[row][colCopy] = new Node(rootValue, leftNode, rightNode);
				}
				colCopy++;
				row++;
			}
		}
	}

	public static void parallelOBST() throws InterruptedException {
		searchCount = new double[numberOfNodes][numberOfNodes];
		treeNodes = new Node[numberOfNodes][numberOfNodes];

		OBST_Parallel threads[] = new OBST_Parallel[numberOfCores];
		for (int col = 0; col < numberOfNodes; col++) {
			int parts = (numberOfNodes - col) / numberOfCores;
			int start = 0;
			int end = parts - 1;
			if (parts == 0) {
				threads[0] = new OBST_Parallel(0, col, 0, numberOfNodes - col - 1);
				threads[0].start();
				threads[0].join();
			} else {
				for (int i = 0; i < numberOfCores - 1; i++) {
					threads[i] = new OBST_Parallel(i, col, start, end);
					start += parts;
					end += parts;
					threads[i].start();
				}
				threads[numberOfCores - 1] = new OBST_Parallel(numberOfCores - 1, col, start, numberOfNodes - col - 1);
				threads[numberOfCores - 1].start();
				for (int i = 0; i < numberOfCores; i++)
					threads[i].join();
			}
			System.out.println("---------------------------------------------------------------------------------------");
			

		}

//		printMatrix(searchCount);
	}

	public void run() {
		System.out.println("Thread Num = " + this.threadNum + "    start index = " + start + "    end Index = " + end + "     currCol = " + currCol);
		int row = start;
		int colCopy = start + currCol;
		while (colCopy <= end) {
			if (colCopy == row) {
				searchCount[row][colCopy] = accessProbabilities[row];
				treeNodes[row][colCopy] = new Node(row);
			} else {
				double sum = 0;
				if (row == 0)
					sum = sumOfFirstNnumbers[colCopy];
				else
					sum = sumOfFirstNnumbers[colCopy] - sumOfFirstNnumbers[row - 1];
				double min = Double.MAX_VALUE;

				Node leftNode = null, rightNode = null;
				int rootValue = 0;

				for (int k = row; k <= colCopy; k++) {
					double left = 0;
					double right = 0;

					if (k != row)
						left = searchCount[row][k - 1];
					if (k != colCopy)
						right = searchCount[k + 1][colCopy];

					if (left + right + sum < min) {
						rootValue = k;
						min = left + right + sum;
						if (left != 0)
							leftNode = treeNodes[row][k - 1];
						if (right != 0)
							rightNode = treeNodes[k + 1][colCopy];
					}
				}
//					System.out.println(row);
				searchCount[row][colCopy] = min;
				treeNodes[row][colCopy] = new Node(rootValue, leftNode, rightNode);
			}
			colCopy++;
			row++;
		}
	}

//	}
	public static void testSerial() {
//		double accessProbabilities2[] = { 3, 6, 4, 8, 7, 3, 7, 4, 7 };
		double accessProbabilities2[] = { 6, 3, 4 };
		accessProbabilities = accessProbabilities2;

		numberOfNodes = 3;
		sumOfFirstNnumbers = new double[numberOfNodes];
		double sum = 0;
		for (int index = 0; index < numberOfNodes; index++) {
			sum += accessProbabilities[index];
			sumOfFirstNnumbers[index] = sum;
		}

		serialOBST();
	}

	public static void testParallel() throws InterruptedException {
		double accessProbabilities2[] = { 6, 3, 4 };
		accessProbabilities = accessProbabilities2;

		numberOfNodes = 3;
		sumOfFirstNnumbers = new double[numberOfNodes];
		double sum = 0;
		for (int index = 0; index < numberOfNodes; index++) {
			sum += accessProbabilities[index];
			sumOfFirstNnumbers[index] = sum;
		}

		parallelOBST();
	}

	public static void printNodeMatrix(Node treeNodes[][]) {
		for (int i = 0; i < treeNodes.length; i++) {
			for (int j = 0; j < treeNodes.length; j++) {
				System.out.print(treeNodes[i][j] + " **** ");
			}
			System.out.println();
		}
	}

	public static void printMatrix(double mat[][]) {
		for (int i = 0; i < mat.length; i++) {
			for (int j = 0; j < mat.length; j++) {
				System.out.print(mat[i][j] + " --- ");
			}
			System.out.println();
		}
	}

	public static void inorderTraversal(Node root) {
		if (root == null)
			return;

		inorderTraversal(root.left);
		System.out.println(root);
		inorderTraversal(root.right);
	}
}

class Node {
	int val;
	Node left;
	Node right;

	public Node(int val) {
		this.val = val;
		left = null;
		right = null;
	}

	public Node(int val, Node left, Node right) {
		this.val = val;
		this.left = left;
		this.right = right;
	}

//	public String toString() {
//		String leftNode = "null";
//		String rightNode = "null";
//		
//		if(this.left != null)
//			leftNode = ""+this.left.val;
//		if(this.right != null)
//			rightNode = ""+ this.right.val;
//		
//		return leftNode + " <------  " + val + "  ------> " + rightNode;
//	}
}