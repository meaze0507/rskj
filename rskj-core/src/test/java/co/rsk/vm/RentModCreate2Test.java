package co.rsk.vm;

/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import co.rsk.config.TestSystemProperties;
import co.rsk.config.VmConfig;
import co.rsk.core.Coin;
import co.rsk.core.RskAddress;
import co.rsk.peg.BridgeSupportFactory;
import co.rsk.peg.RepositoryBtcBlockStoreWithCache;
import co.rsk.test.builders.AccountBuilder;
import co.rsk.test.builders.TransactionBuilder;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.ethereum.config.blockchain.upgrades.ActivationConfig;
import org.ethereum.core.Account;
import org.ethereum.core.BlockFactory;
import org.ethereum.core.Transaction;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.PrecompiledContracts;
import org.ethereum.vm.VM;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.Stack;
import org.ethereum.vm.program.invoke.ProgramInvokeMockImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashSet;

import static org.ethereum.config.blockchain.upgrades.ConsensusRule.RSKIP125;
import static org.mockito.Mockito.*;


/**
 * Created by Sebastian Sicardi on 22/05/2019.
 */
public class RentModCreate2Test {

    private ActivationConfig.ForBlock activationConfig;
    private ProgramInvokeMockImpl invoke = new ProgramInvokeMockImpl();
    private BytecodeCompiler compiler = new BytecodeCompiler();
    private final TestSystemProperties config = new TestSystemProperties();
    private final VmConfig vmConfig = config.getVmConfig();
    private final PrecompiledContracts precompiledContracts = new PrecompiledContracts(
            config,
            new BridgeSupportFactory(
                    new RepositoryBtcBlockStoreWithCache.Factory(
                            config.getNetworkConstants().getBridgeConstants().getBtcParams()),
                    config.getNetworkConstants().getBridgeConstants(),
                    config.getActivationConfig()));
    private final BlockFactory blockFactory = new BlockFactory(config.getActivationConfig());
    private final Transaction transaction = createTransaction(); // passed as argument to new program()

    @Before
    public void setup() {
        activationConfig = mock(ActivationConfig.ForBlock.class);
        when(activationConfig.isActive(RSKIP125)).thenReturn(true);
    }

    @Test
    public void testCREATE2_BasicTest() {
        /**
         * Initial test for Create2, just check that the contract is created
         */
        callCreate2("0x0f6510583d425cfcf94b99f8b073b44f60d1912b",
                "0x0000000000000000000000000000000000000000000000000000000000000000",
                "PUSH32 0x600b000000000000000000000000000000000000000000000000000000000000",
                2,
                0,
                "3BC3EFA1C487A1EBFC911B47B548E2C82436A212",
                32033);
    }

    @Test
    public void testCREATE2_SaltNumber() {
        /**
         * Check that address changes with different salt than before
         */
        callCreate2("0x0f6510583d425cfcf94b99f8b073b44f60d1912b",
                "0x00000000000000000000000000000000000000000000000000000000cafebabe",
                "PUSH32 0x601b000000000000000000000000000000000000000000000000000000000000",
                2,
                0,
                "19542B03F2D5D4E1910DBE096FAF0842D928883D",
                32033);
    }

    @Test
    public void testCREATE2_Address() {
        /**
         * Check that address changes with different sender address than before
         */
        callCreate2("0xdeadbeef00000000000000000000000000000000",
                "0x00000000000000000000000000000000000000000000000000000000cafebabe",
                "PUSH32 0x601b000000000000000000000000000000000000000000000000000000000000",
                2,
                0,
                "3BA1DC70CC17E740F4BD85052AF074B2B2A49E06",
                32033);
    }

    @Test
    public void testCREATE2_InitCode() {
        /**
         * Check for a different length of init_code
         */
        callCreate2("0xdeadbeef00000000000000000000000000000000",
                "0x00000000000000000000000000000000000000000000000000000000cafebabe",
                "PUSH32 0x601b601b601b601b601b601b601b601b601b601b601b601b601b601b601b601b",
                32,
                0,
                "D1FB828980EC250DD0A350E59108ECC63C2C4B36",
                32078);
    }

    @Test
    public void testCREATE2_ZeroSize() {
        /**
         * Check for a call with init_code with size 0
         * (Note that it should return same address than next test)
         */
        callCreate2("0x0f6510583d425cfcf94b99f8b073b44f60d1912b",
                "0x0000000000000000000000000000000000000000000000000000000000000000",
                "PUSH32 0x601b601b601b601b601b601b601b601b601b601b601b601b601b601b601b601b",
                0,
                0,
                "65BD0714DEFB919BC02F9507D6F9D9CD21195ECC",
                32024);
    }

