package org.opal;

import java.nio.file.Paths;

public class Translator {

  public Translator() {
    var readDir = this.getClass().getClassLoader().getResource("program").getPath();
    var readPath = Paths.get(readDir + "/test.opc");

    var reader = new Reader(readPath);
    var out = reader.process();
    System.out.println(out);

    var lexer = new Lexer(out);
    var tokens = lexer.process();
    System.out.println(tokens);

    var parser = new Parser(tokens);
    var root = parser.process();

    var pass1 = new Pass1(root);
    pass1.process();
  }
}
