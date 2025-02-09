package com.codenamed.rodspawn.block.entity.brazier;

import net.minecraft.util.Mth;

public class BrazierClientData {
    public static final float ROTATION_SPEED = 10.0F;
    private float currentSpin;
    private float previousSpin;

    public BrazierClientData() {
    }

    public float currentSpin() {
        return this.currentSpin;
    }

    public float previousSpin() {
        return this.previousSpin;
    }

    public void updateDisplayItemSpin() {
        this.previousSpin = this.currentSpin;
        this.currentSpin = Mth.wrapDegrees(this.currentSpin + 10.0F);
    }
}