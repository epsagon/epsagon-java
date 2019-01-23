package com.epsagon.events;

import com.epsagon.EpsagonConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * A Helper Class for building the event metadata.
 */
public class MetadataBuilder {
    private HashMap<String, String> _metadata;

    /**
     * @param existing A Map with items to initialize the metadata with.
     */
    public MetadataBuilder(Map<String, String> existing) {
        _metadata = new HashMap<>(existing);
    }

    /**
     * Default constructor, initializes an empty metadata map.
     */
    public MetadataBuilder() {
        _metadata = new HashMap<>();
    }

    /**
     * Puts an item in the metadata map.
     * @param key The key of the item.
     * @param value The value for the Item.
     * @return a reference to the Metadata builder.
     */
    public MetadataBuilder put(String key, String value) {
        if (value != null) {
            _metadata.put(key, value);
        }
        return this;
    }

    /**
     * Puts an item in the metadata map, only if {@link EpsagonConfig#isMetadataOnly()} is False.
     * @param key The key of the item.
     * @param value The value for the Item.
     * @return a reference to the Metadata builder.
     */
    public MetadataBuilder putIfAllData(String key, String value) {
        if (!EpsagonConfig.getInstance().isMetadataOnly()) {
            put(key, value);
        }
        return this;
    }

    /**
     * Builds the Metadata map.
     * @return The metadata map.
     */
    public HashMap<String, String> build() {
        return _metadata;
    }
}
