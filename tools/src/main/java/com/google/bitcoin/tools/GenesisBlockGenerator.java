package com.google.bitcoin.tools;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptOpCodes;

/**
 * Generates Nimblecoin genesis block
 * @author Oscar Guindzberg
 *
 */
public class GenesisBlockGenerator {
	public static void main(String[] args) {
		Block genesis = createGenesis(MainNetParams.get());
		System.out.println("Genesis block: " + genesis);
	}
	
    private static Block createGenesis(NetworkParameters params) {
        Transaction t = new Transaction(params);
        try {
        	String genesisMessage = "Life is what happens to you while you’re busy making other plans";
        	char[] chars = genesisMessage.toCharArray();
        	byte[] bytes = new byte[chars.length];
        	for(int i=0;i<bytes.length;i++) bytes[i] = (byte) chars[i];
            TransactionInput ti = new TransactionInput(params, t, bytes);
            t.addInput(ti);
            
            ECKey key = new ECKey();
            System.out.println("Genesis key " + key);
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, key.getPubKey());
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(params, t, Utils.toNanoCoins(50, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        
        long version = NetworkParameters.PROTOCOL_VERSION;
        Sha256Hash prevBlockHash = Sha256Hash.ZERO_HASH;
        Sha256Hash merkleRoot = null;
        long time = System.currentTimeMillis() / 1000;
        long difficultyTarget = 0x1d00ffffL;
        long nonce = 0;
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(t);
        Block genesisBlock = new Block(params, version, prevBlockHash, merkleRoot, time, difficultyTarget, nonce, transactions);
        genesisBlock.solve();
        return genesisBlock;
    }
	
}
