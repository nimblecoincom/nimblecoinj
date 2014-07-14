package com.google.bitcoin.tools;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.params.RegTestParams;
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptOpCodes;

/**
 * Generates Nimblecoin genesis block
 * @author Oscar Guindzberg
 *
 */
public class GenesisBlockGenerator {
	public static void main(String[] args) throws Exception {

        ECKey key = new ECKey();
        System.out.println("public key " + Arrays.toString(key.getPubKey()));
        System.out.println("private key " + Arrays.toString(key.getPrivKeyBytes()));
        System.out.println("-- Main --");		
        generateGenesisForNetwork(MainNetParams.get(), key, 0x1f00ffffL);
        System.out.println("-- Test --");		
        generateGenesisForNetwork(TestNet3Params.get(), key, 0x1f00ffffL);
        System.out.println("-- RegTest --");       
        generateGenesisForNetwork(RegTestParams.get(), key, 0x200fffffL);
    }
	
	public static void generateGenesisForNetwork(NetworkParameters params, ECKey key, long difficultyTarget) throws Exception {
		Transaction t = new Transaction(params);
    	String genesisMessage = "Life is what happens to you while you’re busy making other plans";
    	char[] chars = genesisMessage.toCharArray();
    	byte[] bytes = new byte[chars.length];
    	for(int i=0;i<bytes.length;i++) bytes[i] = (byte) chars[i];
        TransactionInput ti = new TransactionInput(params, t, bytes);
        t.addInput(ti);        
        ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
        Script.writeBytes(scriptPubKeyBytes, key.getPubKey());
        scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
        t.addOutput(new TransactionOutput(params, t, Utils.toNanoCoins(50, 0), scriptPubKeyBytes.toByteArray()));


        Block genesisBlockMain = new Block(params);
        genesisBlockMain.setDifficultyTarget(difficultyTarget);
        genesisBlockMain.setNonce(0);
        genesisBlockMain.addTransaction(t);;
        genesisBlockMain.solve();

        System.out.println("Genesis Main");
        System.out.println("block: " + genesisBlockMain);
		
	}
	
}
