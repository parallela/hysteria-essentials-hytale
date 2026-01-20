package com.nhulston.essentials.commands.personal;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nhulston.essentials.managers.PersonalBenchManager;
import com.nhulston.essentials.managers.PersonalBenchManager.PendingProtection;
import com.nhulston.essentials.models.PersonalBenchProtection;
import com.nhulston.essentials.models.PersonalBenchProtection.ProtectionFlag;
import com.nhulston.essentials.util.Msg;
import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;
public class PersonalProtectCommand extends AbstractPlayerCommand {
    private final PersonalBenchManager benchManager;
    public PersonalProtectCommand(@Nonnull PersonalBenchManager benchManager) {
        super("personal", "Protect your recently placed workbench");
        this.benchManager = benchManager;
        setAllowsExtraArguments(true);
        addAliases("pp");
    }
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String[] args = context.getInputString().trim().split("\\s+");
        if (args.length < 2 || !args[1].equalsIgnoreCase("protect")) {
            Msg.fail(context, "Usage: /personal protect [use] [destroy]");
            Msg.info(context, "Flags:");
            Msg.info(context, "  use - Prevent others from using the bench");
            Msg.info(context, "  destroy - Prevent others from destroying the bench");
            Msg.info(context, "Default: Both flags enabled if none specified");
            return;
        }
        PendingProtection pending = benchManager.getPendingProtection(playerRef.getUuid());
        if (pending == null) {
            Msg.fail(context, "You haven't placed a bench recently.");
            Msg.info(context, "Place a workbench and use this command within 5 minutes.");
            return;
        }
        if (pending.isExpired()) {
            Msg.fail(context, "Your bench placement has expired (5 minute limit).");
            return;
        }
        Set<ProtectionFlag> flags = parseFlags(args);
        PersonalBenchProtection protection = benchManager.protectBench(pending, flags);
        Msg.success(context, "Bench protected successfully!");
        Msg.info(context, "Location: " + protection.getWorldName() + " " +
                 protection.getX() + ", " + protection.getY() + ", " + protection.getZ());
        Msg.info(context, "Type: " + getBenchTypeName(protection.getBenchType()));
        Msg.info(context, "Protection: " + formatFlags(flags));
    }
    private Set<ProtectionFlag> parseFlags(String[] args) {
        Set<ProtectionFlag> flags = EnumSet.noneOf(ProtectionFlag.class);
        if (args.length == 2) {
            flags.add(ProtectionFlag.USE);
            flags.add(ProtectionFlag.DESTROY);
            return flags;
        }
        for (int i = 2; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (arg.equals("use")) {
                flags.add(ProtectionFlag.USE);
            } else if (arg.equals("destroy")) {
                flags.add(ProtectionFlag.DESTROY);
            }
        }
        if (flags.isEmpty()) {
            flags.add(ProtectionFlag.USE);
            flags.add(ProtectionFlag.DESTROY);
        }
        return flags;
    }
    private String formatFlags(Set<ProtectionFlag> flags) {
        if (flags.size() == 2) {
            return "Full (Use + Destroy)";
        } else if (flags.contains(ProtectionFlag.USE)) {
            return "Use Only";
        } else if (flags.contains(ProtectionFlag.DESTROY)) {
            return "Destroy Only";
        }
        return "None";
    }
    private String getBenchTypeName(String blockId) {
        String lower = blockId.toLowerCase();
        if (lower.contains("arcanist")) return "Arcanist Workbench";
        if (lower.contains("smithing")) return "Smithing Bench";
        if (lower.contains("crafting")) return "Crafting Bench";
        if (lower.contains("workbench")) return "Workbench";
        if (lower.contains("bench")) return "Bench";
        return blockId;
    }
}
