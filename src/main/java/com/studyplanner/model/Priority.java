package com.studyplanner.model;

/**
 * 과제 우선순위 열거형
 */
public enum Priority {
    LOW("낮음"),
    MEDIUM("보통"),
    HIGH("높음");
    
    private final String displayName;
    
    Priority(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
