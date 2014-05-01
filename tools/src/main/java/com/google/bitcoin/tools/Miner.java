package com.google.bitcoin.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.VerificationException;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptOpCodes;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;

/**
 * Reference miner
 * @author Oscar Guindzberg
 *
 */
public class Miner {
	
    private static final Logger log = LoggerFactory.getLogger(Miner.class);
	
	public static void main(String[] args) throws Exception {
		NetworkParameters params = MainNetParams.get();
		String walletFileName = "main.miner.wallet";
        String chainFileName = "main.miner.chain";
        Wallet wallet;
        File walletFile = new File(walletFileName);
        if(walletFile.exists()) {
        	wallet = Wallet.loadFromFile(walletFile);	
        } else {
        	wallet = new Wallet(params);
        }
        
        FullPrunedBlockStore store = new H2FullPrunedBlockStore(params, new File(chainFileName).getAbsolutePath(), 5000);
		FullPrunedBlockChain chain = new FullPrunedBlockChain(params, wallet, store);

        for (int i = 0; i < 100; i++) {
    		mineForNetwork(params, wallet, chain, store);
    		Thread.sleep(500);
		}

		wallet.saveToFile(new File(walletFileName));       
		store.close();
    }
	
	public static void mineForNetwork(NetworkParameters params, Wallet wallet, FullPrunedBlockChain chain, FullPrunedBlockStore store) throws Exception {
		Transaction coinbaseTransaction = new Transaction(params);
    	String coibaseMessage = "Minining NimbleCoin" + System.currentTimeMillis();
    	char[] chars = coibaseMessage.toCharArray();
    	byte[] bytes = new byte[chars.length];
    	for(int i=0;i<bytes.length;i++) bytes[i] = (byte) chars[i];
        TransactionInput ti = new TransactionInput(params, coinbaseTransaction, bytes);
        coinbaseTransaction.addInput(ti);        
        ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
        ECKey key = new ECKey();
        wallet.addKey(key);
        Script.writeBytes(scriptPubKeyBytes, key.getPubKey());
        scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
        coinbaseTransaction.addOutput(new TransactionOutput(params, coinbaseTransaction, Utils.toNanoCoins(50, 0), scriptPubKeyBytes.toByteArray()));
        StoredBlock chainHead = chain.getChainHead();
        Sha256Hash prevBlockHash = chainHead.getHeader().getHash();        
        long time = System.currentTimeMillis() / 1000;
        long difficultyTarget = getDifficultyTargetForNewBlock(chainHead, store, params, time);
        
        Block newBlock = new Block(params, NetworkParameters.PROTOCOL_VERSION, prevBlockHash, time, difficultyTarget);
        newBlock.addTransaction(coinbaseTransaction);
        newBlock.solve();
        newBlock.verify();
        chain.add(newBlock);
        log.info("Mined block: " + newBlock);
		
	}

	private static long getDifficultyTargetForNewBlock(StoredBlock storedPrev, BlockStore blockStore, NetworkParameters params, long time) throws BlockStoreException {
        if ((storedPrev.getHeight() + 1) % params.getInterval() != 0) {
    		return storedPrev.getHeader().getDifficultyTarget();
        }
        StoredBlock ancestorBlock = storedPrev;
        for (int i = 0; i < params.getInterval() - 1; i++) {
            if (ancestorBlock == null) {
                // This should never happen. If it does, it means we are following an incorrect or busted chain.
                throw new VerificationException(
                        "Difficulty transition point but we did not find a way back to the genesis block.");
            }
            ancestorBlock = blockStore.get(ancestorBlock.getHeader().getPrevBlockHash());
        }
        int timespan = (int) (storedPrev.getHeader().getTimeSeconds() - ancestorBlock.getHeader().getTimeSeconds());
        log.debug("timespan: " + timespan);
        // Limit the adjustment step.
        final int targetTimespan = params.getTargetTimespan();
        if (timespan < targetTimespan / 4)
            timespan = targetTimespan / 4;
        if (timespan > targetTimespan * 4)
            timespan = targetTimespan * 4;

        BigInteger newDifficulty = Utils.decodeCompactBits(storedPrev.getHeader().getDifficultyTarget());
        newDifficulty = newDifficulty.multiply(BigInteger.valueOf(timespan));
        newDifficulty = newDifficulty.divide(BigInteger.valueOf(targetTimespan));

        if (newDifficulty.compareTo(params.getProofOfWorkLimit()) > 0) {
            newDifficulty = params.getProofOfWorkLimit();
        }

        
        /*
        int accuracyBytes = (int) (nextBlock.getDifficultyTarget() >>> 24) - 3;
        BigInteger receivedDifficulty = nextBlock.getDifficultyTargetAsInteger();
        // The calculated difficulty is to a higher precision than received, so reduce here.
        BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
        newDifficulty = newDifficulty.and(mask);
        */

        long newDifficultyCompact = Utils.encodeCompactBits(newDifficulty);
        log.debug("newDifficultyCompact: " + Long.toHexString(newDifficultyCompact));
        return newDifficultyCompact;
        
	}
	
}
