package com.lothrazar.simpletomb.data;
public enum TombCommands {

  RESTORE, LIST, DELETE, KEY;

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
