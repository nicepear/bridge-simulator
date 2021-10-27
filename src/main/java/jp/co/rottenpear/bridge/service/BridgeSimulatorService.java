package jp.co.rottenpear.bridge.service;

import bridge.domain.BridgeGame;
import bridge.domain.Contract;
import bridge.domain.utils.BridgeHelper;
import ddsjava.DDSConnect;
import ddsjava.DDSException;
import ddsjava.dto.CalculateResponse;
import jp.co.rottenpear.bridge.config.BridgeSimulatorConfig;
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

    private int calcCont = 0;

    public CalculateResponse calculator(BridgeHand bridgeHand) {

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

        int makecount = 0;
        int count = 0;


        List<Contract> contracts = null;


        DDSConnect dds = new DDSConnect();


        for (int i = 0; i < BridgeSimulatorConfig.gamecount; i++) {
            calcCont=0;
            String pbnCode = generateRemainHandsEasy(bridgeHand);

            try {
                contracts = dds.calcMakableContracts(pbnCode);
            } catch (DDSException e) {
                e.printStackTrace();
                dds=null;
                throw new RuntimeException("can not generate dds");
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
        probability = makecount * 100 / BridgeSimulatorConfig.gamecount;
        calculateResponse.setCalculateResults(calcResult);
        calculateResponse.setProbably(String.valueOf(probability));
        dds=null;
        return calculateResponse;

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

    private String generateRemainHandsEasy(BridgeHand bridgeHand) {
        String pbnCode = "";
        try {
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

            String pbnSouthCodeSpades = bridgeHand.getPbncodeSpades();
            String pbnSouthCodeHearts = bridgeHand.getPbncodeHearts();
            String pbnSouthCodeDiamonds = bridgeHand.getPbncodeDiamonds();
            String pbnSouthCodeClubs = bridgeHand.getPbncodeClubs();

            List<SimpleCard> existedSouthCardList = new ArrayList<SimpleCard>();
            if (pbnSouthCodeSpades != null) {
                for (int i = 0; i < pbnSouthCodeSpades.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("S");
                    existedCard.setNumbdr(pbnSouthCodeSpades.substring(i, i + 1));
                    existedSouthCardList.add(existedCard);
                }
            }
            if (pbnSouthCodeHearts != null) {
                for (int i = 0; i < pbnSouthCodeHearts.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("H");
                    existedCard.setNumbdr(pbnSouthCodeHearts.substring(i, i + 1));
                    existedSouthCardList.add(existedCard);
                }
            }
            if (pbnSouthCodeDiamonds != null) {
                for (int i = 0; i < pbnSouthCodeDiamonds.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("D");
                    existedCard.setNumbdr(pbnSouthCodeDiamonds.substring(i, i + 1));
                    existedSouthCardList.add(existedCard);
                }
            }
            if (pbnSouthCodeClubs != null) {
                for (int i = 0; i < pbnSouthCodeClubs.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("C");
                    existedCard.setNumbdr(pbnSouthCodeClubs.substring(i, i + 1));
                    existedSouthCardList.add(existedCard);
                }
            }


            String pbncodeNorthSpades = bridgeHand.getPbncodeNorthSpades();
            String pbncodeNorthHearts = bridgeHand.getPbncodeNorthHearts();
            String pbncodeNorthDiamonds = bridgeHand.getPbncodeNorthDiamonds();
            String pbncodeNorthClubs = bridgeHand.getPbncodeNorthClubs();

            List<SimpleCard> existedNorthCardList = new ArrayList<SimpleCard>();
            if (pbncodeNorthSpades != null) {
                for (int i = 0; i < pbncodeNorthSpades.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("S");
                    existedCard.setNumbdr(pbncodeNorthSpades.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard)) {
                        throw new RuntimeException("黑桃牌重复");
                    }
                    existedNorthCardList.add(existedCard);
                }
            }

            if (pbncodeNorthHearts != null) {
                for (int i = 0; i < pbncodeNorthHearts.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("H");
                    existedCard.setNumbdr(pbncodeNorthHearts.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard)) {
                        throw new RuntimeException("红桃牌重复");
                    }
                    existedNorthCardList.add(existedCard);
                }
            }

            if (pbncodeNorthDiamonds != null) {
                for (int i = 0; i < pbncodeNorthDiamonds.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("D");
                    existedCard.setNumbdr(pbncodeNorthDiamonds.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard)) {
                        throw new RuntimeException("方片牌重复");
                    }
                    existedNorthCardList.add(existedCard);
                }
            }

            if (pbncodeNorthClubs != null) {
                for (int i = 0; i < pbncodeNorthClubs.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("C");
                    existedCard.setNumbdr(pbncodeNorthClubs.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard)) {
                        throw new RuntimeException("草花牌重复");
                    }
                    existedNorthCardList.add(existedCard);
                }
            }

            String pbncodeEastSpades = bridgeHand.getPbncodeEastSpades();
            String pbncodeEastHearts = bridgeHand.getPbncodeEastHearts();
            String pbncodeEastDiamonds = bridgeHand.getPbncodeEastDiamonds();
            String pbncodeEastClubs = bridgeHand.getPbncodeEastClubs();

            List<SimpleCard> existedEastCardList = new ArrayList<SimpleCard>();
            if (pbncodeEastSpades != null) {
                for (int i = 0; i < pbncodeEastSpades.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("S");
                    existedCard.setNumbdr(pbncodeEastSpades.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard)) {
                        throw new RuntimeException("黑桃牌重复");
                    }
                    existedEastCardList.add(existedCard);
                }
            }

            if (pbncodeEastHearts != null) {
                for (int i = 0; i < pbncodeEastHearts.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("H");
                    existedCard.setNumbdr(pbncodeEastHearts.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard)) {
                        throw new RuntimeException("红桃牌重复");
                    }
                    existedEastCardList.add(existedCard);
                }
            }

            if (pbncodeEastDiamonds != null) {
                for (int i = 0; i < pbncodeEastDiamonds.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("D");
                    existedCard.setNumbdr(pbncodeEastDiamonds.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard)) {
                        throw new RuntimeException("方片牌重复");
                    }
                    existedEastCardList.add(existedCard);
                }
            }

            if (pbncodeEastClubs != null) {
                for (int i = 0; i < pbncodeEastClubs.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("C");
                    existedCard.setNumbdr(pbncodeEastClubs.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard)) {
                        throw new RuntimeException("草花牌重复");
                    }
                    existedEastCardList.add(existedCard);
                }
            }

            String pbncodeWestSpades = bridgeHand.getPbncodeWestSpades();
            String pbncodeWestHearts = bridgeHand.getPbncodeWestHearts();
            String pbncodeWestDiamonds = bridgeHand.getPbncodeWestDiamonds();
            String pbncodeWestClubs = bridgeHand.getPbncodeWestClubs();

            List<SimpleCard> existedWestCardList = new ArrayList<SimpleCard>();
            if (pbncodeWestSpades != null) {
                for (int i = 0; i < pbncodeWestSpades.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("S");
                    existedCard.setNumbdr(pbncodeWestSpades.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard) || existedEastCardList.contains(existedCard)) {
                        throw new RuntimeException("黑桃牌重复");
                    }
                    existedWestCardList.add(existedCard);
                }
            }

            if (pbncodeWestHearts != null) {
                for (int i = 0; i < pbncodeWestHearts.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("H");
                    existedCard.setNumbdr(pbncodeWestHearts.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard) || existedEastCardList.contains(existedCard)) {
                        throw new RuntimeException("红桃牌重复");
                    }
                    existedWestCardList.add(existedCard);
                }
            }

            if (pbncodeWestDiamonds != null) {
                for (int i = 0; i < pbncodeWestDiamonds.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("D");
                    existedCard.setNumbdr(pbncodeWestDiamonds.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard) || existedEastCardList.contains(existedCard)) {
                        throw new RuntimeException("方片牌重复");
                    }
                    existedWestCardList.add(existedCard);
                }
            }

            if (pbncodeWestClubs != null) {
                for (int i = 0; i < pbncodeWestClubs.length(); i++) {
                    SimpleCard existedCard = new SimpleCard();
                    existedCard.setSuit("C");
                    existedCard.setNumbdr(pbncodeWestClubs.substring(i, i + 1));
                    if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard) || existedEastCardList.contains(existedCard)) {
                        throw new RuntimeException("草花牌重复");
                    }
                    existedWestCardList.add(existedCard);
                }
            }


            for (SimpleCard existedCard : existedEastCardList) {
                cardList.remove(existedCard);
            }

            for (SimpleCard existedCard : existedWestCardList) {
                cardList.remove(existedCard);
            }

            for (SimpleCard existedCard : existedNorthCardList) {
                cardList.remove(existedCard);
            }

            for (SimpleCard existedCard : existedSouthCardList) {
                cardList.remove(existedCard);
            }

            String pbnCodeNorth = generateRemainHandsNorth(bridgeHand, cardList, existedNorthCardList);

            String pbnCodeWest = generateRemainHandsWest(bridgeHand, cardList, existedWestCardList);

            String pbnCodeEast = generateRemainHandsEast(bridgeHand, cardList, existedEastCardList);


            String scode = bridgeHand.getPbncodeSpades();
            String hcode = bridgeHand.getPbncodeHearts();
            String dcode = bridgeHand.getPbncodeDiamonds();
            String ccode = bridgeHand.getPbncodeClubs();
            for (SimpleCard card : cardList) {

                if (card.getSuit().equals("S")) {
                    scode = scode + card.getNumbdr();
                }
                if (card.getSuit().equals("H")) {
                    hcode = hcode + card.getNumbdr();
                }
                if (card.getSuit().equals("D")) {
                    dcode = dcode + card.getNumbdr();
                }
                if (card.getSuit().equals("C")) {
                    ccode = ccode + card.getNumbdr();
                }

            }
            String shand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

            pbnCode = "S:" + shand + " " + pbnCodeWest + " " + pbnCodeNorth + " " + pbnCodeEast;
        } catch (StackOverflowError error) {
            if (calcCont < BridgeSimulatorConfig.calculateCount) {
                calcCont++;
                return generateRemainHandsEasy(bridgeHand);
            } else {
                throw error;
            }
        }

        return pbnCode;
    }

    private String generateRemainHandsNorth(BridgeHand bridgeHand, List<SimpleCard> cardList, List<SimpleCard> existedEastCardList) {
        Collections.shuffle(cardList);

        List<SimpleCard> tmpCardList = new ArrayList<SimpleCard>();
        int eastCardLength = 13 - existedEastCardList.size();

        String scode = bridgeHand.getPbncodeNorthSpades();
        String hcode = bridgeHand.getPbncodeNorthHearts();
        String dcode = bridgeHand.getPbncodeNorthDiamonds();
        String ccode = bridgeHand.getPbncodeNorthClubs();

        for (int i = 0; i < eastCardLength; i++) {

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
            tmpCardList.add(cardList.get(i));
        }

        if (scode.length() < Integer.parseInt(bridgeHand.getSpadesLengthFrom()) || scode.length() > Integer.parseInt(bridgeHand.getSpadesLengthTo())) {
            return generateRemainHandsNorth(bridgeHand, cardList, existedEastCardList);
        }
        if (hcode.length() < Integer.parseInt(bridgeHand.getHeartsLengthFrom()) || hcode.length() > Integer.parseInt(bridgeHand.getHeartsLengthTo())) {
            return generateRemainHandsNorth(bridgeHand, cardList, existedEastCardList);
        }
        if (dcode.length() < Integer.parseInt(bridgeHand.getDiamondsLengthFrom()) || dcode.length() > Integer.parseInt(bridgeHand.getDiamondsLengthTo())) {
            return generateRemainHandsNorth(bridgeHand, cardList, existedEastCardList);
        }
        if (ccode.length() < Integer.parseInt(bridgeHand.getClubsLengthFrom()) || ccode.length() > Integer.parseInt(bridgeHand.getClubsLengthTo())) {
            return generateRemainHandsNorth(bridgeHand, cardList, existedEastCardList);
        }

        String nhand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

        int hcpEhand = calcHcp(nhand);
        if (hcpEhand < Integer.parseInt(bridgeHand.getHcpFrom()) || hcpEhand > Integer.parseInt(bridgeHand.getHcpTo())) {
            return generateRemainHandsNorth(bridgeHand, cardList, existedEastCardList);
        }
        if (!checkBalanceOrUnbalaceHand(bridgeHand, nhand)) {
            return generateRemainHandsNorth(bridgeHand, cardList, existedEastCardList);
        }

        for (SimpleCard card : tmpCardList) {
            cardList.remove(card);
        }
        return nhand;
    }

    private String generateRemainHandsWest(BridgeHand bridgeHand, List<SimpleCard> cardList, List<SimpleCard> existedEastCardList) {
        Collections.shuffle(cardList);

        List<SimpleCard> tmpCardList = new ArrayList<SimpleCard>();
        int eastCardLength = 13 - existedEastCardList.size();

        String scode = bridgeHand.getPbncodeWestSpades();
        String hcode = bridgeHand.getPbncodeWestHearts();
        String dcode = bridgeHand.getPbncodeWestDiamonds();
        String ccode = bridgeHand.getPbncodeWestClubs();

        for (int i = 0; i < eastCardLength; i++) {

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
            tmpCardList.add(cardList.get(i));
        }

        if (scode.length() < Integer.parseInt(bridgeHand.getSpadesLengthFromWest()) || scode.length() > Integer.parseInt(bridgeHand.getSpadesLengthToWest())) {
            return generateRemainHandsWest(bridgeHand, cardList, existedEastCardList);
        }
        if (hcode.length() < Integer.parseInt(bridgeHand.getHeartsLengthFromWest()) || hcode.length() > Integer.parseInt(bridgeHand.getHeartsLengthToWest())) {
            return generateRemainHandsWest(bridgeHand, cardList, existedEastCardList);
        }
        if (dcode.length() < Integer.parseInt(bridgeHand.getDiamondsLengthFromWest()) || dcode.length() > Integer.parseInt(bridgeHand.getDiamondsLengthToWest())) {
            return generateRemainHandsWest(bridgeHand, cardList, existedEastCardList);
        }
        if (ccode.length() < Integer.parseInt(bridgeHand.getClubsLengthFromWest()) || ccode.length() > Integer.parseInt(bridgeHand.getClubsLengthToWest())) {
            return generateRemainHandsWest(bridgeHand, cardList, existedEastCardList);
        }

        String whand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

        int hcpEhand = calcHcp(whand);
        if (hcpEhand < Integer.parseInt(bridgeHand.getHcpFromWest()) || hcpEhand > Integer.parseInt(bridgeHand.getHcpToWest())) {
            return generateRemainHandsWest(bridgeHand, cardList, existedEastCardList);
        }
        if (!checkBalanceOrUnbalaceHand(bridgeHand, whand)) {
            return generateRemainHandsWest(bridgeHand, cardList, existedEastCardList);
        }

        for (SimpleCard card : tmpCardList) {
            cardList.remove(card);
        }
        return whand;
    }

    private String generateRemainHandsEast(BridgeHand bridgeHand, List<SimpleCard> cardList, List<SimpleCard> existedEastCardList) {

        Collections.shuffle(cardList);

        List<SimpleCard> tmpCardList = new ArrayList<SimpleCard>();
        int eastCardLength = 13 - existedEastCardList.size();

        String scode = bridgeHand.getPbncodeEastSpades();
        String hcode = bridgeHand.getPbncodeEastHearts();
        String dcode = bridgeHand.getPbncodeEastDiamonds();
        String ccode = bridgeHand.getPbncodeEastClubs();

        for (int i = 0; i < eastCardLength; i++) {

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
            tmpCardList.add(cardList.get(i));
        }

        if (scode.length() < Integer.parseInt(bridgeHand.getSpadesLengthFromEast()) || scode.length() > Integer.parseInt(bridgeHand.getSpadesLengthToEast())) {
            return generateRemainHandsEast(bridgeHand, cardList, existedEastCardList);
        }
        if (hcode.length() < Integer.parseInt(bridgeHand.getHeartsLengthFromEast()) || hcode.length() > Integer.parseInt(bridgeHand.getHeartsLengthToEast())) {
            return generateRemainHandsEast(bridgeHand, cardList, existedEastCardList);
        }
        if (dcode.length() < Integer.parseInt(bridgeHand.getDiamondsLengthFromEast()) || dcode.length() > Integer.parseInt(bridgeHand.getDiamondsLengthToEast())) {
            return generateRemainHandsEast(bridgeHand, cardList, existedEastCardList);
        }
        if (ccode.length() < Integer.parseInt(bridgeHand.getClubsLengthFromEast()) || ccode.length() > Integer.parseInt(bridgeHand.getClubsLengthToEast())) {
            return generateRemainHandsEast(bridgeHand, cardList, existedEastCardList);
        }

        String ehand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

        int hcpEhand = calcHcp(ehand);
        if (hcpEhand < Integer.parseInt(bridgeHand.getHcpFromEast()) || hcpEhand > Integer.parseInt(bridgeHand.getHcpToEast())) {
            return generateRemainHandsEast(bridgeHand, cardList, existedEastCardList);
        }
        if (!checkBalanceOrUnbalaceHand(bridgeHand, ehand)) {
            return generateRemainHandsEast(bridgeHand, cardList, existedEastCardList);
        }

        for (SimpleCard card : tmpCardList) {
            cardList.remove(card);
        }
        return ehand;
    }


    private String generateRemainHands(BridgeHand bridgeHand) {
        String pbnCode = "";
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

        String pbnSouthCodeSpades = bridgeHand.getPbncodeSpades();
        String pbnSouthCodeHearts = bridgeHand.getPbncodeHearts();
        String pbnSouthCodeDiamonds = bridgeHand.getPbncodeDiamonds();
        String pbnSouthCodeClubs = bridgeHand.getPbncodeClubs();

        List<SimpleCard> existedSouthCardList = new ArrayList<SimpleCard>();
        if (pbnSouthCodeSpades != null) {
            for (int i = 0; i < pbnSouthCodeSpades.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("S");
                existedCard.setNumbdr(pbnSouthCodeSpades.substring(i, i + 1));
                existedSouthCardList.add(existedCard);
            }
        }
        if (pbnSouthCodeHearts != null) {
            for (int i = 0; i < pbnSouthCodeHearts.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("H");
                existedCard.setNumbdr(pbnSouthCodeHearts.substring(i, i + 1));
                existedSouthCardList.add(existedCard);
            }
        }
        if (pbnSouthCodeDiamonds != null) {
            for (int i = 0; i < pbnSouthCodeDiamonds.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("D");
                existedCard.setNumbdr(pbnSouthCodeDiamonds.substring(i, i + 1));
                existedSouthCardList.add(existedCard);
            }
        }
        if (pbnSouthCodeClubs != null) {
            for (int i = 0; i < pbnSouthCodeClubs.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("C");
                existedCard.setNumbdr(pbnSouthCodeClubs.substring(i, i + 1));
                existedSouthCardList.add(existedCard);
            }
        }


        String pbncodeNorthSpades = bridgeHand.getPbncodeNorthSpades();
        String pbncodeNorthHearts = bridgeHand.getPbncodeNorthHearts();
        String pbncodeNorthDiamonds = bridgeHand.getPbncodeNorthDiamonds();
        String pbncodeNorthClubs = bridgeHand.getPbncodeNorthClubs();

        List<SimpleCard> existedNorthCardList = new ArrayList<SimpleCard>();
        if (pbncodeNorthSpades != null) {
            for (int i = 0; i < pbncodeNorthSpades.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("S");
                existedCard.setNumbdr(pbncodeNorthSpades.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard)) {
                    throw new RuntimeException("黑桃牌重复");
                }
                existedNorthCardList.add(existedCard);
            }
        }

        if (pbncodeNorthHearts != null) {
            for (int i = 0; i < pbncodeNorthHearts.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("H");
                existedCard.setNumbdr(pbncodeNorthHearts.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard)) {
                    throw new RuntimeException("红桃牌重复");
                }
                existedNorthCardList.add(existedCard);
            }
        }

        if (pbncodeNorthDiamonds != null) {
            for (int i = 0; i < pbncodeNorthDiamonds.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("D");
                existedCard.setNumbdr(pbncodeNorthDiamonds.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard)) {
                    throw new RuntimeException("方片牌重复");
                }
                existedNorthCardList.add(existedCard);
            }
        }

        if (pbncodeNorthClubs != null) {
            for (int i = 0; i < pbncodeNorthClubs.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("C");
                existedCard.setNumbdr(pbncodeNorthClubs.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard)) {
                    throw new RuntimeException("草花牌重复");
                }
                existedNorthCardList.add(existedCard);
            }
        }

        String pbncodeEastSpades = bridgeHand.getPbncodeEastSpades();
        String pbncodeEastHearts = bridgeHand.getPbncodeEastHearts();
        String pbncodeEastDiamonds = bridgeHand.getPbncodeEastDiamonds();
        String pbncodeEastClubs = bridgeHand.getPbncodeEastClubs();

        List<SimpleCard> existedEastCardList = new ArrayList<SimpleCard>();
        if (pbncodeEastSpades != null) {
            for (int i = 0; i < pbncodeEastSpades.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("S");
                existedCard.setNumbdr(pbncodeEastSpades.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard)) {
                    throw new RuntimeException("黑桃牌重复");
                }
                existedEastCardList.add(existedCard);
            }
        }

        if (pbncodeEastHearts != null) {
            for (int i = 0; i < pbncodeEastHearts.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("H");
                existedCard.setNumbdr(pbncodeEastHearts.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard)) {
                    throw new RuntimeException("红桃牌重复");
                }
                existedEastCardList.add(existedCard);
            }
        }

        if (pbncodeEastDiamonds != null) {
            for (int i = 0; i < pbncodeEastDiamonds.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("D");
                existedCard.setNumbdr(pbncodeEastDiamonds.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard)) {
                    throw new RuntimeException("方片牌重复");
                }
                existedEastCardList.add(existedCard);
            }
        }

        if (pbncodeEastClubs != null) {
            for (int i = 0; i < pbncodeEastClubs.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("C");
                existedCard.setNumbdr(pbncodeEastClubs.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard)) {
                    throw new RuntimeException("草花牌重复");
                }
                existedEastCardList.add(existedCard);
            }
        }

        String pbncodeWestSpades = bridgeHand.getPbncodeWestSpades();
        String pbncodeWestHearts = bridgeHand.getPbncodeWestHearts();
        String pbncodeWestDiamonds = bridgeHand.getPbncodeWestDiamonds();
        String pbncodeWestClubs = bridgeHand.getPbncodeWestClubs();

        List<SimpleCard> existedWestCardList = new ArrayList<SimpleCard>();
        if (pbncodeWestSpades != null) {
            for (int i = 0; i < pbncodeWestSpades.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("S");
                existedCard.setNumbdr(pbncodeWestSpades.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard) || existedEastCardList.contains(existedCard)) {
                    throw new RuntimeException("黑桃牌重复");
                }
                existedWestCardList.add(existedCard);
            }
        }

        if (pbncodeWestHearts != null) {
            for (int i = 0; i < pbncodeWestHearts.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("H");
                existedCard.setNumbdr(pbncodeWestHearts.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard) || existedEastCardList.contains(existedCard)) {
                    throw new RuntimeException("红桃牌重复");
                }
                existedWestCardList.add(existedCard);
            }
        }

        if (pbncodeWestDiamonds != null) {
            for (int i = 0; i < pbncodeWestDiamonds.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("D");
                existedCard.setNumbdr(pbncodeWestDiamonds.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard) || existedEastCardList.contains(existedCard)) {
                    throw new RuntimeException("方片牌重复");
                }
                existedWestCardList.add(existedCard);
            }
        }

        if (pbncodeWestClubs != null) {
            for (int i = 0; i < pbncodeWestClubs.length(); i++) {
                SimpleCard existedCard = new SimpleCard();
                existedCard.setSuit("C");
                existedCard.setNumbdr(pbncodeWestClubs.substring(i, i + 1));
                if (existedSouthCardList.contains(existedCard) || existedNorthCardList.contains(existedCard) || existedEastCardList.contains(existedCard)) {
                    throw new RuntimeException("草花牌重复");
                }
                existedWestCardList.add(existedCard);
            }
        }

        for (SimpleCard existedCard : existedSouthCardList) {
            cardList.remove(existedCard);
        }

        for (SimpleCard existedCard : existedNorthCardList) {
            cardList.remove(existedCard);
        }
        for (SimpleCard existedCard : existedEastCardList) {
            cardList.remove(existedCard);
        }
        for (SimpleCard existedCard : existedWestCardList) {
            cardList.remove(existedCard);
        }
        Collections.shuffle(cardList);
        int southCardLength = 13 - existedSouthCardList.size();
        int northCardLength = 13 - existedNorthCardList.size();
        int eastCardLength = 13 - existedEastCardList.size();
        int westCardLength = 13 - existedWestCardList.size();

        String scode = pbncodeEastSpades;
        String hcode = pbncodeEastHearts;
        String dcode = pbncodeEastDiamonds;
        String ccode = pbncodeEastClubs;

        for (int i = 0; i < eastCardLength; i++) {

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

        if (scode.length() < Integer.parseInt(bridgeHand.getSpadesLengthFromEast()) || scode.length() > Integer.parseInt(bridgeHand.getSpadesLengthToEast())) {
            return generateRemainHands(bridgeHand);
        }
        if (hcode.length() < Integer.parseInt(bridgeHand.getHeartsLengthFromEast()) || hcode.length() > Integer.parseInt(bridgeHand.getHeartsLengthToEast())) {
            return generateRemainHands(bridgeHand);
        }
        if (dcode.length() < Integer.parseInt(bridgeHand.getDiamondsLengthFromEast()) || dcode.length() > Integer.parseInt(bridgeHand.getDiamondsLengthToEast())) {
            return generateRemainHands(bridgeHand);
        }
        if (ccode.length() < Integer.parseInt(bridgeHand.getClubsLengthFromEast()) || ccode.length() > Integer.parseInt(bridgeHand.getClubsLengthToEast())) {
            return generateRemainHands(bridgeHand);
        }

        String ehand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

        int hcpEhand = calcHcp(ehand);
        if (hcpEhand < Integer.parseInt(bridgeHand.getHcpFromEast()) || hcpEhand > Integer.parseInt(bridgeHand.getHcpToEast())) {
            return generateRemainHands(bridgeHand);
        }
        if (!checkBalanceOrUnbalaceHand(bridgeHand, ehand)) {
            return generateRemainHands(bridgeHand);
        }

        scode = pbncodeWestSpades;
        hcode = pbncodeWestHearts;
        dcode = pbncodeWestDiamonds;
        ccode = pbncodeWestClubs;
        for (int i = eastCardLength; i < eastCardLength + westCardLength; i++) {

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

        if (scode.length() < Integer.parseInt(bridgeHand.getSpadesLengthFromWest()) || scode.length() > Integer.parseInt(bridgeHand.getSpadesLengthToWest())) {
            return generateRemainHands(bridgeHand);
        }
        if (hcode.length() < Integer.parseInt(bridgeHand.getHeartsLengthFromWest()) || hcode.length() > Integer.parseInt(bridgeHand.getHeartsLengthToWest())) {
            return generateRemainHands(bridgeHand);
        }
        if (dcode.length() < Integer.parseInt(bridgeHand.getDiamondsLengthFromWest()) || dcode.length() > Integer.parseInt(bridgeHand.getDiamondsLengthToWest())) {
            return generateRemainHands(bridgeHand);
        }
        if (ccode.length() < Integer.parseInt(bridgeHand.getClubsLengthFromWest()) || ccode.length() > Integer.parseInt(bridgeHand.getClubsLengthToWest())) {
            return generateRemainHands(bridgeHand);
        }
        String whand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

        int hcpWhand = calcHcp(whand);
        if (hcpWhand < Integer.parseInt(bridgeHand.getHcpFromWest()) || hcpWhand > Integer.parseInt(bridgeHand.getHcpToWest())) {
            return generateRemainHands(bridgeHand);
        }
        if (!checkBalanceOrUnbalaceHand(bridgeHand, whand)) {
            return generateRemainHands(bridgeHand);
        }

        scode = pbncodeNorthSpades;
        hcode = pbncodeNorthHearts;
        dcode = pbncodeNorthDiamonds;
        ccode = pbncodeNorthClubs;
        for (int i = eastCardLength + westCardLength; i < eastCardLength + westCardLength + northCardLength; i++) {

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
            return generateRemainHands(bridgeHand);
        }
        if (hcode.length() < Integer.parseInt(bridgeHand.getHeartsLengthFrom()) || hcode.length() > Integer.parseInt(bridgeHand.getHeartsLengthTo())) {
            return generateRemainHands(bridgeHand);
        }
        if (dcode.length() < Integer.parseInt(bridgeHand.getDiamondsLengthFrom()) || dcode.length() > Integer.parseInt(bridgeHand.getDiamondsLengthTo())) {
            return generateRemainHands(bridgeHand);
        }
        if (ccode.length() < Integer.parseInt(bridgeHand.getClubsLengthFrom()) || ccode.length() > Integer.parseInt(bridgeHand.getClubsLengthTo())) {
            return generateRemainHands(bridgeHand);
        }
        String nhand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

        int hcpNhand = calcHcp(nhand);
        if (hcpNhand < Integer.parseInt(bridgeHand.getHcpFrom()) || hcpNhand > Integer.parseInt(bridgeHand.getHcpTo())) {
            return generateRemainHands(bridgeHand);
        }
        if (!checkBalanceOrUnbalaceHand(bridgeHand, nhand)) {
            return generateRemainHands(bridgeHand);
        }

        scode = pbnSouthCodeSpades;
        hcode = pbnSouthCodeHearts;
        dcode = pbnSouthCodeDiamonds;
        ccode = pbnSouthCodeClubs;
        for (int i = eastCardLength + westCardLength + northCardLength; i < cardList.size(); i++) {

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
        String shand = sortPbnCode(scode) + "." + sortPbnCode(hcode) + "." + sortPbnCode(dcode) + "." + sortPbnCode(ccode);

        pbnCode = "S:" + shand + " " + whand + " " + nhand + " " + ehand;
        return pbnCode;
    }
}
