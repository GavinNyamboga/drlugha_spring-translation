package kmusau.translator.enums;

public enum BatchStatus {
    assignedTranslator,
    translated,
    assignedTextVerifier,
    translationVerified,
    assignedExpertReviewer,
    secondVerificationDone,
    assignedRecorder,
    recorded,
    assignedAudioVerifier,
    audioVerified;
    
    public String getLabel(BatchType batchType) {
        if (this == BatchStatus.assignedTranslator) {
            if (batchType == BatchType.AUDIO) {
                return "Assigned Transcriber";
            }

            return "Assigned Translator";
        }
        else if (this == BatchStatus.translated || this == BatchStatus.assignedTextVerifier){
            if (batchType == BatchType.AUDIO){
                return "Transcribed";
            }

            return"Translated";
        }
        else if (this == BatchStatus.translationVerified || this == BatchStatus.assignedExpertReviewer ){
            return"Moderator Reviewed";
        }
        else if (this == BatchStatus.secondVerificationDone || this == BatchStatus.assignedRecorder){
            return"Expert Reviewed";
        }
        else if (this == BatchStatus.recorded || this == BatchStatus.assignedAudioVerifier){
            return"Audio Recorded";
        }
        else if (this == BatchStatus.audioVerified){
            return"Audio Reviewed";
        }
        else {
            return "";
        }
    }

}
