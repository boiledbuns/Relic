package com.relic.data.models;

public class CommentModel implements ListingItem {
  public static int UPVOTE = 1;
  public static int DOWNVOTE = -1;
  public static int NOVOTE = 0;
  public static String TYPE = "t1_";

  private String id;

  public String author;
  private String body;
  public String created;
  private int score;
  private int userUpvoted;

  public String authorFlairText;

  public int platinum;
  public int gold;
  public int silver;

  public boolean isSubmitter;

  public String edited;
  public int depth;
  public int replyCount;
  public String replyLink;

  public CommentModel() {}

  // Note that fullname = type + id
  public String getFullName() {
    return TYPE + id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

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

  public int getUserUpvoted() {
    return userUpvoted;
  }

  public void setUserUpvoted(int userUpvoted) {
    this.userUpvoted = userUpvoted;
  }

  public float position;

  public boolean isLoadMore() {
      return author == null;
  }
}
