translationUnit (packageDeclaration, importDeclarations, packageName, declarations, definitions) ::=  <<
// Implementation unit

<packageDeclaration>

namespace <packageName; separator="::"> {
  <declarations; separator="\n\n">

  <definitions; separator="\n\n">
}
>>
