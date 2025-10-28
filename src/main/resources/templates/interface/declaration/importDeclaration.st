
importDeclaration (importQualifiedName, namespaceQualifiedName, aliasName) ::= <<
import <importQualifiedName>;
<if(aliasName)>namespace <aliasName> = <namespaceQualifiedName>;<endif>
>>
