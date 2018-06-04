package com.relic.data.models;

import com.relic.domain.Account;
import com.relic.domain.Post;

public class PostModel implements Post {
   Account author;
   int commentCount;
   int karma;

   String title;
   String subName;
   String stringDate;
   String id;
}
