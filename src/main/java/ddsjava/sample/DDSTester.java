package ddsjava.sample;

import bridge.domain.BridgeGame;
import bridge.domain.Contract;
import bridge.domain.utils.BridgeHelper;
import ddsjava.DDSConnect;
import ddsjava.DDSException;

import java.util.List;

public class DDSTester {
    public void ma(String[] args) {
        DDSConnect dds = new DDSConnect();
        String pbnCode = "E:AT5.AJT.A632.KJ7 Q763.KQ9.KQJ94.T 942.87653..98653 KJ8.42.T875.AQ42";
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
            BridgeGame game = BridgeHelper.getGameFromPBN(pbnCode, contract.getShortString());
            System.out.println(game.getDeclarer()+":"+game.getContract().getTrump()+":"+game.getContract().getValue());

        }
//        for (Contract contract : contracts) {
//            BridgeGame game = BridgeHelper.getGameFromPBN(pbnCode, contract.getShortString());
//            System.out.println(game.getDeclarer()+":"+game.getContract().getTrump()+":"+game.getContract().getValue());
//        }
    }
}
