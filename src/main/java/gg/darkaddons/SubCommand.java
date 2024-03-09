package gg.darkaddons;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import gg.darkaddons.profiler.Profiler;
import gg.darkaddons.profiler.ProfilerResult;
import gg.darkaddons.profiler.impl.ProfilerImpl;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.Locale;

final class SubCommand {
    private static final int COLLECTIONS_SUB_COMMAND_COUNT_HINT = 16;
    @SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened"})
    @NotNull
    private static final LinkedHashSet<SubCommand> subCommandsRegistered = new LinkedHashSet<>(Utils.calculateHashMapCapacity(SubCommand.COLLECTIONS_SUB_COMMAND_COUNT_HINT));
    @NotNull
    private static final ArrayList<String> subCommandNames = new ArrayList<>(SubCommand.COLLECTIONS_SUB_COMMAND_COUNT_HINT);
    @NotNull
    private static final ArrayList<String> subCommandAliases = new ArrayList<>(SubCommand.COLLECTIONS_SUB_COMMAND_COUNT_HINT);
    @NotNull
    private static final HashMap<String, SubCommand> subCommands = new HashMap<>(Utils.calculateHashMapCapacity(SubCommand.COLLECTIONS_SUB_COMMAND_COUNT_HINT));

    @NotNull
    private static final HashMap<Class<?>, SubCommand.Adapter<?>> adapters = new HashMap<>(Utils.calculateHashMapCapacity(3));

    private static final class Adapter<R> implements Function<String, R> {
        @NotNull
        private static final <R> SubCommand.Adapter<R> newAdapter(@NotNull final Function<String, ? extends R> adapterFunc, @NotNull final Function<String, String[]> tabCompleterFunc) {
            return new SubCommand.Adapter<>(adapterFunc, tabCompleterFunc);
        }

        @NotNull
        private final Function<String, ? extends R> adapterFunction;

        @NotNull
        private final Function<String, String[]> tabCompleterFunction;

        private Adapter(@SuppressWarnings("BoundedWildcard") @NotNull final Function<String, ? extends R> adapterFunc,
                        @SuppressWarnings("BoundedWildcard") @NotNull final Function<String, String[]> tabCompleterFunc) {
            super();

            this.adapterFunction = adapterFunc;
            this.tabCompleterFunction = tabCompleterFunc;
        }

        @Override
        @NotNull
        public final R apply(@SuppressWarnings("NullableProblems") @NotNull final String value) {
            return this.adapterFunction.apply(value);
        }

        @NotNull
        private final String[] tabComplete(@NotNull final String value) {
            return this.tabCompleterFunction.apply(value);
        }

        @Override
        public final String toString() {
            return "Adapter{" +
                "adapterFunction=" + this.adapterFunction +
                ", tabCompleterFunction=" + this.tabCompleterFunction +
                '}';
        }
    }

    @NotNull
    private static final String PLEASE_ENTER_A_VALID_NUMBER = "Please enter a valid number.";

    private static final void registerAdapters() {
        SubCommand.adapters.put(boolean.class, SubCommand.Adapter.newAdapter((@NotNull final String arg) -> switch (arg) {
            case "true" -> true;
            case "false" -> false;
            default -> {
                DarkAddons.queueWarning("Please enter true or false.");
                throw SubCommand.StopCodeflowException.INSTANCE;
            }
        }, (@NotNull final String arg) -> SubCommand.tabComplete(arg, "true", "false")));
        SubCommand.adapters.put(long.class, SubCommand.Adapter.newAdapter((@NotNull final String arg) -> {
            try {
                final var value = Long.parseUnsignedLong(arg);
                //noinspection IfCanBeAssertion
                if (0L == value) {
                    //noinspection ThrowCaughtLocally
                    throw new NumberFormatException("zero is considered negative in sub command arguments");
                }

                return value;
            } catch (final NumberFormatException nfe) {
                DarkAddons.queueWarning(SubCommand.PLEASE_ENTER_A_VALID_NUMBER);

                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw SubCommand.StopCodeflowException.INSTANCE;
            }
        }, (@NotNull final String arg) -> SubCommand.tabComplete(arg, "50", "100", "200", "500", "1000", "3000", "5000", "10000")));
        SubCommand.adapters.put(String.class, SubCommand.Adapter.newAdapter(Function.identity(), (@NotNull final String arg) -> new String[]{arg}));
    }

