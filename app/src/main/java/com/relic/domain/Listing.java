package com.relic.domain;

import java.util.List;

public interface Listing {
  String getBefore();
  String getNext();

  List <Thing> getChildren();
}
