package com.relic.domain.models;

import com.squareup.moshi.JsonClass;

//@JsonClass(generateAdapter = true)
public class CommentModel extends ListingItem {
    public static int UPVOTE = 1;
    public static int DOWNVOTE = -1;
    public static int NOVOTE = 0;
    public static String TYPE = "t1_";

    public String id;
    private String body;
    public String created;
    private int score;

    // post parent id, NOT fullname
    // TODO tbh really need to tidy up consistency with convention
    // TODO I know I used id for fullname of posts, but should be changed to fullname there
    public String parentPostId;
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

    @Override
    public String getFullName() {
        return "t1_" + id;
    }
}
