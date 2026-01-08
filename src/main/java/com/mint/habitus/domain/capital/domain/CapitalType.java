package com.mint.habitus.domain.capital.domain;

public enum CapitalType {

    PHYSICAL("신체자본"),
    MENTAL("심리자본"),
    KNOWLEDGE("지식자본"),
    CULTURAL("문화자본"),
    LINGUISTIC("언어자본"),
    SOCIAL("사회자본"),
    ECONOMIC("경제자본");

    private final String koreanName;

    CapitalType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
