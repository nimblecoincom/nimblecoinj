/*
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A protocol message that contains a block header, sent when a new block was mined.
 */
public class PushHeader extends Message {

    private static final long serialVersionUID = 4727383916183085872L;

    private static final Logger log = LoggerFactory.getLogger(PushHeader.class);

    private Block blockHeader;

    public PushHeader(NetworkParameters params, byte[] payload) throws ProtocolException {
        super(params, payload, 0);
    }

    public PushHeader(NetworkParameters params, Block header) throws ProtocolException {
        super(params);
        blockHeader = header;
    }

    @Override
    public void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        if (blockHeader.transactions == null)
            blockHeader.bitcoinSerializeToStream(stream);
        else
            blockHeader.cloneAsHeader().bitcoinSerializeToStream(stream);
        stream.write(0);
    }

    @Override
    protected void parseLite() throws ProtocolException {
        if (length == UNKNOWN_LENGTH) {
            length = 81;
        }
    }

    @Override
    void parse() throws ProtocolException {
        byte[] bytes = readBytes(81);
        if (bytes[80] != 0)
            throw new ProtocolException("Block header does not end with a null byte");        
        blockHeader = new Block(this.params, bytes, true, true, 81);
        if (log.isDebugEnabled()) {
            log.debug(this.blockHeader.toString());
        }
    }


    public Block getBlockHeader() {
        return blockHeader;
    }
    
    @Override
    public Sha256Hash getHash() {
        return blockHeader.getHash();
    }

    @Override
    public String toString() {
        return "PushHeader [blockHeader=" + blockHeader + "]";
    }
    
    

}
