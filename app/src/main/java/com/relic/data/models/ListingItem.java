package com.relic.data.models;

public class ListingItem {

    // the id is the "full name" of an item
    protected String fullName = "";
    private boolean visited;
    private int userUpvoted;
    public boolean saved;
    public String subreddit = "";

    public int userSubmittedPosition;
    public int userCommentsPosition;
    public int userSavedPosition;
    public int userUpvotedPosition;
    public int userDownvotedPosition;
    public int userGildedPosition;
    public int userHiddenPosition;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public int getUserUpvoted() {
        return userUpvoted;
    }

    public void setUserUpvoted(int userUpvoted) {
        this.userUpvoted = userUpvoted;
    }
}