    @Nullable
    private static Profiler profiler;

    private static final void registerSimple() {
        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> Diagnostics.dumpDiagnosticData(DarkAddons::queueWarning), "diag", "diagnostic", "diagnostics"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> Diagnostics.dumpEntities(DarkAddons::queueWarning), "dumpentities"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> Diagnostics.dumpPlayerEntities(DarkAddons::queueWarning), "dumpplayerentities"));
    }

    private static final void registerSubCommands() {
        SubCommand.registerSimple();

        SubCommand.registerDump();

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> Diagnostics.dumpLastGameLoopTime(DarkAddons::queueWarning), "gamelooptime"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            if (Diagnostics.isGCTrackerRegistered()) {
                DarkAddons.queueWarning("GC tracking is already enabled.");
                return;
            }
            final var detailed = self.hasArgument(1) && self.<Boolean>getArgument(1);

            Diagnostics.registerGCTracker(detailed);
            DarkAddons.queueWarning("Started tracking GC" + (detailed ? " [detailed]" : ""));
        }, "trackgc", new Class<?>[]{boolean.class}, "<detailed: true/false>"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            if (!Diagnostics.isGCTrackerRegistered()) {
                DarkAddons.queueWarning("GC tracking is already stopped or never started. Please start it first.");
                return;
            }
            Diagnostics.unregisterGCTracker();
            DarkAddons.queueWarning("Stopped tracking GC");
        }, "untrackgc"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            if (self.hasArgument(1)) {
                if (Diagnostics.isLagTracking()) {
                    DarkAddons.queueWarning("Already tracking lag, please stop it first before starting again.");
                    return;
                }
                Diagnostics.enableLagTracking(self.getArgument(1));
                DarkAddons.queueWarning("Started tracking lag");
            } else {
                DarkAddons.queueWarning("Wrong usage, please provide the number of milliseconds for the lag threshold.");
            }
        }, "tracklag", new Class<?>[]{long.class}, "<lag threshold in ms: number>"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            if (!Diagnostics.isLagTracking()) {
                DarkAddons.queueWarning("Lag tracking is already stopped or never started. Please start it first.");
                return;
            }
            Diagnostics.disableLagTracking();
            DarkAddons.queueWarning("Stopped tracking lag");
        }, "untracklag"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            if (self.hasArgument(1)) {
                final long ms = self.getArgument(1);

                //noinspection StringConcatenationMissingWhitespace
                DarkAddons.queueWarning("Simulating lag for " + ms + "ms...");
                DarkAddons.registerTickTask("darkaddons_simulate_lag", 1, false, () -> {
                    try {
                        Thread.sleep(ms);
                    } catch (final InterruptedException ie) {
                        DarkAddons.handleInterruptedException(ie);
                    }
                });
            } else {
                DarkAddons.queueWarning("Wrong usage, please provide the number of milliseconds to simulate lag duration.");
            }
        }, "simulatelag", new Class<?>[]{long.class}, "<ms to freeze the game for: number>"));

        SubCommand.registerProfilerCommands();

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            final var startTime = System.currentTimeMillis();
            var mode = RunsTillCA50.Mode.OPTIMAL;
            if (self.hasArgument(4)) {
                final String modeToParse = self.getArgument(4);
                final var parsed = RunsTillCA50.Mode.parseMode(modeToParse);
                if (null == parsed) {
                    DarkAddons.queueWarning("Unknown mode \"" + modeToParse + "\". Defaulting to \"optimal\".");
                } else {
                    mode = parsed;
                }
            }
            if (self.hasArgument(1) && !self.<String>getArgument(1).equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.getName())) {
                RunsTillCA50.calculate(self.getArgument(1), !(self.hasArgument(2) && self.<Boolean>getArgument(2)), () -> {
                    DarkAddons.echoEmpty();
                    DarkAddons.queueWarning("Took " + (System.currentTimeMillis() - startTime) + "ms.");
                }, self.hasArgument(3) && self.<Boolean>getArgument(3), mode);
            } else {
                RunsTillCA50.calculateUUID(Minecraft.getMinecraft().thePlayer.getUniqueID(), !(self.hasArgument(2) && self.<Boolean>getArgument(2)), () -> {
                    DarkAddons.echoEmpty();
                    DarkAddons.queueWarning("Took " + (System.currentTimeMillis() - startTime) + "ms.");
                }, self.hasArgument(3) && self.<Boolean>getArgument(3), mode);

                ClassAverage50Display.syncClassXP();
            }
        }, "rtca", new Class<?>[]{String.class, boolean.class, boolean.class, String.class}, "<player name (default: self)> <m6: true/false (default: false)> <derpy: true/false (default: false)> <mode: optimal/early/skull (default: optimal)>"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> UpdateChecker.checkInBackground((@NotNull final UpdateChecker.UpdateCheckerResult result) -> {
            if (UpdateChecker.UpdateCheckerResult.UP_TO_DATE == result) {
                DarkAddons.queueWarning("You already have the latest version of " + DarkAddons.MOD_NAME + ". How good!");
            }
        }), "checkupdates"));

        SubCommand.registerOthers();

        SubCommand.registerHelp();
    }

    private static final void processThreadDatas(@NotNull final long runtime, @NotNull final ArrayList<Diagnostics.ThreadData> threadDatas) {
        var totalBytes = 0L;

        for (final var data : threadDatas) {
            totalBytes += data.getMem();
        }

        final var builder = new StringBuilder(100);

        for (final var data : threadDatas) {
            final var cpu = data.getCpu();
            final var cpuPerc = Math.round(cpu * 100.0D / TimeUnit.MILLISECONDS.toNanos(runtime));

            final var mem = data.getMem();
            final var mbs = data.getMem() / TimeUnit.MILLISECONDS.toSeconds(runtime);

            final var memPerc = Math.round(mem * 100.0D / Math.max(1, totalBytes));

            builder.append(data.getName()).append(" - Priority: ").append(data.getPriority()).append(", Total CPU Time Spent: ").append(Utils.formatTime(TimeUnit.NANOSECONDS.toMillis(cpu), true)).append(" (").append(cpuPerc).append("%), Total Memory Allocated: ").append(Utils.bytesToString(mem)).append(" (").append(Utils.bytesToString(mbs)).append("/s) (").append(memPerc).append("%)").append(data.isDeadlocked() ? " DEADLOCKED" : "").append('\n');
        }

        final var response = SubCommand.generateResponse(builder, runtime);
        SubCommand.startHttpServer(response);
    }

    @SuppressWarnings("fallthrough")
    private static final void registerOthers() {
        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            // Need to do argument logic in the main thread
            final String sortBy = self.hasArgument(1) ? self.getArgument(1) : "prio";
            final var group = self.hasArgument(2) && self.<Boolean>getArgument(2);

            // From here runs on diagnostics thread
            Diagnostics.generateThreadDatas(group, (@NotNull final ArrayList<Diagnostics.ThreadData> threadDatas) -> {
                switch (sortBy) {
                    case "mem":
                        threadDatas.sort(Comparator.comparingLong(Diagnostics.ThreadData::getMem).reversed());
                        break;
                    case "cpu":
                        threadDatas.sort(Comparator.comparingLong(Diagnostics.ThreadData::getCpu).reversed());
                        break;
                    //noinspection DefaultNotLastCaseInSwitch
                    default:
                        DarkAddons.queueWarning("Unknown sorting option, using the default of \"prio\"!");
                    case "prio":
                        threadDatas.sort(Comparator.comparingInt(Diagnostics.ThreadData::getPriority).reversed());
                        break;
                }

                SubCommand.processThreadDatas(Diagnostics.getJVMUptime(), threadDatas);
                DarkAddons.queueWarning("Thread list generated. Open http://localhost:8000/ in your browser for results.");
            });
        }, "listthreads", new Class<?>[]{String.class, boolean.class}, "<sort by prio/mem/cpu (default: prio)> <group (default: false) true/false>"));
    }

    private static final void registerDump() {
        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            DarkAddons.queueWarning("Dumping client thread to logs...");
            Diagnostics.dumpThread(Thread.currentThread(), Utils::printErr);
        }, "dumpthread"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            DarkAddons.queueWarning("Dumping all threads to logs...");
            Diagnostics.dumpAllThreads(Utils::printErr);
        }, "dumpthreads"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            final var live = !self.hasArgument(1) || self.<Boolean>getArgument(1);

            DarkAddons.queueWarning("Dumping " + (live ? "live-objects only" : "full") + " heap to heapdumps folder in game directory... (Note: This might freeze your game for a while as essentially all the RAM used is written to the disk. You also need enough free space in the disk.)");
            Diagnostics.dumpHeap(live, () -> DarkAddons.queueWarning("Dump successful. You can inspect the created file with VisualVM (free & from Java developers itself, included in JDK), Eclipse Memory Analyzer/MAT (kinda buggy) or YourKit (paid; has trial)."));
        }, "dumpheap", new Class<?>[]{boolean.class}, "<live objects only: true/false>"));

        /*SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            if (self.hasArgument(1)) {
                final String className = self.getArgument(1);
                final Class<?> clazz;

                try {
                    clazz = Class.forName(ASMUtils.remapToInternalClassName(className));
                } catch (final ClassNotFoundException classNotFoundException) {
                    DarkAddons.queueWarning("Couldn't find a class with the given fully qualified name. Please check.");
                    return;
                }

                final byte[] contents = ASMUtils.writeClass(ASMUtils.readClass(clazz));
                final File file = new File(new File(new File("config", "darkaddons"), "classdumps"), clazz.getSimpleName() + ".class");

                file.mkdirs();

                try {
                    file.createNewFile();

                    Files.write(file.toPath(), contents);
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }

                DarkAddons.queueWarning("Operation finished. Open .minecraft/config/darkaddons/classdumps folder to see the class file.");
            } else {
                DarkAddons.queueWarning("Please enter the class to be dumped.");
            }
        }, "dumpclass", new Class<?>[]{String.class}, "<fully qualified class name>"));*/
    }

    private static final void registerProfilerCommands() {
        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            if (self.hasArgument(1)) {
                if (null == SubCommand.profiler) {
                    SubCommand.profiler = Profiler.newSamplingProfiler(self.getArgument(1));

                    ProfilerImpl.setLineNumbers(!self.hasArgument(2) || self.<Boolean>getArgument(2));
                    if (self.hasArgument(3)) {
                        final String filter = self.getArgument(3);
                        if ("default".equals(filter)) {
                            ProfilerImpl.setThreadFilter("Client thread");
                        } else if (!"nofilter".equals(filter)) {
                            ProfilerImpl.setThreadFilter(filter);
                        }
                    } else {
                        ProfilerImpl.setThreadFilter("Client thread");
                    }

                    ProfilerImpl.setCPUBoundOnly(!self.hasArgument(4) || self.<Boolean>getArgument(4));
                    ProfilerImpl.setUniquifyByThread(!self.hasArgument(5) || self.<Boolean>getArgument(5));
                    ProfilerImpl.setUniquifyByStack(!self.hasArgument(6) || self.<Boolean>getArgument(6));
                    ProfilerImpl.setUniquifyByThreadId(!self.hasArgument(7) || self.<Boolean>getArgument(7));

                    SubCommand.profiler.startProfiling();
                    DarkAddons.queueWarning("Started profiling successfully. Type /darkaddons stopprofiling to get results at any time.");
                } else {
                    DarkAddons.queueWarning("Profiler already started, please stop it first before starting another one.");
                }
            } else {
                DarkAddons.queueWarning("Wrong usage, please provide the sampling interval to use for the profiler. Some example good/common sampling intervals are: 1 (most accuracy but might cause extra overhead), 16 (60 FPS is 16.6ms per frame), 50 (20 TPS is 50ms per tick), 100 (value used by default on most Java sampling profilers), 200 (Java GCPauseIntervalMillis default value)");
            }
        }, "startprofiling", new Class<?>[]{long.class, boolean.class, String.class, boolean.class, boolean.class, boolean.class, boolean.class}, "<sampling interval: number>, <include line numbers: true/false (default: true)> <filter: filter/nofilter (default: Client thread)> <filter to CPU bound: true/false (default: true)> <uniquify by thread: true/false (default: true)> <uniquify by stack: true/false (default: true)> <uniquify by thread id: true/false (default: true)>"));

        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            if (null == SubCommand.profiler) {
                DarkAddons.queueWarning("Profiler not started yet, or already stopped.");
            } else {
                SubCommand.profiler.endProfiling(); // TODO add separate command to get results without stopping profiling

                var results = SubCommand.profiler.pollResults().sort().remap();

                try {
                    results = results.decompile();
                } catch (final UnsupportedOperationException unsupportedOperationException) {
                    DarkAddons.queueWarning("Decompiling statements is not supported currently. Skipping...");
                }

                if (self.hasArgument(1) && self.<Boolean>getArgument(1)) {
                    results = results.filter("darkaddons");
                }

                final double runtime = ((ProfilerImpl) SubCommand.profiler).getTotalRuntime();
                final var builder = new StringBuilder(100);

                for (final var result : results.results()) {
                    SubCommand.parseResult(result, runtime, builder);
                }

                SubCommand.profiler = null;

                ProfilerImpl.setLineNumbers(true);
                ProfilerImpl.setThreadFilter(null);

                final var response = SubCommand.generateResponse(builder, runtime);
                SubCommand.startHttpServer(response);

                DarkAddons.queueWarning("Stopped profiler successfully. Open http://localhost:8000/ in your browser for results.");
            }
        }, "stopprofiling", new Class<?>[]{boolean.class}, "<darkaddons only: true/false (default: false)>"));
    }

    private static final void startHttpServer(@SuppressWarnings("TypeMayBeWeakened") @NotNull final StringBuilder response) {
        final HttpServer server;

        try {
            server = HttpServer.create(new InetSocketAddress(8_000), 1);
        } catch (final IOException e) {
            DarkAddons.modError(e);

            throw SubCommand.StopCodeflowException.INSTANCE;
        }

        Objects.requireNonNull(server).createContext("/", (@NotNull final HttpExchange exchange) -> {
            final var responseBytes = response.toString().getBytes(StandardCharsets.UTF_8);

            Objects.requireNonNull(exchange.getResponseHeaders()).add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (final var os = exchange.getResponseBody()) {
                Objects.requireNonNull(os).write(responseBytes);
            }

            server.stop(0);
        });

        server.setExecutor(null); // TODO set thread name
        server.start();
    }

    @NotNull
    private static final StringBuilder generateResponse(@SuppressWarnings("TypeMayBeWeakened") @NotNull final StringBuilder builder, final double runtime) {
        final var response = new StringBuilder(builder.length());

        final var favicon = "<link href=\"data:image/x-icon;base64,AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAED//wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABA//8AAAAAAED//wBA//8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAVy8C/wBA//8AQP//AED//wBA//8AAAAAAED//wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD83Lj//Ny4//zcuP8AQP//AED//yPF//8AQP//AED//wBP/wYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD83Lj//Ny4//zcuP+hVQD/I8X//yPF//8jxf//I8X//wBL/yUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/Ny4/1cvAv/869n//OvZ/1cvAv9XLwL/I8X//yPF//8AQP//AED//wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFcvAv/869n//OvZ/1cvAv/869n/Vy8C/yPF//8AQP//AB6ABgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFcvAv/869n//OvZ/6FVAP9XLwL/Vy8C//zr2f9XLwL/Vy8C/1cvAv8AAAAAAAAAAAAAAAAAAAAAAAAAAFcvAv/869n//OvZ//zr2f/83Lj/Vy8C//zr2f/869n/oVUA//zcuP9XLwL/AAAAAAAAAAAAAAAAAAAAAFcvAv/869n//OvZ//zr2f/869n//OvZ//zr2f/869n//OvZ//zcuP/83Lj/AAAAAAAAAAAAAAAAAAAAAAIvuP/869n//OvZ////1P///67//OvZ//zr2f/869n//OvZ/1cvAv/83Lj//Ny4/wAAAAAAAAAAAAAAAAAAAAAFQ/z//OvZ//zr2f///7D///+u//zr2f/869n//OvZ/6FVAP8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABUP8/wQ62//869n//OvZ//zr2f/869n//OvZ/wAAAHEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAVD/P8FQ/z/ADC///zr2f/869n/oVUA/wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQP//AED//wQ62/8CL7j/Ai+4/wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA//sAAP/TAAD/BQAA/AMAAPgHAAD4AQAA+AcAAPADAADgAwAAwAcAAIAHAACAPwAAgP8AAIH/AACD/wAA//8AAA==\" rel=\"icon\" type=\"image/x-icon\">";
        response.append("<!doctype html><html lang=\"en\"><head><style>details summary {cursor: pointer;}details summary > * {display: inline;}.text {white-space: pre-wrap;}</style><title>Profiler Results</title>").append(favicon).append("</head><body><h4>Note: Do not refresh the page. The web server will be closed to not create memory leaks after this page is served to you.</h4><p>Total Runtime: ").append(Utils.formatTime((long) runtime, true)).append("</p>");

        final var summaryTemplate = "<details class=\"text\"><summary>%summary%</summary>";
        String currentSummary = null;

        for (var line : builder.toString().split("\n")) {
            line = StringUtils.replace(StringUtils.replace(line, ">", "&gt;"), "<", "&lt;"); // Otherwise <init> and <clinit> in stack traces will be parsed as HTML tags

            line = StringUtils.replace(line, ":-2)", ":native)");
            line = StringUtils.replace(line, ":-1)", ":unknown)");

            if (Objects.requireNonNull(line).contains("\t")) {
                response.append(StringUtils.remove(line, '\t')).append("<br>");
            } else {
                if (null != currentSummary) {
                    response.append("</details>");
                }

                currentSummary = line;
                response.append(StringUtils.replace(summaryTemplate, "%summary%", line));
            }
        }

        response.append("</details></body></html>");
        return response;
    }

    private static final void parseResult(@NotNull final ProfilerResult result, final double runtime, @NotNull final StringBuilder builder) {
        final double timeSpent = Objects.requireNonNull(result).timeSpent();
        final var percent = 100.0D - (runtime - timeSpent) / runtime * 100.0D;

        builder.append(result.fullName()).append(" - ").append(Utils.formatTime(result.timeSpent(), true)).append(", %").append(String.format(Locale.ROOT, "%f", percent)).append(" of total in ").append(result.threadName()).append('\n');

        final var stack = result.fullStack();
        final var len = stack.length;

        final var indent = '\t';

        for (var i = 1; i < len; ++i) { // Skip the first one as its already printed above
            final var elem = stack[i];

            final var className = ProfilerImpl.getClassName(elem);

            final var fileName = elem.getFileName();
            final var sourceFile = null == fileName || "SourceFile".equals(fileName) ? className + ".java" : fileName;

            builder.append(indent).append(ProfilerImpl.getPackageName(elem)).append('.').append(className).append('.').append(elem.getMethodName()).append('(').append(sourceFile).append(':').append(elem.getLineNumber()).append(')').append('\n');
        }
    }

    private static final void registerHelp() {
        SubCommand.register(new SubCommand((@NotNull final SubCommand self) -> {
            DarkAddons.queueWarning("Available subcommands:");
            for (final var subCommand : SubCommand.subCommandsRegistered) {
                DarkAddons.queueWarning(subCommand.getHelpText());
            }
        }, "help"));
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void registerAll() {
        SubCommand.registerAdapters();
        SubCommand.registerSubCommands();
    }

    private static final void register(@NotNull final SubCommand subCommand) {
        final var localName = subCommand.name;

        SubCommand.subCommandsRegistered.add(subCommand);
        SubCommand.subCommandNames.add(localName);

        SubCommand.subCommands.put(localName, subCommand);
        for (final var alias : subCommand.aliases) {
            SubCommand.subCommands.put(alias, subCommand);
            SubCommand.subCommandAliases.add(alias);
        }
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    @Nullable
    static final SubCommand match(@NotNull final String nameOrAlias) {
        return SubCommand.subCommands.get(nameOrAlias);
    }

    @NotNull
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final String[] tabComplete(@NotNull final String typedSoFar, @NotNull final String... candidates) {
        return SubCommand.tabComplete(SubCommand.EMPTY_STRING_ARRAY, typedSoFar, candidates);
    }

    private static final String[] tabComplete(@NotNull final String[] completions, @NotNull final String typedSoFar, @NotNull final String... candidates) {
        final var comps = Utils.asArrayList(completions);

        SubCommand.tabComplete(comps, typedSoFar, candidates);
        return comps.toArray(SubCommand.EMPTY_STRING_ARRAY);
    }

    private static final void tabComplete(@NotNull final ArrayList<String> completions, @NotNull final String typedSoFar, @NotNull final String... candidates) {
        SubCommand.tabComplete(completions, typedSoFar, candidates, SubCommand.EMPTY_STRING_ARRAY);
    }

    private static final void tabComplete(@SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened", "BoundedWildcard"}) @NotNull final ArrayList<String> completions, @NotNull final String typedSoFar, @NotNull final String[] candidates, @NotNull final String... alternativeCandidates) {
        if (typedSoFar.isEmpty()) {
            Collections.addAll(completions, candidates);
            return;
        }

        for (final var candidate : candidates) {
            if (candidate.startsWith(typedSoFar)) {
                completions.add(candidate);
                return;
            }
        }

        for (final var alternativeCandidate : alternativeCandidates) {
            if (alternativeCandidate.startsWith(typedSoFar)) {
                completions.add(alternativeCandidate);
                return;
            }
        }
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    @NotNull
    static final ArrayList<String> tabCompleteSubCommand(@NotNull final String typedSoFar) {
        final var tabCompletions = new ArrayList<String>(SubCommand.subCommandNames.size());

        SubCommand.tabComplete(tabCompletions, typedSoFar, SubCommand.subCommandNames.toArray(SubCommand.EMPTY_STRING_ARRAY), SubCommand.subCommandAliases.toArray(SubCommand.EMPTY_STRING_ARRAY));
        return tabCompletions;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    @Nullable
    static final ArrayList<String> tabCompleteArguments(@NotNull final SubCommand subCommand, @NotNull final String... arguments) {
        final var len = arguments.length;
        var start = 1; // Skip arguments[0] since it's the sub command name

        while (start != len - 1) {
            ++start; // Skip already filled arguments
        }

        for (var i = start; i < len; ++i) {
            subCommand.setArgs(arguments);
            if (subCommand.hasArgument(i)) { // Checks if index is not out of bounds plus if the command has a non-void argument at that index
                final var tabCompletionsArray = subCommand.getAdapter(i).tabComplete(arguments[i]);
                subCommand.resetArgs();

                return Utils.asArrayList(tabCompletionsArray);
            }
            subCommand.resetArgs();
        }

        return null;
    }

    @NotNull
    private final String name;

    @SuppressWarnings("FieldHasSetterButNoGetter")
    @Nullable
    private String[] args;

    @NotNull
    private final Class<?>[] argumentTypes;

    @NotNull
    private final String argsDesc;

    @NotNull
    private final String[] aliases;

    @NotNull
    private final Consumer<SubCommand> executeFunction;

    private SubCommand(@NotNull final Consumer<SubCommand> onExecute, @NotNull final String subCommandName, @NotNull final String... subCmdAliases) {
        this(onExecute, subCommandName, new Class<?>[]{void.class}, "", subCmdAliases);
    }

    private SubCommand(@NotNull final Consumer<SubCommand> onExecute, @NotNull final String subCommandName, @NotNull final Class<?>[] subCommandArgumentTypes, @NotNull final String argsDescription, @NotNull final String... subCmdAliases) {
        super();

        this.name = subCommandName;
        this.argumentTypes = subCommandArgumentTypes;
        for (final var argumentType : this.argumentTypes) {
            //noinspection IfCanBeAssertion,ObjectEquality
            if (void.class != argumentType && !SubCommand.adapters.containsKey(argumentType)) {
                throw new IllegalArgumentException("adapter not found for " + argumentType.getSimpleName());
            }
        }

        this.aliases = subCmdAliases;
        this.executeFunction = onExecute;

        this.argsDesc = argsDescription;
    }

    final void setArgs(@Nullable final String... newArgs) {
        // noinspection IfCanBeAssertion
        if (null != newArgs && null != this.args) {
            throw new IllegalStateException("resetArgs() not called");
        }
        this.args = newArgs;
    }

    final void resetArgs() {
        this.setArgs((String[]) null);
    }

    @NotNull
    private static final String[] sanityCheckArgs(@Nullable final String... arguments) {
        //noinspection IfCanBeAssertion
        if (null == arguments) {
            throw new IllegalStateException("setArgs not called");
        }

        return arguments;
    }

    final void execute() {
        try {
            this.executeFunction.accept(this);
        } catch (final SubCommand.StopCodeflowException ignored) {
            // ignored
        }
    }

    private final boolean hasArgument(final int index) {
        //noinspection IfCanBeAssertion
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            throw new IllegalStateException("hasArgument() called from unexpected thread \"" + Thread.currentThread().getName() + '"');
        }

        //noinspection IfCanBeAssertion
        if (0 > index) {
            throw new IllegalArgumentException("index must not be negative");
        }

        //noinspection IfCanBeAssertion
        if (0 == index) {
            throw new IllegalArgumentException("subcommand argument indexes start at 1, not 0");
        }

        if (this.argumentTypes.length < index) {
            return false;
        }

        final var type = this.argumentTypes[index - 1];

        //noinspection ObjectEquality
        if (void.class == type) {
            return false;
        }

        final var arguments = SubCommand.sanityCheckArgs(this.args);
        return arguments.length > index && null != arguments[index]; // the first argument is the sub-command name so no - 1's here
    }

    @Nullable
    private final SubCommand.Adapter<?> getAdapter(final int index) {
        return SubCommand.adapters.get(this.argumentTypes[index - 1]);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    @NotNull
    private final <T extends Serializable> T getArgument(final int index, final T... doNotPassThisParameter) {
        //noinspection IfCanBeAssertion
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            throw new IllegalStateException("getArgument() called from unexpected thread \"" + Thread.currentThread().getName() + '"');
        }

        //noinspection IfCanBeAssertion
        if (null == doNotPassThisParameter || 0 != doNotPassThisParameter.length) {
            throw new IllegalArgumentException("second parameter must not be manually passed");
        }

        //noinspection IfCanBeAssertion
        if (!this.hasArgument(index)) {
            throw new IllegalStateException("no argument at index " + index);
        }

        final var arguments = SubCommand.sanityCheckArgs(this.args);
        final var adapter = this.getAdapter(index);

        //noinspection IfCanBeAssertion
        if (null == adapter) {
            throw new IllegalStateException("adapter gone missing after construction");
        }

        final var value = adapter.apply(arguments[index]);

        final var actualType = value.getClass();
        final var inferredType = doNotPassThisParameter.getClass().getComponentType();

        //noinspection IfCanBeAssertion,ObjectEquality
        if (inferredType != actualType) {
            throw new IllegalStateException("type inference failed or supplied argument types were wrong, expected <" + actualType.getSimpleName() + "> but inferred type is <" + inferredType.getSimpleName() + '>');
        }

        return (T) value;
    }

    @NotNull
    private final String getHelpText() {
        return DarkAddonsCommand.COMMAND_PREFIX + DarkAddonsCommand.MAIN_COMMAND_NAME + ' ' + this.name + (this.argsDesc.isEmpty() ? "" : ' ' + this.argsDesc);
    }

    @Override
    public final String toString() {
        return "SubCommand{" +
            "name='" + this.name + '\'' +
            ", args=" + Arrays.toString(this.args) +
            ", argumentTypes=" + Arrays.toString(this.argumentTypes) +
            ", argsDesc='" + this.argsDesc + '\'' +
            ", aliases=" + Arrays.toString(this.aliases) +
            ", executeFunction=" + this.executeFunction +
            '}';
    }

    /**
     * An exception just to stop code execution flow. Has no stack trace and minimal performance overhead.
     */
    @SuppressWarnings("Singleton")
    private static final class StopCodeflowException extends RuntimeException {
        @NotNull
        private static final SubCommand.StopCodeflowException INSTANCE = new SubCommand.StopCodeflowException();

        private static final long serialVersionUID = 0L;

        private StopCodeflowException() {
            super(null, null, true, false);
        }

        @SuppressWarnings({"MethodDoesntCallSuperMethod", "NonSynchronizedMethodOverridesSynchronizedMethod"})
        @NotNull
        @Override
        public final Throwable fillInStackTrace() {
            return this; // Do nothing for increasing performance
        }
    }
}
