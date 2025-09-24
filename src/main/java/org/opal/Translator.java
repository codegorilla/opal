package org.opal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Set;
import java.util.stream.Collectors;

public class Translator {

  public Translator (String packageDirectory) {
    var packagePath = getPackagePath(packageDirectory);
    if (packagePath == null) {
      System.out.println("error: specified package does not exist");
      System.exit(1);
    }
    var filePaths = getFilePaths(packagePath);
    // For each file, load and process
    for (var filePath : filePaths) {
      loadFile(filePath);
    }
  }

  private Set<Path> getFilePaths (Path packagePath) {
    // Given a package path
    final var OPAL_EXTENSION = ".opc";
    try (var filePaths = Files.list(packagePath)) {
      return filePaths
        .filter(Files::isRegularFile)
        .filter(filePath -> filePath.getFileName().toString().endsWith(OPAL_EXTENSION))
        .collect(Collectors.toSet());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Path getPackagePath (String packageDirectory) {
    // Given a package directory, return the path
    var packagePath = Paths.get(packageDirectory).toAbsolutePath();
    if (!Files.exists(packagePath) || !Files.isDirectory(packagePath))
      return null;
    else
      return packagePath;
  }

  private void loadFile (Path filePath) {
    var reader = new Reader(filePath);
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
