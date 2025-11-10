package org.opal;

// This class provides a marker that can be set to a given value. The current
// value of the counter is available through the get() method. It is
// initialized to a generally invalid value for debugging purposes.

// To do: We might reset it to the invalid value whenever the get() method is
// called to enforce one-time use. One we can have a boolean that tracks if it
// is has already been used. In this case, an exception can be thrown if the
// get() method is called when it hasn't been primed after use.

public class Marker {

  public int current;

  public Marker () {
    current = -1;
  }

  public void set (int value) {
    current = value;
  }

  public int get () {
    return current;
  }

}
