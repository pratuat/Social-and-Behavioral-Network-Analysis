package com.sbn.twitter;

//Dummy Classes
public class Tweet {
    String user_id;
    String tweet_id;
    String content;

    public Tweet(String user, String tweet, String text){
        user_id = user;
        tweet_id = tweet;
        content = text;
    }

    public void show(){
        System.out.println(user_id + tweet_id + content);
    }
}
