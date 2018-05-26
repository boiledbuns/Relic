package com.relic.domain.Listing;

import com.relic.domain.Thing;

import java.util.List;

public interface Listing {
  String getBefore();
  String getNext();
  List <Thing> getChildren();
}
