package ddsjava.sample;

import bridge.domain.BridgeGame;
import bridge.domain.Contract;
import bridge.domain.PlayerPosition;
import bridge.domain.utils.BridgeHelper;
import ddsjava.DDSConnect;
import ddsjava.DDSException;
import ddsjava.dto.BestCard;

import java.util.List;

public class Application {
    public  void man(String[] args) {
        DDSConnect dds = new DDSConnect();
        String pbnCode = "E:A4.K98.T543.AQ65 Q832.A432..K7432 T965.J765.J876.8 KJ7.QT.AKQ92.JT9";
        System.out.println("Board: " + pbnCode);
        List<Contract> contracts = null;
        try {
             contracts = dds.calcMakableContracts(pbnCode);
        } catch (DDSException e) {
            e.printStackTrace();
            System.out.println("Error Code: " + e.getErrorCode());
            System.exit(1);
        }
        System.out.println("Best Results:");
        for (Contract contract : contracts) {
            System.out.println(contract);
        }
        for (Contract contract : contracts) {
            System.out.println("------------- Game Starts ----------------");
            System.out.println("Contract: " + contract);
            BridgeGame game = BridgeHelper.getGameFromPBN(pbnCode, contract.getShortString());
            System.out.println("Trump: " + game.getContract().getTrump());
            PlayerPosition player = BridgeHelper.getNextPlayerPosition(game.getDeclarer());
            while (game.getCardsRemaining() > 0) {
                BestCard result = null;
                try {
                    result = dds.solveBoardPBNBestCard(game);
                } catch (DDSException e) {
                    System.out.println("Error Message: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
                System.out.println(player + ": " + result.getCard() + ". Score: " + result.getScore());
                player = game.playCard(result.getCard(), player);
                if (game.getCurrentTrick().getDeck().getCount() == 0) {
                    System.out.println("Trick Winner: " + game.getTricks().get(game.getTricks().size() - 1).getTrickWinner());
                }
            }
            System.out.println("-----------Results----------");
            System.out.println("South/North: " + game.getNorthSouthTricksMade() + " tricks");
            System.out.println("East/West: " + game.getEastWestTricksMade() + " tricks");
        }
    }
}
