# Useful-Parser
Some useful parser for personal data based  on public APIs.

###Email Parser

Concealing the privacy in email,containing **personal name**, **email name**, **phone number**, **url** and etc.
And this part is based on **scrubadub**, I sincerely appreciate his work. But, there also exist some special cases which its API can not handle.           

**Dependency**

if you have not install **scrubadub** before, you shall pip install it

```
pip install scrubadub
```

**Example** 

used the email data from **enron**
   
   
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

###Time Parser

Extract the time component from the daily used texting messages. Based on the current time, it could simply judge whether the message is related to the time by **containTime** , extract the time context of the message by **getTimeContext** and semantic target time by **extractTime**.

Should be noticed, these time or context could be mutiple from a single message. And, there still exist special cases that can not handle, but it is accurate enought for a demo system or a fault tolerant system.

Eg1. offset for 15mins
```
Message:Be there in 15 min
Current Time:2017-02-11 11:43:51.386 
Target  Time:2017-02-11 11:58:51.385-05:00
```

Eg2. the same time in tomorrow and the possible semantic time for 'Friday' depending on the present weekday.
```
Message:Coolness..I hv an interview tomorrow and on Friday..
Current Time:2017-02-11 11:43:51.386 
Target  Time:2017-02-12T11:43:51.710-05:00
Target  Time:2017-02-17T11:43:51.711-05:00
```


Eg3. combine the 'Thursday' and 'night'
```
Message:Home now. Will sleep. Working with <NAME>. Deadline is Thursday night.
Current Time:2017-02-11 11:43:51.386 
Target  Time:2017-02-16T18:00:00.646-05:00
```
