package br.ufrn.imd.capivagitscalator.util;

import br.ufrn.imd.capivagitscalator.domain.Severity;
import org.kohsuke.github.GHLabel;

import java.util.Collection;


public class SeverityUtil {

    public static Severity find(Collection<GHLabel> labels){
        if (labels == null) return Severity.LOW;

        for (GHLabel ghLabel : labels){
            String labelName = ghLabel.getName().toUpperCase();
            Severity severity = switch (labelName) {
                case "LOW" -> Severity.LOW;
                case "MID" -> Severity.MID;
                case "HIGH" -> Severity.HIGH;
                case "URGENT" -> Severity.URGENT;
                default -> null;
            };
            if (severity != null) return severity;
        }
        return Severity.LOW;
    }

    public static Severity upSeverity(Severity severity){
        if (severity == null) return Severity.MID;
        return switch (severity){
            case LOW -> Severity.MID;
            case MID -> Severity.HIGH;
            case HIGH -> Severity.URGENT;
            case URGENT -> severity;
        };
    }
}
