memberUsingDeclaration (accessSpecifier, name, type, declarator) ::= <%
<if(accessSpecifier)><accessSpecifier>: <endif>
using <name> = <type> <declarator>;
%>
