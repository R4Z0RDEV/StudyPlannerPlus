package com.studyplanner.util;

/**
 * 시간 형식 변환 유틸리티
 */
public class TimeUtil {

    /**
     * 초를 mm:ss 형식으로 변환
     * 
     * @param seconds 초
     * @return "mm:ss" 형식의 문자열
     */
    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * mm:ss 형식 문자열을 초로 변환
     * 
     * @param timeString "mm:ss" 형식의 문자열
     * @return 초
     */
    public static int parseTime(String timeString) {
        String[] parts = timeString.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("시간 형식이 올바르지 않습니다: " + timeString);
        }
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return minutes * 60 + seconds;
    }
}
