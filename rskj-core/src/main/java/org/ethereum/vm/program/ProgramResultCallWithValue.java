package org.ethereum.vm.program;

import org.ethereum.vm.GasCost;

public class ProgramResultCallWithValue extends ProgramResult {
    public ProgramResultCallWithValue(ProgramResult result) {
        this.addDeductedRefund(result.getDeductedRefund());
        this.setGasUsed(result.getGasUsed());
    }

    @Override
    public long getGasUsed() {
        return super.getGasUsed() + GasCost.STIPEND_CALL;
    }
}
