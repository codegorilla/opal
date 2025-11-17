Package Declaration
===================

The **package** declaration indicates that the current translation unit belongs to the specified package.

Each translation unit must contain exactly one package declaration. It must appear at the top of the file.

A package corresponds to a directory of individual
source files. Every source file in a given directory belongs to the same package.

Package declaration grammar rules:

> Note: Qualified names are not supported for now. This will be fixed in the future.

```
packageDeclaration
  : "package" packageName ";"
  ;
  
packageName
  : IDENTIFIER
  ;
```
