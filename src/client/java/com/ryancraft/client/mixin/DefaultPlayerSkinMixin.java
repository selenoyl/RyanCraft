package com.ryancraft.client.mixin;

import com.ryancraft.RyanConfig;
import net.minecraft.client.resources.DefaultPlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Players without a custom skin get Steve or Alex, chosen the pre-1.19.3 way
 * (odd UUID hash = Alex, even = Steve), instead of the nine-skin pool.
 */
@Mixin(DefaultPlayerSkin.class)
public abstract class DefaultPlayerSkinMixin {
    @Unique private static boolean ryancraft$probing;
    @Unique private static Object ryancraft$steve;
    @Unique private static Object ryancraft$alex;

    @Inject(method = "get(Ljava/util/UUID;)Lnet/minecraft/world/entity/player/PlayerSkin;", at = @At("HEAD"), cancellable = true)
    private static void ryancraft$classicSkin(UUID uuid, CallbackInfoReturnable<Object> cir) {
        if (ryancraft$probing || !RyanConfig.get().classicDefaultSkins) {
            return;
        }
        if (ryancraft$steve == null || ryancraft$alex == null) {
            ryancraft$findClassicSkins();
        }
        Object skin = (uuid.hashCode() & 1) == 1 ? ryancraft$alex : ryancraft$steve;
        if (skin != null) {
            cir.setReturnValue(skin);
        }
    }

    /** Find vanilla's Steve (wide) and Alex (slim) entries by asking for skins until both turn up. */
    @Unique
    private static void ryancraft$findClassicSkins() {
        ryancraft$probing = true;
        try {
            for (long i = 0; i < 64 && (ryancraft$steve == null || ryancraft$alex == null); i++) {
                Object skin = DefaultPlayerSkin.get(new UUID(0L, i));
                String text = String.valueOf(skin);
                if (text.contains("wide/steve")) ryancraft$steve = skin;
                if (text.contains("slim/alex")) ryancraft$alex = skin;
            }
        } finally {
            ryancraft$probing = false;
        }
    }
}
