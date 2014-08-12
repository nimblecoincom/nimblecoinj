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
import java.util.List;

import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.StoredUndoableBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.params.RegTestParams;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;
import com.google.bitcoin.store.WalletProtobufSerializer;

/**
 * A command line tool for manipulating wallets and working with Bitcoin.
 */
public class DbTool {
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
       
       /*
       StoredBlock stored1 = store.get(new Sha256Hash("0134924192cd6c2bdb9b39a65327e29f5dc76fcf9d13862d48f8da5e8fd0d75d"));                                                       
       StoredBlock stored2 = store.get(new Sha256Hash("0000004192cd6c2bdb9b39a65327e29f5dc76fcf9d13862d48f8da5e8fd0d700"));
       StoredUndoableBlock undo1 = store.getUndoBlock(new Sha256Hash("0000004192cd6c2bdb9b39a65327e29f5dc76fcf9d13862d48f8da5e8fd0d700"));
       StoredUndoableBlock undo2 = store.getUndoBlock(new Sha256Hash("000000c8bc9dfb736dcc02e0800e4aa24868e740344706e85adb13070cbbec00"));
       */
       
       List<StoredUndoableBlock> undos = store.getUndoBlocksUsingTransaction(new Sha256Hash("6567f6384b75d52e29c0f71002d48ba30ca7fd391b382c05041da14de4c4c878"));
       System.out.println(undos);
       
        
       store.close();
        
    }


   
   
    

}
