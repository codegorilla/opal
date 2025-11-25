Use Declarations
================

Use declarations bring names of objects and types into the current namespace.

The grammar for use declarations is as follows:

```
useDeclarations
  : useDeclaration+
  ;

useDeclaration
  : "use" useQualifiedName ";"
  ;

useQualifiedName
  : useName "." useQualifiedNameTail
  ;
  
useQualifiedNameTail
  : useNameWildcard
  | useNameGroup
  | useName ("." useQualifiedNameTail)?
  ;

useNameWildcard
  : "*"
  ;

useNameGroup
  : "{" useName ("," useName)* "}"
  ;
  
useName
  : IDENTIFIER
  ;
```
