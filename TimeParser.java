package 

import java.util.ArrayList;
import java.util.regex.Pattern;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

/**
 * Created by CharlieQiu on 2017/2/8.
 */

public class TimeParser {
    static String[] Weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday","Sunday","Mon","Tue","Wed","Thur","Fri"};
    static String[] Expression = {"morning", "afternoon", "evening", "night", "tonight", "noon"};

    static String[] Expression_pre = {"last","this","next","yesterday","tomorrow","tomo","tmrw"};
    static String[] words_day = {"yesterday","today","tomorrow","tomo","tmrw"};

    static String[] Time_suffix = {"a.m.", "p.m.","AM", "PM"};
    static String[] Time_punctuation = {"!",",",".","?"};
    static String[] Time_span = {"hour","hrs","hr","hours","mins","minute","minutes","min"};
    static String[] Time_minute = {"mins","minute","minutes","min"};
    static String[] Time_hour = {"hour","hours","hr","hrs"};
    static String[] Time_preposition = {"at","around","till","until"};
    static String[] Time_middle = {"to","or"};

    private DateTime time_set;
    //current time
    private int current_day;
    private int current_hour;
    private int current_minute;
    private int current_second;
    private int current_weekday;
    //targer time
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int weekday;

    private static boolean Contains_String(String[] Str, String word) {
        for(int i = 0; i < Str.length; i++)
            if (word.toLowerCase().equals(Str[i].toLowerCase()))
                return true;
        return false;
    }
    private static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]+$");
        return pattern.matcher(str).matches();
    }

    private static boolean isLetter(String str) {
        Pattern pattern = Pattern.compile("^[[a-z]|[A-Z]]+$");
        return pattern.matcher(str).matches();
    }
    private boolean isTime(String s){
        if(!(s.contains(":")||s.contains("#")))return false;
        if(s.contains("#")&&s.contains(":"))
            s=s.replace("#","");
        if(s.contains("#")){
            s=s.split("#")[0];
            if(!isInteger(s))return false;
            int time = Integer.parseInt(s);
            if(time>2359||time<0)return false;
            if(time%100>59)return false;
            return true;
        }
        else {
            String[] tmp = s.split(":");
            if (tmp.length != 2) return false;
            if (isInteger(tmp[0]) && isInteger(tmp[1]) && Integer.parseInt(tmp[0]) < 24 && Integer.parseInt(tmp[1]) < 60)
                return true;
            else return false;
        }
    }
    private static String Weekday2Context(String s){
        s=s.toLowerCase();
        if(s.equals("monday")||s.equals("mon"))return "Monday";
        else if(s.equals("tuesday")||s.equals("tuesday"))return "Tuesday";
        else if(s.equals("wednesday")||s.equals("wed"))return "Wednesday";
        else if(s.equals("thursday")||s.equals("thur"))return "Thursday";
        else if(s.equals("friday")||s.equals("fri"))return "Friday";
        else if(s.equals("saturday")||s.equals("sat"))return "Saturday";
        else if(s.equals("sunday")||s.equals("sun"))return "Sunday";
        return "";
    }
    private static int Weekday2Int(String weekday){
        switch (weekday){
            case "Monday":return 1;
            case "Tuesday":return 2;
            case "Wednesday":return 3;
            case "Thursday":return 4;
            case "Friday":return 5;
            case "Saturday":return 6;
            case "Sunday":return 7;
            default:return -1;
        }
    }

    public boolean containTime(String message){
        ArrayList<String> contexts =getTimeContext(message);
        if(contexts.size()==0)return false;
        return true;
    }

    public ArrayList<Long> extractTime(String message){
        ArrayList<Long> results = new ArrayList<>();
        ArrayList<String> contexts =getTimeContext(message);
        if(contexts.size()==0)return results;
        for(String context:contexts)
            results.add(context2Time(context));
        return results;
    }

    public String preprocess(String s){
        String result="";
        for(int i=0;i<s.length();i++){
            if(Contains_String(Time_punctuation, s.charAt(i)+"")){
                //float
                if(s.charAt(i)=='.'&&(i+1)<s.length()&&i-1>=0&&isInteger(s.charAt(i-1)+"")&&isInteger(s.charAt(i-1)+""))
                    result+=".";
                else if(i-1>=0&&isInteger(s.charAt(i-1)+""))result+="# ";
                else result+=" ";
            }
            else if(isInteger(s.charAt(i)+"")){
                if(i-1>=0&&isLetter(s.charAt(i-1)+""))result+=" ";
                result+=s.charAt(i);
                if(i+1<s.length()&&isLetter(s.charAt(i+1)+""))result+=" ";
                if(i==s.length()-1)result+="#";
            }else {
                result+=s.charAt(i);
            }
        }
        return result;
    }

    public void getCurrentTime(){
        time_set = new DateTime();
        day=current_day=time_set.getDayOfMonth();
        weekday=current_weekday=time_set.getDayOfWeek();
        hour=current_hour=time_set.getHourOfDay();
        minute=current_minute=time_set.getMinuteOfHour();
        second=current_second=time_set.getSecondOfMinute();
    }
    public ArrayList<String> getTimeContext(String sentence){
        ArrayList<String> contexts = new ArrayList<>();
        String s = preprocess(sentence);
//        Log.e("Pre",s);
        ArrayList<String> tokens=split(s);
        for(int i=0;i<tokens.size();i++){
            String result="";
            String token = tokens.get(i).toLowerCase();
//            Log.e("token",token);
            if(Contains_String(Weekdays,token)){
                if(i-1>=0&&Contains_String(Expression_pre,tokens.get(i-1)))
                    result+= tokens.get(i-1).toLowerCase()+" ";
                result+=Weekday2Context(token);
                if(i+1<tokens.size()&&Contains_String(Expression,tokens.get(i+1))){
                    result+=" "+tokens.get(i+1);
                    i+=1;
                }
            }else if(Contains_String(Expression,token)){
                if(i-1>=0&&Contains_String(Expression_pre,tokens.get(i-1)))
                    result+= tokens.get(i-1)+" ";
                result+=token;
                if(i-1>=0&&tokens.get(i-1).toLowerCase().equals("good"))
                    result="";
            }else if(Contains_String(words_day,token)){
                if(i+1<tokens.size()&&Contains_String(Expression,tokens.get(i+1)))continue;
                if(token.equals("tomo")||token.equals("tmrw"))result+="tomorrow";
                else result+=token;
            }else {
                if(isTime(token)){
                    //no context here, so don't accept one integer
                    if(tokens.size()==1){
                        continue;
                    }
                    token=token.replace("#","");
                    token=token.replace(":","");
                    result+=token;
                    if (i + 1 < tokens.size() && Contains_String(Time_suffix, tokens.get(i + 1)))
                        result += " "+tokens.get(i + 1);
                    else if (i + 1 < tokens.size() && Contains_String(Time_span, tokens.get(i + 1))){
                        if(Contains_String(Time_minute, tokens.get(i + 1)))result += " minute";
                        else if(Contains_String(Time_hour,tokens.get(i+1)))result += " hour";
                    }
                }
                else if(isInteger(token)){
                    if (i + 1 < tokens.size() && Contains_String(Time_suffix, tokens.get(i + 1)))
                        result += token+" " + tokens.get(i + 1);
                    else if (i + 1< tokens.size() && Contains_String(Time_span, tokens.get(i + 1))){
                        if(Contains_String(Time_minute, tokens.get(i + 1)))result += token+" minute";
                        else if(Contains_String(Time_hour,tokens.get(i+1)))result += token+" hour";
                    }
                    else if (i - 1 >= 0 && Contains_String(Time_preposition, tokens.get(i - 1))&&((Integer.parseInt(token)>99&&Integer.parseInt(token)<2359)||(Integer.parseInt(token)<24&&Integer.parseInt(token)>=0)))
                        result += token;
                    else if(i-1>=0 && Contains_String(Time_middle,tokens.get(i-1)))
                        result += token;
                    else if(i+1< tokens.size() && Contains_String(Time_middle,tokens.get(i+1)))
                        result += token;
                }
            }
            if(result.equals(""))continue;
            contexts.add(result);
        }

        return contexts;
    }

    public long context2Time(String sentence){
//        Log.e("Context",sentence);
        ArrayList<String> contexts = split(sentence);

        getCurrentTime();

//        Log.e("Current Time",time_set+"");


        if(isOffset(contexts)){
            extractOffset(contexts);
        }else {
            if(contexts.size()==1){
                String context = contexts.get(0);
                if(isInteger(context)){
                    int time_set = Integer.parseInt(context);
                    if(time_set<24) {
                        if (time_set < 8) time_set += 12;
                        hour = time_set;
                        minute = 0;
                        second = 0;
                    }else{
                        int hour_set = time_set/100;
                        if(hour_set < 8) hour_set += 12;
                        hour=hour_set;
                        minute=time_set%100;
                        second=0;
                    }
                }else if(Contains_String(Weekdays,context)){
                    extractWeekday(context);
                }else if(Contains_String(Expression,context)){
                    extractExpression(context);
                }else if(Contains_String(words_day,context)){
                    extractWordDay(context);
                }
            }else if(contexts.size()==2){
                String first=contexts.get(0);
                String second=contexts.get(1);
                if(isInteger(first)){
                    if(Contains_String(Time_span,second)) extractTimeSpan(Integer.parseInt(first),second);
                    else if(Contains_String(Time_suffix,second)) extractTimeSuffix(Integer.parseInt(first),second);
                }else if(Contains_String(Expression,second)){
                    extractExpression(first,second);
                }else if(Contains_String(Weekdays,second)){
                    extractWeekday(first,second);
                }
            }
        }

//        Log.e("Day",day+"");
//        Log.e("Weekday",weekday+"");
//        Log.e("Hour",hour+"");
//        Log.e("Minute",minute+"");
//        Log.e("Second",second+"");

        time_set = time_set.plusDays(-current_day + day);
        time_set = time_set.plusHours(-current_hour + hour);
        time_set = time_set.plusMinutes(-current_minute + minute);
        time_set = time_set.plusSeconds(-current_second+second);

        return time_set.getMillis();
    }

    private static ArrayList<String> split(String sentence) {
        ArrayList<String> WordList = new ArrayList<>();
        String[] strings = sentence.split(" ");
        for(String s :strings)
                WordList.add(s);
        return WordList;
    }

    private boolean isOffset(ArrayList<String> contexts){
        if (contexts.size()!=2)return false;
        if(!isInteger(contexts.get(0)))return false;
        if(!Contains_String(Time_span,contexts.get(1)))return false;
        return true;
    }
    private void extractOffset(ArrayList<String> contexts){
        int time = Integer.parseInt(contexts.get(0));
        switch (contexts.get(1)){
            case "minute":
                minute+= time;
                break;
            case "hour":
                hour+= time;
                break;

        }
    }
    private void extractExpression(String s){
        switch (s){
            case "morning":
                hour=9;
                minute=0;
                second=0;
                break;
            case "evening":
            case "night":
            case "tonight":
                hour=18;
                minute=0;
                second=0;
                break;
            case "noon":
                hour=12;
                minute=0;
                second=0;
                break;
            case "afternoon":
                hour=15;
                minute=0;
                second=0;
                break;
        }
    }
    private void extractTimeSpan(int time,String span){
        switch (span){
            case "minute":
                minute+=time;
                break;
            case "hour":
                hour+=time;
                break;
        }
    }
    private void extractExpression(String pre,String expression){
        if(Contains_String(Weekdays,pre))extractWeekday(pre);
        else{
            switch (pre){
                case "last":
                case "yesterday":
                    day-=1;
                    break;
                case "this":
                    break;
                case "next":
                case "tomorrow":
                    day+=1;
                    break;
            }
        }
        extractExpression(expression);
    }
    private void extractWeekday(String context){
        int weekday_set = Weekday2Int(context);
        if(current_weekday>weekday_set){
            weekday=weekday_set;
            day+= 7 -current_weekday+weekday_set;
        }else if(current_weekday<weekday_set){
            weekday=weekday_set;
            day+=weekday_set-current_weekday;
        }
    }
    private void extractWeekday(String pre,String context){
        int weekday_set = Weekday2Int(context);
        switch (pre){
            case "last":
                if(current_weekday>weekday_set){
                    weekday=weekday_set;
                    day-= current_weekday-weekday_set;
                } else{
                    weekday=weekday_set;
                    day-=7;
                    day+=weekday_set-current_weekday;
                }
                break;
            case "this":
                if(current_weekday<weekday_set){
                    weekday=weekday_set;
                    day+=weekday_set-current_weekday;
                }else{
                    weekday=weekday_set;
                    day-=current_weekday-weekday_set;
                }
                break;
            case "next":
                weekday=weekday_set;
                day+=7;
                day-=current_weekday-weekday_set;
                break;
        }
    }
    private void extractTimeSuffix(int time_set,String suffix){
        switch (suffix){
            case "am":
                if(time_set<13) {
                    hour = time_set;
                    minute = 0;
                    second = 0;
                }else{
                    int hour_set = time_set/100;
                    hour=hour_set;
                    minute=time_set%100;
                    second=0;
                }
                break;
            case "pm":
                if(time_set<13) {
                    hour = time_set+12;
                    minute = 0;
                    second = 0;
                }else{
                    int hour_set = time_set/100;
                    hour_set += 12;
                    hour=hour_set;
                    minute=time_set%100;
                    second=0;
                }
                break;
        }
    }
    private void extractWordDay(String word_day){
        switch (word_day){
            case "yesterday":
                day-=1;
                break;
            case "tomorrow":
                day+=1;
                break;
        }
    }
}
