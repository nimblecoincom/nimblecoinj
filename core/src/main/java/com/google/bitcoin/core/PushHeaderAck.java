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
public class PushHeaderAck extends Message {

    private static final long serialVersionUID = 7715090780994971876L;

    private static final Logger log = LoggerFactory.getLogger(PushHeaderAck.class);

    private Sha256Hash hash;

    public PushHeaderAck(NetworkParameters params, byte[] payload) throws ProtocolException {
        super(params, payload, 0);
    }

    public PushHeaderAck(NetworkParameters params, Sha256Hash hash) throws ProtocolException {
        super(params);
        this.hash = hash;
    }

    @Override
    public void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        stream.write(Utils.reverseBytes(hash.getBytes()));
    }

    @Override
    protected void parseLite() throws ProtocolException {
        if (length == UNKNOWN_LENGTH) {
            length = Sha256Hash.HASH_SIZE_IN_BYTES;
        }
    }

    @Override
    void parse() throws ProtocolException {
        hash = readHash();
    }


    @Override
    public Sha256Hash getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "PushHeaderAck [hash=" + hash + "]";
    }
    
    
    
    

}
