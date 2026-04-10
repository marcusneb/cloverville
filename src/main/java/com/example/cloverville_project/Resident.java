package com.example.cloverville_project;

import java.util.ArrayList;
import java.util.List;

public class Resident {

    // Internal (real) name used in the Java application
    private String name;

    // Stable public ID used for website display
    private int publicId;

    private int personalPoints;
    private int greenPoints;

    private List<TradeOffer> assignedTradeOffers = new ArrayList<>();
    private List<TradeOffer> ownedTradeOffers = new ArrayList<>();

    // Percentage boost applied to communal tasks
    private int participationBoostPercent = 0;

    public Resident() { }

    public Resident(String name, int personalPoints, int greenPoints) {
        this.name = name;
        this.personalPoints = personalPoints;
        this.greenPoints = greenPoints;
    }

    // Name (internal)

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // Public ID (website only)

    public int getPublicId() {
        return publicId;
    }

    public void setPublicId(int publicId) {
        this.publicId = publicId;
    }


    // Points

    public int getPersonalPoints() {
        return personalPoints;
    }

    public void setPersonalPoints(int personalPoints) {
        this.personalPoints = personalPoints;
    }

    public int getGreenPoints() {
        return greenPoints;
    }

    public void setGreenPoints(int greenPoints) {
        this.greenPoints = greenPoints;
    }


    // Trade offers

    public List<TradeOffer> getAssignedTradeOffers() {
        return assignedTradeOffers;
    }

    public List<TradeOffer> getOwnedTradeOffers() {
        return ownedTradeOffers;
    }


    // Participation boost

    public int getParticipationBoostPercent() {
        return participationBoostPercent;
    }

    public void setParticipationBoostPercent(int participationBoostPercent) {
        this.participationBoostPercent = participationBoostPercent;
    }

    @Override
    public String toString() {
        // Keep real name visible inside the Java app
        return name;
    }
}
