/*
 * Copyright 2013 Google Inc.
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

package com.google.bitcoin.params;

import static com.google.common.base.Preconditions.checkState;

import java.math.BigInteger;

import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Utils;

/**
 * Network parameters for the regression test mode of bitcoind in which all blocks are trivially solvable.
 */
public class RegTestParams extends NetworkParameters {

    public RegTestParams() {
        super();
        id = ID_REGTEST;
        packetMagic = 0xfabfb5daL;
        port = 18555;
        addressHeader = 111;
        p2shHeader = 196;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        proofOfWorkLimit = Utils.decodeCompactBits(0x200fffffL);
        dumpedPrivateKeyHeader = 239;
        genesisPubKey = new byte[]{2, -89, -16, -84, 59, -17, -13, -114, 124, 51, 13, -9, -97, 104, -74, -25, -61, 12, -28, 98, 70, 120, 16, 113, -110, -102, -109, 81, -64, 54, -44, -86, 19};
        genesisBlock = createGenesis();
        genesisBlock.setTime(1405357765L);
        genesisBlock.setDifficultyTarget(0x200fffffL);
        genesisBlock.setNonce(8);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;

        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("0134924192cd6c2bdb9b39a65327e29f5dc76fcf9d13862d48f8da5e8fd0d75d"));
        dnsSeeds = null;
    }

    @Override
    public boolean allowEmptyPeerChain() {
        return true;
    }


    private static RegTestParams instance;
    public static synchronized RegTestParams get() {
        if (instance == null) {
            instance = new RegTestParams();
        }
        return instance;
    }

    public String getPaymentProtocolId() {
        return null;
    }
}
