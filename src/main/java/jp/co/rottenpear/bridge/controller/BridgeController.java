package jp.co.rottenpear.bridge.controller;

import ddsjava.dto.CalculateResponse;
import jp.co.rottenpear.bridge.config.BridgeSimulatorConfig;
import jp.co.rottenpear.bridge.model.Hand;
import jp.co.rottenpear.bridge.model.BridgeHand;
import jp.co.rottenpear.bridge.service.BridgeSimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
        int hcp = calcHcp(bridgeHand.getPbncodeSpades()+"."+bridgeHand.getPbncodeHearts()+"."+bridgeHand.getPbncodeDiamonds()+"."+bridgeHand.getPbncodeClubs());
        try {
            hcpFromInt = Integer.parseInt(hcpFrom);
            hcpToInt = Integer.parseInt(hcpTo);
            if (hcpFromInt > hcpToInt) {
                throw new RuntimeException();
            }
            if (hcpFromInt < 0 || hcpToInt < 0) {
                throw new RuntimeException();
            }
            if (hcpFromInt < 2 && hcpToInt < hcpFromInt + 3) {
                throw new RuntimeException();
            }
            if (hcpFromInt > 33 - hcp) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            resultMessage = "点力范围设置非法或设置过大或过小";
            ModelAndView mv = new ModelAndView("bridgeSimulatorResult");
            mv.addObject("resultMessage", resultMessage);
            return mv;
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
        try {
            spadesLengthFrom = Integer.parseInt(bridgeHand.getSpadesLengthFrom());
            spadesLengthTo = Integer.parseInt(bridgeHand.getSpadesLengthTo());
            heartsLengthFrom = Integer.parseInt(bridgeHand.getHeartsLengthFrom());
            heartsLengthTo = Integer.parseInt(bridgeHand.getHeartsLengthTo());
            diamondsLengthFrom = Integer.parseInt(bridgeHand.getDiamondsLengthFrom());
            diamondsLengthTo = Integer.parseInt(bridgeHand.getDiamondsLengthTo());
            clubsLengthFrom = Integer.parseInt(bridgeHand.getClubsLengthFrom());
            clubsLengthTo = Integer.parseInt(bridgeHand.getClubsLengthTo());
            if (spadesLengthFrom < 0 || spadesLengthFrom > 9) {
                throw new RuntimeException();
            }
            if (spadesLengthTo < 0 || spadesLengthTo < spadesLengthFrom) {
                throw new RuntimeException();
            }
            if (heartsLengthFrom < 0 || heartsLengthFrom > 9) {
                throw new RuntimeException();
            }
            if (heartsLengthTo < 0 || heartsLengthTo < heartsLengthFrom) {
                throw new RuntimeException();
            }
            if (diamondsLengthFrom < 0 || diamondsLengthFrom > 9) {
                throw new RuntimeException();
            }
            if (diamondsLengthTo < 0 || diamondsLengthTo < diamondsLengthFrom) {
                throw new RuntimeException();
            }
            if (clubsLengthFrom < 0 || clubsLengthFrom > 9) {
                throw new RuntimeException();
            }
            if (clubsLengthTo < 0 || diamondsLengthTo < diamondsLengthFrom) {
                throw new RuntimeException();
            }
            if (spadesLengthFrom + heartsLengthFrom + diamondsLengthFrom + clubsLengthFrom > 13) {
                throw new RuntimeException();
            }
            if (spadesLengthTo + heartsLengthTo + diamondsLengthTo + clubsLengthTo < 13) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            resultMessage = "花色长度设置非法或设置过大或过小";
            ModelAndView mv = new ModelAndView("bridgeSimulatorResult");
            mv.addObject("resultMessage", resultMessage);
            return mv;
        }

        if (!"ANY".equals(bridgeHand.getPartnerHandPattern())) {
            if ("UNBALANCE".equals(bridgeHand.getPartnerHandPattern())) {
                if (checkUnBalanceHand(bridgeHand)) {
                    resultMessage = "非平均牌型设置与花色张数设置有误";
                    ModelAndView mv = new ModelAndView("bridgeSimulatorResult");
                    mv.addObject("resultMessage", resultMessage);
                    return mv;
                }
            } else {
                if ("BALANCE".equals(bridgeHand.getPartnerHandPattern())) {
                    if (!checkBalanceHand(bridgeHand)) {
                        resultMessage = "平均牌型设置与花色张数设置有误";
                        ModelAndView mv = new ModelAndView("bridgeSimulatorResult");
                        mv.addObject("resultMessage", resultMessage);
                        return mv;
                    }
                }
            }
        }
        CalculateResponse calculateResponse = null;
        try {
            if (BridgeSimulatorConfig.syncCount >= BridgeSimulatorConfig.syncLimit) {
                resultMessage = "服务器繁忙，有其他用户正在使用，请稍后再试";
                ModelAndView mv = new ModelAndView("bridgeSimulatorResult");
                mv.addObject("resultMessage", resultMessage);
                return mv;
            }
            BridgeSimulatorConfig.syncCount++;
            bridgeHand.setHcpFrom(String.valueOf(hcpFromInt));
            bridgeHand.setHcpTo(String.valueOf(hcpToInt));
            calculateResponse = bridgeSimulatorService.calculator(bridgeHand);
        } catch (Exception e) {
            resultMessage = "抱歉，程序内部错误：" + e.getStackTrace();
            ModelAndView mv = new ModelAndView("bridgeSimulatorResult");
            mv.addObject("resultMessage", resultMessage);
            return mv;
        } catch (StackOverflowError error) {
            resultMessage = "条件太苛刻，无法模拟牌型，请等待网站算法升级";
            ModelAndView mv = new ModelAndView("bridgeSimulatorResult");
            mv.addObject("resultMessage", resultMessage);
            return mv;
        } finally {
            if (BridgeSimulatorConfig.syncCount > 0) {
                BridgeSimulatorConfig.syncCount--;
            }
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
        return  String.valueOf(BridgeSimulatorConfig.syncCount);
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
}
