package com;

public class AppConfigs {
    public static final String PROJECT_DIR = "/Users/pratuat/course_materials/sbn/sbn_project/";

    public static final String DATA_DIR = PROJECT_DIR + "data/";

    public static final String RESOURCES_DIR = DATA_DIR + "resources/";

    //... RESOURCE FILES ...//

    public static final String USER_GRAPH_PATH = DATA_DIR + "input/Official_SBN-ITA-2016-Net_short.txt.gz";
    // public static final String USER_GRAPH_PATH = DATA_DIR + "input/Official_SBN-ITA-2016-Net.gz";

    public static final String ALL_POLITICIANS = DATA_DIR + "input/all_politicians.csv";

    public static final String YES_POLITICIANS = DATA_DIR + "input/yes_politicians.csv";

    public static final String NO_POLITICIANS = DATA_DIR + "input/no_politicians.csv";

    //... OUTPUT DATA FILES ...//

    public static final String ALL_USER = RESOURCES_DIR + "all_users.csv";

    public static final String USER_TWEET_COUNT = RESOURCES_DIR + "user_tweet_count.csv";

    public static final String USER_TWEET_COUNT_TRIMMED = RESOURCES_DIR + "user_tweet_count_trimmed.csv";

    public static final String USER_GRAPH_LCC_PATH = RESOURCES_DIR + "user_induced_lcc_sub_graph.gz";

    public static final String ALL_USERS_TOP_AUTHORITIES = RESOURCES_DIR + "top_authorities/all_users_top";

    public static final String YES_USERS_TOP_AUTHORITIES = RESOURCES_DIR + "top_authorities/yes_users_top";

    public static final String NO_USERS_TOP_AUTHORITIES = RESOURCES_DIR + "top_authorities/no_users_top";

    public static final String YES_USERS_500KPP = RESOURCES_DIR + "top_authorities/yes_users_500KPP.csv";

    public static final String NO_USERS_500KPP = RESOURCES_DIR + "top_authorities/no_users_500KPP.csv";

    //... INDEXES ...//

    public static final String ALL_TWEET_INDEX = DATA_DIR + "indexes/all_tweet/";

    public static final String USER_TWEET_INDEX = DATA_DIR + "indexes/user_tweet/";

    public static final String USER_POLITICIAN_INDEX = DATA_DIR + "indexes/user_politician/";

    public static final String USER_YES_POLITICIAN_INDEX = DATA_DIR + "indexes/user_yes_politician/";

    public static final String USER_NO_POLITICIAN_INDEX = DATA_DIR + "indexes/user_no_politician/";
}