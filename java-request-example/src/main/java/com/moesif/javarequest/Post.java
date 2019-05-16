package com.moesif.javarequest;

public class Post {

  private final long id;
  private final String name;

  public Post(long id, String name) {
    this.id = id;
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
