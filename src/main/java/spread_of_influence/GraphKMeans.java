/** 
 * Class for kmeans clustering of a G graph
 * May. 2019
 * @author Durand Azimedem
*/

package spread_of_influence;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedGraph;
import it.stilo.g.util.ArraysUtil;

//import org.jgrapht.*;
//import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
//import org.jgrapht.alg.shortestpath.*;
//import org.jgrapht.graph.*;

import io.TxtUtils;


public class GraphKMeans {
	
	private int k; // Number of clusters
	private int maxIter = 0; // Maximum number of iterations
	private WeightedGraph g; // The graph to cluster
	//Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
	private ArrayList<ArrayList<Integer>> clusters; // ArrayList of k ArrayLists of Integers that will contain the k clusters as k ArrayLists
	private ArrayList<Integer> centroids; // ArrayList of k Integers that will contain the k centroids for each cluster
	private Dictionary<Integer, String> labels = new Hashtable<Integer, String>(); // Dictionary containing YES/NO labels for each node
	private Dictionary<Integer, Integer> node2Cluster; // Dictionary containing the cluster id for each node in the graph
	
	// The constructor
	public GraphKMeans(int k, WeightedGraph g) {
		this.k = k;
		this.g = g;
		//this.graph = fromGToJgrapht(g);
		this.centroids = new ArrayList<Integer>();
		// initialize the node2Cluster dictionary by assigning 0 as cluster to all the nodes of the graph
		this.node2Cluster = new Hashtable<Integer, Integer>();
		for (int node: this.g.V) {
			this.node2Cluster.put(node, 0);
		}
		// initialize the clusters as empty ArrayLists
		this.clusters = new ArrayList<ArrayList<Integer>>();
		int i = 0;
		while(i < this.k) {
			this.clusters.add(new ArrayList<Integer>());
			i++;
		}
	}
	
	// The constructor with max number of iterations
	public GraphKMeans(int k, WeightedGraph g, int maxIter) {
		this.k = k;
		this.g = g;
		//this.graph = fromGToJgrapht(g);
		this.maxIter = maxIter;
		this.centroids = new ArrayList<Integer>();
		// initialize the node2Cluster dictionary by assigning 0 as cluster to all the nodes of the graph
		this.node2Cluster = new Hashtable<Integer, Integer>();
		for (int node: this.g.V) {
			this.node2Cluster.put(node, 0);
		}
		// initialize the clusters as empty ArrayLists
		this.clusters = new ArrayList<ArrayList<Integer>>();
		int i = 0;
		while(i < this.k) {
			this.clusters.add(new ArrayList<Integer>());
			i++;
		}
	}
	
	// This method is to be used to initialize the clusters
	public void setClusters(ArrayList<ArrayList<Integer>> clusters) {
		if (clusters.size() != this.k) {
			SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");
			Date date = new Date(System.currentTimeMillis());
			System.out.println(formatter.format(date)+" ERROR: Invalid size for the input ArrayList: " + clusters.size());
		}
		else {
			this.clusters = clusters;
			// update the node2Cluster Dictionary
			int clusterId = 1;
			for (ArrayList<Integer> cluster: this.clusters) {
				for (Integer node: cluster) {
					this.node2Cluster.put(node, clusterId);
				}
				clusterId++;
			}
		}
	}
	
	public ArrayList<ArrayList<Integer>> getClusters(){
		return this.clusters;
	};
	
	public Dictionary<Integer, Integer> getNode2Cluster(){
		return this.node2Cluster;
	}
	
	// This method is to be used to initialize the labels
	public void setLabels(Dictionary<Integer, String> labels) {
		this.labels = labels;
	}
	
	// Get the centroids
	public ArrayList<Integer> getCentroids() {
		return this.centroids;
	}
	
	// Load the seeds nodes from a file containing userIds mapping them from Long to int
    public static ArrayList<Integer> loadSeed(LongIntDict mapLong2Int, String seeds_filename) throws IOException {
    	// Load IDs from file
    	
        List<String> seeds = TxtUtils.txtToList(seeds_filename, 0);

        // List that will contain the seeds mapped from Long to Integer
        ArrayList<Integer> seedsAsInt = new ArrayList<Integer>();

        // Map the TwitterIDs from Long to integer
        for (int i = 0; i < seeds.size(); i++) {
            seedsAsInt.add(mapLong2Int.get(Long.parseLong(seeds.get(i))));
        }
        return seedsAsInt;
    }
	
