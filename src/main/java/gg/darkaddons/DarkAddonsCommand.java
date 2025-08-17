package gg.darkaddons;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import com.google.common.collect.ImmutableList;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
final class DarkAddonsCommand extends CommandBase {
    @NotNull
    static final String COMMAND_PREFIX = "/";
    @NotNull
    static final String COMMAND_NAME = "darkaddons";

    @NotNull
    private static final List<String> COMMAND_ALIASES =
        ImmutableList.of("darkaddon", "da");

    DarkAddonsCommand() {
        super();
    }

    @Override
    @NotNull
    public final String getCommandName() {
        return DarkAddonsCommand.COMMAND_NAME;
    }

    @Override
    @NotNull
    public final List<String> getCommandAliases() {
        return ImmutableList.<String>builder()
            .addAll(super.getCommandAliases())
            .addAll(DarkAddonsCommand.COMMAND_ALIASES)
            .build();
    }

    @Override
    public final boolean canCommandSenderUseCommand(@Nullable final ICommandSender sender) {
        return super.canCommandSenderUseCommand(sender);
    }

    @Override
    @Nullable
    public final List<String> addTabCompletionOptions(@Nullable final ICommandSender sender, @Nullable final String[] args, @Nullable final BlockPos pos) {
        if (DarkAddonsCommand.hasArgs(args)) {
            final var subcommand = args[0];
            if (null != subcommand) {
                final var exactMatch = SubCommand.match(subcommand);

                if (null == exactMatch) {
                    return SubCommand.tabCompleteSubCommand(subcommand);
                }

                if (2 <= args.length) {
                    return SubCommand.tabCompleteArguments(exactMatch, args);
                }
            }
        }
        return super.addTabCompletionOptions(sender, args, pos);
    }

    @Override
    @NotNull
    public final String getCommandUsage(@Nullable final ICommandSender sender) {
        return DarkAddonsCommand.COMMAND_PREFIX + DarkAddonsCommand.COMMAND_NAME;
    }

    @Override
    public final int getRequiredPermissionLevel() {
        return super.getRequiredPermissionLevel() - 4;
    }

    private static final boolean hasArgs(@Nullable final String... args) {
        return null != args && 0 < args.length;
    }

    @Override
    public final void processCommand(@Nullable final ICommandSender sender, @Nullable final String... args) {
        if (DarkAddonsCommand.hasArgs(args)) {
            final var subcommand = args[0];
            if (null != subcommand) {
                final var subCommand = SubCommand.match(subcommand);
                if (null == subCommand) {
                    DarkAddons.queueWarning("Unknown subcommand. Type " + DarkAddonsCommand.COMMAND_PREFIX + DarkAddonsCommand.COMMAND_NAME + " help to get a list of subcommands.");
                } else {
                    subCommand.setArgs(args);

                    try {
                        subCommand.execute();
                    } catch (final Throwable error) {
                        DarkAddons.queueWarning("Error when executing command " + args[0]);
                        DarkAddons.modError(error);
                    } finally {
                        subCommand.resetArgs();
                    }
                }
                return;
            }
        }

        DarkAddons.openGui("options", new OptionsScreen());
    }
}
