package drlugha.translator.system.batch.enums;

public enum BatchStatus {
    ASSIGNED_TRANSLATOR,
    TRANSLATED,
    ASSIGNED_TEXT_VERIFIER,
    TRANSLATION_VERIFIED,
    ASSIGNED_EXPERT_REVIEWER,
    SECOND_VERIFICATION_DONE,
    ASSIGNED_RECORDER,
    RECORDED,
    ASSIGNED_AUDIO_VERIFIER,
    AUDIO_VERIFIED;

    public String getLabel(BatchType batchType) {
        if (this == BatchStatus.ASSIGNED_TRANSLATOR) {
            if (batchType == BatchType.AUDIO) {
                return "Assigned Transcriber";
            }

            return "Assigned Translator";
        } else if (this == BatchStatus.TRANSLATED || this == BatchStatus.ASSIGNED_TEXT_VERIFIER) {
            if (batchType == BatchType.AUDIO) {
                return "Transcribed";
            }

            return "Translated";
        } else if (this == BatchStatus.TRANSLATION_VERIFIED || this == BatchStatus.ASSIGNED_EXPERT_REVIEWER) {
            return "Moderator Reviewed";
        } else if (this == BatchStatus.SECOND_VERIFICATION_DONE || this == BatchStatus.ASSIGNED_RECORDER) {
            return "Expert Reviewed";
        } else if (this == BatchStatus.RECORDED || this == BatchStatus.ASSIGNED_AUDIO_VERIFIER) {
            return "Audio Recorded";
        } else if (this == BatchStatus.AUDIO_VERIFIED) {
            return "Audio Reviewed";
        } else {
            return "";
        }
    }

    public String fromOrdinal(int ordinal) {
        BatchType batchType = BatchType.values()[ordinal];
        return getLabel(batchType);
    }

}
