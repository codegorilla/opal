Translation Unit
================

A translation unit is a single source code file.

Translation unit grammar rules:

```
translationUnit
  : packageDeclaration importDeclarations? useDeclarations? otherDeclarations?
  ;
```
