package de.energiequant.xprc;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExceptionHandler implements Consumer<XPRCException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @Override
    public void accept(XPRCException ex) {
        LOGGER.warn("{} caught unhandled exception: {}", ex.getClient(), ex);
    }
}
