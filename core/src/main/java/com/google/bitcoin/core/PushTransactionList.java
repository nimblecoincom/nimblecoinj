/**
 * Copyright 2011 Google Inc.
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

package com.google.bitcoin.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Represents the "pushtxlist" P2P network message. An pushtxlist contains a list of hashes of transactions.</p>
 */
public class PushTransactionList extends Message {

    private static final long serialVersionUID = -3625238502771381841L;

    private Sha256Hash blockHash;
    private Transaction coinbaseTransaction;
    private long arrayLen;
    protected List<Sha256Hash> transactionHashes;

    public static final long MAX_ITEMS = 50000;
    
    public PushTransactionList(NetworkParameters params, byte[] bytes) throws ProtocolException {
        super(params, bytes, 0);
    }

    /**
     * Deserializes an 'pushtxlist' message.
     * @param params NetworkParameters object.
     * @param msg Bitcoin protocol formatted byte array containing message content.
     * @param parseLazy Whether to perform a full parse immediately or delay until a read is requested.
     * @param parseRetain Whether to retain the backing byte array for quick reserialization.  
     * If true and the backing byte array is invalidated due to modification of a field then 
     * the cached bytes may be repopulated and retained if the message is serialized again in the future.
     * @param length The length of message if known.  Usually this is provided when deserializing of the wire
     * as the length will be provided as part of the header.  If unknown then set to Message.UNKNOWN_LENGTH
     * @throws ProtocolException
     */
    public PushTransactionList(NetworkParameters params, byte[] msg, boolean parseLazy, boolean parseRetain, int length)
            throws ProtocolException {
        super(params, msg, 0, parseLazy, parseRetain, length);
    }

    public Sha256Hash getBlockHash() {
        return blockHash;
    }
    
    @Override
    public Sha256Hash getHash() {
        return blockHash;
    }
    
    
    public PushTransactionList(NetworkParameters params, Block block) {
        super(params);
        this.blockHash = block.getHash();
        this.transactionHashes = new ArrayList<Sha256Hash>();
        length = Sha256Hash.HASH_SIZE_IN_BYTES; //the size of a block hash
        length += 1; //length of 0 varint (empty transaction list);
        for (Transaction tx : block.getTransactions()) {
            this.addTransaction(tx.getHash());          
            if (tx.isCoinBase()) {
                this.coinbaseTransaction = tx;
                length += tx.getMessageSize();
            }
        }
        
    }

    public List<Sha256Hash> getTransactionHashes() {
        maybeParse();
        return Collections.unmodifiableList(transactionHashes);
    }
    
    public Transaction getCoinbaseTransaction() {
        return coinbaseTransaction;
    }

    public void addTransaction(Sha256Hash txHash) {
        unCache();
        length -= VarInt.sizeOf(transactionHashes.size());
        transactionHashes.add(txHash);
        length += VarInt.sizeOf(transactionHashes.size()) + Sha256Hash.HASH_SIZE_IN_BYTES;
    }

    public void removeTransaction(int index) {
        unCache();
        length -= VarInt.sizeOf(transactionHashes.size());
        transactionHashes.remove(index);
        length += VarInt.sizeOf(transactionHashes.size()) - Sha256Hash.HASH_SIZE_IN_BYTES;
    }

    @Override
    protected void parseLite() throws ProtocolException {
        cursor = offset;
        blockHash = readHash();
        coinbaseTransaction = new Transaction(params, bytes, cursor, this, parseLazy, parseRetain, UNKNOWN_LENGTH);
        // Label the transaction as coming from the P2P network, so code that cares where we first saw it knows.
        coinbaseTransaction.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
        cursor += coinbaseTransaction.getMessageSize();        
        arrayLen = readVarInt();
        if (arrayLen > MAX_ITEMS)
            throw new ProtocolException("Too many items in pushtxlist message: " + arrayLen);
        length = (int) (cursor - offset + (arrayLen * Sha256Hash.HASH_SIZE_IN_BYTES));
    }

    @Override
    public void parse() throws ProtocolException {
        cursor = offset;
        blockHash = readHash();
        coinbaseTransaction = new Transaction(params, bytes, cursor, this, parseLazy, parseRetain, UNKNOWN_LENGTH);
        // Label the transaction as coming from the P2P network, so code that cares where we first saw it knows.
        coinbaseTransaction.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
        cursor += coinbaseTransaction.getMessageSize();        
        arrayLen = readVarInt();
        if (arrayLen > MAX_ITEMS)
            throw new ProtocolException("Too many items in PUSHTXLIST message: " + arrayLen);

        transactionHashes = new ArrayList<Sha256Hash>((int) arrayLen);
        for (int i = 0; i < arrayLen; i++) {
            if (cursor + Sha256Hash.HASH_SIZE_IN_BYTES > bytes.length) {
                throw new ProtocolException("Ran off the end of the PUSHTXLIST");
            }
            Sha256Hash txHash = readHash();
            transactionHashes.add(txHash);
        }
        bytes = null;
    }

    @Override
    public void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        stream.write(Utils.reverseBytes(blockHash.getBytes()));
        coinbaseTransaction.bitcoinSerialize(stream);
        stream.write(new VarInt(transactionHashes.size()).encode());
        for (Sha256Hash txHash : transactionHashes) {
            stream.write(Utils.reverseBytes(txHash.getBytes()));
        }
    }

    @Override
    public String toString() {
        return "PushTransactionList [blockHash=" + blockHash
                + ", coinbaseTransaction=" + coinbaseTransaction
                + ", arrayLen=" + arrayLen + ", transactionHashes="
                + transactionHashes + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PushTransactionList other = (PushTransactionList) obj;
        if (arrayLen != other.arrayLen)
            return false;
        if (blockHash == null) {
            if (other.blockHash != null)
                return false;
        } else if (!blockHash.equals(other.blockHash))
            return false;
        if (coinbaseTransaction == null) {
            if (other.coinbaseTransaction != null)
                return false;
        } else if (!coinbaseTransaction.equals(other.coinbaseTransaction))
            return false;
        if (transactionHashes == null) {
            if (other.transactionHashes != null)
                return false;
        } else if (!transactionHashes.equals(other.transactionHashes))
            return false;
        return true;
    }

    
    
    
}
