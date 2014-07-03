package com.google.bitcoin.tools;

import java.math.BigInteger;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * Generates N random transactions per second and broadcast them to the network
 * @author Oscar Guindzberg
 *
 */
public class TransactionGenerator extends AbstractExecutionThreadService {
	
    private static final Logger log = LoggerFactory.getLogger(TransactionGenerator.class);
    private NetworkParameters params; 
    private PeerGroup peers;
    private Wallet wallet;

    private int numberOfTxPerSecondToGenerate;

    public TransactionGenerator(NetworkParameters params, PeerGroup peers, Wallet wallet, int numberOfTxPerSecondToGenerate) {
        this.params = params;
        this.peers = peers;
        this.wallet = wallet;
        this.numberOfTxPerSecondToGenerate = numberOfTxPerSecondToGenerate;
    }
	
    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            try {
                peers.waitForPeers(1).get();
                generateAndBroadcastTx();
                Thread.sleep(getMillisToSleep());
            } catch (Exception e) {
                log.error("Exception mining", e);
            }
        }
    }
    
    public long getMillisToSleep(){        
        double result = -1 * Math.log(1 - new Random().nextDouble()) / this.numberOfTxPerSecondToGenerate;
        long millis = Math.round(result*1000);
        return millis;
    }
	
	
	private void generateAndBroadcastTx() throws Exception {
        Transaction t = new Transaction(params);
        ECKey outputKey = new ECKey();
        BigInteger value = Utils.toNanoCoins(0,1);
        t.addOutput(value, outputKey);

        Wallet.SendRequest req = Wallet.SendRequest.forTx(t);
        if (t.getOutput(0).getValue().equals(wallet.getBalance())) {
            log.info("Emptying out wallet, recipient may get less than what you expect");
            req.emptyWallet = true;
        }
        BigInteger fee = BigInteger.ZERO;
        req.fee = fee;
        wallet.completeTx(req);
        t = req.tx;   // Not strictly required today.
        wallet.commitTx(t);
        peers.broadcastTransactionToAll(t);
 
	}


	
}
