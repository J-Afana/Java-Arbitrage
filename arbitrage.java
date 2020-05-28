/*Arbitrage.java
 * by John Afana
 * Takes a file and parses out nodes and edges representing a graph
 * of different exchange rates. The user defines the starting currency and amount
 * the arbitrage program then outputs trades which produce profits of 15% or more
 * as well as which order to make the trades in to produce the profit. 
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public class Arbitrage {
	
	//defines the edge class	
	static class Edge{
		private String nodeA, nodeB;
		private double exch;

		
		public Edge(String nodeA, String nodeB, double exch) {
			this.nodeA = nodeA;
			this.nodeB = nodeB;
			this.exch = exch;
		}
		
		public String getNodeA() {
			return nodeA;
		}
		
		public void setNodeA(String nodeA) {
			this.nodeA = nodeA;
		}
		
		public String getNodeB() {
			return nodeB;
		}
		
		public void setNodeB(String nodeB) {
			this.nodeB = nodeB;
		}
		
		public double getExch() {
			return exch;
		}
		
		public void setExch(double exch) {
			this.exch = exch;
		}
	}
	
	//method to permute through the list of nodes and return all the combinations
	static class PermutateArray {
		 
		public List<List<String>> permute(ArrayList<String> nodes) {
			List<List<String>> list = new ArrayList<>();
			permuteHelper(list, new ArrayList<>(), nodes);
			return list;
		}
	 
		private void permuteHelper(List<List<String>> list, List<String> resultList, ArrayList<String> arr){
	 
			// Base case
			if(resultList.size() == arr.size()){
				list.add(new ArrayList<>(resultList));
			} 
			else{
				for(int i = 0; i < arr.size(); i++){ 
	 
					if(resultList.contains(arr.get(i))) 
					{
						// If node already exists in the list then skip
						continue; 
					}
					// Choose node
					resultList.add(arr.get(i));
					permuteHelper(list, resultList, arr);
					// Unchoose node
					resultList.remove(resultList.size() - 1);
				}
			}
		} 
	 
	}

	public static void main(String [] args) {
		//define variables
		PermutateArray pa=new Arbitrage.PermutateArray();
		ArrayList<String> nodes = new ArrayList<String>();
		HashSet<Edge> edges = new HashSet<Edge>();
		HashSet<String> earnings = new HashSet<String>();
		String address = new String(); //variable to store the file path for input file
		
		//get file path for data
		System.out.println("Enter filepath");
		
		Scanner sc = new Scanner(System.in); //reads user input 
		address = sc.nextLine();
		
		
		System.out.println("Importing file");
		
		/*reads the file and builds edges for each set of relationships inserts into ArrayList*/
		try {
			Scanner scanner = new Scanner(new File(address));

			while (scanner.hasNextLine()) {

				String text = scanner.nextLine();
				if (text.contains("*") || text.contains("/")) {
					continue;					
				}

				String a = scanner.next();
				if (a.contains("*") || a.contains("/") || a.contains("9")) {
					continue;
				}
				String b = scanner.next();
				String rate = scanner.next();
				double ex = Double.parseDouble(rate);
				//System.out.println(a + " " + b + " " + rate);
				
				if (nodes.contains(b) == false)
						nodes.add(b);
				if (nodes.contains(a) == false)
					nodes.add(a);
				
				Edge edge = new Edge(a, b, ex);
				edges.add(edge);
				edge = new Edge(b, a, 1/ex);
				edges.add(edge);
				//System.out.println(a+", "+b+", "+ex+" | "+b+", "+a+", "+1/ex);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//Get user input for which currency and the amount to try.
		//print list of currencies
		for (int i = 0; i < nodes.size(); i++) {
		System.out.println(i+" "+nodes.get(i));
		}
		//Get user input selecting currency.
		System.out.println("Which Currency do you want to use?");
		int ind = sc.nextInt();
		System.out.println("How much money do you want to arbitrage?");
		double money = sc.nextDouble();
		sc.close();

		String start = nodes.get(ind);
		double initMoney = 1000;
		double runningTot;
		Iterator<Edge> itr = edges.iterator();
		String curNode, nextNode;
		nodes.remove(ind);
		curNode = start;
		if (money < 1) {
			initMoney = 100;
			System.out.println("that's not enough, try 100.00");
		}else {
			initMoney = money;
		}
		
		
		//Permute through all available nodes returns lists containing all nodes
		List<List<String>> permute = pa.permute(nodes);
		for(List<String> perm:permute){
			runningTot = initMoney;
			curNode = start;
			nextNode = perm.get(0);
			
			//loops through permutations and removes the first character from the ArrayList each loop
			//allows the next loop to calculate the edge total for paths with 3 to 8 nodes 
			for (int k = perm.size(); k > 2; k--) {
				if (k == perm.size()) {
					continue;
				}else {
					perm.remove(0);
					runningTot = initMoney;
				}
				//calculates the edges for each node traversed
				for (int j = 0; j < perm.size()+1; j++) {
				
					while (itr.hasNext()) {
						Edge curEdge = itr.next();
						if (curNode.equals(curEdge.getNodeA()) && nextNode.equals(curEdge.getNodeB())) {
							//System.out.println(curEdge.getNodeA() + ", " + curEdge.getNodeB() + ", " + curEdge.getExch());
							runningTot = runningTot*curEdge.getExch();
						}
				
					}
				
					itr = edges.iterator();
					curNode = nextNode;
					if (j < perm.size()-1) {
						nextNode = perm.get(j+1);
					}else {
						nextNode = start;
					}
				}
				//saves paths that produce at least 15% profit
				if (runningTot > initMoney*1.15) {
					DecimalFormat df = new DecimalFormat("#.##");
					df.setRoundingMode(RoundingMode.CEILING);

					StringBuffer s = new StringBuffer(start + " -> ");
					for (int i = 0; i < perm.size(); i++) {
						s.append(perm.get(i) + " -> ");
					}

					s.append(start + " Total = " + df.format(runningTot));
					String earn = s.toString();
					earnings.add(earn);
					//System.out.println(s);
					
				}
			}
		}
		//prints out the list of paths and how much they would produce
		for (String out:earnings) {
			System.out.println(out);
		}
		
		
	}
}




