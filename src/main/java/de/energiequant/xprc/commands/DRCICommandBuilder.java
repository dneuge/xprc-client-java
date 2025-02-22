package de.energiequant.xprc.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.energiequant.xprc.Command;
import de.energiequant.xprc.DataRef;
import de.energiequant.xprc.XPRCClient;
import de.energiequant.xprc.types.ValueType;

public class DRCICommandBuilder<SELF extends DRCICommandBuilder<SELF, CH, CFB, C>, CH extends DRCIChannel<CH, CFB, C>, CFB extends DRCIChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRCIMessage>> extends Command.Builder<SELF, C, CH, CFB, DRCIMessage> {
    // FIXME: draft/WIP

    private List<ValueType<?>> types;
    private String dataRefName;
    private int arrayLength = -1;

    private static final char TYPE_SEPARATOR = ',';
    private static final char SECTION_SEPARATOR = ':';
    private static final char ESCAPE_CHARACTER = '\\'; // TODO: standard escape character is defined/used protocol-wide, extract to a single definition

    private static final String MONITOR_OPTION = "echo";
    private static final String INTEGER_CONVERSION_OPTION = "intConv";
    private static final String WRITE_PERMISSION_OPTION = "writable";

    private static final Collection<Set<ValueType<?>>> COMPATIBLE_TYPES = unmodifiableCollectionOfValueTypeSets(new ValueType[][]{
        {ValueType.INTEGER, ValueType.FLOAT, ValueType.DOUBLE},
        {ValueType.INTEGER_ARRAY, ValueType.FLOAT_ARRAY},
        {ValueType.BLOB}
    });

    public enum MonitorMode {
        /**
         * In addition to updates submitted through X-Plane, notify also about submission from any XPRC session.
         */
        ALL("all"),
        /**
         * In addition to updates submitted through X-Plane, notify also about submission from any XPRC session other than the current one.
         */
        XPLANE_AND_OTHER_SESSIONS("other"),
        /**
         * Only notify about updates submitted through X-Plane. Notifications are disabled for submissions from XPRC.
         */
        XPLANE_ONLY("none");

        private final String optionValue;

        MonitorMode(String optionValue) {
            this.optionValue = optionValue;
        }
    }

    public enum IntegerConversionMode {
        /**
         * apply standard mathematical rounding when converting from floating point to integers (floor if &lt; .5, ceil if >= .5)
         */
        ROUND("round"),
        /**
         * always choose next lower integer unless close to .0
         */
        FLOOR("floor"),
        /**
         * always choose next higher integer unless close to .0
         */
        CEIL("ceil");

        private final String optionValue;

        IntegerConversionMode(String optionValue) {
            this.optionValue = optionValue;
        }
    }

    public enum Permission {
        /**
         * permit X-Plane and all XPRC sessions
         */
        ALL("all"),
        /**
         * permit all XPRC sessions, deny access through X-Plane
         */
        XPRC("xprc"),
        /**
         * permit only the current XPRC session, deny all other access
         */
        SESSION("session");

        private final String optionValue;

        Permission(String optionValue) {
            this.optionValue = optionValue;
        }
    }

    public DRCICommandBuilder(XPRCClient client) {
        super(client, "DRCI");
    }

    public DRCICommandBuilder(XPRCClient client, DataRef<?> dataRef) {
        this(client);
        named(dataRef.getName());
        havingType(dataRef.getType());
        dataRef.getArrayLength().ifPresent(this::withArrayLength);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CFB createChannelFactoryBuilder(XPRCClient client, Supplier<C> commandSupplier) {
        return (CFB) new DRCIChannel.FactoryBuilder<>(client, commandSupplier, types);
    }

    public SELF monitoring(MonitorMode mode) {
        return setOption(MONITOR_OPTION, mode.optionValue);
    }

    public SELF usingIntegerConversion(IntegerConversionMode mode) {
        return setOption(INTEGER_CONVERSION_OPTION, mode.optionValue);
    }

    public SELF allowingWriteAccessTo(Permission permission) {
        return setOption(WRITE_PERMISSION_OPTION, permission.optionValue);
    }

    public SELF havingType(ValueType<?> type) {
        return havingTypes(Collections.singletonList(type));
    }

    public SELF havingTypes(ValueType<?>... types) {
        return havingTypes(Arrays.asList(types));
    }

    @SuppressWarnings("unchecked")
    public SELF havingTypes(List<ValueType<?>> types) {
        if (containsDuplicates(types)) {
            throw new IllegalArgumentException("list of types is positional and must not hold duplicates");
        }

        if (!containsOnlyCompatibleTypes(types)) {
            throw new IllegalArgumentException("requested types cannot be converted between; incompatible types must be registered separately");
        }

        this.types = new ArrayList<>(types);

        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF named(String dataRefName) {
        // TODO: check for invalid characters

        if (dataRefName.isEmpty()) {
            throw new IllegalArgumentException("DataRef name must not be empty");
        }

        this.dataRefName = dataRefName;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF withArrayLength(int arrayLength) {
        this.arrayLength = arrayLength;
        return (SELF) this;
    }

    List<ValueType<?>> getTypes() {
        return Collections.unmodifiableList(new ArrayList<>(types));
    }

    @Override
    public C build() {
        if (dataRefName == null) {
            throw new IllegalArgumentException("DataRef name has not been specified");
        }

        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("types have not been specified");
        }

        boolean isArray = containsArrayType(types);
        if (isArray) {
            if (arrayLength < 0) {
                throw new IllegalArgumentException("array length has not been specified");
            }
        } else if (arrayLength >= 0) {
            throw new IllegalArgumentException("array length must only be specified for arrays");
        }

        StringBuilder sb = new StringBuilder();

        boolean isFirst = true;
        for (ValueType<?> type : types) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(TYPE_SEPARATOR);
            }

            sb.append(type.getEncodedTypeName());
        }

        sb.append(SECTION_SEPARATOR);

        sb.append(escapeDataRefName(dataRefName));

        if (isArray) {
            sb.append(SECTION_SEPARATOR);
            sb.append(arrayLength);
        }

        setParameter(0, sb.toString());

        return super.build();
    }

    private String escapeDataRefName(String s) {
        boolean escaped = false;

        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            boolean needsEscape = (ch == ESCAPE_CHARACTER) || (ch == SECTION_SEPARATOR);
            if (needsEscape) {
                sb.append(ESCAPE_CHARACTER);
                escaped = true;
            }
            sb.append(ch);
        }

        return escaped ? sb.toString() : s;
    }

    private boolean containsArrayType(Collection<ValueType<?>> types) {
        for (ValueType<?> type : types) {
            if (type.isArray()) {
                return true;
            }
        }

        return false;
    }

    private boolean containsDuplicates(Collection<ValueType<?>> types) {
        return (new HashSet<>(types)).size() != types.size();
    }

    private boolean containsOnlyCompatibleTypes(Collection<ValueType<?>> types) {
        for (Set<ValueType<?>> compatibleTypes : COMPATIBLE_TYPES) {
            int numCompatible = 0;
            for (ValueType<?> type : types) {
                if (compatibleTypes.contains(type)) {
                    numCompatible++;
                }
            }

            if (numCompatible == types.size()) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Collection<Set<ValueType<?>>> unmodifiableCollectionOfValueTypeSets(ValueType[][] b) {
        return Collections.unmodifiableCollection(
            Arrays.stream(b)
                  .map(x -> Collections.unmodifiableSet(new HashSet<>((Collection<ValueType<?>>) (Collection) Arrays.asList(x))))
                  .collect(Collectors.toList())
        );
    }
}
