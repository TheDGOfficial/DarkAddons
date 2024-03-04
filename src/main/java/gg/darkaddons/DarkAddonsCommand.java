package gg.darkaddons;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
final class DarkAddonsCommand extends CommandBase {
    @NotNull
    static final String COMMAND_PREFIX = "/";
    @NotNull
    static final String MAIN_COMMAND_NAME = "darkaddons";

    @SuppressWarnings("StaticCollection")
    @NotNull
    private static final ArrayList<String> commandAliases = new ArrayList<>(2);

    static {
        DarkAddonsCommand.commandAliases.add("darkaddon");
        DarkAddonsCommand.commandAliases.add("da");
    }

    DarkAddonsCommand() {
        super();
    }

    @SuppressWarnings("SuspiciousGetterSetter")
    @Override
    @NotNull
    public final String getCommandName() {
        return DarkAddonsCommand.MAIN_COMMAND_NAME;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    @NotNull
    public final ArrayList<String> getCommandAliases() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return DarkAddonsCommand.commandAliases;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public final boolean canCommandSenderUseCommand(@SuppressWarnings("NullableProblems") @NotNull final ICommandSender sender) {
        return true;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    @Nullable
    public final ArrayList<String> addTabCompletionOptions(@SuppressWarnings("NullableProblems") @NotNull final ICommandSender sender, @Nullable final String[] args, @SuppressWarnings("NullableProblems") @NotNull final BlockPos pos) {
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
        return null;
    }

    @Override
    @NotNull
    public final String getCommandUsage(@Nullable final ICommandSender sender) {
        return DarkAddonsCommand.COMMAND_PREFIX + DarkAddonsCommand.MAIN_COMMAND_NAME;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public final int getRequiredPermissionLevel() {
        return 0;
    }

    private static final boolean hasArgs(@Nullable final String... args) {
        return null != args && 0 < args.length;
    }

    @Override
    public final void processCommand(@SuppressWarnings("NullableProblems") @NotNull final ICommandSender sender, @Nullable final String... args) {
        if (DarkAddonsCommand.hasArgs(args)) {
            final var subcommand = args[0];
            if (null != subcommand) {
                final var subCommand = SubCommand.match(subcommand);
                if (null == subCommand) {
                    DarkAddons.queueWarning("Unknown subcommand. Type /" + DarkAddonsCommand.MAIN_COMMAND_NAME + " help to get a list of subcommands.");
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
