package com.relic.data.Listing;

import com.relic.domain.Thing;

import java.util.List;

public abstract class Listing {
  abstract String getBefore();
  abstract String getNext();

  abstract List <Thing> getChildren();
}
