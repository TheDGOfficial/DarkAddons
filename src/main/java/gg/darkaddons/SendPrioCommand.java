package gg.darkaddons;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
final class SendPrioCommand extends CommandBase {
    @NotNull
    private static final String COMMAND_PREFIX = "/";
    @NotNull
    private static final String COMMAND_NAME = "sendprio";

    SendPrioCommand() {
        super();
    }

    @Override
    @NotNull
    public final String getCommandName() {
        return SendPrioCommand.COMMAND_NAME;
    }

    @Override
    @NotNull
    public final List<String> getCommandAliases() {
        return super.getCommandAliases();
    }

    @Override
    public final boolean canCommandSenderUseCommand(@Nullable final ICommandSender sender) {
        return super.canCommandSenderUseCommand(sender);
    }

    @Override
    @Nullable
    public final List<String> addTabCompletionOptions(@Nullable final ICommandSender sender, @Nullable final String[] args, @Nullable final BlockPos pos) {
        return super.addTabCompletionOptions(sender, args, pos);
    }

    @Override
    @NotNull
    public final String getCommandUsage(@Nullable final ICommandSender sender) {
        return SendPrioCommand.COMMAND_PREFIX + SendPrioCommand.COMMAND_NAME;
    }

    @Override
    public final int getRequiredPermissionLevel() {
        return super.getRequiredPermissionLevel() - 4;
    }

    @Override
    public final void processCommand(@Nullable final ICommandSender sender, @Nullable final String... args) {
        DarkAddons.queueUserSentMessageOrCommand("/pc DP Settings: Power: 0 | Easy: 0 | Healer: 1 : 1 | Tank: 0 : 0");
    }
}
