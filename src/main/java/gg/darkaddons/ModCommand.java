package gg.darkaddons;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import com.google.common.collect.ImmutableList;

abstract class ModCommand extends CommandBase {
    @NotNull
    static final String GENERIC_COMMAND_PREFIX = "/";

    @NotNull
    private final String name;

    @NotNull
    private final ImmutableList<String> aliases;

    ModCommand(@NotNull final String name, @NotNull final String... aliases) {
        super();

        this.name = name;
        this.aliases = ImmutableList.copyOf(aliases);
    }

    @Override
    @NotNull
    public final String getCommandName() {
        return this.name;
    }

    @Override
    @NotNull
    public final List<String> getCommandAliases() {
        final var superAliases = super.getCommandAliases();

        if (superAliases.isEmpty()) {
            return this.aliases;
        }

        return ImmutableList.<String>builder()
            .addAll(superAliases)
            .addAll(this.aliases)
            .build();
    }

    @Override
    public final boolean canCommandSenderUseCommand(@Nullable final ICommandSender sender) {
        return super.canCommandSenderUseCommand(sender);
    }

    @Override
    @NotNull
    public final String getCommandUsage(@Nullable final ICommandSender sender) {
        return ModCommand.GENERIC_COMMAND_PREFIX + this.name;
    }

    @Override
    public final int getRequiredPermissionLevel() {
        return super.getRequiredPermissionLevel() - 4;
    }

    @Override
    @Nullable
    public final List<String> addTabCompletionOptions(@Nullable final ICommandSender sender, @Nullable final String[] args, @Nullable final BlockPos pos) {
        final var completions = this.tabComplete(args);

        if (null != completions && !completions.isEmpty()) {
            return completions;
        }

        return super.addTabCompletionOptions(sender, args, pos);
    }

    @Override
    public final void processCommand(@Nullable final ICommandSender sender, @Nullable final String... args) {
        execute(args);
    }

    @Nullable
    List<String> tabComplete(@Nullable String... args) {
        return null;
    }

    void execute(@Nullable String... args) {
        execute();
    }

    void execute() {
        // do nothing by default
    }
}
