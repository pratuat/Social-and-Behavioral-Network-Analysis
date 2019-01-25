import com.sbn.twitter.*;

public class Main {

    public static void main(String[] args){

        System.out.println("Hello World !!!");

        MyNewClass object = new MyNewClass("Blah Blah Blah...");

        object.showString();

        Tweet tweet = new Tweet("Matteo Salvini", "123", "blah blah");
        tweet.show();
    }
}
