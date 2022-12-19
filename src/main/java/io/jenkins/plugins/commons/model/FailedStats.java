package io.jenkins.plugins.commons.model;

import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;

public class FailedStats {

    @Getter
    @Setter
    private int high;

    @Getter
    @Setter
    private int medium;

    @Getter
    @Setter
    private int low;

    public FailedStats(int high, int medium, int low) {
        this.high = high;
        this.medium = medium;
        this.low = low;
    }

    public FailedStats(String high, String medium, String low) {
        try {
            this.high = Integer.parseInt(high);
            this.medium = Integer.parseInt(medium);
            this.low = Integer.parseInt(low);
        } catch (NumberFormatException numberFormatException) {
            Logger.getLogger(FailedStats.class.getName()).log(Level.SEVERE, null, numberFormatException);
        }
    }
}
