package gg.darkaddons.mixins;

import gg.darkaddons.EntityWitherKingDragon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.entity.boss.EntityDragon;

@Mixin(value = EntityDragon.class, priority = 999)
final class MixinEntityDragon implements EntityWitherKingDragon {
    @Unique
    private int witherKingDragonTypeOrdinal;

    private MixinEntityDragon() {
        super();
    }

    @Override
    public final int getWitherKingDragonTypeOrdinal() {
        return this.witherKingDragonTypeOrdinal;
    }

    @Override
    public final void setWitherKingDragonTypeOrdinal(final int witherKingDragonTypeOrdinal) {
        this.witherKingDragonTypeOrdinal = witherKingDragonTypeOrdinal;
    }

    @Unique
    @Override
    public final String toString() {
        return "MixinEntityDragon{" +
            "witherKingDragonTypeOrdinal=" + this.witherKingDragonTypeOrdinal +
            '}';
    }
}