	// Generate random centroids: choose k random nodes as centroids
	private void setRandomCentroids() {
		int nNodes = g.size;
		ArrayList<Integer> list = new ArrayList<Integer>();
        
		for (int i=0; i<nNodes; i++) {
			if(this.g.V[i] != -1){
				list.add(new Integer(this.g.V[i]));
			}
        }
        
		Collections.shuffle(list);
        
		for (int i=0; i<this.k; i++) {
            this.centroids.add(list.get(i));
        }

	}
	
	// Computes the average shortest path for a node within its cluster
	private double sumOfDistances(int node, ArrayList<Integer> cluster) {
		
		// Fetch the neighbors of node
		//int[] neighbors_Node = ArraysUtil.concat(this.g.in[node], this.g.out[node]);
		//int[] neighbors_Node_No_Dup = ArraysUtil.uniq(neighbors_Node);
	    double sum = 0.0;
	    for (int node2: cluster) {
	    	if (node2 != node) {
			// Fetch the neighbors of node2
			//int[] neighbors_Node2 = ArraysUtil.concat(this.g.in[node2], this.g.out[node2]);
			//int[] neighbors_Node2_No_Dup = ArraysUtil.uniq(neighbors_Node2);
			// distance= inverse of similarity
			//double distance = (1/ArraysUtil.cosineSimilarity(neighbors_Node_No_Dup, neighbors_Node2_No_Dup));
			double distance = distance(node, node2);
			if (Double.isInfinite(distance)) {
				distance = 0;
			}
    		sum += distance; 
	    	}
	    }
	    return sum;
	}
	
	// Converts a G graph into a Jgrapht graph
	/*
	private Graph<String, DefaultEdge> fromGToJgrapht(WeightedGraph g) {
		// Instantiate a new jgrapht graph
		Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		// Let's convert our graph into a jgrapht graph
		// For each node in the graph, add it to the jgrapht graph
		for (Integer node: g.V) {
			if(node != -1) {
				if(g.in[node] != null || g.out[node] != null) {
					graph.addVertex(node.toString());
				}
			}
		}
		
		// Add all "in" edges to the jgrapht graph
		for (Integer node1: g.V) {
			if(node1 != -1) {
				if(g.in[node1] != null) {
					for(Integer node2: g.in[node1]) {
						graph.addEdge(node2.toString(), node1.toString());
					}
				}
			}
		}
		
		// Add all "out" edges to the jgrapht graph
		for (Integer node1: g.V) {
			if(node1 != -1) {
				if(g.out[node1] != null) {
					for(Integer node2: g.out[node1]) {
						graph.addEdge(node1.toString(), node2.toString());
					}
				}
			}
		}
		
		return graph;
	}
	*/
	
	// Compute the centers of existing clusters
	private boolean computeCentroids() {
		int center = 0;
		double curSum;
		double sum;
		ArrayList<Integer> possibleCenters = new ArrayList<Integer>();
		// Here we empty the centers container because we are going to recompute them
		// and save the previous centroids that we will use to test the convergence
		ArrayList<Integer> previourCentroids = new ArrayList<Integer>();
		if (!this.centroids.isEmpty()) {
			for(int i=0; i<this.k; i++) {
				previourCentroids.add(this.centroids.get(i));
			}
		}
		this.centroids.clear();
		
		// For each cluster
		for (ArrayList<Integer> cluster: this.clusters) {
			
			/**
			 * compute center: the center is going to be the node having the minimum total sum 
			 * of distances between between it and all other nodes in the cluster
			 **/
			
			// Let's compute the sum of distances to other nodes for each node in the cluster and pick the node with the smallest sum as the center of the cluster
			curSum = Double.POSITIVE_INFINITY;
			possibleCenters.clear();
			
			for (Integer node: cluster) {
				sum = sumOfDistances(node, cluster);
				if(sum < curSum) {
					curSum = sum;
					possibleCenters.clear();
					possibleCenters.add(node); 
				}
				else if(sum == curSum) {
					possibleCenters.add(node); 
				}
			 }
			
			// pick randomly a center in case of ties
			Collections.shuffle(possibleCenters);
			center = possibleCenters.get(0);
			
			// add the center
			this.centroids.add(center);
		}
		
		return (this.centroids.equals(previourCentroids));
		
	}
	
