package com.nhulston.essentials.commands.top;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.util.Msg;
import com.nhulston.essentials.util.TeleportUtil;
import javax.annotation.Nonnull;
public class TopCommand extends AbstractPlayerCommand {
    private static final int MAX_HEIGHT = 256;
    public TopCommand() {
        super("top", "Teleport to the highest block");
        requirePermission("essentials.top");
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            Msg.fail(context, "Could not get your position.");
            return;
        }
        Vector3d pos = transform.getPosition();
        int blockX = (int) Math.floor(pos.x);
        int blockZ = (int) Math.floor(pos.z);
        long chunkIndex = ChunkUtil.indexChunkFromBlock(blockX, blockZ);
        WorldChunk chunk = world.getChunk(chunkIndex);
        if (chunk == null) {
            Msg.fail(context, "Chunk not loaded.");
            return;
        }
        Integer topY = findHighestSolidBlock(chunk, blockX, blockZ);
        if (topY == null) {
            Msg.fail(context, "No solid ground found above.");
            return;
        }
        double targetY = topY + 1;
        double centerX = Math.floor(pos.x) + 0.5;
        double centerZ = Math.floor(pos.z) + 0.5;
        Vector3d targetPos = new Vector3d(centerX, targetY, centerZ);
        float yaw = transform.getRotation().y;
        float cardinalYaw = TeleportUtil.roundToCardinalYaw(yaw);
        Vector3f rotation = new Vector3f(0, cardinalYaw, 0);
        Teleport teleport = new Teleport(world, targetPos, rotation);
        store.putComponent(ref, Teleport.getComponentType(), teleport);
        Msg.success(context, "Poof!");
    }
    private Integer findHighestSolidBlock(WorldChunk chunk, int x, int z) {
        for (int y = MAX_HEIGHT; y >= 0; y--) {
            BlockType blockType = chunk.getBlockType(x, y, z);
            if (blockType != null && blockType.getMaterial() == BlockMaterial.Solid) {
                return y;
            }
        }
        return null;
    }
}
