package com.riiablo.attributes;

public class GemAttributes extends Attributes {
  GemAttributes() {
    super(StatList.obtain(StatListFlags.NUM_GEM_LISTS));
  }

  @Override
  public Attributes reset() {
    throw new UnsupportedOperationException();
  }
}