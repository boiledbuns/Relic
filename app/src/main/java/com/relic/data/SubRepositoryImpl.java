package com.relic.data;

public class SubRepositoryImpl implements SubRepository {
  private SubRepositoryImpl INSTANCE;

  private SubRepositoryImpl() {

  }

  public SubRepositoryImpl getSubRepository() {
    if (INSTANCE == null) {
      INSTANCE = new SubRepositoryImpl();
    }
    return INSTANCE;
  }


}
