
importDeclaration (importQualifiedName, namespaceQualifiedName, aliasName) ::= <%
import <importQualifiedName>;
<if(aliasName)><\n>namespace <aliasName> = <namespaceQualifiedName>;<endif>
%>
