package com;

public class AppConfigs {
    public static final String PROJECT_DIR = "/Users/pratuat/course_materials/sbn/sbn_project/";

    public static final String USER_GRAPH_PATH = "./data/input/short_Official_SBN-ITA-2016-Net.gz";

    public static final String DATA_DIR = PROJECT_DIR + "data/";

    public static final String RESOURCES_DIR = DATA_DIR + "resources/";

    public static final String ALL_POLITICIANS_LIST = DATA_DIR + "input/all_politicians.csv";

    //... OUTPUT DATA FILES ...//

    public static final String ALL_USER = RESOURCES_DIR + "all_users.csv";

    public static final String USER_POLITICIAN_MAP = RESOURCES_DIR + "user_politician_distinct_map.csv";

    public static final String USER_TWEET_COUNT = RESOURCES_DIR + "user_tweet_count.csv";

    //... INDEXES ...//

    public static final String ALL_TWEET_INDEX = DATA_DIR + "indexes/all_tweet/";

    public static final String USER_TWEET_INDEX = DATA_DIR + "indexes/user_tweet/";

    public static final String USER_POLITICIAN_INDEX = DATA_DIR + "indexes/user_politician_map/";
}