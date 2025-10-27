translationUnit (packageDeclaration, importDeclarations, packageName, declarations) ::=  <<
// Interface unit

<packageDeclaration>

import std;
<importDeclarations>

namespace <packageName; separator="::"> {
  <declarations>
}
>>
