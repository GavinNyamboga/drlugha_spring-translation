package drlugha.translator.enums;

import java.util.Arrays;
import java.util.Optional;

public enum BatchType {
    TEXT("text"),
    AUDIO("audio"),
    TEXT_FEEDBACK("text_feedback"),;

    private final String name;

    BatchType(String name) {
        this.name = name;
    }

    public static Optional<BatchType> fromName(String batchType) {
        return Arrays.stream(values()).filter(bt -> bt.name.equalsIgnoreCase(batchType))
                .findFirst();
    }

    public String getName() {
        return name;
    }
}
