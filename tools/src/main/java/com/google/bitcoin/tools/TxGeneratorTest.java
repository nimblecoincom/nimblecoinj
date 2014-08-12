/*
 * Copyright 2012 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.bitcoin.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredUndoableBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionOutPoint;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.params.RegTestParams;
import com.google.bitcoin.store.H2FullPrunedBlockStore;
import com.google.bitcoin.store.WalletProtobufSerializer;

/**
 * A command line tool for manipulating wallets and working with Bitcoin.
 */
public class TxGeneratorTest {
   public static void main(String[] args) throws Exception {
       String walletPath = "/Users/oscar/codigo/nimblecoinj/tools/testbox/data/regtest1.wallet";
       String chainPath = "/Users/oscar/codigo/nimblecoinj/tools/testbox/data/regtest1.chain";

       File walletFile = new File(walletPath);
       Wallet wallet = null;

       try {
           WalletProtobufSerializer loader = new WalletProtobufSerializer();
           FileInputStream fis=new FileInputStream(walletFile);
           wallet = loader.readWallet(new BufferedInputStream(fis));
           fis.close();
       } catch (Exception e) {
           System.err.println("Failed to load wallet '" + walletFile + "': " + e.getMessage());
           e.printStackTrace();
           return;
       }

       NetworkParameters params = RegTestParams.get();
       H2FullPrunedBlockStore store = new H2FullPrunedBlockStore(params, chainPath, 500000);
       FullPrunedBlockChain chain = new FullPrunedBlockChain(params, wallet, store);
       
       Set<TransactionOutPoint> usedOutpoints = new HashSet<TransactionOutPoint>();

       for (int i = 0; i < 100000; i++) {
           Transaction t = new Transaction(params);
           ECKey outputKey = new ECKey();
           BigInteger value = Utils.toNanoCoins(0,1);
           t.addOutput(value, outputKey);

           Wallet.SendRequest req = Wallet.SendRequest.forTx(t);
           if (t.getOutput(0).getValue().equals(wallet.getBalance())) {
               req.emptyWallet = true;
           }
           BigInteger fee = BigInteger.ZERO;
           req.fee = fee;
           wallet.completeTx(req);
           t = req.tx;   // Not strictly required today.
           wallet.commitTx(t);
           TransactionOutPoint outPoint = t.getInputs().get(0).getOutpoint();
           if (!usedOutpoints.contains(outPoint)) {
               usedOutpoints.add(outPoint);
           } else {
               System.out.println("Double spend");
               System.out.println("i " + i);
               System.out.println(outPoint);
               System.exit(1);
           }
       }
       
       List<StoredUndoableBlock> undos = store.getUndoBlocksUsingTransaction(new Sha256Hash("6567f6384b75d52e29c0f71002d48ba30ca7fd391b382c05041da14de4c4c878"));
       System.out.println(undos);
       
        
       store.close();
        
    }


   
   
    

}
