package org.opal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {

  private final String directoryName;

  public Translator (String directoryName) {
    this.directoryName = directoryName;
    loadPackage();
  }

  private Set<Path> getFiles () {
    // Get list of all files in the package directory
    var packageUrl  = this.getClass().getClassLoader().getResource(directoryName);
    var packageDir  = Objects.requireNonNull(packageUrl).getPath();
    var packagePath = Paths.get(packageDir);

    try (var filePaths = Files.list(packagePath)) {
      return filePaths
        .filter(Files::isRegularFile)
        .filter(filePath -> filePath.getFileName().toString().endsWith(".opc"))
        .collect(Collectors.toSet());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // This loads resources from the classpath. We actually want to load from the current directory or perhaps $OPATH
  // because this is a CLI application. How can you make a java program look in the current directory?

  private void loadPackage () {

    var files = getFiles();
    for (var item : files)
      System.out.println(item);
  }
//    Set<Path> filesToProcess = null;
//    // Get a list of all files in the directory and process each individually.
//    try (var filePaths = Files.list(packagePath)) {
//      filesToProcess = filePaths
//        .filter(filePath -> Files.isRegularFile(filePath))
//        .filter(filePath -> filePath.getFileName().toString().endsWith(".opc"))
//        .collect(Collectors.toSet());
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }

//    try (var filePaths = Files.list(packagePath)) {
//      for (var filePath : filePaths.toList()) {
//        if (Files.isRegularFile(filePath) && filePath.getFileName().toString().endsWith(".opc"))
//          System.out.println(filePath.getFileName());
//      }

/*
    var readPath = Paths.get(packagePath + "/" + "test.opc");

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
 */

}