	// We compute the distance between the center and a node as follows:
	// distance(c, n) = (1/cosineSimilarity(c, n)) + (1-Label_Similarity)
			// c: centroid
			// n: node
			// Label_Similarity: The percentage of neighbors of the node n having the same label(YES/NO) as the center
	private double distance(int c, int n) {
		
		// Computing distance(c, n)
		int[] neighbors_c = ArraysUtil.intersection(ArraysUtil.concat(this.g.in[c], this.g.out[c]), this.g.V);
		int[] neighbors_c_No_Dup = ArraysUtil.uniq(neighbors_c);
		int[] neighbors_n = ArraysUtil.intersection(ArraysUtil.concat(this.g.in[n], this.g.out[n]), this.g.V);
		int[] neighbors_n_No_Dup = ArraysUtil.uniq(neighbors_n);
		double distance = (1/ArraysUtil.cosineSimilarity(neighbors_c_No_Dup, neighbors_n_No_Dup));
	    
		// Computing Label_Similarity
		int[] cArr = {c};
		int[] nNeighbors = ArraysUtil.intersection(ArraysUtil.concat(this.g.in[n], this.g.out[n]), this.g.V);
		int[] nArr = {n};
		int[] nIncludedNeighbors = ArraysUtil.concat(nNeighbors, nArr);
		int[] cExcludedNIncludedNeighbors = ArraysUtil.remove(cArr, nIncludedNeighbors);
		int nbrSameLabel = 0;
		for (int node: ArraysUtil.uniq(cExcludedNIncludedNeighbors)) {
			if(this.labels.get(node).equalsIgnoreCase(this.labels.get(c))) {
				nbrSameLabel++;
			}
		}
		double Label_Similarity = Double.valueOf(nbrSameLabel)/Double.valueOf(ArraysUtil.uniq(cExcludedNIncludedNeighbors).length);
		
		//distance(c, n) + (1-Label_Similarity)
		return (distance + (1 - Label_Similarity));
	}
	
	// Assign each node of the graph (this.g) to one of the k clusters based on the distance with the k'th center.
	// Returns a boolean indicating true if all the nodes maintained their previous cluster and false otherwise.
	private void assignClusters() {
		// Assign clusters.
		// For each node in the graph we will assign it to a cluster based on the lowest distance value
		
		int clusterId;
		ArrayList<Integer> possibleClusterIds = new ArrayList<Integer>();
		
		double curDistance;
		double distance;
		
		for (int node: this.g.V) {
			possibleClusterIds.clear();
			if(node != -1) {
				clusterId = 0;
				curDistance = Double.POSITIVE_INFINITY;
				distance = 0;
				for(int center: this.centroids) {
					clusterId++;
					distance = distance(center, node);
					if(distance < curDistance) {
						curDistance = distance;
						possibleClusterIds.clear();
						possibleClusterIds.add(clusterId);
					}
					else if(distance == curDistance) {
						possibleClusterIds.add(clusterId);
					}
				} 
				
				// pick randomly a cluster Id in case of ties
				Collections.shuffle(possibleClusterIds);
				clusterId = possibleClusterIds.get(0);
				
				// Updating the cluster if changed
				if(this.node2Cluster.get(node) != clusterId) {
					// Remove node from the previous cluster
					if (node2Cluster.get(node) != 0) {
						this.clusters.get((node2Cluster.get(node))-1).remove(new Integer(node));
					}
					// Update the cluster
					this.node2Cluster.put(node, clusterId);
					this.clusters.get(clusterId-1).add(node);
				}
			}
		}
		
	}
	
	// The algorithm
	public void cluster() {
		
		// Starting the iterations
		SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		System.out.println(formatter.format(date)+" INFO: Clustering...");
		
		boolean converged = false;
		
		// Initialize/compute the cluster centers
		boolean empty = false;
		if(this.clusters.isEmpty()) {
			empty = true;
		}
		for (ArrayList<Integer> cluster: this.clusters) {
			if(cluster.isEmpty()) {
				empty = true;
			}
		}
		
		if(empty) {
			this.setRandomCentroids();
		}
		else {
			converged = this.computeCentroids();
		}
		
		int i = 0;
		
		while (true) {
			if ((i == this.maxIter && this.maxIter != 0) || converged) {
				break;
			}
			
			// Assign/Re-assign the nodes to clusters
			this.assignClusters();
			
			// re-compute the cluster centers
			converged = this.computeCentroids();
			
			i++;
			
			if (converged) {
				date = new Date(System.currentTimeMillis());
				System.out.println(formatter.format(date)+" INFO: Iteration "+i+" completed. The algorithm converged.");
			}
			else {
				date = new Date(System.currentTimeMillis());
				System.out.println(formatter.format(date)+" INFO: Iteration "+i+" completed. The algorithm didn't converge yet.");
			}
		}
		
		date = new Date(System.currentTimeMillis());
		System.out.println(formatter.format(date)+" INFO: Done clustering.");
		
	}
	
}