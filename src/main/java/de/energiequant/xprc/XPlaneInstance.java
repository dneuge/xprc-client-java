package de.energiequant.xprc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPlaneInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(XPlaneInstance.class);

    private final File rootDirectory;
    private final int generation;

    private Instant logTimestamp;
    private boolean foundXPRC;
    private File xprcPasswordFile;
    private File xprcPortFile;

    private Instant updated;

    private static final byte LF = 0x0A;
    private static final byte CR = 0x0D;
    private static final byte[] EOL_SEQ_LF = {LF};
    private static final byte[] EOL_SEQ_CR = {CR};
    private static final byte[] EOL_SEQ_CRLF = {CR, LF};

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static final Pattern INSTALLER_FILENAME_PATTERN = Pattern.compile("^X-Plane (\\d+) Installer.*$", Pattern.CASE_INSENSITIVE);
    private static final int INSTALLER_FILENAME_PATTERN_GENERATION = 1;

    private static final String SERVER_LIST_DIRECTORY_PATH = "Output" + FILE_SEPARATOR + "preferences";
    private static final Pattern SERVER_LIST_FILENAME_PATTERN = Pattern.compile("^server_list_(\\d+)\\.txt$", Pattern.CASE_INSENSITIVE);
    private static final int SERVER_LIST_FILENAME_PATTERN_GENERATION = 1;

    private static final Pattern LOG_VERSION_PATTERN = Pattern.compile("^Log\\.txt for X-Plane (\\d+)\\..*", Pattern.CASE_INSENSITIVE); // case is different between 10/11 and 12
    private static final int LOG_VERSION_PATTERN_GENERATION = 1;

    public XPlaneInstance(File rootDirectory, int generation) {
        this.rootDirectory = rootDirectory;
        this.generation = generation;

        // verify version - this is mainly to make users aware of potential issues that may prevent proper operation,
        // such as listed but not mounted (empty) directories
        Optional<Integer> identified = identifyGeneration(rootDirectory);
        if (!identified.isPresent()) {
            LOGGER.warn("{} could not be verified to actually be X-Plane {} (check directory permissions)", rootDirectory, generation);
        } else if (identified.get() != generation) {
            LOGGER.warn("{} is supposed to be X-Plane {} but has been identified as X-Plane {} (repurposed installation directory?)", rootDirectory, generation, identified.get());
        }

        update();
    }

    public XPlaneInstance update() {
        synchronized (this) {
            updated = Instant.now();

            File logFile = new File(rootDirectory, "Log.txt");
            logTimestamp = Instant.ofEpochMilli(logFile.lastModified());
            if (logTimestamp.equals(Instant.EPOCH)) {
                logTimestamp = null;
            }

            File outputDirectory = new File(rootDirectory, "Output");
            File preferencesDirectory = new File(outputDirectory, "preferences");
            File xprcDirectory = new File(preferencesDirectory, "xprc");
            foundXPRC = xprcDirectory.exists();

            xprcPasswordFile = new File(xprcDirectory, "password.cfg");
            if (!xprcPasswordFile.exists()) {
                xprcPasswordFile = null;
            }

            xprcPortFile = new File(xprcDirectory, "port.cfg");
            if (!xprcPortFile.exists()) {
                xprcPortFile = null;
            }
        }

        return this;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public int getGeneration() {
        return generation;
    }

    public Optional<Instant> getLogTimestamp() {
        synchronized (this) {
            return Optional.ofNullable(logTimestamp);
        }
    }

    public Optional<File> getXPRCPasswordFile() {
        synchronized (this) {
            return Optional.ofNullable(xprcPasswordFile);
        }
    }

    public Optional<File> getXPRCPortFile() {
        synchronized (this) {
            return Optional.ofNullable(xprcPortFile);
        }
    }

    public boolean hasXPRC() {
        synchronized (this) {
            return foundXPRC;
        }
    }

    public Instant getLastUpdated() {
        synchronized (this) {
            return updated;
        }
    }

    public Optional<char[]> readXPRCPassword() {
        File file = getXPRCPasswordFile().orElse(null);
        if (file == null) {
            LOGGER.debug("XPRC password file not found for X-Plane installation at {}", rootDirectory);
            return Optional.empty();
        }

        return readXPRCStandardFile(xprcPasswordFile);
    }

    private Optional<char[]> readXPRCStandardFile(File file) {
        char[] out = null;
        try {
            byte[] content = Files.readAllBytes(file.toPath());
            if (content.length == 0) {
                LOGGER.warn("XPRC standard file is empty: {}", file);
                return Optional.empty();
            }

            // XPRC only supports US-ASCII and passwords are not expected to contain control characters
            // validate what we have read; EOL termination may be ignored
            int goodCharacters = 0;
            for (int i = 0; i < content.length; i++) {
                byte b = content[i];
                if (b < 0x20 || b > 0x7E) {
                    break;
                }
                goodCharacters++;
            }

            int numDiscardedCharacters = content.length - goodCharacters;
            if (numDiscardedCharacters <= 2) {
                // check if it is an EOL sequence which can be ignored
                byte[] discardedCharacters = (numDiscardedCharacters == 1)
                    ? new byte[]{content[content.length - 1]}
                    : new byte[]{content[content.length - 2], content[content.length - 1]};

                if (isEndOfLineSequence(discardedCharacters)) {
                    LOGGER.debug("Ignoring trailing EOL in XPRC standard file {}", file);
                    numDiscardedCharacters = 0;
                }

                Arrays.fill(discardedCharacters, (byte) 0);
            }

            if (numDiscardedCharacters != 0) {
                LOGGER.warn("Unsupported characters found in XPRC standard file, unable to use: {}", file);
            } else {
                out = new char[goodCharacters];
                for (int i = 0; i < out.length; i++) {
                    out[i] = (char) content[i];
                }
            }

            Arrays.fill(content, (byte) 0);
        } catch (IOException ex) {
            LOGGER.warn("Failed to read XPRC standard file {}", file, ex);
        }

        return Optional.ofNullable(out);
    }

    public Optional<Integer> readXPRCPort() {
        File file = getXPRCPortFile().orElse(null);
        if (file == null) {
            LOGGER.debug("XPRC port file not found for X-Plane installation at {}", rootDirectory);
            return Optional.empty();
        }

        String portString = readXPRCStandardFile(xprcPortFile).map(String::new)
                                                              .orElse(null);
        if (portString == null) {
            LOGGER.debug("XPRC port file {} could not be read", xprcPortFile);
            return Optional.empty();
        }

        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException ex) {
            LOGGER.debug("failed to parse network port from XPRC standard file {}: \"{}\"", xprcPortFile, portString, ex);
            return Optional.empty();
        }

        if ((port < 1) || (port > 65535)) {
            LOGGER.warn("Network port read from XPRC standard file {} is out of range: {}", xprcPortFile, port);
            return Optional.empty();
        }

        return Optional.of(port);
    }

    private static boolean isEndOfLineSequence(byte[] bytes) {
        return Arrays.equals(bytes, EOL_SEQ_LF) || Arrays.equals(bytes, EOL_SEQ_CRLF) || Arrays.equals(bytes, EOL_SEQ_CR);
    }

    private static Optional<Integer> identifyGenerationFromFiles(String type, File directory, Pattern pattern, int patternGroup) {
        File[] files = directory.listFiles((dir, name) -> pattern.matcher(name).matches());
        if (files == null) {
            return Optional.empty();
        }

        Set<Integer> generations = new HashSet<>();
        try {
            for (File file : files) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    generations.add(Integer.parseUnsignedInt(matcher.group(patternGroup)));
                }
            }
        } catch (NumberFormatException ex) {
            // any parsing error should void all results
            LOGGER.debug("Failed to parse generation number from {} files: {}", type, files);
            return Optional.empty();
        }

        if (generations.size() == 1) {
            return Optional.of(generations.iterator().next());
        } else if (generations.size() > 1) {
            LOGGER.debug("Different generation numbers found on {} files in {}: {}", type, directory, generations);
        }

        return Optional.empty();
    }

    public static Optional<Integer> identifyGeneration(File xplaneRootDirectory) {
        // There does not seem to be any particular file pointing out just the X-Plane version number/generation
        // but we can try reading the log file or, if unavailable, attempt to figure it out from several clues of
        // which at least one should be present if that instance was run at least once since installation.

        // Try reading the log file first. Version number should always be on the first line, so we do not need to read
        // any further.
        LOGGER.debug("Trying to identify generation of {} by log file content", xplaneRootDirectory);
        File logFile = new File(xplaneRootDirectory, "Log.txt");
        if (logFile.canRead()) {
            try (
                FileInputStream fis = new FileInputStream(logFile);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.ISO_8859_1);
                BufferedReader br = new BufferedReader(isr);
            ) {
                String line = br.readLine();
                if (line != null) {
                    Matcher matcher = LOG_VERSION_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        return Optional.of(Integer.parseUnsignedInt(matcher.group(LOG_VERSION_PATTERN_GENERATION)));
                    }
                }
            } catch (IOException | NumberFormatException ex) {
                LOGGER.debug("Failed to parse version number from log file {}", logFile, ex);
            }
        }

        // If at any point the user installed an update (which should always be the case except on very first run),
        // we should have the installer still in the root directory which indicates the generation.
        // At least we have it on Linux and Windows; it appears to be absent on macOS... :/
        LOGGER.debug("Trying to identify generation of {} by installer file(s)", xplaneRootDirectory);
        Optional<Integer> generation = identifyGenerationFromFiles("installer", xplaneRootDirectory, INSTALLER_FILENAME_PATTERN, INSTALLER_FILENAME_PATTERN_GENERATION);
        if (generation.isPresent()) {
            return generation;
        }

        // If at any point X-Plane retrieved the update/version list then its file name should also indicate the
        // generation.
        LOGGER.debug("Trying to identify generation of {} by server list file(s)", xplaneRootDirectory);
        File serverListDirectory = new File(xplaneRootDirectory, SERVER_LIST_DIRECTORY_PATH);
        return identifyGenerationFromFiles("server list", serverListDirectory, SERVER_LIST_FILENAME_PATTERN, SERVER_LIST_FILENAME_PATTERN_GENERATION);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("XPlaneInstance(");

        sb.append(generation);
        sb.append(" @ ");
        sb.append(rootDirectory);
        sb.append(", foundXPRC=");
        sb.append(foundXPRC);
        sb.append(", xprcPasswordFile=");
        sb.append(xprcPasswordFile);
        sb.append(", xprcPortFile=");
        sb.append(xprcPortFile);
        sb.append(", logTimestamp=");
        sb.append(DateTimeFormatter.ISO_INSTANT.format(logTimestamp));
        sb.append(", updated=");
        sb.append(DateTimeFormatter.ISO_INSTANT.format(updated));

        sb.append(")");

        return sb.toString();
    }
}
