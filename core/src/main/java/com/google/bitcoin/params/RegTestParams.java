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
    private static final BigInteger PROOF_OF_WORK_LIMIT = new BigInteger("7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

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
        proofOfWorkLimit = Utils.decodeCompactBits(0x1f00ffffL);
        dumpedPrivateKeyHeader = 239;
        genesisBlock.setTime(1398701303L);
        genesisBlock.setDifficultyTarget(0x1f00ffffL);
        genesisBlock.setNonce(61715);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("0000ea3f810a0dd1226a2a6ac69539e53b81e85d91f6c58e051b6ee02abe2e95"));
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
