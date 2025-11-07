package org.opal;

import org.opal.error.Error;

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
    // To do: For each file, load and process
    for (var filePath : filePaths) {
      loadFile(filePath);
    }
  }

  private Set<Path> getFilePaths (Path packagePath) {
    // Given a package path, return a list of all files in the package
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
    var source = reader.process();
    // Lines are used for error analysis. We don't necessarily want the lexer
    // to operate on lines of text since program elements may span multiple
    // lines or multiple elements may occur on a single line.
    var sourceLines = source.lines().toList();

    var lexer = new Lexer(source);
    var tokens = lexer.process();
    System.out.println(tokens);
    var parser = new Parser(tokens);
    var root = parser.process();

    var pass1 = new Pass1(root);
    pass1.process();

    // Determine import aliases
    var pass10 = new Pass10(root, sourceLines);
    pass10.process();
    var pass20 = new Pass20(root, sourceLines);
    pass20.process();
//    var pass30 = new Pass30(root, sourceLines);
//    pass30.process();

    var generator1 = new Generator1(root);
    generator1.process();
    var generator2 = new Generator2(root);
    generator2.process();
//    var generator3 = new Generator3(root);
//    generator3.process();
  }

}
