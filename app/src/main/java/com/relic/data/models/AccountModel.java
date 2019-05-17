package com.relic.data.models;

import com.relic.domain.Account;

/**
 * This model contains the preferences for an **AUTHENTICATED** account
 */
public class AccountModel implements Account {
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
