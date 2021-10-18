package jp.co.rottenpear.bridge.service;

import bridge.domain.BridgeGame;
import bridge.domain.Contract;
import bridge.domain.utils.BridgeHelper;
import ddsjava.DDSConnect;
import ddsjava.DDSException;
import ddsjava.dto.CalculateResponse;
import jp.co.rottenpear.bridge.model.Hand;
import ddsjava.dto.SimpleCard;
import jp.co.rottenpear.bridge.model.BridgeCardCompare;
import jp.co.rottenpear.bridge.model.BridgeHand;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BridgeSimulatorService {

    //String npbnCode, String trump, String rank, int hcpFromInt, int hcpToInt,
    public CalculateResponse calculator(BridgeHand bridgeHand) {

        String npbnCode = bridgeHand.getPbncode();
        String trump = bridgeHand.getContractTrumpX();
        int hcpFromInt = Integer.parseInt(bridgeHand.getHcpFrom());
        int hcpToInt = Integer.parseInt(bridgeHand.getHcpTo());
        String rank = bridgeHand.getContractRank();

        CalculateResponse calculateResponse = new CalculateResponse();
        List<Hand> calcResult = new ArrayList<Hand>();
        Integer stepCount = Integer.valueOf(rank);

        if ("C".equals(trump)) {
            trump = "Clubs";
        } else if ("S".equals(trump)) {
            trump = "Spades";
        } else if ("H".equals(trump)) {
            trump = "Hearts";
        } else if ("D".equals(trump)) {
            trump = "Diamonds";
        } else if ("NT".equals(trump)) {
            trump = "NoTrump";
        }
        float probability = 0;
        int gamecount = 50;
        int makecount = 0;
        int count = 0;


        List<Contract> contracts = null;


        DDSConnect dds = new DDSConnect();


        for (int i = 0; i < gamecount; i++) {
            String pbnCode = generateRemainHands(npbnCode, hcpFromInt, hcpToInt, bridgeHand);
            try {
                contracts = dds.calcMakableContracts(pbnCode);
            } catch (DDSException e) {
                e.printStackTrace();
            }


            for (Contract contract : contracts) {
                BridgeGame game = BridgeHelper.getGameFromPBN(pbnCode, contract.getShortString());
                String trumpFullName = game.getContract().getTrump().getFullName();
                String declarerFullName = game.getDeclarer().getFullName();
                if (!(trumpFullName.equals(trump) && declarerFullName.equals("South"))) {
                    continue;
                }
                count++;
                String[] hands = pbnCode.split(" ");
                Hand hand = new Hand();
                hand.setEhand("　　　　　　　　　　　　" + hands[3]);
                hand.setNhand("　　　　　　　　　　　" + hands[2]);
                hand.setWhand(hands[1]);
                hand.setShand("　　　　　　　　　　　" + hands[0].substring(2));
                hand.setHandCount("　　　　　　　　　　　第" + String.valueOf(count) + "手牌");
                if (contract.getValue() >= stepCount + 6) {
                    makecount++;
                    hand.setMakable("makable=true");
                    hand.setSpace("-----------------------------------------------------------------------");
                    calcResult.add(hand);
                } else {
                    hand.setMakable("makable=false");
                    hand.setSpace("-----------------------------------------------------------------------");
                    calcResult.add(hand);
                }
            }
        }
        probability = makecount * 100 / gamecount;
        calculateResponse.setCalculateResults(calcResult);
        calculateResponse.setProbably(String.valueOf(probability));
        return calculateResponse;

    }


    private String generateRemainHands(String npbnCode, int hcpFromInt, int hcpToInt, BridgeHand bridgeHand) {
        String pbnCode = "";
//        String npbnCode="AT5.AJT.A632.KJ7";
        List<SimpleCard> cardList = new ArrayList<SimpleCard>();
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j <= 13; j++) {
                SimpleCard card = new SimpleCard();
                if (i == 0) {
                    card.setSuit("S");
                } else if (i == 1) {
                    card.setSuit("H");
                } else if (i == 2) {
                    card.setSuit("D");
                } else if (i == 3) {
                    card.setSuit("C");
                }
                if (j == 10) {
                    card.setNumbdr("T");
                } else if (j == 11) {
                    card.setNumbdr("J");
                } else if (j == 12) {
                    card.setNumbdr("Q");
                } else if (j == 13) {
                    card.setNumbdr("K");
                } else if (j == 1) {
                    card.setNumbdr("A");
                } else {
                    card.setNumbdr(String.valueOf(j));
                }
                cardList.add(card);
            }
        }

        String[] cardArray = npbnCode.split("\\.");
        List<SimpleCard> existedCardList = new ArrayList<SimpleCard>();
        if (cardArray[0] != null) {
            for (int i = 0; i < cardArray[0].length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("S");
                existedCard.setNumbdr(cardArray[0].substring(i, i + 1));
                existedCardList.add(existedCard);
            }
        }
        if (cardArray[1] != null) {
            for (int i = 0; i < cardArray[1].length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("H");
                existedCard.setNumbdr(cardArray[1].substring(i, i + 1));
                existedCardList.add(existedCard);
            }
        }
        if (cardArray[2] != null) {
            for (int i = 0; i < cardArray[2].length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("D");
                existedCard.setNumbdr(cardArray[2].substring(i, i + 1));
                existedCardList.add(existedCard);
            }
        }
        if (cardArray.length == 4 && cardArray[3] != null) {
            for (int i = 0; i < cardArray[3].length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("C");
                existedCard.setNumbdr(cardArray[3].substring(i, i + 1));
                existedCardList.add(existedCard);
            }
        }

        for (SimpleCard existedCard : existedCardList) {
            cardList.remove(existedCard);
        }

        Collections.shuffle(cardList);

        String scode = "";
        String hcode = "";
        String dcode = "";
        String ccode = "";

        for (int i = 0; i < 13; i++) {

            if (cardList.get(i).getSuit().equals("S")) {
                scode = scode + cardList.get(i).getNumbdr();
            }
            if (cardList.get(i).getSuit().equals("H")) {
                hcode = hcode + cardList.get(i).getNumbdr();
            }
            if (cardList.get(i).getSuit().equals("D")) {
                dcode = dcode + cardList.get(i).getNumbdr();
            }
            if (cardList.get(i).getSuit().equals("C")) {
                ccode = ccode + cardList.get(i).getNumbdr();
            }

        }

        String ehand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

        scode = "";
        hcode = "";
        dcode = "";
        ccode = "";
        for (int i = 13; i < 26; i++) {

            if (cardList.get(i).getSuit().equals("S")) {
                scode = scode + cardList.get(i).getNumbdr();
            }
            if (cardList.get(i).getSuit().equals("H")) {
                hcode = hcode + cardList.get(i).getNumbdr();
            }
            if (cardList.get(i).getSuit().equals("D")) {
                dcode = dcode + cardList.get(i).getNumbdr();
            }
            if (cardList.get(i).getSuit().equals("C")) {
                ccode = ccode + cardList.get(i).getNumbdr();
            }

        }

        String whand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

        scode = "";
        hcode = "";
        dcode = "";
        ccode = "";
        for (int i = 26; i < 39; i++) {

            if (cardList.get(i).getSuit().equals("S")) {
                scode = scode + cardList.get(i).getNumbdr();
            }
            if (cardList.get(i).getSuit().equals("H")) {
                hcode = hcode + cardList.get(i).getNumbdr();
            }
            if (cardList.get(i).getSuit().equals("D")) {
                dcode = dcode + cardList.get(i).getNumbdr();
            }
            if (cardList.get(i).getSuit().equals("C")) {
                ccode = ccode + cardList.get(i).getNumbdr();
            }

        }

        if (scode.length() < Integer.parseInt(bridgeHand.getSpadesLengthFrom()) || scode.length() > Integer.parseInt(bridgeHand.getSpadesLengthTo())) {
            return generateRemainHands(npbnCode, hcpFromInt, hcpToInt, bridgeHand);
        }
        if (hcode.length() < Integer.parseInt(bridgeHand.getHeartsLengthFrom()) || hcode.length() > Integer.parseInt(bridgeHand.getHeartsLengthTo())) {
            return generateRemainHands(npbnCode, hcpFromInt, hcpToInt, bridgeHand);
        }
        if (dcode.length() < Integer.parseInt(bridgeHand.getDiamondsLengthFrom()) || dcode.length() > Integer.parseInt(bridgeHand.getDiamondsLengthTo())) {
            return generateRemainHands(npbnCode, hcpFromInt, hcpToInt, bridgeHand);
        }
        if (ccode.length() < Integer.parseInt(bridgeHand.getClubsLengthFrom()) || ccode.length() > Integer.parseInt(bridgeHand.getClubsLengthTo())) {
            return generateRemainHands(npbnCode, hcpFromInt, hcpToInt, bridgeHand);
        }
        String nhand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

        int hcpNhand = calcHcp(nhand);
        if (hcpNhand < hcpFromInt || hcpNhand > hcpToInt) {
            return generateRemainHands(npbnCode, hcpFromInt, hcpToInt, bridgeHand);
        }
        if (!checkBalanceOrUnbalaceHand(bridgeHand, nhand)) {
            return generateRemainHands(npbnCode, hcpFromInt, hcpToInt, bridgeHand);
        }

        pbnCode = "S:" + npbnCode + " " + whand + " " + nhand + " " + ehand;
        return pbnCode;
    }

    private boolean checkBalanceOrUnbalaceHand(BridgeHand bridgeHand, String nhand) {
        boolean checkBalance = false;
        if ("ANY".equals(bridgeHand.getPartnerHandPattern())) {
            checkBalance = true;
        } else if ("BALANCE".equals(bridgeHand.getPartnerHandPattern())) {
            if (checkIfBanlancedHand(nhand)) {
                checkBalance = true;
            }
        } else if ("UNBALANCE".equals(bridgeHand.getPartnerHandPattern())) {
            if (!checkIfBanlancedHand(nhand)) {
                checkBalance = true;
            }
        }
        return checkBalance;
    }

    private boolean checkIfBanlancedHand(String nhand) {
        boolean ifBanlancedHand = false;
        String pattern = getHandPattern(nhand);
//        System.out.println(pattern);
        if ("2335".equals(pattern) || "2344".equals(pattern) || "3334".equals(pattern)) {
            ifBanlancedHand = true;
        }
        return ifBanlancedHand;
    }

    private String getHandPattern(String nhand) {
        String pattern = "";
        String[] patternArray = nhand.split("\\.");
        List<String> tmpList = new ArrayList<String>();
        for (String p : patternArray) {
            tmpList.add(String.valueOf(p.length()));
        }
        Collections.sort(tmpList);
        for (String length : tmpList) {
            pattern = pattern + length;
        }
        return pattern;
    }

    private int calcHcp(String pbnCode) {
        int hcp = 0;
        String[] suits = pbnCode.split("\\.");
        for (String suit : suits) {
            for (int i = 0; i < suit.length(); i++) {
                String subStr = suit.substring(i, i + 1);
                if ("A".equals(subStr)) {
                    hcp = hcp + 4;
                } else if ("K".equals(subStr)) {
                    hcp = hcp + 3;
                } else if ("Q".equals(subStr)) {
                    hcp = hcp + 2;
                } else if ("J".equals(subStr)) {
                    hcp = hcp + 1;
                }
            }
        }
        return hcp;
    }

    private String sortPbnCode(String code) {

        byte[] bys = code.getBytes();//将字符串转化为字节数组
        List<Integer> list = new ArrayList<Integer>();
        for (byte bite : bys) {
            list.add(Integer.valueOf(bite));
        }
        BridgeCardCompare compare = new BridgeCardCompare();
        //排序
        Collections.sort(list, compare);
        //char[] toCharArray()： 将字符串转化为字符数组。

        String sortedString = "";
        for (Integer inte : list) {
            sortedString = sortedString + new String(new byte[]{inte.byteValue()});
        }
        return sortedString;

    }
}
