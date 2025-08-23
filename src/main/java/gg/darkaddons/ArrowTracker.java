package gg.darkaddons;

import com.google.common.base.MoreObjects;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

final class ArrowTracker {
    private static final @NotNull Map<Vec3i, ArrowTracker.OwnedData> ownedArrows = new ConcurrentHashMap<>(Utils.calculateHashMapCapacity(500));
    private static final @NotNull Map<Integer, ArrowTracker.ArrowData> arrows = new ConcurrentHashMap<>(Utils.calculateHashMapCapacity(500));
    private static long currentTick;

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private ArrowTracker() {
        super();

        throw Utils.staticClassException();
    }

    private static final boolean shouldTrack() {
        return Config.isStatueDestroyedNotification() || Config.isStatueMissedNotification();
    }

    static final void onArrowHit(@NotNull final EntityArrow arrow, @NotNull final Entity entity) {
        if (ArrowTracker.shouldTrack() && !entity.isDead) {
            final var data = ArrowTracker.arrows.get(arrow.getEntityId());
            if (null != data) {
                data.entitiesHit.add(entity);
            }
        }
    }

    static final void onEntityMetadata(@NotNull final S1CPacketEntityMetadata pem) {
        if (!ArrowTracker.shouldTrack()) {
            return;
        }

        final var entityId = pem.getEntityId();
        final var data = ArrowTracker.arrows.get(entityId);
        if (null != data && null == data.arrow) {
            final var world = Minecraft.getMinecraft().theWorld;
            if (null != world) {
                final var entity = world.getEntityByID(entityId);
                if (entity instanceof final EntityArrow arrow) {
                    data.arrow = arrow;
                    data.owner = ArrowTracker.findOwner(arrow);
                }
            }
        }
    }

    static final void onPacket(@NotNull final Packet<?> packet) {
        if (!ArrowTracker.shouldTrack()) {
            return;
        }

        if (packet instanceof final S0EPacketSpawnObject spawn && 60 == spawn.getType()) {
            ArrowTracker.arrows.put(spawn.getEntityID(), new ArrowTracker.ArrowData());
        } else if (packet instanceof final S13PacketDestroyEntities destroy) {
            for (final var id : destroy.getEntityIDs()) {
                final var data = ArrowTracker.arrows.remove(id);
                if (null != data && null != data.arrow && null != data.owner) {
                    DarkAddons.onArrowDespawn(data.arrow, data.owner, data.entitiesHit);
                }
            }
        } else if (packet instanceof final S32PacketConfirmTransaction pct && 1 > pct.getActionNumber()) {
            ++ArrowTracker.currentTick;
            ArrowTracker.ownedArrows.entrySet().removeIf(e -> 12L < ArrowTracker.currentTick - e.getValue().addedTime);
        }
    }

    private static final @Nullable Entity findOwner(@NotNull final EntityArrow arrow) {
        final var entityId = arrow.getEntityId();
        final var existingData = ArrowTracker.arrows.get(entityId);

        if (null != existingData && null != existingData.owner) {
            return existingData.owner;
        }

        final var arrowPos = new Vec3i(arrow.serverPosX, arrow.serverPosY, arrow.serverPosZ);

        if (null != arrow.shootingEntity) {
            ArrowTracker.ownedArrows.put(arrowPos, new ArrowTracker.OwnedData(arrow.shootingEntity, ArrowTracker.currentTick));
            return arrow.shootingEntity;
        }

        final var direct = ArrowTracker.ownedArrows.get(arrowPos);

        if (null != direct) {
            return direct.owner;
        }

        final var offsetPos = new Vec3i(arrowPos.getX(), arrowPos.getY() + 16, arrowPos.getZ());
        final var offset = ArrowTracker.ownedArrows.get(offsetPos);

        return null == offset ? null : offset.owner;
    }

    private static final class OwnedData {
        private final @NotNull Entity owner;
        private final long addedTime;

        private OwnedData(@NotNull final Entity owner, final long addedTime) {
            super();

            this.owner = owner;
            this.addedTime = addedTime;
        }

        @Override
        public final boolean equals(@Nullable final Object obj) {
            return this == obj || obj instanceof final ArrowTracker.OwnedData ownedData && this.addedTime == ownedData.addedTime && this.owner.equals(ownedData.owner);
        }

        @Override
        public final int hashCode() {
            var result = this.owner.hashCode();
            result = 31 * result + Long.hashCode(this.addedTime);

            return result;
        }

        @Override
        @NotNull
        public final String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("owner", this.owner)
                    .add("addedTime", this.addedTime)
                    .toString();
        }
    }

    private static final class ArrowData {
        private @Nullable Entity owner;
        private @Nullable EntityArrow arrow;
        private final @NotNull ArrayList<Entity> entitiesHit = new ArrayList<>(1);

        private ArrowData() {
            super();
        }

        @Override
        public final boolean equals(@Nullable final Object obj) {
            return this == obj || obj instanceof final ArrowTracker.ArrowData arrowData && Objects.equals(this.owner, arrowData.owner)
                && Objects.equals(this.arrow, arrowData.arrow)
                && this.entitiesHit.equals(arrowData.entitiesHit);
        }

        @Override
        public final int hashCode() {
            var result = Objects.hashCode(this.owner);
            result = 31 * result + Objects.hashCode(this.arrow);
            result = 31 * result + this.entitiesHit.hashCode();

            return result;
        }

        @Override
        @NotNull
        public final String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("owner", this.owner)
                    .add("arrow", this.arrow)
                    .add("entitiesHit", this.entitiesHit)
                    .toString();
        }
    }
}

