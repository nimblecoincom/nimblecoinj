package com.google.bitcoin.tools;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.AbstractBlockChain;
import com.google.bitcoin.core.AbstractBlockChainListener;
import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.StoredTransactionOutput;
import com.google.bitcoin.core.StoredUndoableBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutPoint;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.VerificationException;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.params.RegTestParams;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptOpCodes;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * Reference miner
 * @author Oscar Guindzberg
 *
 */
public class Miner extends AbstractExecutionThreadService {
	
    private static final Logger log = LoggerFactory.getLogger(Miner.class);
    private NetworkParameters params; 
    private PeerGroup peers;
    private Wallet wallet;
    private FullPrunedBlockStore store; 
    private AbstractBlockChain chain;
    private boolean newBestBlockArrivedFromAnotherNode = false;
    private int numberOfMinersInParallelToEmulate = 0;
    
    public Miner(NetworkParameters params, PeerGroup peers, Wallet wallet, FullPrunedBlockStore store, AbstractBlockChain chain) {
        this.params = params;
        this.peers = peers;
        this.wallet = wallet;
        this.store = store;
        this.chain = chain;
    }
	
    private class MinerBlockChainListener extends AbstractBlockChainListener {
        @Override
        public void notifyNewBestBlock(StoredBlock storedBlock) throws VerificationException {
            try {
                boolean isMyBlock = false;
                StoredUndoableBlock storedUndoableBlock = store.getUndoBlock(storedBlock.getHeader().getHash());
                for (Transaction tx : storedUndoableBlock.getTransactions()) {
                    if (tx.isCoinBase() && tx.getOutput(0).isMine(wallet)) {
                        isMyBlock = true;
                        break;
                    }
                }
                if (!isMyBlock) {
                    newBestBlockArrivedFromAnotherNode=true;
                    log.info("Signaled mining to interrupt because this block arrived: " + storedBlock.getHeader().getHash());                
                }                
            } catch (BlockStoreException e) {
                log.warn("Exception retrieving undoable block: " + storedBlock.getHeader().getHash(), e);                                
            }
        }
    }
    
    MinerBlockChainListener minerBlockChainListener = new MinerBlockChainListener();
    
    @Override
    protected void startUp() throws Exception {
        super.startUp();
        chain.addListener(minerBlockChainListener);
    }
    
    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        chain.removeListener(minerBlockChainListener);
    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            try {
                //System.out.println("Press any key to mine 1 block...");
                //System.in.read();
                mine();
                Thread.sleep(getMillisToSleep());
            } catch (Exception e) {
                log.error("Exception mining", e);
            }
        }
    }
    
    public long getMillisToSleep() {
        if (numberOfMinersInParallelToEmulate>0) {
            double rate = 1d/NetworkParameters.TARGET_SPACING;
            double result = -1 * Math.log(1 - new Random().nextDouble()) / rate;
            long millis = Math.round(result*1000);
            return millis * numberOfMinersInParallelToEmulate;            
        } else {
            return 0;
        }
    }
    
    public void setNumberOfMinersInParallelToEmulate(int numberOfMinersInParallelToEmulate) {
        this.numberOfMinersInParallelToEmulate = numberOfMinersInParallelToEmulate;
    }
	
	
	private void mine() throws Exception {
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
        Set<Transaction> transactionsToInclude = getTransactionsToInclude(peers.getMemoryPool().getAll());
        for (Transaction transaction : transactionsToInclude) {
            newBlock.addTransaction(transaction);
        }       
        log.info("Starting to mine block " + newBlock);
        newBestBlockArrivedFromAnotherNode = false;
        while (!newBestBlockArrivedFromAnotherNode) {
            try {
                // Is our proof of work valid yet?
                if (newBlock.checkProofOfWork(false))
                    break;
                // No, so increment the nonce and try again.
                newBlock.setNonce(newBlock.getNonce() + 1);
                if (newBlock.getNonce() % 100000 == 0 ) {
                    log.info("Solving block. Nonce: " + newBlock.getNonce());
                }
                
            } catch (VerificationException e) {
                throw new RuntimeException(e); // Cannot happen.
            }
        }
        if (newBestBlockArrivedFromAnotherNode) {
            log.info("Interrupted mining because another best block arrived");
            return;
        }
        newBlock.verify();
        chain.add(newBlock);
        log.info("Mined block: " + newBlock);
        for (Transaction transaction : transactionsToInclude) {
            peers.getMemoryPool().remove(transaction.getHash());
        }
        peers.broadcastBlock(newBlock);
	}

	private Set<Transaction> getTransactionsToInclude(Set<Transaction> allTransactions) throws BlockStoreException {
	    Set<Transaction> transactionsToInclude = new TreeSet<Transaction>(new TransactionPriorityComparator());
	    for (Transaction transaction : allTransactions) {
            if (!store.hasUnspentOutputs(transaction.getHash(), transaction.getOutputs().size())) {                
                // Transaction was not already included in a block that is part of the best chain 
                boolean allOutPointsAreInTheBestChain = true;
                boolean allOutPointsAreMature = true;
                for (TransactionInput transactionInput : transaction.getInputs()) {
                    TransactionOutPoint outPoint = transactionInput.getOutpoint();
                    StoredTransactionOutput storedOutPoint = store.getTransactionOutput(outPoint.getHash(), outPoint.getIndex());
                    if (storedOutPoint == null) {
                        //Outpoint not in the best chain
                        allOutPointsAreInTheBestChain = false;
                        break;
                    }
                    if ((chain.getBestChainHeight()+1) - storedOutPoint.getHeight() < params.getSpendableCoinbaseDepth()) {
                        //Outpoint is a non mature coinbase
                        allOutPointsAreMature = false;
                        break;
                    }
                    
                }
                if (allOutPointsAreInTheBestChain && allOutPointsAreMature) {
                    transactionsToInclude.add(transaction);                    
                }
            }
            
        }	    
        return ImmutableSet.copyOf(Iterables.limit(transactionsToInclude, 1000));
    }

    private static class TransactionPriorityComparator implements Comparator<Transaction>{
        @Override
        public int compare(Transaction tx1, Transaction tx2) {
            int updateTimeComparison = tx1.getUpdateTime().compareTo(tx2.getUpdateTime());
            //If time1==time2, compare by tx hash to make comparator consistent with equals
            return updateTimeComparison!=0 ? updateTimeComparison : tx1.getHash().compareTo(tx2.getHash());
        }
    }
    	

    private long getDifficultyTargetForNewBlock(StoredBlock storedPrev, BlockStore blockStore, NetworkParameters params, long time) throws BlockStoreException {
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
        log.info("Difficulty changed to : " + Long.toHexString(newDifficultyCompact));
        return newDifficultyCompact;
        
	}
	
}
