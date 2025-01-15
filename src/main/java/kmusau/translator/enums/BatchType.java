package kmusau.translator.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum BatchType {
    TEXT("Text"), AUDIO("Audio");

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
