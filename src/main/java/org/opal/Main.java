package org.opal;

// The way this should work is that the compile command is given to compile one
// package, which is a directory. The compiler must compile all files in the
// directory. Each file is a translation unit, but the ASTs from each must be
// combined into a single AST prior to performing semantic analysis. The
// semantic analysis and code generation is thus done for the entire combined
// package.

import com.beust.jcommander.JCommander;

public class Main {
  public static void main(String[] args) {
    var jArgs = new Args();
    JCommander.newBuilder()
      .addObject(jArgs)
      .build()
      .parse(args);
    var name = jArgs.getName();
    var translator = new Translator(name);
  }
}
