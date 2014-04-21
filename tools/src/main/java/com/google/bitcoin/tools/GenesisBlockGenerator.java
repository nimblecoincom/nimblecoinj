package com.google.bitcoin.tools;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.spongycastle.util.encoders.Hex;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptOpCodes;

public class GenesisBlockGenerator {
	public static void main(String[] args) {
		createGenesis(MainNetParams.get());
	}
	
    private static Block createGenesis(NetworkParameters params) {
        Transaction t = new Transaction(params);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"
            byte[] bytes = Hex.decode
                    ("04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73");
            TransactionInput ti = new TransactionInput(params, t, bytes);
            t.addInput(ti);
            
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Hex.decode
                    ("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(params, t, Utils.toNanoCoins(50, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }

        //length = 80;
        
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
