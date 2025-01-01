package de.energiequant.xprc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.energiequant.xprc.utils.Maps;
import de.energiequant.xprc.utils.Strings;

public class XPlaneLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(XPlaneLocator.class);

    private static final String USER_HOME = System.getProperty("user.home");
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String FILE_SEPARATOR_QUOTED = Pattern.quote(FILE_SEPARATOR);

    private static final Pattern INSTALL_FILENAME_PATTERN = Pattern.compile("^x-plane_install_(\\d+)\\.txt$", Pattern.CASE_INSENSITIVE);
    private static final int INSTALL_FILENAME_PATTERN_GENERATION = 1;

    private static final Duration TOOL_TERMINATION_TIMEOUT = Duration.ofSeconds(30);

    private static final Map<String, File> toolLocations = Optional.ofNullable(OperatingSystem.detectedSystem)
                                                                   .map(OperatingSystem::findTools)
                                                                   .orElse(Collections.emptyMap());

    private enum OperatingSystem {
        WINDOWS(
            USER_HOME + FILE_SEPARATOR + "Local Settings",
            "C:" + FILE_SEPARATOR + "Windows", // FIXME: could be any drive; check environment variables instead?
            Arrays.asList("wmic", "process", "where", "name='X-Plane.exe'", "get", "ExecutablePath", "/FORMAT:LIST"),
            Pattern.compile("ExecutablePath=(.+)" + FILE_SEPARATOR_QUOTED + "X-Plane\\.exe$", Pattern.CASE_INSENSITIVE),
            false,
            Maps.createUnmodifiableHashMap(
                Maps.entry(
                    "wmic",
                    Collections.singletonList(
                        "${SystemRoot}" + FILE_SEPARATOR + "System32" + FILE_SEPARATOR + "wbem" + FILE_SEPARATOR + "WMIC.exe"
                    )
                )
            ),
            "Windows"
        ),
        MACOS(
            USER_HOME + FILE_SEPARATOR + "Library" + FILE_SEPARATOR + "Preferences",
            FILE_SEPARATOR + "System" + FILE_SEPARATOR + "Library",
            Arrays.asList("ps", "-a", "-x", "-ww", "-o", "command"),
            Pattern.compile("(.+)" + FILE_SEPARATOR_QUOTED + "X-Plane.app" + FILE_SEPARATOR_QUOTED + "Contents" + FILE_SEPARATOR_QUOTED + "Frameworks" + FILE_SEPARATOR_QUOTED + ".*", Pattern.CASE_INSENSITIVE),
            true,
            Maps.createUnmodifiableHashMap(
                Maps.entry(
                    "ps",
                    Collections.singletonList(
                        FILE_SEPARATOR + "bin" + FILE_SEPARATOR + "ps"
                    )
                )
            ),
            "Mac OS X"
        ),
        LINUX(
            USER_HOME + FILE_SEPARATOR + ".x-plane",
            FILE_SEPARATOR + "proc",
            Arrays.asList("ps", "-a", "-x", "-ww", "-o", "command"),
            Pattern.compile("(.+)" + FILE_SEPARATOR_QUOTED + "X-Plane-x86_64$", Pattern.CASE_INSENSITIVE),
            true,
            Maps.createUnmodifiableHashMap(
                Maps.entry(
                    "ps",
                    Arrays.asList(
                        FILE_SEPARATOR + "bin" + FILE_SEPARATOR + "ps",
                        FILE_SEPARATOR + "usr" + FILE_SEPARATOR + "bin" + FILE_SEPARATOR + "ps"
                    )
                )
            ),
            "Linux"
        );

        private final Collection<String> names;
        private final File xplaneUserPreferencesPath;
        private final File systemDetectionDirectory;
        private final List<String> xplaneProcessPathCommand;
        private final Pattern xplaneProcessPathPattern;
        private final boolean caseSensitiveEnvironmentVariables;
        private final Map<String, Collection<String>> preferredToolLocations;

        private static final int XPLANE_PROCESS_PATH_PATTERN_GROUP = 1;

        private static final OperatingSystem detectedSystem = detect();

        OperatingSystem(String xplaneUserPreferencesPath, String systemDetectionDirectory, List<String> xplaneProcessPathCommand, Pattern xplaneProcessPathPattern, boolean caseSensitiveEnvironmentVariables, Map<String, Collection<String>> preferredToolLocations, String... names) {
            this.xplaneUserPreferencesPath = new File(xplaneUserPreferencesPath);
            this.systemDetectionDirectory = new File(systemDetectionDirectory);
            this.xplaneProcessPathCommand = xplaneProcessPathCommand;
            this.xplaneProcessPathPattern = xplaneProcessPathPattern;
            this.preferredToolLocations = preferredToolLocations;
            this.caseSensitiveEnvironmentVariables = caseSensitiveEnvironmentVariables;
            this.names = new HashSet<>(Arrays.asList(names));
        }

        private String resolveEnvironmentVariables(String s, Map<String, String> environmentVariables) {
            return Strings.substituteVariables(s, caseSensitiveEnvironmentVariables, environmentVariables);
        }

        private Map<String, File> findTools() {
            Map<String, File> out = new HashMap<>();

            Map<String, String> environment = System.getenv();

            for (Map.Entry<String, Collection<String>> toolEntry : preferredToolLocations.entrySet()) {
                String toolName = toolEntry.getKey();
                Collection<String> paths = toolEntry.getValue();

                boolean found = false;
                for (String path : paths) {
                    File file = new File(resolveEnvironmentVariables(path, environment));
                    if (file.canExecute()) {
                        LOGGER.debug("Found tool {} at {} => {}", toolName, path, file);
                        out.put(toolName, file);
                        found = true;
                        break;
                    } else {
                        LOGGER.debug("Tool {} does not exist or is not executable at {} => {}", toolName, path, file);
                    }
                }

                if (!found) {
                    LOGGER.warn("Tool {} was not found at any of the following locations: {}", toolName, paths);
                }
            }

            return out;
        }

        private static OperatingSystem detect() {
            String name = System.getProperty("os.name");
            for (OperatingSystem system : values()) {
                if (system.names.contains(name)) {
                    return system;
                }
            }

            LOGGER.warn("Operating system name \"{}\" is unknown, falling back to identification by X-Plane user directory...", name);
            for (OperatingSystem system : values()) {
                if (system.xplaneUserPreferencesPath.isDirectory()) {
                    return system;
                }
            }

            LOGGER.warn("X-Plane user preferences directory was not found, falling back to identification by system directories...");
            for (OperatingSystem system : values()) {
                if (system.systemDetectionDirectory.isDirectory()) {
                    return system;
                }
            }

            LOGGER.warn("All methods for identifying current operating system have failed!");

            return null;
        }
    }

    private static List<String> runTool(List<String> toolNameAndParameters) {
        return runTool(
            toolNameAndParameters.iterator().next(),
            toolNameAndParameters.subList(1, toolNameAndParameters.size())
        );
    }

    private static List<String> runTool(String toolName, List<String> parameters) {
        File toolLocation = toolLocations.get(toolName);
        if (toolLocation == null) {
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }

        List<String> command = new ArrayList<>();
        command.add(toolLocation.getAbsolutePath());
        command.addAll(parameters);

        LOGGER.debug("Running command: {}", command);

        Process process;
        try {
            process = new ProcessBuilder(command).start();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to spawn command: " + String.join(" ", command), ex);
        }

        List<String> lines = new ArrayList<>();
        try (
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isr);
        ) {
            String line = br.readLine();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to read tool output: " + String.join(" ", command), ex);
        }

        try {
            long seconds = TOOL_TERMINATION_TIMEOUT.getSeconds();
            if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                LOGGER.warn("Command timed out after {} seconds: {}", seconds, command);
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException("interrupted during command termination", ex);
        }

        return lines;
    }

    public static Collection<XPlaneInstance> findRunningInstances() {
        if (OperatingSystem.detectedSystem == null) {
            throw new UnknownOperatingSystem();
        }

        Set<File> directories = new HashSet<>();
        for (String line : runTool(OperatingSystem.detectedSystem.xplaneProcessPathCommand)) {
            Matcher matcher = OperatingSystem.detectedSystem.xplaneProcessPathPattern.matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            File directory = new File(matcher.group(OperatingSystem.XPLANE_PROCESS_PATH_PATTERN_GROUP));
            LOGGER.debug("Found running X-Plane process at {}: {}", directory, line);
            directories.add(directory);
        }

        Collection<XPlaneInstance> out = new ArrayList<>();
        for (File directory : directories) {
            Optional<Integer> generation = XPlaneInstance.identifyGeneration(directory);
            if (!generation.isPresent()) {
                LOGGER.warn("Unable to identify version/generation of X-Plane installation at {}", directory);
                continue;
            }

            LOGGER.debug("Identified X-Plane installation at {}: {}", directory, generation);
            out.add(new XPlaneInstance(directory, generation.get()));
        }

        return out;
    }

    public static Collection<XPlaneInstance> findInstallations() {
        Collection<XPlaneInstance> out = new ArrayList<>();

        if (OperatingSystem.detectedSystem == null) {
            return out;
        }

        File[] installFiles = OperatingSystem.detectedSystem.xplaneUserPreferencesPath.listFiles((dir, name) -> INSTALL_FILENAME_PATTERN.matcher(name).matches());
        if (installFiles == null) {
            return out;
        }

        for (File installFile : installFiles) {
            Matcher matcher = INSTALL_FILENAME_PATTERN.matcher(installFile.getName());
            if (!matcher.matches()) {
                continue;
            }
            int generation = Integer.parseUnsignedInt(matcher.group(INSTALL_FILENAME_PATTERN_GENERATION));

            try {
                for (String line : Files.readAllLines(installFile.toPath())) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    File xpDirectory = new File(line);
                    if (!xpDirectory.isDirectory()) {
                        LOGGER.warn("{} lists inaccessible installation in {}", installFile, xpDirectory);
                        continue;
                    }

                    LOGGER.debug("Found X-Plane {} in {}", generation, xpDirectory);

                    out.add(new XPlaneInstance(xpDirectory, generation));
                }
            } catch (IOException ex) {
                LOGGER.warn("Failed to read {}", installFile, ex);
            }
        }

        return out;
    }

    private static Collection<XPlaneInstance> filterOnlyXPRCSortedRecentLog(Collection<XPlaneInstance> instances) {
        return instances.stream()
                        .filter(XPlaneInstance::hasXPRC)
                        .sorted(Comparator.comparing((XPlaneInstance x) -> x.getLogTimestamp().orElse(Instant.EPOCH)).reversed())
                        .collect(Collectors.toList());
    }

    public static Optional<XPlaneInstance> findMostRecentInstanceWithXPRC() {
        Collection<XPlaneInstance> candidates = filterOnlyXPRCSortedRecentLog(XPlaneLocator.findRunningInstances());
        if (!candidates.isEmpty()) {
            LOGGER.debug("findMostRecentInstance: Found running X-Plane instances with XPRC: {}", candidates);
            return Optional.of(candidates.iterator().next());
        }

        candidates = filterOnlyXPRCSortedRecentLog(XPlaneLocator.findInstallations());
        if (!candidates.isEmpty()) {
            LOGGER.debug("findMostRecentInstance: Found X-Plane installations with XPRC: {}", candidates);
            return Optional.of(candidates.iterator().next());
        }

        return Optional.empty();
    }

    private static class UnknownOperatingSystem extends RuntimeException {
        UnknownOperatingSystem() {
            super("The operating system could not be identified.");
        }
    }
}
