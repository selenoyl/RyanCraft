package com.ryancraft.client.mixin;

import com.ryancraft.RyanConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hide the Report button in Social Interactions. */
@Mixin(PlayerEntry.class)
public abstract class PlayerEntryMixin {
    @Shadow @Nullable private Button reportButton;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ryancraft$hideReport(CallbackInfo ci) {
        if (RyanConfig.get().hideReportButton && this.reportButton != null) {
            this.reportButton.visible = false;
            this.reportButton.active = false;
        }
    }
}
