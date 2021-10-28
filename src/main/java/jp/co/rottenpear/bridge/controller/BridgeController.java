package jp.co.rottenpear.bridge.controller;

import ddsjava.dto.CalculateResponse;
import ddsjava.dto.SimpleCard;
import jp.co.rottenpear.bridge.config.BridgeSimulatorConfig;
import jp.co.rottenpear.bridge.model.Hand;
import jp.co.rottenpear.bridge.model.BridgeHand;
import jp.co.rottenpear.bridge.service.BridgeSimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BridgeController {


    @Autowired
    BridgeSimulatorService bridgeSimulatorService;

    @GetMapping("/info")
    public String bridgeSimulator(Model model) {
        Map<String, String> contractRanks = getContractRanks();

        model.addAttribute("contractRanks", contractRanks);
        model.addAttribute("message", "欢迎来到桥牌模拟器");
        return "bridgeSimulator";
    }

    @PostMapping("/infoResult")
    public ModelAndView bridgeSimulatorResult(@ModelAttribute(value = "bridgeHand") BridgeHand bridgeHand, Model model) {


        String resultMessage = "";
        CalculateResponse calculateResponse = null;
        try {
            //現時点このメソッドはパートナーのハンドをチェックするだけ
            validationBridgeHand(bridgeHand);


            if (BridgeSimulatorConfig.syncCount >= BridgeSimulatorConfig.syncLimit) {
                resultMessage = "服务器繁忙，有其他用户正在使用，请稍后再试";
                ModelAndView mv = new ModelAndView("bridgeSimulatorResult");
                mv.addObject("resultMessage", resultMessage);
                BridgeSimulatorConfig.threadLocal.set("true");
                return mv;
            }
            BridgeSimulatorConfig.syncCount++;
            calculateResponse = bridgeSimulatorService.calculator(bridgeHand);
        } catch (Exception e) {
            resultMessage = e.getMessage();
            ModelAndView mv = new ModelAndView("bridgeSimulatorResult");
            mv.addObject("resultMessage", resultMessage);
            return mv;
        } catch (StackOverflowError error) {
            resultMessage = "条件太苛刻，无法模拟牌型，请等待网站算法升级";
            ModelAndView mv = new ModelAndView("bridgeSimulatorResult");
            mv.addObject("resultMessage", resultMessage);
            return mv;
        } finally {
            if (BridgeSimulatorConfig.syncCount > 0 && !"true".equals(BridgeSimulatorConfig.threadLocal.get())) {
                BridgeSimulatorConfig.syncCount--;
            }
            BridgeSimulatorConfig.threadLocal.remove();
        }

        List<Hand> calcResults = calculateResponse.getCalculateResults();
        resultMessage = "模拟计算" + String.valueOf(BridgeSimulatorConfig.gamecount) + "次结果:南家为坐庄者时，打成定约" + bridgeHand.getContractRank() + bridgeHand.getContractTrumpX() + "成功率为" + calculateResponse.getProbably() + "%";
        ModelAndView mv = new ModelAndView("bridgeSimulatorResult");

        mv.addObject("resultMessage", resultMessage);
        mv.addObject("simulatedHandsResultList", calcResults);
        return mv;
    }


    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    @ResponseBody
    String reset() {

        BridgeSimulatorConfig.syncCount = 0;
        String testValue = "reset成功";
        return testValue;
    }

    @RequestMapping(value = "/syncCount", method = RequestMethod.GET)
    @ResponseBody
    String syncCount() {
        return String.valueOf(BridgeSimulatorConfig.syncCount);
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

    private boolean checkBalanceHand(BridgeHand bridgeHand) {
        boolean isBanlanceHand = true;
        if (Integer.parseInt(bridgeHand.getClubsLengthFrom()) > 5 || Integer.parseInt(bridgeHand.getDiamondsLengthFrom()) > 5 || Integer.parseInt(bridgeHand.getHeartsLengthFrom()) > 5 || Integer.parseInt(bridgeHand.getSpadesLengthFrom()) > 5) {
            return false;
        }
        if (Integer.parseInt(bridgeHand.getClubsLengthTo()) < 2 || Integer.parseInt(bridgeHand.getDiamondsLengthTo()) < 2 || Integer.parseInt(bridgeHand.getHeartsLengthTo()) < 2 || Integer.parseInt(bridgeHand.getSpadesLengthTo()) < 2) {
            return false;
        }
        if (bridgeHand.getPbncodeNorthClubs().length() > 5 || bridgeHand.getPbncodeNorthDiamonds().length() > 5 || bridgeHand.getPbncodeNorthHearts().length() > 5 || bridgeHand.getPbncodeNorthSpades().length() > 5) {
            return false;
        }
        return isBanlanceHand;
    }

    private boolean checkBalanceHandEast(BridgeHand bridgeHand) {
        boolean isBanlanceHand = true;
        if (Integer.parseInt(bridgeHand.getClubsLengthFromEast()) > 5 || Integer.parseInt(bridgeHand.getDiamondsLengthFromEast()) > 5 || Integer.parseInt(bridgeHand.getHeartsLengthFromEast()) > 5 || Integer.parseInt(bridgeHand.getSpadesLengthFromEast()) > 5) {
            return false;
        }
        if (Integer.parseInt(bridgeHand.getClubsLengthToEast()) < 2 || Integer.parseInt(bridgeHand.getDiamondsLengthToEast()) < 2 || Integer.parseInt(bridgeHand.getHeartsLengthToEast()) < 2 || Integer.parseInt(bridgeHand.getSpadesLengthToEast()) < 2) {
            return false;
        }
        if (bridgeHand.getPbncodeEastClubs().length() > 5 || bridgeHand.getPbncodeEastDiamonds().length() > 5 || bridgeHand.getPbncodeEastHearts().length() > 5 || bridgeHand.getPbncodeEastSpades().length() > 5) {
            return false;
        }
        return isBanlanceHand;
    }

    private boolean checkBalanceHandWest(BridgeHand bridgeHand) {
        boolean isBanlanceHand = true;
        if (Integer.parseInt(bridgeHand.getClubsLengthFromWest()) > 5 || Integer.parseInt(bridgeHand.getDiamondsLengthFromWest()) > 5 || Integer.parseInt(bridgeHand.getHeartsLengthFromWest()) > 5 || Integer.parseInt(bridgeHand.getSpadesLengthFromWest()) > 5) {
            return false;
        }
        if (Integer.parseInt(bridgeHand.getClubsLengthToWest()) < 2 || Integer.parseInt(bridgeHand.getDiamondsLengthToWest()) < 2 || Integer.parseInt(bridgeHand.getHeartsLengthToWest()) < 2 || Integer.parseInt(bridgeHand.getSpadesLengthToWest()) < 2) {
            return false;
        }
        if (bridgeHand.getPbncodeWestClubs().length() > 5 || bridgeHand.getPbncodeWestDiamonds().length() > 5 || bridgeHand.getPbncodeWestHearts().length() > 5 || bridgeHand.getPbncodeWestSpades().length() > 5) {
            return false;
        }
        return isBanlanceHand;
    }

    private boolean checkUnBalanceHand(BridgeHand bridgeHand) {
        boolean isBanlanceHand = false;
        return isBanlanceHand;
    }

    private Map<String, String> getContractRanks() {
        Map<String, String> contractRanks = new LinkedHashMap<String, String>();

        contractRanks.put("1", "1");
        contractRanks.put("2", "2");
        contractRanks.put("3", "3");
        contractRanks.put("4", "4");
        contractRanks.put("5", "5");
        contractRanks.put("6", "6");
        contractRanks.put("7", "7");
        return contractRanks;
    }

    private String validationBridgeHand(BridgeHand bridgeHand) {

        checkHand(bridgeHand);
        validationHCP(bridgeHand);
        validationHCPEast(bridgeHand);
        validationHCPWest(bridgeHand);
        String resultMessage = "";
        String pbncodeNorthSpades = bridgeHand.getPbncodeNorthSpades();
        String pbncodeNorthHearts = bridgeHand.getPbncodeNorthHearts();
        String pbncodeNorthDiamonds = bridgeHand.getPbncodeNorthDiamonds();
        String pbncodeNorthClubs = bridgeHand.getPbncodeNorthClubs();

        if (pbncodeNorthSpades.length() > Integer.parseInt(bridgeHand.getSpadesLengthTo())) {
            throw new RuntimeException("黑桃张数设置错误");
        } else if (pbncodeNorthHearts.length() > Integer.parseInt(bridgeHand.getHeartsLengthTo())) {
            throw new RuntimeException("红桃张数设置错误");
        } else if (pbncodeNorthDiamonds.length() > Integer.parseInt(bridgeHand.getDiamondsLengthTo())) {
            throw new RuntimeException("方片张数设置错误");
        } else if (pbncodeNorthClubs.length() > Integer.parseInt(bridgeHand.getClubsLengthTo())) {
            throw new RuntimeException("草花张数设置错误");
        }

        if ("".equals(bridgeHand.getHcpTo())) {
            bridgeHand.setHcpTo("37");
        }
        if (calcHcp(pbncodeNorthClubs) + calcHcp(pbncodeNorthDiamonds) + calcHcp(pbncodeNorthHearts) + calcHcp(pbncodeNorthSpades) > Integer.parseInt(bridgeHand.getHcpTo())) {
            throw new RuntimeException("点力设置有误，请确认");
        }
        return resultMessage;
    }

    private void validationHCP(BridgeHand bridgeHand) {
        String hcpFrom = bridgeHand.getHcpFrom();
        String hcpTo = bridgeHand.getHcpTo();

        if ("".equals(hcpFrom)) {
            hcpFrom = "0";
        }
        if ("".equals(hcpTo)) {
            hcpTo = "40";
        }

        int hcpFromInt = 0;
        int hcpToInt = 0;
        int hcp = calcHcp(bridgeHand.getPbncodeSpades() + "." + bridgeHand.getPbncodeHearts() + "." + bridgeHand.getPbncodeDiamonds() + "." + bridgeHand.getPbncodeClubs());

        hcpFromInt = Integer.parseInt(hcpFrom);
        hcpToInt = Integer.parseInt(hcpTo);
        bridgeHand.setHcpFrom(String.valueOf(hcpFromInt));
        bridgeHand.setHcpTo(String.valueOf(hcpToInt));
        if (hcpFromInt > hcpToInt) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }
        if (hcpFromInt < 0 || hcpToInt < 0) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }
        if (hcpFromInt < 2 && hcpToInt < hcpFromInt + 3) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }
        if (hcpFromInt > 33 - hcp) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }

        if ("".equals(bridgeHand.getSpadesLengthTo())) {
            bridgeHand.setSpadesLengthTo("13");
        }
        if ("".equals(bridgeHand.getHeartsLengthTo())) {
            bridgeHand.setHeartsLengthTo("13");
        }
        if ("".equals(bridgeHand.getDiamondsLengthTo())) {
            bridgeHand.setDiamondsLengthTo("13");
        }
        if ("".equals(bridgeHand.getClubsLengthTo())) {
            bridgeHand.setClubsLengthTo("13");
        }
        int spadesLengthFrom = 0;
        int spadesLengthTo = 0;
        int heartsLengthFrom = 0;
        int heartsLengthTo = 0;
        int diamondsLengthFrom = 0;
        int diamondsLengthTo = 0;
        int clubsLengthFrom = 0;
        int clubsLengthTo = 0;
        spadesLengthFrom = Integer.parseInt(bridgeHand.getSpadesLengthFrom());
        spadesLengthTo = Integer.parseInt(bridgeHand.getSpadesLengthTo());
        heartsLengthFrom = Integer.parseInt(bridgeHand.getHeartsLengthFrom());
        heartsLengthTo = Integer.parseInt(bridgeHand.getHeartsLengthTo());
        diamondsLengthFrom = Integer.parseInt(bridgeHand.getDiamondsLengthFrom());
        diamondsLengthTo = Integer.parseInt(bridgeHand.getDiamondsLengthTo());
        clubsLengthFrom = Integer.parseInt(bridgeHand.getClubsLengthFrom());
        clubsLengthTo = Integer.parseInt(bridgeHand.getClubsLengthTo());
        if (spadesLengthFrom < 0 || spadesLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (spadesLengthTo < 0 || spadesLengthTo < spadesLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (heartsLengthFrom < 0 || heartsLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (heartsLengthTo < 0 || heartsLengthTo < heartsLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (diamondsLengthFrom < 0 || diamondsLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (diamondsLengthTo < 0 || diamondsLengthTo < diamondsLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (clubsLengthFrom < 0 || clubsLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (clubsLengthTo < 0 || diamondsLengthTo < diamondsLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (spadesLengthFrom + heartsLengthFrom + diamondsLengthFrom + clubsLengthFrom > 13) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (spadesLengthTo + heartsLengthTo + diamondsLengthTo + clubsLengthTo < 13) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }


        if (!"ANY".equals(bridgeHand.getPartnerHandPattern())) {
            if ("UNBALANCE".equals(bridgeHand.getPartnerHandPattern())) {
                if (checkUnBalanceHand(bridgeHand)) {
                    throw new RuntimeException("非平均牌型设置与花色张数设置有误");
                }
            } else {
                if ("BALANCE".equals(bridgeHand.getPartnerHandPattern())) {
                    if (!checkBalanceHand(bridgeHand)) {
                        throw new RuntimeException("平均牌型设置与花色张数设置有误");
                    }
                }
            }
        }
    }

    private void validationHCPEast(BridgeHand bridgeHand) {
        String hcpFrom = bridgeHand.getHcpFromEast();
        String hcpTo = bridgeHand.getHcpToEast();

        if ("".equals(hcpFrom)) {
            hcpFrom = "0";
        }
        if ("".equals(hcpTo)) {
            hcpTo = "40";
        }

        int hcpFromInt = 0;
        int hcpToInt = 0;
        int hcp = calcHcp(bridgeHand.getPbncodeEastSpades() + "." + bridgeHand.getPbncodeEastHearts() + "." + bridgeHand.getPbncodeEastDiamonds() + "." + bridgeHand.getPbncodeEastClubs());

        hcpFromInt = Integer.parseInt(hcpFrom);
        hcpToInt = Integer.parseInt(hcpTo);
        bridgeHand.setHcpFromEast(String.valueOf(hcpFromInt));
        bridgeHand.setHcpToEast(String.valueOf(hcpToInt));
        if (hcpFromInt > hcpToInt) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }
        if (hcpFromInt < 0 || hcpToInt < 0) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }
        if (hcpFromInt < 2 && hcpToInt < hcpFromInt + 3) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }
        if (hcpFromInt > 33 - hcp) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }

        if ("".equals(bridgeHand.getSpadesLengthToEast())) {
            bridgeHand.setSpadesLengthToEast("13");
        }
        if ("".equals(bridgeHand.getHeartsLengthToEast())) {
            bridgeHand.setHeartsLengthToEast("13");
        }
        if ("".equals(bridgeHand.getDiamondsLengthToEast())) {
            bridgeHand.setDiamondsLengthToEast("13");
        }
        if ("".equals(bridgeHand.getClubsLengthToEast())) {
            bridgeHand.setClubsLengthToEast("13");
        }
        int spadesLengthFrom = 0;
        int spadesLengthTo = 0;
        int heartsLengthFrom = 0;
        int heartsLengthTo = 0;
        int diamondsLengthFrom = 0;
        int diamondsLengthTo = 0;
        int clubsLengthFrom = 0;
        int clubsLengthTo = 0;
        spadesLengthFrom = Integer.parseInt(bridgeHand.getSpadesLengthFromEast());
        spadesLengthTo = Integer.parseInt(bridgeHand.getSpadesLengthToEast());
        heartsLengthFrom = Integer.parseInt(bridgeHand.getHeartsLengthFromEast());
        heartsLengthTo = Integer.parseInt(bridgeHand.getHeartsLengthToEast());
        diamondsLengthFrom = Integer.parseInt(bridgeHand.getDiamondsLengthFromEast());
        diamondsLengthTo = Integer.parseInt(bridgeHand.getDiamondsLengthToEast());
        clubsLengthFrom = Integer.parseInt(bridgeHand.getClubsLengthFromEast());
        clubsLengthTo = Integer.parseInt(bridgeHand.getClubsLengthToEast());
        if (spadesLengthFrom < 0 || spadesLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (spadesLengthTo < 0 || spadesLengthTo < spadesLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (heartsLengthFrom < 0 || heartsLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (heartsLengthTo < 0 || heartsLengthTo < heartsLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (diamondsLengthFrom < 0 || diamondsLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (diamondsLengthTo < 0 || diamondsLengthTo < diamondsLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (clubsLengthFrom < 0 || clubsLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (clubsLengthTo < 0 || diamondsLengthTo < diamondsLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (spadesLengthFrom + heartsLengthFrom + diamondsLengthFrom + clubsLengthFrom > 13) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (spadesLengthTo + heartsLengthTo + diamondsLengthTo + clubsLengthTo < 13) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }


        if (!"ANY".equals(bridgeHand.getPartnerHandPatternEast())) {
            if ("UNBALANCE".equals(bridgeHand.getPartnerHandPatternEast())) {
                if (checkUnBalanceHand(bridgeHand)) {
                    throw new RuntimeException("非平均牌型设置与花色张数设置有误");
                }
            } else {
                if ("BALANCE".equals(bridgeHand.getPartnerHandPatternEast())) {
                    if (!checkBalanceHandEast(bridgeHand)) {
                        throw new RuntimeException("平均牌型设置与花色张数设置有误");
                    }
                }
            }
        }
    }

    private void validationHCPWest(BridgeHand bridgeHand) {
        String hcpFrom = bridgeHand.getHcpFromWest();
        String hcpTo = bridgeHand.getHcpToWest();

        if ("".equals(hcpFrom)) {
            hcpFrom = "0";
        }
        if ("".equals(hcpTo)) {
            hcpTo = "40";
        }

        int hcpFromInt = 0;
        int hcpToInt = 0;
        int hcp = calcHcp(bridgeHand.getPbncodeWestSpades() + "." + bridgeHand.getPbncodeWestHearts() + "." + bridgeHand.getPbncodeWestDiamonds() + "." + bridgeHand.getPbncodeWestClubs());

        hcpFromInt = Integer.parseInt(hcpFrom);
        hcpToInt = Integer.parseInt(hcpTo);
        bridgeHand.setHcpFromWest(String.valueOf(hcpFromInt));
        bridgeHand.setHcpToWest(String.valueOf(hcpToInt));
        if (hcpFromInt > hcpToInt) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }
        if (hcpFromInt < 0 || hcpToInt < 0) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }
        if (hcpFromInt < 2 && hcpToInt < hcpFromInt + 3) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }
        if (hcpFromInt > 33 - hcp) {
            throw new RuntimeException("点力范围设置非法或设置过大或过小");
        }

        if ("".equals(bridgeHand.getSpadesLengthToWest())) {
            bridgeHand.setSpadesLengthToWest("13");
        }
        if ("".equals(bridgeHand.getHeartsLengthToWest())) {
            bridgeHand.setHeartsLengthToWest("13");
        }
        if ("".equals(bridgeHand.getDiamondsLengthToWest())) {
            bridgeHand.setDiamondsLengthToWest("13");
        }
        if ("".equals(bridgeHand.getClubsLengthToWest())) {
            bridgeHand.setClubsLengthToWest("13");
        }
        int spadesLengthFrom = 0;
        int spadesLengthTo = 0;
        int heartsLengthFrom = 0;
        int heartsLengthTo = 0;
        int diamondsLengthFrom = 0;
        int diamondsLengthTo = 0;
        int clubsLengthFrom = 0;
        int clubsLengthTo = 0;
        spadesLengthFrom = Integer.parseInt(bridgeHand.getSpadesLengthFromWest());
        spadesLengthTo = Integer.parseInt(bridgeHand.getSpadesLengthToWest());
        heartsLengthFrom = Integer.parseInt(bridgeHand.getHeartsLengthFromWest());
        heartsLengthTo = Integer.parseInt(bridgeHand.getHeartsLengthToWest());
        diamondsLengthFrom = Integer.parseInt(bridgeHand.getDiamondsLengthFromWest());
        diamondsLengthTo = Integer.parseInt(bridgeHand.getDiamondsLengthToWest());
        clubsLengthFrom = Integer.parseInt(bridgeHand.getClubsLengthFromWest());
        clubsLengthTo = Integer.parseInt(bridgeHand.getClubsLengthToWest());
        if (spadesLengthFrom < 0 || spadesLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (spadesLengthTo < 0 || spadesLengthTo < spadesLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (heartsLengthFrom < 0 || heartsLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (heartsLengthTo < 0 || heartsLengthTo < heartsLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (diamondsLengthFrom < 0 || diamondsLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (diamondsLengthTo < 0 || diamondsLengthTo < diamondsLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (clubsLengthFrom < 0 || clubsLengthFrom > 9) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (clubsLengthTo < 0 || diamondsLengthTo < diamondsLengthFrom) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (spadesLengthFrom + heartsLengthFrom + diamondsLengthFrom + clubsLengthFrom > 13) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }
        if (spadesLengthTo + heartsLengthTo + diamondsLengthTo + clubsLengthTo < 13) {
            throw new RuntimeException("花色长度设置非法或设置过大或过小");
        }


        if (!"ANY".equals(bridgeHand.getPartnerHandPatternWest())) {
            if ("UNBALANCE".equals(bridgeHand.getPartnerHandPatternWest())) {
                if (checkUnBalanceHand(bridgeHand)) {
                    throw new RuntimeException("非平均牌型设置与花色张数设置有误");
                }
            } else {
                if ("BALANCE".equals(bridgeHand.getPartnerHandPatternWest())) {
                    if (!checkBalanceHandWest(bridgeHand)) {
                        throw new RuntimeException("平均牌型设置与花色张数设置有误");
                    }
                }
            }
        }
    }

    private void checkHand(BridgeHand bridgeHand) {
        checkSuits(bridgeHand.getPbncodeClubs());
        checkSuits(bridgeHand.getPbncodeDiamonds());
        checkSuits(bridgeHand.getPbncodeHearts());
        checkSuits(bridgeHand.getPbncodeSpades());
        checkSuits(bridgeHand.getPbncodeClubs());
        checkSuits(bridgeHand.getPbncodeEastClubs());
        checkSuits(bridgeHand.getPbncodeEastDiamonds());
        checkSuits(bridgeHand.getPbncodeEastHearts());
        checkSuits(bridgeHand.getPbncodeEastSpades());
        checkSuits(bridgeHand.getPbncodeWestClubs());
        checkSuits(bridgeHand.getPbncodeWestDiamonds());
        checkSuits(bridgeHand.getPbncodeWestHearts());
        checkSuits(bridgeHand.getPbncodeWestSpades());
        checkSuits(bridgeHand.getPbncodeNorthSpades());
        checkSuits(bridgeHand.getPbncodeNorthHearts());
        checkSuits(bridgeHand.getPbncodeNorthDiamonds());
        checkSuits(bridgeHand.getPbncodeNorthClubs());
    }

    private void checkSuits(String suits) {
        List<String> tmpList = new ArrayList<String>();
        if (suits != null) {
            for (int i = 0; i < suits.length(); i++) {
                if (!BridgeSimulatorConfig.numberList.contains(suits.substring(i, i + 1))) {
                    throw new RuntimeException("卡牌设置错误，请仔细确认");
                }
                if (tmpList.contains(suits.substring(i, i + 1))) {
                    throw new RuntimeException("卡牌设置重复，请仔细确认");
                }
                tmpList.add(suits.substring(i, i + 1));
            }
        }
    }
}
