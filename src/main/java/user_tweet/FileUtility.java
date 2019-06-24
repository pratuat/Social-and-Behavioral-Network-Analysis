package user_tweet;

import com.AppConfigs;
import com.opencsv.CSVReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtility {
    public static List<String> list_politician_ids(){
        List<String> politician_ids = new ArrayList<String>();

        try {
            try (CSVReader csvReader = new CSVReader(new FileReader(AppConfigs.LIST_POLITICIANS));) {
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

    public static List<Integer> list_all_user_ids(){
        List<Integer> user_ids = new ArrayList<Integer>();

        try {
            try (CSVReader csvReader = new CSVReader(new FileReader(AppConfigs.OUTPUT_PATH + "all_user_ids.csv"));) {
                String[] values = null;
                while ((values = csvReader.readNext()) != null) {
                    user_ids.add(Integer.valueOf(values[0]));
                }
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

        return user_ids;
    }
}
