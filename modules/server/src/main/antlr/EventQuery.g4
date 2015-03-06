grammar EventQuery;

expression
	: filter (PLUS filter)*
	;

filter
	: fieldFilter
	| logicalGroup
	;

fieldFilter
        : NAME ( (DOT equalsComparison) | ( DOT otherComparison )+ )
        ;

equalsComparison
	: EQ OPEN_PAR value CLOSE_PAR
	;

otherComparison
	: simpleComparison
        | containsComparison
        | arrayComparison
        | regularExpressionEvaluation
	;

simpleComparison
        : operation OPEN_PAR value CLOSE_PAR
        ;

containsComparison
	: containsOperation OPEN_PAR list CLOSE_PAR
	;

arrayComparison
	: arrayOperation OPEN_PAR list CLOSE_PAR
	;

regularExpressionEvaluation
	: RE OPEN_PAR REGEX CLOSE_PAR
	;

operation
	: LT | LE | GT | GE | NE
	;

containsOperation
        : IN | NIN
        ;

arrayOperation
        : ALL
        ;

logicalGroup
        : (OR | NOR) OPEN_PAR OPEN_BRACKET expression ( COMMA expression )* CLOSE_BRACKET CLOSE_PAR
        ;

list
	: OPEN_BRACKET value ( COMMA value )* CLOSE_BRACKET
	;

nameValuePair
	: NAME COMMA value
	;


EQ	: 'eq' ;
LT	: 'lt' ;
LE	: 'le' | 'lte' ;
GT	: 'gt' ;
GE	: 'ge' | 'gte' ;
NE	: 'ne' ;
RE	: 're' | 'regex' ;
IN	: 'in' ;
NIN	: 'nin' ;
ALL	: 'all' ;
OR	: 'or' ;
NOR	: 'nor' ;

DOT : '.' ;
OPEN_PAR : '(' ;
CLOSE_PAR : ')' ;
OPEN_BRACKET : '[' ;
CLOSE_BRACKET : ']' ;
PLUS : WS? '+' WS?;
COMMA : WS? ',' WS? ;

value
	: numeric | ( BOOLEAN | STRING | NULL ) //| name
	;

STRING 	: '"' ( ESC_SEQ | ~('\\'|'"') )* '"' ;
numeric	: INTEGER | DECIMAL ;
INTEGER	: DIGIT+ ;
DECIMAL	: DIGIT* '.' DIGIT+ ;
BOOLEAN	: 'true' | 'false' ;
NULL	: 'null' | 'NULL' ;

NAME
	: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
	;

REGEX
        : '/' ( REGEX_ESC_SEQ | ~('\\'|'/') )* '/' ('i'|'m'|'x'|'s')*
        ;

fragment
REGEX_ESC_SEQ
	: '\\' ('\\'|'^'|'$'|'.'|'|'|'?'|'*'|'+'|'('|')'|'['|']'|'/')
        ;

fragment
DIGIT 
	: '0'..'9'
	;



fragment
HEX_DIGIT
	: ('0'..'9'|'a'..'f'|'A'..'F')
	;

fragment
ESC_SEQ
	: '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
	| UNICODE_ESC
	| OCTAL_ESC
	;

fragment
OCTAL_ESC
	: '\\' ('0'..'3') ('0'..'7') ('0'..'7')
	| '\\' ('0'..'7') ('0'..'7')
	| '\\' ('0'..'7')
	;

fragment
UNICODE_ESC
	: '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	;

WS      : [ \t\n\r]+ -> skip ;