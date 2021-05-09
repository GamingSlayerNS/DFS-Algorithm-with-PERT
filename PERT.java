/* Starter code for PERT algorithm (Project 4)
 * @author SpeedyNS  - nas180011
 * @author Danny Bao - dxb180034
 */

// change dsa to your netid
package nas180011;

import nas180011.Graph.Vertex;
import nas180011.Graph.Edge;
import nas180011.Graph.GraphAlgorithm;
import nas180011.Graph.Factory;

import java.io.File;
import java.util.*;

public class PERT extends GraphAlgorithm<PERT.PERTVertex>
{
	public static final int INFINITY = Integer.MAX_VALUE;									//Infinite int
	LinkedList<Vertex> finishList = new LinkedList<>();										//List that holds the topological order of the graph
	Vertex src;																				//Source vertex/root
	boolean isDAG;																			//Boolean, is graph a DAG?

    public static class PERTVertex implements Factory
	{
		// Add fields to represent attributes of vertices here
		boolean seen;
		ArrayList<Vertex> parent;															//Parent array of the vertex
		ArrayList<Vertex> child;															//Child array of the vertex.
		int distance;  																		// distance of vertex from source
		int duration;																		//Duration of each Vertex
		String status;																		//Status of the Vertex for the DAG function (New, Active, Finished)
		int ec;																				//Early Completion
		int lc;																				//Latest Completion
		int es;																				//Early Start
		int ls;																				//Latest Start
		int slack;																			//Slack is ls - lc
		public PERTVertex(Vertex u)
		{
			seen = false;
			parent = new ArrayList<>();
			child = new ArrayList<>();
			distance = INFINITY;
			duration = INFINITY;
			status = "New";
			ec = 0;
			lc = 0;
			es = 0;
			ls = 0;
			slack = 0;
		}
		public PERTVertex make(Vertex u)
		{
			return new PERTVertex(u);
		}
    }

    // Constructor for PERT is private. Create PERT instances with static method pert().
    private PERT(Graph g)
	{
		super(g, new PERTVertex(null));
		src = g.adjList[0].vertex;
    }

    public void setDuration(Vertex u, int d)
	{
		get(u).duration = d;
    }

    // Implement the PERT algorithm. Returns false if the graph g is not a DAG.
    public boolean pert()
	{
		//Run DFS through topological order first
		topologicalOrder();
		//Run DAG to determine if graph is DAG
		isDAG = DAG(src);
		for (Graph.AdjList i: g.adjList)													//Checks final vertexes that were left as New
		{
			if (get(i.vertex).status.compareTo("New") == 0)
			{
				get(i.vertex).status = "Active";
				DAG(i.vertex);
			}
		}

		//Reverse list for right order.
		Collections.reverse(finishList);
		//Check Ecs and LCs
		for (Vertex vertex: finishList)
		{
			//Assigns ECs
			get(vertex).ec = get(vertex).es + get(vertex).duration;
			for (Edge edge: g.incident(vertex))
			{
				Vertex child = edge.otherEnd(vertex);
				if(get(child).es < get(vertex).ec)
					get(child).es = get(vertex).ec;
			}

		}
		int CPL = 0;
		for (Vertex vertex: finishList)														//Assign the correct CPL to each vertex
		{
			for (Edge edge: g.incident(vertex)) {
				Vertex child = edge.otherEnd(vertex);
				if (get(child).ec > CPL)
					CPL = get(child).ec;
			}
			get(vertex).lc = CPL;
		}
		Collections.reverse(finishList);													//Reverse List for reverse topological order for LCs
		for (Vertex vertex: finishList)
		{
			//Assigns LCs
			for (Edge edge: g.incident(vertex))
			{
				Vertex child = edge.otherEnd(vertex);
				get(child).ls = get(child).lc - get(child).duration;
				if (get(vertex).lc > get(child).ls)
					get(vertex).lc = get(child).ls;
			}
		}
		return isDAG;
    }

    // Find a topological order of g using DFS
    LinkedList<Vertex> topologicalOrder()
	{
		dfsVisit(src);
		return finishList;
    }

    //DFS algorithm
    void dfsVisit(Vertex u)
	{
		get(u).seen = true;
		get(u).distance = 1;
		get(u).status = "Active";

		//Run DFS to assign parents
		dfsVisitUtil(u);
		for (Graph.AdjList i: g.adjList)
		{
			if (!get(i.vertex).seen)
			{
				get(i.vertex).seen = true;
				dfsVisitUtil(i.vertex);
			}
		}
    }

