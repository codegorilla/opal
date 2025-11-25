
IMPORTS
=======

The import declaration is used to import package names into the current package.

A package may be imported with the `import` keyword as follows:

```
import <qualified-name>;
```

Entities from imported packages may be referred to by their fully qualified name. Provided that there are no other packages imported with the same final name of their fully qualified names, the entity may also be referred to by its partially qualified name.

In the example below, both variable declarations are valid:

```
import opal.lang.math;
var x: double = opal.lang.math.cos(30);
var y: double = math.cos(30);
```

However, in the following example, the third variable declaration is invalid:

```
import std.math;
import opal.lang.math;
var x: double = std.math.cos(30);
var y: double = opal.lang.math.cos(30);
var z: double = math.cos(30); // invalid
```

An import declaration may use an *as clause* to explicitly specify the import name alias.

In the next example, both variable declarations are valid:

```
import std.math;
import opal.lang.math as m;
var x: double = math.cos(30);
var y: double = m.cos(30);
```

The grammar for import declarations is as follows:

```
importDeclarations
  : importDeclaration+
  ;

importDeclaration
  : "import" importQualifiedName importAsClause? ";"
  ;

importAsClause
  : "as" importName
  ;

importQualifiedName
  : importName ("." importName)*
  ;

importName
  : IDENTIFIER
  ;
