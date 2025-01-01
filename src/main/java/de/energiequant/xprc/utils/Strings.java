package de.energiequant.xprc.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Strings {
    private static final Logger LOGGER = LoggerFactory.getLogger(Strings.class);

    private Strings() {
        // utility class; hide constructor
    }

    public static String substituteVariables(String s, boolean caseSensitive, Map<String, String> variables) {
        Set<String> collisions = new HashSet<>();
        if (!caseSensitive) {
            Map<String, String> tmp = new HashMap<>();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String key = entry.getKey().toLowerCase();
                if (tmp.containsKey(key)) {
                    LOGGER.debug("substituteVariables: case-insensitive variable \"{}\" is defined multiple times, ignoring", key);
                    collisions.add(key);
                } else {
                    tmp.put(key, entry.getValue());
                }
            }
            for (String collision : collisions) {
                tmp.remove(collision);
            }
            variables = tmp;
        }

        StringBuilder resultBuilder = new StringBuilder();
        StringBuilder variableNameBuilder = new StringBuilder();

        boolean inControl = false;
        boolean inVariable = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '$') {
                if (inControl) {
                    throw new IllegalArgumentException("Variable not closed at i=" + i + " (duplicate control character): " + s);
                }
                inControl = true;
            } else if (inVariable) {
                if (ch != '}') {
                    variableNameBuilder.append(ch);
                } else {
                    String variableName = variableNameBuilder.toString();
                    if (!caseSensitive) {
                        variableName = variableName.toLowerCase();
                    }
                    if (variableName.isEmpty()) {
                        throw new IllegalArgumentException("Empty variable at i=" + i + ": " + s);
                    }

                    String variableValue = variables.get(variableName);
                    if (variableValue == null) {
                        if (collisions.contains(variableName)) {
                            throw new IllegalArgumentException("Case-insensitive variable \"" + variableName + "\" is defined multiple times, unable to substitute: " + s);
                        }

                        throw new IllegalArgumentException("Variable \"" + variableName + "\" is undefined, unable to substitute: " + s);
                    }

                    resultBuilder.append(variableValue);
                    variableNameBuilder = new StringBuilder();
                    inVariable = false;
                    inControl = false;
                }
            } else if (inControl) {
                if (ch == '{') {
                    inVariable = true;
                } else {
                    throw new IllegalArgumentException("Illegal control sequence character " + ch + " at i=" + i + ": " + s);
                }
            } else {
                resultBuilder.append(ch);
            }
        }

        if (inControl) {
            throw new IllegalArgumentException("Control sequence not closed: " + s);
        }

        return resultBuilder.toString();
    }
}
