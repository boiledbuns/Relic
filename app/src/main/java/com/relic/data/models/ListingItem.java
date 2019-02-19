package com.relic.data.models;

public class ListingItem {

    // the id is the "full name" of an item
    protected String id = "";
    private boolean visited;
    private int userUpvoted;
    public boolean saved;

    public int userSubmittedPosition;
    public int userCommentsPosition;
    public int userSavedPosition;
    public int userUpvotedPosition;
    public int userDownvotedPosition;
    public int userGildedPosition;
    public int userHiddenPosition;

    public String getFullName() {
        return id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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