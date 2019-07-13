# TWEET ANALYSIS ON ITALIAN REFERENDUM

### SOCIAL AND BEHAVIORAL NETWORK ANALYSIS [FALL 2018/19]

#### SUBMITTED BY:
Malick Alexandre N. Sarr **(1788832)**  
Pratuat Amatya **(1800063)**  
Vigèr Durand Azimedem Tsafack **(1792126)**  

### **PROJECT SETUP**

#### 1. Clone the repository
The repository can be downloaded by using the github file download link or can be cloned locally using the following command.
```
git clone git@github.com:pratuat/sbn_project.git
```

#### 2. Specify data stream path
The tweet data bundle has to be explicitly configured for the application to discover it. For that, go to **src/main/java/utils/AppConfigs.java** and specify the path to **TWEET_STREAM** like follwoing.

```java
// in your $PROJET_DIR/src/main/java/utils/AppConfigs.java
...

public static final String TWEET_STREAM = "path_to_your_data_stream";

...
```

#### 3. Specify user graph path
One more thing to configure is to specify the path to user graph. For that go to **src/main/java/utils/AppConfigs.java** and specify the path to **USER_GRAPH_PATH** variable like follwoing.

```java
// in your $PROJET_DIR/src/main/java/utils/AppConfigs.java
...

public static final String USER_GRAPH_PATH = "path_to_user_graph";

...
```

#### 4. Run the main class **'$PROJECT_DIR/src/main/java/Main.java'**

### **REPORT**

[Link to report (PDF).](report/report.pdf)









