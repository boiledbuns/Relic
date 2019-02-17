package com.relic.data.models;


import android.arch.persistence.room.Ignore;

import org.jetbrains.annotations.Nullable;

public class ListingItem {
    private static final int UNINITIALIZED = -1;

    public String id = "";
    @Ignore
    public boolean isVisited;
    public int userUpvoted;
    public boolean saved;

    @Ignore public int userSubmittedPosition;
    @Ignore public int userCommentsPosition;
    @Ignore public int userSavedPosition;
    @Ignore public int userUpvotedPosition;
    @Ignore public int userDownvotedPosition;
    @Ignore public int userGildedPosition;
    @Ignore public int userHiddenPosition;

    public String getFullName() {
        return "";
    }
}