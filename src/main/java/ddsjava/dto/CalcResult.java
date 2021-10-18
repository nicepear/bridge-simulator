package ddsjava.dto;

import jp.co.rottenpear.bridge.model.Hand;

import java.util.List;

public class CalcResult {
    private List<Hand> handList;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private int count;
}
