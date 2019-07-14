import pipeline.*;

public class Main {

    public static void main(String[] args) throws Exception {
        TemporalAnalysisPipeline.run();
        UserAnalysisPipeline.run();
        SpreadOfInfluencePipeline.run();
    }
}