    @Test
    public void testCREATE2_EmptyCode() {
        /**
         * Check for a call with no init_code
         * (Note that it should return same address than previous test)
         */
        callCreate2("0x0f6510583d425cfcf94b99f8b073b44f60d1912b",
                "0x0000000000000000000000000000000000000000000000000000000000000000",
                "",
                0,
                0,
                "65BD0714DEFB919BC02F9507D6F9D9CD21195ECC",
                32012);
    }

    @Test
    public void testCREATE2_CodeOffset() {
        /**
         * Check that the offset parameter works correctly
         */
        callCreate2("0x0f6510583d425cfcf94b99f8b073b44f60d1912b",
                "0x0000000000000000000000000000000000000000000000000000000000000000",
                "PUSH32 0x601b601b601b601b601b601b601b601b601b601b601b601b601b601b601b601b",
                10,
                8,
                "A992CD9E3E78C0A6BBBB4F06B52B3AD8924B0916",
                32045);
    }
    @Test
    public void testCREATE2_NoCodePushed() {
        /**
         * No code pushed but code sized is greater than zero, it should get zeroes and pass
         */
        callCreate2("0x0f6510583d425cfcf94b99f8b073b44f60d1912b",  //addr
                "0x0000000000000000000000000000000000000000000000000000000000000000", //salt
                "", //initcode
                12, //size
                0, //offset
                "16F27A604035007FA9925DB8CC2CAFDCFFC6278C", //expected addr
                32021); //gasexpected
    }


    @Test
    public void testCREATE_CheckFunctionBeforeRSKIP() {
        /**
         * Check that the CREATE opcode functions correctly before the RSKIP
         * It should create the contract and have nonce 0
         */

        when(activationConfig.isActive(RSKIP125)).thenReturn(false);

        String code = "PUSH1 0x01 PUSH1 0x02 PUSH1 0x00 CREATE";

        Program program = executeCode(code);

        Stack stack = program.getStack();
        String address = Hex.toHexString(stack.peek().getLast20Bytes());
        long nonce = program.getStorage().getNonce(new RskAddress(address)).longValue();

        Assert.assertEquals(0, nonce);
        Assert.assertEquals("77045E71A7A2C50903D88E564CD72FAB11E82051", address.toUpperCase());
        Assert.assertEquals(1, stack.size());
    }

   
    /** helpers */

    private void callCreate2(String address, String salt, String pushInitCode, int size, int intOffset, String expected, long gasExpected) {
        int value = 10;
        RskAddress testAddress = new RskAddress(address);
        invoke.setOwnerAddress(testAddress);
        invoke.getRepository().addBalance(testAddress, Coin.valueOf(value + 1));
        String inSize = "0x" + DataWord.valueOf(size);
        String inOffset = "0x" + DataWord.valueOf(intOffset);

        if (!pushInitCode.isEmpty()) {
            pushInitCode += " PUSH1 0x00 MSTORE";
        }

        Program program = executeCode(
                pushInitCode +
                        " PUSH32 " + salt +
                        " PUSH32 " + inSize +
                        " PUSH32 " + inOffset +
                        " PUSH32 " + "0x" + DataWord.valueOf(value) +
                        " CREATE2");
        Stack stack = program.getStack();
        String result = Hex.toHexString(Arrays.copyOfRange(stack.peek().getData(), 12, stack.peek().getData().length));

        Assert.assertEquals(1, stack.size());
        Assert.assertEquals(expected.toUpperCase(), result.toUpperCase());
        Assert.assertEquals(gasExpected, program.getResult().getExecGasUsed());
    }

    // for program, in VMComplex test, just `null` is used
    private static Transaction createTransaction() {
        int number = 0;
        AccountBuilder acbuilder = new AccountBuilder();
        acbuilder.name("sender" + number);
        Account sender = acbuilder.build();
        acbuilder.name("receiver" + number);
        Account receiver = acbuilder.build();
        TransactionBuilder txbuilder = new TransactionBuilder();
        return txbuilder.sender(sender).receiver(receiver).value(BigInteger.valueOf(number * 1000 + 1000)).build();
    }


    private Program executeCode(String stringCode) {
        byte[] code = compiler.compile(stringCode);
        VM vm = new VM(vmConfig,precompiledContracts);

        Program program = new Program(vmConfig, precompiledContracts, blockFactory, activationConfig, code, invoke, transaction, new HashSet<>());

        while (!program.isStopped()){
            vm.step(program);
        }

        return program;
    }
}
