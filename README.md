# Useful-Parser
Some useful parser for personal data based  on public APIs.

###Email Parser

Concealing the privacy in email,containing **personal name**, **email name**, **password**, **url** and etc.
And this part is based on scrubadub, I sincerely appreciate his work. But, there also exist some special cases which its API can not handle.           

Example
   
Original Email Body
```
Can you send me a schedule of the salary and level of everyone in the 
 scheduling group. Plus your thoughts on any changes that need to be made. 
 (Patti S for example)
 Phillip
```
 
Cleaned up Email Body
```
{{NAME}},
  Can you send me a schedule of the salary and level of everyone in the 
 scheduling group. {{NAME}} your thoughts on any changes that need to be made. 
 ({{NAME}} {{NAME}} for example)
 {{NAME}} 
 ```