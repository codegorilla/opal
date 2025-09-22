package org.opal;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class Args {

  @Parameter
  public List<String> parameters = new ArrayList<>();

  @Parameter(
    names = "--name",
    description = "User name",
    required = true
  )
  private String name;

  public String getName () {
    return name;
  }

//  @Parameter (
//    names = { "-log", "-verbose" },
//    description = "Level of verbosity"
//  )
//  private Integer verbose = 1;
//
//  @Parameter (
//    names = "-groups",
//    description = "Comma-separated list of group names to be run"
//  )
//  private String groups;
//
//  @Parameter (
//    names = "-debug",
//    description = "Debug mode"
//  )
//  private boolean debug = false;
//
//  private Integer setterParameter;
//
//  @Parameter (
//    names = "-setterParameter",
//    description = "A parameter annotation on a setter method"
//  )
//  public void setParameter(Integer value) {
//    this.setterParameter = value;
//  }
}
