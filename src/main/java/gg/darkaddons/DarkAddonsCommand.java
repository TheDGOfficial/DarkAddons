package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
final class DarkAddonsCommand extends ModCommand {
    @NotNull
    static final String MAIN_COMMAND_NAME = "darkaddons";

    DarkAddonsCommand() {
        super(DarkAddonsCommand.MAIN_COMMAND_NAME, "darkaddon", "da");
    }

    @Override
    @Nullable
    final List<String> tabComplete(@Nullable final String... args) {
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
        return super.tabComplete(args);
    }

    private static final boolean hasArgs(@Nullable final String... args) {
        return null != args && 0 < args.length;
    }

    @Override
    final void execute(@Nullable final String... args) {
        super.execute(args);

        if (DarkAddonsCommand.hasArgs(args)) {
            final var subcommand = args[0];
            if (null != subcommand) {
                final var subCommand = SubCommand.match(subcommand);
                if (null == subCommand) {
                    DarkAddons.queueWarning("Unknown subcommand. Type " + ModCommand.GENERIC_COMMAND_PREFIX + this.getCommandName() + " help to get a list of subcommands.");
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
