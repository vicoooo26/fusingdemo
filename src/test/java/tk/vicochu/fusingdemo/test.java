package tk.vicochu.fusingdemo;

import tk.vicochu.fusingdemo.Entry.ResilienceEntry;

public class test {

    public static void main(String[] args) {
//        HystrixEntry hystrixEntry = new HystrixEntry("true");
//        hystrixEntry.execute();
        new ResilienceEntry().realDo();
    }
}
