package it.polito.oma.solver.threads;

import java.util.*;
import java.util.stream.Collectors;

public class GraphColoring {
    int connected[][];
    int[] colors;
    int nColors;
    int nNodes;
    int result[];
    
    private int V;   // No. of vertices
    private LinkedList<Integer> adj[]; //Adjacency List
 
    //Constructor
    public GraphColoring(int v)
    {
        V = v;
        result = new int[V];
        adj = new LinkedList[v];
        for (int i=0; i<v; ++i)
            adj[i] = new LinkedList();
    }
 
    //Function to add an edge into the graph
    public void addEdge(int v,int w)
    {
        adj[v].add(w);
        adj[w].add(v); //Graph is undirected
    }
 
    // Assigns colors (starting from 0) to all vertices and
    // prints the assignment of colors
    public void greedyColoring()
    {
 
        // Initialize all vertices as unassigned
        Arrays.fill(result, -1);
        
        for(int i = 0; i < V; i++)
        	coloring(i);
 
//        // Assign the first color to first vertex
//        result[0] = 0;
// 
//        // A temporary array to store the available colors. False
//        // value of available[cr] would mean that the color cr is
//        // assigned to one of its adjacent vertices
//        boolean available[] = new boolean[V];
//         
//        // Initially, all colors are available
//        Arrays.fill(available, true);
// 
//        // Assign colors to remaining V-1 vertices
//        for (int u = 1; u < V; u++)
//        {
//            // Process all adjacent vertices and flag their colors
//            // as unavailable
//            Iterator<Integer> it = adj[u].iterator() ;
//            while (it.hasNext())
//            {
//                int i = it.next();
//                if (result[i] != -1)
//                    available[result[i]] = false;
//            }
// 
//            // Find the first available color
//            int cr;
//            for (cr = 0; cr < V; cr++){
//                if (available[cr])
//                    break;
//            }
// 
//            result[u] = cr; // Assign the found color
// 
//            // Reset the values back to true for the next iteration
//            Arrays.fill(available, true);
//        }
 
        // print the result
        for (int u = 0; u < V; u++)
            System.out.println("Vertex " + u + " --->  Color "
                                + result[u]);
    }
    
    private void coloring(int pos) {
    	if(result[pos] != -1)
    		return;
    	
    	int color = 1;
    	List<Integer> l = adj[pos];
    	l = l.stream().sorted((s1, s2) -> Integer.compare(result[s1], result[s2])).collect(Collectors.toList());
    	for(Integer i:l) {
    		if(color == result[i]) {
    			color++;
    		}
    	}
    	
    	result[pos] = color;
    	for(Integer i:l) {
    		coloring(i);
    	}
    }
    
    public int[] getResult() {
    	return result;
    	
    }
}