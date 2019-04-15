package  utils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

/**
 *
 * @author stilo
 */
public class StatusWrapper {

    private static long GCOUNT = 0;
    private long time;
    private Map<String, Object> local = Collections.synchronizedMap(new HashMap<String,Object>());

    private static Map<String, Object> global = Collections.synchronizedMap(new HashMap<String,Object>());
    private String lang = "null";
    private long count;
    private Status status = null;
    private String state = PROCESS_STATE;
    private final static Matcher m = Pattern.compile("^<o><t>([0-9]+)</t><l>([a-zA-Z]+.[a-zA-Z]*)</l><p><\\!\\[CDATA\\[(.*)\\]\\]></p></o>").matcher("");
    public static String PROCESS_STATE = "process";
    public static String SKIP_STATE = "skip";
    public static String STOP_STATE = "stop";
    private String rawJson;
    

    public StatusWrapper(long tms, Status st) {
        synchronized (this) {
            this.count = GCOUNT;
            GCOUNT++;
        }
        this.time = tms;
        this.status = st;
    }

    public StatusWrapper() {
        synchronized (this) {
            this.count = GCOUNT;
            GCOUNT++;
        }
    }

    public void load(String xmlLine) throws TwitterException {
        if (status == null) {
            m.reset(xmlLine);
            if (m.find()) {
                this.time = Long.parseLong(m.group(1));
                this.lang = m.group(2);
                this.status = DataObjectFactory.createStatus(m.group(3));
                this.rawJson= m.group(3);
            }
        }
    }

    /*public String getRawJson() {
        return rawJson;
    }*/
    
    public long getCount() {
        return count;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Status getStatus() {
        return status;
    }

    public long getTime() {
        return time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, Object> getLocal() {
        return local;
    }

    public Map<String, Object> getGlobal() {
        return global;
    }
    
    public synchronized String getRawJson(){
        if(this.rawJson==null){
            this.rawJson=DataObjectFactory.getRawJSON(status);
        }
        return this.rawJson;
    }

    public String toXMLLine() {
        String ret = "<o>";
        ret += "<t>" + time + "</t>";
        ret += "<l>" + lang + "</l>";
        
        if(DataObjectFactory.getRawJSON(status)!=null)
            ret += "<p><![CDATA[" + DataObjectFactory.getRawJSON(status) + "]]></p>";
        else
            ret += "<p><![CDATA[" + this.rawJson + "]]></p>";
        ret += "</o>\n";

        return ret;
    }
    
    //"<o><t>12</t><l>f</l><p><![CDATA["+"]]></p></o>"
}

