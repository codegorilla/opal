translationUnit (packageDeclaration, importDeclarations, declarations, definitions) ::=  <<
// Implementation unit

<packageDeclaration>

namespace {
  <declarations; separator="\n\n">

  <definitions; separator="\n\n">
}
>>
