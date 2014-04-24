package com.google.bitcoin.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptOpCodes;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;

/**
 * Reference miner
 * @author Oscar Guindzberg
 *
 */
public class Miner {
	public static void main(String[] args) throws Exception {
		NetworkParameters params = MainNetParams.get();
		String walletFileName = "miner.wallet";
        String chainFileName = "miner.chain";
        Wallet wallet = new Wallet(params);
        FullPrunedBlockStore store = new H2FullPrunedBlockStore(params, new File(chainFileName).getAbsolutePath(), 5000);
		FullPrunedBlockChain chain = new FullPrunedBlockChain(params, wallet, store);

        mineForNetwork(params, wallet, chain);

		wallet.saveToFile(new File(walletFileName));        
    }
	
	public static void mineForNetwork(NetworkParameters params, Wallet wallet, FullPrunedBlockChain chain) throws Exception {
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

        Sha256Hash prevBlockHash = Sha256Hash.ZERO_HASH;
        Sha256Hash merkleRoot = null;
        long time = System.currentTimeMillis() / 1000;
        long difficultyTarget = 0x1f00ffffL;
        long nonce = 0;
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(coinbaseTransaction);
        Block newBlock = new Block(params, NetworkParameters.PROTOCOL_VERSION, prevBlockHash, merkleRoot, time, difficultyTarget, nonce, transactions);        
        newBlock.solve();
        chain.add(newBlock);
        System.out.println("block: " + newBlock);
		
	}
	
}
