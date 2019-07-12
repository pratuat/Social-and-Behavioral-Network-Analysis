## METHODS
### iii. Identify mentions of candidates and Yes/No supporters

#### iii.1 Largest Connected Components
Largest Connected Component (LCC) of a graph is the largest possible subset of the original graph with all the vertices connected through a path of any possible length. Real world networks often comprise of mutltiple disjoint component of nodes which are connected graph themselves. The good majority of the nodes from original graph often forms the Largest Connected Component and hence anlysis done on LCC generalises well for whole of the graph giving the advantage of reduced computational need due to smaller number of nodes and avoidance of complexity due to highly clustered or disjoint graph components.

#### iii.2 HITS analysis

#### iii.3 KPP-NEG algorithm

## RESULTS AND DISCUSSIONS


#### iii.1 Identifying tweets mentioning politicians

In this section we analyzed the network of twitter users who have expressed their opinions regarding politicins by mentioning them directly in tweets. All the politician screen names were loaded from the csv. A lucene query was built that checked the inclusion of any of the politician names in the `mentioned` field of the original tweet. The query structure is outlined as following.

$$ mentioned: politician_1 OR mentioned: politician_2 OR mentioned: politician_3 OR ... $$

All the resulting documents for the query were indexed into new lucene index, called USER_TWEET_INDEX . A list of unique user identifier was created from the above query results hence giving us the set of base users (M) and the resulting index of tweets T(M).

#### iii.2 Subgraph Analysis

The second fold of the graph analysis involved generating the M user sub-graph from the the root graph originally provided. We leveraged existing java graphing library `G`(https://github.com/giovanni-stilo/G) to extract subgraphs for set of users M and then further on extract the largest connected component of the sub-graph. Lastly we ran HITS analysis on that sub-graph to identify 2000 highest ranked (Authority) users.



* user sub-graph S(M)
* largest connected component
* compute HITS
* 2000 hightes ranked authority users
* classification of users in YES / NO group

#### iii.3 Partitioning of users in YES/NO group

The purpose of this section was to classify the user set M into YES and NO vote users taking into consideration the candidates they mention. For the computational convenience we intially built a lucene index USER_POLITICIAN_INDEX with the documents comprising of just two fields `userScreenName` and `politicianScreenName`. The document entry was created for each single mention of a politician in the USER_TWEET_INDEX. Then on for each user in set M, we counted number of mentions of both YES and NO politicians. Depending upon the frequency of mentions, whichever is greater, we classify the user to that group, i.e,

E u belongs to M
if n_y > n_n then classify user as YES
if n_y < n_n then classify user as NO
if n_y == n_n then classify user as neutral

Using above classification measure, user set M was classified into two groups and HITS analysis was performed onto respective largest connected components of the sub-graphs to obtain 1000 authority users for each group, namely M'. The sub-graph extraction and HITS analysis was performed using G library.

* Identifying the user mentioning more frequency candidates, classify user, measuer HITS centrality
* find 1000 supporters for each YES/NO party (M')

#### iv.4
Lastly in this section we used KPP-NEG algortihm on the user sub-graph S(M) to identify key players. Because of high computational cost we applied varying threshold value for each node's out-degree centrality to reduce the graph size.




### Identifying Yes/No supporters
