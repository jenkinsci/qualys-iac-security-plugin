package com.qualys.plugins.commons.model;

import lombok.Getter;
import lombok.Setter;

public class ParsingError {

    @Getter
    @Setter
    private String checkType;
    @Getter
    @Setter
    private String parsingErrorLocation;

    public ParsingError(String checkType, String parsingErrorLocation) {
        this.checkType = checkType;
        this.parsingErrorLocation = parsingErrorLocation;
    }
}
