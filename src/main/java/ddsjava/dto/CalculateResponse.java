package ddsjava.dto;

import jp.co.rottenpear.bridge.model.Hand;

import java.util.List;

public class CalculateResponse {
    public List<Hand> getCalculateResults() {
        return calculateResults;
    }

    public void setCalculateResults(List<Hand> calculateResults) {
        this.calculateResults = calculateResults;
    }

    private List<Hand> calculateResults;

    public String getProbably() {
        return probably;
    }

    public void setProbably(String probably) {
        this.probably = probably;
    }

    private String probably;
}
