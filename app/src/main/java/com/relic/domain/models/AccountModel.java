package com.relic.domain.models;

/**
 * This model contains the preferences for an **AUTHENTICATED** account
 */
public class AccountModel {
    public String name;
    public boolean autoplayVideo;
    public boolean overAge;
    public boolean searchOverAge;
    public String defaultCommentSort;
    public int minLinkScore;
    public boolean publicVotes;
    public boolean showFlair;
    public boolean showLinkFlair;
    public boolean nightmode;
    public String acceptPMs;
}
