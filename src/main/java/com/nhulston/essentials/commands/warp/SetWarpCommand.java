package com.nhulston.essentials.commands.warp;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.managers.WarpManager;
import com.nhulston.essentials.util.Msg;
import javax.annotation.Nonnull;
public class SetWarpCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;
    private final RequiredArg<String> nameArg;
    public SetWarpCommand(@Nonnull WarpManager warpManager) {
        super("setwarp", "Set a warp location");
        this.warpManager = warpManager;
        this.nameArg = withRequiredArg("name", "Warp name", ArgTypes.STRING);
        requirePermission("essentials.setwarp");
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String warpName = context.get(nameArg);
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            Msg.fail(context, "Could not get your position. Try again.");
            return;
        }
        Vector3d position = transform.getPosition();
        HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        Vector3f rotation = (headRotation != null) ? headRotation.getRotation() : new Vector3f(0.0F, 0.0F, 0.0F);
        String error = warpManager.setWarp(
                warpName,
                world.getName(),
                position.getX(),
                position.getY(),
                position.getZ(),
                rotation.getY(),
                rotation.getX()
        );
        if (error != null) {
            Msg.fail(context, error);
            return;
        }
        Msg.success(context, String.format("Warp '%s' set in world %s", warpName, world.getName()));
    }
}
