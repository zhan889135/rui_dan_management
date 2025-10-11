package com.talent.common.utils.poi;

/**
 * 封装键值对
 * */
public class LabelValue {
    private final String label;
    private final String value;
    private final boolean isBlank;

    private LabelValue(String label, String value, boolean isBlank) {
        this.label = label;
        this.value = value;
        this.isBlank = isBlank;
    }

    public static LabelValue of(String label, String value) {
        return new LabelValue(label, value, false);
    }

    public static LabelValue blank() {
        return new LabelValue("", "", true);
    }

    public String[] toArray() {
        return new String[]{label, value};
    }

    public boolean[] toFlagArray() {
        return new boolean[]{true, false};
    }

    public boolean isBlank() {
        return isBlank;
    }
}

