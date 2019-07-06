package user_tweet;

import com.AppConfigs;
import com.opencsv.CSVReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    public static List<String> loadColumnsFromCSV(String fileName, int columnIndex){

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

        return column;
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
            System.out.println("Writing " + i + "/" + len);
            writer.write(objects[i] + "\n");
        }

        writer.close();
    }

//    public static List<Integer> list_all_user_ids(){
//        List<Integer> user_ids = new ArrayList<Integer>();
//
//        try {
//            try (CSVReader csvReader = new CSVReader(new FileReader(AppConfigs.OUTPUT_PATH + "all_user_ids.csv"));) {
//                String[] values = null;
//                while ((values = csvReader.readNext()) != null) {
//                    user_ids.add(Integer.valueOf(values[0]));
//                }
//            }
//
//        } catch (Exception e) {
//            System.out.println(e.getStackTrace());
//        }
//
//        return user_ids;
//    }
}
