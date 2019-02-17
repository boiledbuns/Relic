package com.relic.data.models;


import android.arch.persistence.room.Ignore;

public class ListingItem {

    // the id is the "full name" of an item
    protected String id = "";
    private boolean visited;
    private int userUpvoted;
    public boolean saved;

    @Ignore public int userSubmittedPosition;
    @Ignore public int userCommentsPosition;
    @Ignore public int userSavedPosition;
    @Ignore public int userUpvotedPosition;
    @Ignore public int userDownvotedPosition;
    @Ignore public int userGildedPosition;
    @Ignore public int userHiddenPosition;

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