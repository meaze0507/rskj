package co.rsk.peg;

import co.rsk.bitcoinj.core.Sha256Hash;
import co.rsk.bitcoinj.store.BlockStoreException;
import co.rsk.config.TestSystemProperties;
import org.ethereum.config.Constants;
import org.ethereum.config.blockchain.upgrades.ActivationConfig;
import org.ethereum.config.blockchain.upgrades.ActivationConfigsForTest;
import org.ethereum.core.Block;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.mockito.Mockito.*;

public class BridgeNewMethodsTest {
    private static Random random = new Random();

    private TestSystemProperties config;
    private Constants constants;
    private ActivationConfig activationConfig;
    private BridgeSupport bridgeSupport;
    private BridgeSupportFactory bridgeSupportFactory;
    private Block rskExecutionBlock;
    private Bridge bridge;

    @Before
    public void beforeEach() {
        config = spy(new TestSystemProperties());
        constants = Constants.regtest();
        when(config.getNetworkConstants()).thenReturn(constants);
        activationConfig = spy(ActivationConfigsForTest.genesis());
        when(config.getActivationConfig()).thenReturn(activationConfig);
        bridgeSupport = mock(BridgeSupport.class);
        bridgeSupportFactory = mock(BridgeSupportFactory.class);
        when(bridgeSupportFactory.newInstance(any(), any(), any(), any())).thenReturn(bridgeSupport);
        rskExecutionBlock = mock(Block.class);
        when(rskExecutionBlock.getNumber()).thenReturn(42L);

        bridge = new Bridge(
                null,
                constants,
                activationConfig,
                bridgeSupportFactory
        );

        bridge.init(null, rskExecutionBlock, null, null, null, null);
    }

    @Test
    public void getBestBlockNumber() throws IOException, BlockStoreException {
        when(bridgeSupport.getBtcBlockchainBestChainHeight()).thenReturn(42);

        long result = bridge.getBestBlockNumber(new Object[0]);

        Assert.assertEquals(42, result);
    }

    @Test
    public void getBestBlockHeader() throws IOException, BlockStoreException {
        byte[] header = new byte[80];
        random.nextBytes(header);
        when(bridgeSupport.getBtcBlockchainSerializedBestBlockHeader()).thenReturn(header);
        byte[] result = bridge.getBestBlockHeader(new Object[0]);

        Assert.assertArrayEquals(header, result);
    }

    @Test
    public void getBestBlockHash() throws IOException, BlockStoreException {
        byte[] hashBytes = new byte[32];
        random.nextBytes(hashBytes);
        Sha256Hash hash = new Sha256Hash(hashBytes);
        when(bridgeSupport.getBtcBlockchainBestBlockHash()).thenReturn(hash);
        byte[] result = bridge.getBestBlockHash(new Object[0]);

        Assert.assertArrayEquals(hashBytes, result);
    }
}
