package jp.co.rottenpear.bridge.config;

import java.util.Arrays;
import java.util.List;

public class BridgeSimulatorConfig {

    public static final int gamecount = 100;

    public static int syncCount = 0;

    public final static int syncLimit = 1;

    public static int calculateCount = 1000;

    public static ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    public static List<String> numberList =  Arrays.asList("A","K","Q","J","T","9","8","7","6","5","4","3","2");
}
