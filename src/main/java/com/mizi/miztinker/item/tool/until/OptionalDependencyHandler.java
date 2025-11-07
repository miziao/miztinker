package com.mizi.miztinker.item.tool.until;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionalDependencyHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OptionalDependencyHandler.class);

    /**
     * 检测指定类是否存在
     * @param className 完整类名
     * @return 是否存在
     */
    public static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Optional dependency not found: {}", className);
            return false;
        }
    }

}
