package user_tweet;

import com.opencsv.CSVReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtility {
    public static List<String> listPoliticianIds(String filename){

        List<String> politician_ids = new ArrayList<String>();

        try {
            try (CSVReader csvReader = new CSVReader(new FileReader(filename));) {
                String[] values = null;
                while ((values = csvReader.readNext()) != null) {
                    politician_ids.add(values[0]);
                }
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

        return politician_ids;
    }

    public static String[] loadColumnsFromCSV(String fileName, int columnIndex){

        List<String> column = new ArrayList<>();

        try {
            try (CSVReader csvReader = new CSVReader(new FileReader(fileName));) {
                String[] values = null;
                while ((values = csvReader.readNext()) != null) {
                    column.add(values[columnIndex]);
                }
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

        return column.stream().toArray(String[]::new);
    }

    public static PrintWriter getPrintWriter(String fileName) throws IOException {

        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        return new PrintWriter(bufferedWriter);
    }

    public static void writeToFile(String fileName, Object[] objects) throws IOException {
        PrintWriter writer = getPrintWriter(fileName);

        int len = objects.length;

        for (int i=0; i<len; i++) {
            writer.write(objects[i] + "\n");
        }

        writer.close();
    }

    public static List<String[]> loadCSV(String fileName){

        List<String[]> dataFrame = new ArrayList<>();

        try {
            try (CSVReader csvReader = new CSVReader(new FileReader(fileName));) {
                String[] row = null;
                while ((row = csvReader.readNext()) != null) {
                    dataFrame.add(row);
                }
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

        return dataFrame;
    }

    public static List<String[]> loadSortCSV(String fileName, int sortIndex) {

        List<String[]> userData = loadCSV(AppConfigs.USER_TWEET_COUNT);

        Comparator<String[]> comparator = new Comparator<String[]>() {
            @Override
            public int compare(String[] ra, String[] rb) {
                Integer a = new Integer(Integer.parseInt(ra[sortIndex].trim()));
                Integer b = new Integer(Integer.parseInt(rb[sortIndex].trim()));

                return a.compareTo(b);
            }
        };

        userData.sort(comparator.reversed());

        return userData;
    }

    public static List<String[]> loadSortFilterCSV(String fileName, int sortIndex, int filterIndex, String filter) {

        List<String[]> userData = loadSortCSV(fileName, sortIndex);

        return userData.stream().filter((String[] row) -> row[filterIndex].trim().equals(filter)).collect(Collectors.toList());
    }
}
