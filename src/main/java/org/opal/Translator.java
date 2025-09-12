package org.opal;

import java.nio.file.Paths;

public class Translator {

  public Translator() {
    var readDir = this.getClass().getClassLoader().getResource("program").getPath();
    var readPath = Paths.get(readDir + "/hello.opc");

    var reader = new Reader(readPath);
    var out = reader.process();
    System.out.println(out);

    var lexer = new Lexer(out);
    var tokens = lexer.process();
    System.out.println(tokens);
  }
}
