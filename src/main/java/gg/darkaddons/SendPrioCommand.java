package gg.darkaddons;

import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
final class SendPrioCommand extends ModCommand {
    SendPrioCommand() {
        super("sendprio");
    }

    @Override
    final void execute() {
        super.execute();

        DarkAddons.queueUserSentMessageOrCommand("/pc DP Settings: Power: 0 | Easy: 0 | Healer: 1 : 1 | Tank: 0 : 0");
    }
}
