package org.opal;

// This class provides a counter that can be initialized with a chosen starting
// value (which defaults to zero). The counter can only be incremented,
// decremented, or reset to its initial starting value. The current value of
// the counter is available through the get() method.

public class Counter {

  public final int start;
  public int current;

  public Counter () {
    this.start = 0;
    current = 0;
  }

  public Counter (int start) {
    this.start = start;
    current = this.start;
  }

  public void decrement () {
    current -= 1;
  }

  public void increment () {
    current += 1;
  }

  public void reset () {
    current = start;
  }

  public int get () {
    return current;
  }

}
