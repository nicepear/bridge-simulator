package jp.co.rottenpear.bridge.model;

import java.util.Comparator;

public class BridgeCardCompare implements Comparator<Integer> {

    @Override
    public int compare(Integer o1, Integer o2) {

        Integer t1 = Integer.valueOf(o1.toString());
        Integer t2 = Integer.valueOf(o2.toString());
        if (o1.intValue() == 65) {
            t1 = 165;
        }
        if (o1.intValue() == 75) {
            t1 = 164;
        }
        if (o1.intValue() == 81) {
            t1 = 163;
        }
        if (o1.intValue() == 74) {
            t1 = 162;
        }
        if (o1.intValue() == 84) {
            t1 = 161;
        }
        if (o2.intValue() == 65) {
            t2 = 165;
        }
        if (o2.intValue() == 75) {
            t2 = 164;
        }
        if (o2.intValue() == 81) {
            t2 = 163;
        }
        if (o2.intValue() == 74) {
            t2 = 162;
        }
        if (o2.intValue() == 84) {
            t2 = 161;
        }
        return t1 > t2 ? -1 : (t1 == t2 ? 0 : 1);
    }

}
