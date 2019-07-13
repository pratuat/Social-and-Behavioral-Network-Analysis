package utils;

public class AppConfigs {

    public static final String PROJECT_DIR = "./";

    public static final String DATA_DIR = PROJECT_DIR + "data/";

    public static final String RESOURCES_DIR = DATA_DIR + "resources/";


    //... RESOURCE FILES ...//

    public static final String USER_GRAPH_PATH = DATA_DIR + "input/Official_SBN-ITA-2016-Net.gz";

    public static final String ALL_POLITICIANS = DATA_DIR + "input/all_politicians.csv";

    public static final String YES_POLITICIANS = DATA_DIR + "input/yes_politicians.csv";

    public static final String NO_POLITICIANS = DATA_DIR + "input/no_politicians.csv";
    
    public static final String STOP_WORDS_LOCATION = DATA_DIR + "input/stopwords.txt";

    //... OUTPUT DATA FILES ...//

    public static final String ALL_USER = RESOURCES_DIR + "all_users.csv";

    public static final String USER_TWEET_COUNT = RESOURCES_DIR + "user_tweet_count.csv";

    public static final String USER_GRAPH_LCC_PATH = RESOURCES_DIR + "user_induced_lcc_sub_graph.gz";

    public static final String ALL_USERS_TOP_AUTHORITIES = RESOURCES_DIR + "top_authorities/all_users_top";

    public static final String YES_USERS_TOP_AUTHORITIES = RESOURCES_DIR + "top_authorities/yes_users_top";

    public static final String NO_USERS_TOP_AUTHORITIES = RESOURCES_DIR + "top_authorities/no_users_top";

    public static final String YES_USERS_500KPP = RESOURCES_DIR + "top_authorities/yes_users_500KPP.csv";

    public static final String NO_USERS_500KPP = RESOURCES_DIR + "top_authorities/no_users_500KPP.csv";

    public static final String CLUSTER_LOCATION_YES = RESOURCES_DIR + "yesClusters/yes_";

    public static final String CLUSTER_LOCATION_NO = RESOURCES_DIR + "noClusters/no_";

    public static final String CA_KCORE_YES = RESOURCES_DIR + "cooc/kcore/yes/KCORE_YES_";

    public static final String CA_KCORE_NO = RESOURCES_DIR + "cooc/kcore/no/KCORE_NO_";

    public static final String CA_LCC_YES = RESOURCES_DIR + "cooc/lcc/yes/LCC_YES_";

    public static final String CA_LCC_NO = RESOURCES_DIR + "cooc/lcc/no/LCC_NO_";

    public static final String GRAPH_K_MEANS_OUTPUT_KPLAYES = RESOURCES_DIR + "Spread_Of_Influence_Output/KMeans/GraphKMeans_Output_KPlayes.csv";
    
    public static final String GRAPH_K_MEANS_OUTPUT_M = RESOURCES_DIR + "Spread_Of_Influence_Output/KMeans/GraphKMeans_Output_M.csv";
    
    public static final String GRAPH_K_MEANS_OUTPUT_MPRIME = RESOURCES_DIR + "Spread_Of_Influence_Output/KMeans/GraphKMeans_Output_Mprime.csv";
    
    public static final String YES_LABELS_TEMPORAL_COUNTER = RESOURCES_DIR + "Spread_Of_Influence_Output/Modified_LPA/yes_labels_temporal_counter_K-Players_.csv";
    
    public static final String NO_LABELS_TEMPORAL_COUNTER = RESOURCES_DIR + "Spread_Of_Influence_Output/Modified_LPA/no_labels_temporal_counter_K-Players_.csv";
    
    public static final String UNKNOWN_LABELS_TEMPORAL_COUNTER = RESOURCES_DIR + "Spread_Of_Influence_Output/Modified_LPA/unknown_labels_temporal_counter_K-Players_.csv";
    
    public static final String MODIFIED_LPA_OUTPUT = RESOURCES_DIR + "Spread_Of_Influence_Output/Modified_LPA/Modifed_LPA_Output_K_Players.csv";
    
    public static final String LPA_RUN_OUTPUT = RESOURCES_DIR + "Spread_Of_Influence_Output/LPA/LPA_Output_Run";
    
    public static final String LPA_FINAL_OUTPUT = RESOURCES_DIR + "Spread_Of_Influence_Output/LPA/LPA_Final_Output.csv";

    public static final String NMI_MATRIX = RESOURCES_DIR + "Spread_Of_Influence_Output/NMI/NMI_Matrix.csv";
    
    public static final String SUB_GRAPH_S_OF_M = RESOURCES_DIR + "Sub_graph_S_of_M.gz";

    public static final String M_YES = RESOURCES_DIR + "top_authorities/M_yes.csv";

    public static final String M_NO = RESOURCES_DIR + "top_authorities/M_no.csv";

    public static final String MP_YES = YES_USERS_TOP_AUTHORITIES + "_authorities.csv";

    public static final String MP_NO = NO_USERS_TOP_AUTHORITIES + "_authorities.csv";

    public static final String K_YES = YES_USERS_500KPP;

    public static final String K_NO = NO_USERS_500KPP;

    //... INDEXES ...//

    public static final String ALL_TWEET_INDEX = DATA_DIR + "indexes/all_tweet/";

    public static final String USER_TWEET_INDEX = DATA_DIR + "indexes/user_tweet/";

    public static final String USER_POLITICIAN_INDEX = DATA_DIR + "indexes/user_politician/";

    public static final String USER_YES_POLITICIAN_INDEX = DATA_DIR + "indexes/user_yes_politician/";

    public static final String USER_NO_POLITICIAN_INDEX = DATA_DIR + "indexes/user_no_politician/";

}