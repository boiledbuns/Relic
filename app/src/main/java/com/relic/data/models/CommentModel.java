package com.relic.data.models;

public class CommentModel extends ListingItem {
    public static int UPVOTE = 1;
    public static int DOWNVOTE = -1;
    public static int NOVOTE = 0;
    public static String TYPE = "t1_";

    public String author;
    private String body;
    public String created;
    private int score;

    public String authorFlairText;

    public int platinum;
    public int gold;
    public int silver;

    public boolean isSubmitter;

    public String edited;
    public int depth;
    public int replyCount;
    public String replyLink;

    public String linkTitle;
    public String linkAuthor;

    public CommentModel() {}

    public String getBody() {
    return body == null ? "" : body;
    }

    public void setBody(String body) {
    this.body = body;
    }

    public int getScore() {
    return score;
    }

    public void setScore(int score) {
    this.score = score;
    }

    public float position;

    public boolean isLoadMore() {
      return author == null;
  }
}
