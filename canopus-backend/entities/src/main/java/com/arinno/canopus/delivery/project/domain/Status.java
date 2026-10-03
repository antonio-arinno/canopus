package com.arinno.canopus.delivery.project.domain;

public enum Status {

	DEVELOPMENT("Development"),
	PRE_PRODUCTION("Pre Production"),
	PRODUCTION("Production");
	
	private final String value;

	Status(String value) {
		this.value = value;
	}
	
    public static Status DEVELOPMENT() {
        return DEVELOPMENT;
    }

    public static Status PRE_PRODUCTION() {
        return PRE_PRODUCTION;
    }
    
    public static Status PRODUCTION() {
        return PRODUCTION;
    }

    public String getValue() {
        return value;
    }	    

}