    //Recursive version of dfsVisit
    void dfsVisitUtil(Vertex u)
	{

		for (Edge edge: g.incident(u))
		{
			Vertex vertex = edge.otherEnd(u);
			get(vertex).parent.add(u);
			get(u).child.add(vertex);
			if (!get(vertex).seen)
			{
				get(vertex).seen = true;
				get(vertex).distance = get(u).distance + 1;
				dfsVisitUtil(vertex);
			}
		}
	}

	//Checks if graph is a DAG
	boolean DAG(Vertex u)
	{
		//Run DAG
		get(u).status = "Active";
		for (Edge edge: g.incident(u))
		{
			Vertex vertex = edge.otherEnd(u);
			if (get(vertex).status.compareTo("Active") == 0)
				return false;
			else if (get(vertex).status.compareTo("New") == 0)
			{
				if (!DAG(vertex))
					return false;
			}
		}
		get(u).status = "Finished";
		finishList.add(u);
		return true;
	}

	//Extra lines used for testing
//	void assignEC(Vertex u)
//	{
//		//Assigns ECs
//		if (!get(u).parent.isEmpty())
//		{
//			int maxP = 0;
//			for (Vertex vertex: get(u).parent)
//				maxP = Math.max(maxP, get(vertex).ec);
//			get(u).ec = maxP + get(u).duration;
//		}
//		else
//			get(u).ec = get(u).duration;
//	}
//
//	void assignLC(Vertex u)
//	{
//		//Assigns LCs
//		if (!get(u).child.isEmpty())		//if u has a child
//		{
//			int minC = INFINITY;
//			for (Vertex vertex: get(u).child)
//				minC = Math.min(minC, get(vertex).lc - get(vertex).duration);
//			get(u).lc = minC;
//		}
//		else
//			get(u).lc = get(u).ec;
//	}

    //////////////////////// The following methods are called after calling pert().////////////////////////

    // Earliest time at which task u can be completed
    public int ec(Vertex u)
	{
		return get(u).ec;
    }

    // Latest completion time of u
    public int lc(Vertex u)
	{
		return get(u).lc;
    }

    // Slack of u
    public int slack(Vertex u)
	{
		return get(u).lc - get(u).ec;
    }

    // Length of a critical path (time taken to complete project)
    public int criticalPath()
	{
		int max = 0;
		for (Vertex vertex: finishList)
		{
			if (critical(vertex))
				max = Math.max(get(vertex).ec, max);
		}
		return max;
    }

    // Is u a critical vertex?
    public boolean critical(Vertex u)
	{
		return slack(u) == 0;
    }

    // Number of critical vertices of g
    public int numCritical()
	{
		int crits = 0;
		for (Vertex vertex: finishList)
		{
			if (critical(vertex))
				crits++;
		}
		return crits;
    }

    /* Create a PERT instance on g, runs the algorithm.
     * Returns PERT instance if successful. Returns null if G is not a DAG.
     */
    public static PERT pert(Graph g, int[] duration)
	{
		PERT p = new PERT(g);
		for(Vertex u: g)
			p.setDuration(u, duration[u.getIndex()]);
		// Run PERT algorithm.  Returns false if g is not a DAG
		if(p.pert()) {
			return p;
		} else {
			return null;
		}
    }
    
    public static void main(String[] args) throws Exception
	{
		String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
		//String graph = "8 11   1 2 1   1 3 1   2 4 1   3 5 1   3 6 1   4 6 1   4 7 1   5 7 1   5 8 1   6 8 1   7 8 1   3 3 2 3 7 4 5 1";
		//String graph = "11 12   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1 0";
		Scanner in;
		// If there is a command line argument, use it as file from which
		// input is read, otherwise use input from string.
		in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);
		Graph g = Graph.readDirectedGraph(in);
		g.printGraph(false);

		int[] duration = new int[g.size()];
		for(int i=0; i<g.size(); i++)
			duration[i] = in.nextInt();
		PERT p = pert(g, duration);
		//System.out.println(p.dur);
		if(p == null)
			System.out.println("Invalid graph: not a DAG");
		else
		{
			System.out.println("u\tDur\tEC\tLC\tSlack\tCritical");
			for(Vertex u: g)
				System.out.println(u + "\t" + duration[u.getIndex()] + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
			System.out.println("Critical Path: " + p.criticalPath() + "\tNumber of critical vertices: " + p.numCritical());
		}
    }
}
