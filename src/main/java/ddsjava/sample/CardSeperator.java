package ddsjava.sample;

public class CardSeperator {
    public static void man(String[] args) {

        String code="3968KTAJQ";
        byte[] bytes=code.getBytes();

        for(byte bite:bytes){

//            if(bite==65){
//                byte[] bites={bite};
//                System.out.println(new String(bites));
//            }
            System.out.println(bite);
        }
//        String npbnCode="2Q4.8Q359K7.5QT.. ";
//
//        String[] abc=npbnCode.split("\\.");
//
//        System.out.println(abc[0]);
//        System.out.println(abc[1]);
//        System.out.println(abc[2]);
//        System.out.println(abc[3]);
//        String allHands = null;
//
//        allHands = "E:AT5.AJT.A632.KJ7 Q763.KQ9.KQJ94.T 942.87653..98653 KJ8.42.T875.AQ42";
//
//        String npbnCode = "AT5.AJT.A632.KJ7";
//        String pbnCode = "";
////        String npbnCode="AT5.AJT.A632.KJ7";
//        List<SimpleCard> cardList = new ArrayList<SimpleCard>();
//        for (int i = 0; i < 4; i++) {
//            for (int j = 1; j <= 13; j++) {
//                SimpleCard card = new SimpleCard();
//                if (i == 0) {
//                    card.setSuit("S");
//                } else if (i == 1) {
//                    card.setSuit("H");
//                } else if (i == 2) {
//                    card.setSuit("D");
//                } else if (i == 3) {
//                    card.setSuit("C");
//                }
//                if (j == 10) {
//                    card.setNumbdr("T");
//                } else if (j == 11) {
//                    card.setNumbdr("J");
//                } else if (j == 12) {
//                    card.setNumbdr("Q");
//                } else if (j == 13) {
//                    card.setNumbdr("K");
//                } else if (j == 1) {
//                    card.setNumbdr("A");
//                } else {
//                    card.setNumbdr(String.valueOf(j));
//                }
//                cardList.add(card);
//            }
//        }
//
//        String[] cardArray = npbnCode.split("\\.");
//        System.out.println(cardList.size());
//        List<SimpleCard> existedCardList = new ArrayList<SimpleCard>();
//        if (cardArray[0] != null) {
//            for (int i = 0; i < cardArray[0].length(); i++) {
//                SimpleCard existedCard = new SimpleCard();
//                existedCard.setSuit("S");
//                existedCard.setNumbdr(cardArray[0].substring(i, i + 1));
//                existedCardList.add(existedCard);
//            }
//        }
//        if (cardArray[1] != null) {
//            for (int i = 0; i < cardArray[1].length(); i++) {
//                SimpleCard existedCard = new SimpleCard();
//                existedCard.setSuit("H");
//                existedCard.setNumbdr(cardArray[1].substring(i, i + 1));
//                existedCardList.add(existedCard);
//            }
//        }
//        if (cardArray[2] != null) {
//            for (int i = 0; i < cardArray[2].length(); i++) {
//                SimpleCard existedCard = new SimpleCard();
//                existedCard.setSuit("D");
//                existedCard.setNumbdr(cardArray[2].substring(i, i + 1));
//                existedCardList.add(existedCard);
//            }
//        }
//        if (cardArray[3] != null) {
//            for (int i = 0; i < cardArray[3].length(); i++) {
//                SimpleCard existedCard = new SimpleCard();
//                existedCard.setSuit("C");
//                existedCard.setNumbdr(cardArray[3].substring(i, i + 1));
//                existedCardList.add(existedCard);
//            }
//        }
//
//        for (SimpleCard existedCard : existedCardList) {
//            cardList.remove(existedCard);
//        }
//
//        System.out.println(existedCardList.size());
//
//        Collections.shuffle(cardList);
//        System.out.println(cardList.size());
//
//
//        String scode = "";
//        String hcode = "";
//        String dcode = "";
//        String ccode = "";
//
//        for (int i = 0; i < 13; i++) {
//
//            if (cardList.get(i).getSuit().equals("S")) {
//                scode = scode + cardList.get(i).getNumbdr();
//            }
//            if (cardList.get(i).getSuit().equals("H")) {
//                hcode = hcode + cardList.get(i).getNumbdr();
//            }
//            if (cardList.get(i).getSuit().equals("D")) {
//                dcode = dcode + cardList.get(i).getNumbdr();
//            }
//            if (cardList.get(i).getSuit().equals("C")) {
//                ccode = ccode + cardList.get(i).getNumbdr();
//            }
//
//        }
//        String shand = scode + "." + hcode + "." + dcode + "." + ccode;
//
//        scode = "";
//        hcode = "";
//        dcode = "";
//        ccode = "";
//        for (int i = 13; i < 26; i++) {
//
//            if (cardList.get(i).getSuit().equals("S")) {
//                scode = scode + cardList.get(i).getNumbdr();
//            }
//            if (cardList.get(i).getSuit().equals("H")) {
//                hcode = hcode + cardList.get(i).getNumbdr();
//            }
//            if (cardList.get(i).getSuit().equals("D")) {
//                dcode = dcode + cardList.get(i).getNumbdr();
//            }
//            if (cardList.get(i).getSuit().equals("C")) {
//                ccode = ccode + cardList.get(i).getNumbdr();
//            }
//
//        }
//        String whand = scode + "." + hcode + "." + dcode + "." + ccode;
//
//        scode = "";
//        hcode = "";
//        dcode = "";
//        ccode = "";
//        for (int i = 26; i < 39; i++) {
//
//            if (cardList.get(i).getSuit().equals("S")) {
//                scode = scode + cardList.get(i).getNumbdr();
//            }
//            if (cardList.get(i).getSuit().equals("H")) {
//                hcode = hcode + cardList.get(i).getNumbdr();
//            }
//            if (cardList.get(i).getSuit().equals("D")) {
//                dcode = dcode + cardList.get(i).getNumbdr();
//            }
//            if (cardList.get(i).getSuit().equals("C")) {
//                ccode = ccode + cardList.get(i).getNumbdr();
//            }
//
//        }
//        String nhand = scode + "." + hcode + "." + dcode + "." + ccode;
//
//        pbnCode = "E:" + npbnCode + " " + shand + " " + whand + " " + nhand;
//        System.out.println(pbnCode);
    }
}
