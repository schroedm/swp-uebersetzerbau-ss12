0|([1-9]+[0-9]*)		{num}
test{num}dfse			{test}
def{test}				{dummy}

%%

\(						{return("BRACKET", "(")}
\)						{return("BRACKET", ")")}
\{						{return("BRACKET", "{")}
\}						{return("BRACKET", "}")}
\=						{return("ASSIGNEMENT", "=")}
<						{return("OP", "L")}
>						{return("OP", "G")}
<=						{return("OP", "LE")}
>=						{return("OP", "GE")}
==						{return("OP", "EQ")}
!=						{return("OP", "NEQ")}
\+						{return("OP", "PLUS")}
\*						{return("OP", "MUL")}
/						{return("OP", "DIVIDE")}
;						{return("KEYWORD", "SEMIKOLON")}
def						{return("KEYWORD", "DEF")}
if						{return("KEYWORD", "IF")}
else					{return("KEYWORD", "ELSE")}
do						{return("KEYWORD", "DO")}
print					{return("KEYWORD", "PRINT")}
return					{return("KEYWORD", "RETURN")}
break					{return("KEYWORD", "BREAK")}
while					{return("KEYWORD", "WHILE")}
{num}					{return("NUM", parseInt())}
{num}?\.{num}			{return("RAT", parseDouble())}
[a-z]+[a-zA-Z0-9]*		{return("ID", parseString())}