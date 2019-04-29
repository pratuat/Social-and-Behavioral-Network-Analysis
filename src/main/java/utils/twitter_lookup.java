/*
package utils;//import utils.csv;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import twitter4j.TwitterException;
import utils.csv;
import utils.twitter;

public abstract class twitter_lookup {

    private static List<String[]> fromMatrixNameToMatrixTwitterID(List<String[]> politiciansMatrix) {
        politiciansMatrix.forEach((politiciansLine) -> {
            if (politiciansLine != politiciansMatrix.get(0)) {
                try {
                    String politician_name = politiciansLine[0];
                    String politician_vote = politiciansLine[2];
                    politician_name = twitter.fromNameToTwitterScreenName(politician_name);
                    politiciansLine[0] = politician_name;
                    politiciansLine[1] = politician_vote;
                } catch (InterruptedException ie) {
                    System.out.println(ie.getMessage());
                }
            }
        });

        return politiciansMatrix;
    }

    public static void main(String[] args) throws TwitterException, FileNotFoundException, IOException {
        String csvFile = "./src/util/test.utils.csv";

        List<String[]> politicians = csv.read_csv(csvFile, ",");
        politicians = fromMatrixNameToMatrixTwitterID(politicians);
        csv.write_csv(politicians, "./src/util/politicians_loadeds.utils.csv");
    }
}*/