package ddsjava.dto;

import java.util.Objects;

public class SimpleCard {
    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    private String suit;

    public String getNumbdr() {
        return numbdr;
    }

    public void setNumbdr(String numbdr) {
        this.numbdr = numbdr;
    }

    private String numbdr;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleCard that = (SimpleCard) o;
        return Objects.equals(suit, that.suit) && Objects.equals(numbdr, that.numbdr);
    }
}
