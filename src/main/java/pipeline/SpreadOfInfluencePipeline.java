package pipeline;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.queryparser.classic.ParseException;
import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.ArraysUtil;
import it.stilo.g.util.GraphReader;
import it.stilo.g.util.ZacharyNetwork;
import it.stilo.g.algo.ComunityLPA;
import gnu.trove.map.TIntLongMap;
import index.indexTweets;
import spread_of_influence.GraphKMeans;
import spread_of_influence.LPA;

@SuppressWarnings("unused")
public class SpreadOfInfluencePipeline {
	
	private static final String RESOURCES_LOCATION = "src/resources/";
	
	private static int getGraphSize(String fileName) {
		String line;
        HashSet<String> noDupSet = new HashSet<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(RESOURCES_LOCATION+fileName))) {
            while ((line = br.readLine()) != null) {
            	noDupSet.add(line.split("\t")[0]);
            	noDupSet.add(line.split("\t")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return noDupSet.size();
	}
	
	private static void runKMeansWithSeed(WeightedDirectedGraph g, LongIntDict mapLong2Int, String seedsYesFile, String seedsNoFile, String outputFile, String seeds) throws IOException {
		SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		System.out.println(formatter.format(date)+" INFO: Running the GraphKMeans algorithm using "+seeds+" as seeds...");
		
		// Instantiate a GraphKMeans object with k = 2 and maxIter = 1000
		GraphKMeans kMeans = new GraphKMeans(2, g, 50);
		
		// Load the seeds nodes from file mapping them from Long to int
		ArrayList<Integer> seedsYes = GraphKMeans.loadSeed(mapLong2Int, seedsYesFile);
		ArrayList<Integer> seedsNo = GraphKMeans.loadSeed(mapLong2Int, seedsNoFile);
		
		// Set the Yes seeds and No seeds as initial clusters of the GraphKMeans object
		ArrayList<ArrayList<Integer>> initialClusters = new ArrayList<ArrayList<Integer>>();
		initialClusters.add(seedsYes);
		initialClusters.add(seedsNo);
		kMeans.setClusters(initialClusters);
		
		// Initialize the labels for each node in the graph
		Dictionary<Integer, String> labels = new Hashtable<Integer, String>();
		for (int node: g.V) {
			labels.put(node, "None");
		}
		for (int node: seedsYes) {
			labels.put(node, "Yes");
		}
		for (int node: seedsNo) {
			labels.put(node, "No");
		}
		kMeans.setLabels(labels);
		
		// Perform the clustering operation
		kMeans.cluster();
		
		// Save the clustering result in a csv file
		Path file = Paths.get(RESOURCES_LOCATION+"Spread_Of_Influence_Output/KMeans/"+outputFile);
		
		TIntLongMap mapInt2Long = mapLong2Int.getInverted();
		
		ArrayList<String> lines = new ArrayList<String>(); 
		lines.add("Node,Cluster");
		
		int i = 1;
		for(ArrayList<Integer> cluster: kMeans.getClusters()) {
			for(Integer node: cluster) {
				lines.add(String.valueOf(mapInt2Long.get(node))+","+String.valueOf(i));
			}
			i++;
		}
		
		Files.write(file, lines, StandardCharsets.UTF_8);
		date = new Date(System.currentTimeMillis());
		System.out.println(formatter.format(date)+" INFO: Results saved in the folowing csv file: "+RESOURCES_LOCATION+"Spread_Of_Influence_Output/KMeans/"+outputFile+".");
		
	}
	
	public static void runGraphKMeans() throws FileNotFoundException, IOException, ParseException {
		/*
			// Create a fake graph for testing
			// the real network is going to be S(M)
			WeightedDirectedGraph g = new WeightedDirectedGraph(ZacharyNetwork.VERTEX);
			ZacharyNetwork.generate(g, 2);
			
			// Indices of nodes from group 1 (seeds)
			ArrayList<Integer> seedsYes = new ArrayList<Integer>();
			seedsYes.add(1);seedsYes.add(2);seedsYes.add(3);seedsYes.add(4);seedsYes.add(5);seedsYes.add(6);seedsYes.add(7);
			
			// Indices of nodes of group 2
			ArrayList<Integer> seedsNo = new ArrayList<Integer>();
			seedsNo.add(14);seedsNo.add(15);seedsNo.add(16);seedsNo.add(17);
		*/
		
		// Convert Twitter screenNames into TwitterIds
		SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");
		
		Date date = new Date(System.currentTimeMillis());
		System.out.println(formatter.format(date)+" INFO: Starting GraphKMeans...");
		
		//date = new Date(System.currentTimeMillis());
		//System.out.println(formatter.format(date)+" INFO: Converting the twitter screenNames into twitterIDs...");
		
		/*
		indexTweets.fromScreenNameToUserId(RESOURCES_LOCATION+"Yes_No_Identification/M_no.csv", RESOURCES_LOCATION+"Yes_No_Identification/M_no_IDs.csv");
		indexTweets.fromScreenNameToUserId(RESOURCES_LOCATION+"Yes_No_Identification/M_yes.csv", RESOURCES_LOCATION+"Yes_No_Identification/M_yes_IDs.csv");
		indexTweets.fromScreenNameToUserId(RESOURCES_LOCATION+"Yes_No_Identification/K_no.csv", RESOURCES_LOCATION+"Yes_No_Identification/K_no_IDs.csv");
		indexTweets.fromScreenNameToUserId(RESOURCES_LOCATION+"Yes_No_Identification/K_yes.csv", RESOURCES_LOCATION+"Yes_No_Identification/K_yes_IDs.csv");
		indexTweets.fromScreenNameToUserId(RESOURCES_LOCATION+"Yes_No_Identification/Mp_no.csv", RESOURCES_LOCATION+"Yes_No_Identification/Mp_no_IDs.csv");
		indexTweets.fromScreenNameToUserId(RESOURCES_LOCATION+"Yes_No_Identification/Mp_yes.csv", RESOURCES_LOCATION+"Yes_No_Identification/Mp_yes_IDs.csv");
		*/
		
		// load the S(M) graph mapping Long to Integer
		date = new Date(System.currentTimeMillis());
		System.out.println(formatter.format(date)+" INFO: Loading the S(M) network...");
	
		int graphSize = 10997;
		WeightedDirectedGraph g = new WeightedDirectedGraph(graphSize + 1);
		String graphFilename = "Sub_Graph_S_of_M.gz";
		LongIntDict mapLong2Int = new LongIntDict();
		GraphReader.readGraphLong2IntRemap(g, RESOURCES_LOCATION+graphFilename, mapLong2Int, false);
		
		
		/**
		 * run the GraphKMeans for the top k players
		 */
		
		//runKMeansWithSeed(g, mapLong2Int, RESOURCES_LOCATION+"Yes_No_Identification/K_yes.csv", RESOURCES_LOCATION+"Yes_No_Identification/K_no.csv", "GraphKMeans_Output_KPlayes.csv", "the K-Playes");
		
		
		/**
		 * run for M
		 */
		
		runKMeansWithSeed(g, mapLong2Int, RESOURCES_LOCATION+"Yes_No_Identification/M_yes.csv", RESOURCES_LOCATION+"Yes_No_Identification/M_no.csv", "GraphKMeans_Output_M.csv", "M");
		
		
		/**
		 * run for M prime
		 */
		
		runKMeansWithSeed(g, mapLong2Int, RESOURCES_LOCATION+"Yes_No_Identification/Mp_yes.csv", RESOURCES_LOCATION+"Yes_No_Identification/Mp_no.csv", "GraphKMeans_Output_Mprime.csv", "M_prime");
		
	}

    public static void runLPA() throws FileNotFoundException, IOException, org.apache.lucene.queryparser.classic.ParseException, InterruptedException {
    	/*
        // Create a fake graph for testing
        WeightedDirectedGraph g = new WeightedDirectedGraph(ZacharyNetwork.VERTEX);
        ZacharyNetwork.generate(g, 2);

        // Indices of nodes from group 1 (seeds)
        int[] seedsYes = new int[]{1, 2, 3, 4, 5, 6, 7};
        // Indices of nodes of group 2
        int[] seedsNo = new int[]{14, 15, 16, 17};

        lpaAlgorithm(g, seedsYes, seedsNo, "Test", 20);
        */
 
        
        
        // Convert Twitter names into TwitterID
    	SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");
		
		Date date = new Date(System.currentTimeMillis());
		System.out.println(formatter.format(date)+" INFO: LPA Algorithm...");
		//date = new Date(System.currentTimeMillis());
		// System.out.println(formatter.format(date)+" INFO: Converting the twitter screenNames into twitterIDs...");
        
		// uncomment this if running for the first time
		/*
    	indexTweets.fromScreenNameToUserId(RESOURCES_LOCATION+"Yes_No_Identification/K_no.csv", RESOURCES_LOCATION+"Yes_No_Identification/K_no_IDs.csv");
		indexTweets.fromScreenNameToUserId(RESOURCES_LOCATION+"Yes_No_Identification/K_yes.csv", RESOURCES_LOCATION+"Yes_No_Identification/K_yes_IDs.csv");
		*/
		
        // load graph and mapper
    	date = new Date(System.currentTimeMillis());
    	System.out.println(formatter.format(date)+" INFO: Loading the full network...");
        int graphSize = 450193;
    	//int graphSize = 6217;
        WeightedDirectedGraph g = new WeightedDirectedGraph(graphSize + 1);
        String graphFilename = "data/Official_SBN-ITA-2016-Net.gz";
        LongIntDict mapLong2Int = new LongIntDict();
        GraphReader.readGraphLong2IntRemap(g, RESOURCES_LOCATION+graphFilename, mapLong2Int, false);
        
        // Instantiate an LPA object
        LPA GraphLabelPropagator = new LPA();
        
        
        /**
		 * run for the top k players
		 */
        date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date)+" INFO: Running the LPA algorithm using the top K-Playes as seeds...");
        
        // Load the seeds nodes from file mapping them from Long to int
        int[] seedsYes = GraphLabelPropagator.loadSeed(mapLong2Int, RESOURCES_LOCATION+"Yes_No_Identification/K_yes.csv");
        int[] seedsNo = GraphLabelPropagator.loadSeed(mapLong2Int, RESOURCES_LOCATION+"Yes_No_Identification/K_no.csv");
        
        // Run the label propagation algorithm on the full graph
        GraphLabelPropagator.lpaAlgorithm(g, seedsYes, seedsNo, "_K-Players_", 100);
        
        // Save the clustering result in a csv file
 		Path file = Paths.get(RESOURCES_LOCATION+"Spread_Of_Influence_Output/Modified_LPA/Modifed_LPA_Output_K_Players.csv");
 		
 		TIntLongMap mapInt2Long = mapLong2Int.getInverted();
 		
 		ArrayList<String> lines = new ArrayList<String>(); 
 		lines.add("Node,Label");
 		
 		for(Map.Entry<Integer, Integer> entry : GraphLabelPropagator.nodesLabels.entrySet()) {
 			String label;
 			if(entry.getValue()==-1) {
 				label = "Yes";
 			}
 			else if(entry.getValue()==-2) {
 				label = "No";
 			}
 			else {
 				label = "N/A";
 			}
			lines.add(String.valueOf(mapInt2Long.get(entry.getKey()))+","+label);
 		}
 		
 		Files.write(file, lines, StandardCharsets.UTF_8);
        
 		date = new Date(System.currentTimeMillis());
 		System.out.println(formatter.format(date)+" INFO: Results saved in the folowing csv file: "+RESOURCES_LOCATION+"Spread_Of_Influence_Output/Modified_LPA/Modifed_LPA_Output_K_Players.csv.");
      
    }

    public static void runLPAX10() throws IOException {
    	
    	SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");
    	Date date = new Date(System.currentTimeMillis());
		System.out.println(formatter.format(date)+" INFO: About to run the LPA 10 times on the S(M) graph...");
		
		// load graph and mapper
    	date = new Date(System.currentTimeMillis());
    	System.out.println(formatter.format(date)+" INFO: Loading the S(M) network...");
        //int graphSize = 450193;
    	int graphSize = 10997;
        WeightedDirectedGraph g = new WeightedDirectedGraph(graphSize + 1);
        String graphFilename = "Sub_Graph_S_of_M.gz";
        LongIntDict mapLong2Int = new LongIntDict();
        GraphReader.readGraphLong2IntRemap(g, RESOURCES_LOCATION+graphFilename, mapLong2Int, false);
		
        int i = 1;
        while (i<=10) {
        	
        	date = new Date(System.currentTimeMillis());
        	System.out.println(formatter.format(date)+" INFO: Run N°"+i+".");
        	
        	// Run the label propagation algorithm on the S(M) graph
        	int[] labels = ComunityLPA.compute(g, 1.0, 1);
        	Integer[] labels_Integer =Arrays.stream(labels).boxed().toArray(Integer[]::new);
        	
        	// Create community Ids
        	Dictionary<Integer, Integer> Label2Community = new Hashtable<Integer, Integer>();
        	Label2Community.put(-1, -1);
        	Set<Integer> labels_set = new HashSet<Integer>(Arrays.asList(labels_Integer));
        	
        	int community_Id = 1;
        	for(int label: labels_set) {
        		if(label != -1) {
        			Label2Community.put(label, community_Id);
        			community_Id++;
        		}
        	}
            
            // Save the clustering result in a csv file
     		Path file = Paths.get(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_"+i+".csv");
     		
     		TIntLongMap mapInt2Long = mapLong2Int.getInverted();
     		
     		ArrayList<String> lines = new ArrayList<String>(); 
     		lines.add("Node,Comunity_ID");
     		
     		for(int j = 0; j<labels.length; j++) {
    			lines.add(String.valueOf(mapInt2Long.get(g.V[j]))+","+Integer.toString(Label2Community.get(labels[j])));
     		}
     		
     		Files.write(file, lines, StandardCharsets.UTF_8);
            
     		date = new Date(System.currentTimeMillis());
     		System.out.println(formatter.format(date)+" INFO: Output of run N°"+i+" saved in the folowing csv file: "+RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_"+i+".csv");
        	
        	i++;
        	
        }
    }
    
    // Function that return average of an array. 
    public static float average(int a[], int n) 
    { 
          
        // Find sum of array element 
        int sum = 0; 
          
        for (int i = 0; i < n; i++) 
            sum += a[i]; 
      
        return sum / n; 
    } 
	
    public static void chooseCommunityXUser() throws IOException {
    	
    	SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");
    	Date date = new Date(System.currentTimeMillis());
    	System.out.println(formatter.format(date)+" INFO: About to go over the 10 LPA runs to decide finally to which community a user u belongs to.");
    	
    	
    	// prepare the output file
    	Path file = Paths.get(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Final_Output.csv");
    	ArrayList<String> lines = new ArrayList<String>(); 
 		lines.add("Node,Comunity_ID");
    	
 		// prepare the file readers
    	BufferedReader[] readers = new BufferedReader[10];
    	readers[0] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_1.csv"));
    	readers[1] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_2.csv"));
    	readers[2] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_3.csv"));
    	readers[3] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_4.csv"));
    	readers[4] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_5.csv"));
    	readers[5] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_6.csv"));
    	readers[6] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_7.csv"));
    	readers[7] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_8.csv"));
    	readers[8] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_9.csv"));
		readers[9] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_10.csv"));
    	
		Long[] users = new Long[10];
		int[] communities = new int[10];
		int comminity_Id;
    			
		boolean noMoreLine = false;
		while (!noMoreLine) {
			int i = 0;
			boolean header = false;
			
			// Here we read the 10 userIds and the 10 communitie_Ids for the current line
			for (BufferedReader reader : readers){
				String line = reader.readLine();
				if (line == null){
					noMoreLine = true;
					break;
				}
				if (!line.split(",")[0].equals("Node")) {
					users[i] = Long.parseLong(line.split(",")[0]);
					communities[i] = Integer.parseInt(line.split(",")[1]);
				}
				else {
					header = true;
				}
				i++;
			}
			
			if(!header && !noMoreLine) {
				// Here we check if all the 10 users of the current line are the same
				Set<Long> users_set = new HashSet<Long>(Arrays.asList(users));
				if (users_set.size() != 1) {
					date = new Date(System.currentTimeMillis());
			 		System.out.println(formatter.format(date)+" ERROR: Differents users found on the same line number in the files. The file might not be in the same order.");
			 		break;
				}
				
				// Save the final community for current user
				List<Long> users_list = new ArrayList<Long>(users_set);
				// The final community_Id is going to be the average of the 10 community_Ids
				comminity_Id = Math.round(average(communities, communities.length));
				lines.add(String.valueOf(users_list.get(0))+","+comminity_Id);
			}
	    }
		
		Files.write(file, lines, StandardCharsets.UTF_8);
        
 		date = new Date(System.currentTimeMillis());
 		System.out.println(formatter.format(date)+" INFO: Final output saved in the folowing csv file: "+RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Final_Output.csv");
		
    }
    
    public static double computeNMI(Integer[] partition_a, Integer[] partition_b) {
    	// number of points to in clusters
    	double n = partition_a.length;
    	
    	// number of clusters
    	Set<Integer> partition_a_set = new HashSet<Integer>(Arrays.asList(partition_a));
    	Set<Integer> partition_b_set = new HashSet<Integer>(Arrays.asList(partition_b));
    	int k_a = partition_a_set.size();
    	int k_b = partition_b_set.size();
    	
    	// number of nodes in each cluster - partition_a
    	int[] number_node_per_cluster_a = new int[k_a];
    	List<Integer> partition_a_list = Arrays.asList(partition_a);
    	List<Integer> partition_a_set_list = new ArrayList<Integer>(partition_a_set);
    	Collections.sort(partition_a_set_list);
    	int i = 0;
    	for(Integer clusterId: partition_a_set_list) {
    		number_node_per_cluster_a[i] = Collections.frequency(partition_a_list, clusterId);
    		i++;
    	}
    	
    	// number of nodes in each cluster - partition_b
    	int[] number_node_per_cluster_b = new int[k_b];
    	List<Integer> partition_b_set_list = new ArrayList<Integer>(partition_b_set);
    	Collections.sort(partition_b_set_list);
    	List<Integer> partition_b_list = Arrays.asList(partition_b);
    	i = 0;
    	for(Integer clusterId: partition_b_set_list) {
    		number_node_per_cluster_b[i] = Collections.frequency(partition_b_list, clusterId);
    		i++;
    	}
    	
    	// number of shared nodes between clusters
    	double[][] number_shared_nodes_btw_clusters = new double[k_a][k_b];
    	
    	for(int h = 0; h < k_a; h++) {
    		// cluster h related to partition a
			int[] partition_a_cluster_h = new int[number_node_per_cluster_a[h]];
			int index = 0;
			for(int node = 1; node <= n; node++) {
				if(partition_a[node-1] == h+1) {
					partition_a_cluster_h[index] = node;
					index++;
				}
			}
			
    		for(int l = 0; l < k_b; l++) {
    				// cluster l related to partition b
    				int[] partition_b_cluster_l = new int[number_node_per_cluster_b[l]];
    				index = 0;
    				for(int node = 1; node <= n; node++) {
    					if(partition_b[node-1] == l+1) {
    						partition_b_cluster_l[index] = node;
    						index++;
    					}
    				}
    				number_shared_nodes_btw_clusters[h][l] = ArraysUtil.intersection(partition_a_cluster_h, partition_b_cluster_l).length;
    		}
    	}
    	
    	
    	// compute the numerator
    	double nmi_numerator = 0.0;
    	for(int h = 0; h < k_a; h++) {
    		for(int l = 0; l < k_b; l++) {
    			if (number_shared_nodes_btw_clusters[h][l] == 0)
    			{
    				nmi_numerator += 0; // https://math.stackexchange.com/questions/667854/normalized-mutual-information-results-in-log0-with-non-overlapping-clusters
    			}
    			else {
    				nmi_numerator += number_shared_nodes_btw_clusters[h][l]*(Math.log((n*number_shared_nodes_btw_clusters[h][l])/(Double.valueOf(number_node_per_cluster_a[h])*Double.valueOf(number_node_per_cluster_b[l])))/Math.log(2.0));
    			}
    		}
    	}
    	
    	// compute the tem1 of the denominator
    	double term1 = 0.0;
    	for(int h = 0; h < k_a; h++) {
    		term1 += Double.valueOf(number_node_per_cluster_a[h])*(Math.log(Double.valueOf(number_node_per_cluster_a[h])/n)/Math.log(2.0));
    	}
    	
    	// compute the tem2 of the denominator
    	double term2 = 0.0;
    	for(int l = 0; l < k_b; l++) {
    		term2 +=  Double.valueOf(number_node_per_cluster_b[l])*(Math.log( Double.valueOf(number_node_per_cluster_b[l])/n)/Math.log(2.0));
    	}
    	
    	// compute the denominator
    	double nmi_denominator = Math.sqrt(term1*term2);
    	
    	
    	return nmi_numerator/nmi_denominator;
    }
    
    public static void generateNMI_Matrix() throws IOException{
    	
    	SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");
    	Date date = new Date(System.currentTimeMillis());
    	System.out.println(formatter.format(date)+" INFO: About to compute the Normalized Mutual Information measure and plot the matrix that represents the NMI between the 10 LPA runs.");
    	
    	// will contain the file readers
    	BufferedReader[] readers = new BufferedReader[10];
    	
		// prepare the NMI Matrix
		double[][] NMI_Matrix = new double[10][10];
		
		// compute the NMIs
		for(int i = 0; i <= 9; i++) {
			// prepare the file readers
			readers[0] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_1.csv"));
	    	readers[1] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_2.csv"));
	    	readers[2] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_3.csv"));
	    	readers[3] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_4.csv"));
	    	readers[4] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_5.csv"));
	    	readers[5] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_6.csv"));
	    	readers[6] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_7.csv"));
	    	readers[7] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_8.csv"));
	    	readers[8] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_9.csv"));
			readers[9] = new BufferedReader(new FileReader(RESOURCES_LOCATION+"Spread_Of_Influence_Output/LPA/LPA_Output_Run_10.csv"));
			
			// prepare partition a
			BufferedReader reader = readers[i];
			String line;
			ArrayList<Integer> partition_a_list = new ArrayList<Integer>();
			int row = 1;
			while((line = reader.readLine()) != null){
				if(row != 1 && row != 2){
					partition_a_list.add(Integer.parseInt(line.split(",")[1]));
				}
				row++;
			}
			Integer[] partition_a = new Integer[partition_a_list.size()];
			partition_a = partition_a_list.toArray(partition_a);
			
			for(int j = 0; j <= 9; j++) {
				// prepare partition b
				Integer[] partition_b = partition_a;
				if(i != j) {
					reader = readers[j];
					ArrayList<Integer> partition_b_list = new ArrayList<Integer>();
					row = 1;
					while((line = reader.readLine()) != null){
						if(row != 1 && row != 2){
							partition_b_list.add(Integer.parseInt(line.split(",")[1]));
						}
						row++;
					}
					partition_b = new Integer[partition_b_list.size()];
					partition_b = partition_b_list.toArray(partition_b);
				}
				NMI_Matrix[i][j] = computeNMI(partition_a, partition_b);
			}
		}
		
		// Save the matrix in the file system
    	Path file = Paths.get(RESOURCES_LOCATION+"Spread_Of_Influence_Output/NMI/NMI_Matrix.csv");
    	ArrayList<String> lines = new ArrayList<String>();
    	String line;
 		for(int i = 0; i < 10; i++) {
 			line = "";
 			for(int j = 0; j < 10; j++) {
 				line = line + NMI_Matrix[i][j];
 				if(j != 9) {
 					line = line + ",";
 				}
 			}
 			lines.add(line);
 		}
 		Files.write(file, lines, StandardCharsets.UTF_8);
 		
 		date = new Date(System.currentTimeMillis());
 		System.out.println(formatter.format(date)+" INFO: The final matrix has been saved in the folowing csv file: "+RESOURCES_LOCATION+"Spread_Of_Influence_Output/NMI/NMI_Matrix.csv");
		
 		// Plot the Matrix
		
    }
    
	public static void run() throws FileNotFoundException, IOException, ParseException, InterruptedException {
		//int graphSize = getGraphSize("Sub_Graph_S_of_M");
		//System.out.println(graphSize);
		runGraphKMeans();
		//runLPA();
		//runLPAX10();
		//chooseCommunityXUser();
		//generateNMI_Matrix();
		
	}

}
