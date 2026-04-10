package com.example.cloverville_project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ClovervilleFacade {


    // SINGLETON

    private static ClovervilleFacade instance;

    public static ClovervilleFacade getInstance() {
        if (instance == null) instance = new ClovervilleFacade();
        return instance;
    }

    private ClovervilleFacade() {
        this.residentList = new ResidentList();
        this.tradeOfferList = new TradeOfferList();
        this.taskList = new CommunalTaskList();
        this.greenActionList = new GreenActionList();
    }


    // INTERNAL DATA

    private ResidentList residentList;
    private TradeOfferList tradeOfferList;
    private CommunalTaskList taskList;
    private GreenActionList greenActionList;


    // COMMUNITY POOL

    private int communityPool = 0;

    public int getCommunityPool() {
        return communityPool;
    }

    public void addToCommunityPool(int amount) {
        if (amount > 0) {
            communityPool += amount;
        }
    }



    // Locate the website folder dynamically so the same code
    // works regardless of where the project is run from
    private Path getDataFile(String filename) throws IOException {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

        while (current != null) {
            Path candidate = current.resolve("cloverville_website");
            if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                return candidate.resolve(filename);
            }
            current = current.getParent();
        }

        throw new IOException("cloverville_website folder not found");
    }


    // RESIDENTS

    public Resident createResident(String name, int personalPoints, int greenPoints) {
        Resident r = new Resident(name, personalPoints, greenPoints);

        // Assign a stable public ID so residents keep the same identifier
        // across export/import and deletion of other residents
        int nextId = residentList.getAllResidents().stream()
                .mapToInt(Resident::getPublicId)
                .max()
                .orElse(0) + 1;

        r.setPublicId(nextId);
        residentList.addResident(r);
        return r;
    }

    public void deleteResident(Resident r) {
        if (r == null) return;

        residentList.removeResident(r);
        // Remove trade offers owned by the resident to avoid
        // orphaned offers referencing deleted users
        for (TradeOffer offer : new ArrayList<>(tradeOfferList.getAllOffers())) {
            if (offer.getOwner().equals(r.getName())) {
                deleteTradeOffer(offer);
            }
        }

        TaskLog.log.removeIf(entry -> entry.getResident().equals(r));
    }

    private Resident selectedResident;

    public void setSelectedResident(Resident r) {
        this.selectedResident = r;
    }

    public Resident getSelectedResident() {
        return selectedResident;
    }

    public Resident getResident(String name) {
        return residentList.findByName(name);
    }

    public List<Resident> getAllResidents() {
        return residentList.getAllResidents();
    }


// PARTICIPATION COUNT

// Count how many communal tasks a resident has completed
// based on the shared task log
    public int getParticipationCount(Resident resident) {
        int count = 0;
        for (TaskLogEntry entry : TaskLog.log) {
            if (entry.getResident().equals(resident)) {
                count++;
            }
        }
        return count;
    }


    // TRADE OFFERS

    public List<TradeOffer> getAllTradeOffers() {
        return tradeOfferList.getAllOffers();
    }

    public TradeOffer createTradeOffer(String ownerName, String offer, String price,
                                       String status, Integer pointCost) {

        TradeOffer t = new TradeOffer(ownerName, offer, price, status, pointCost);
        tradeOfferList.addOffer(t);

        Resident owner = residentList.findByName(ownerName);
        if (owner != null) owner.getOwnedTradeOffers().add(t);

        return t;
    }

    public void deleteTradeOffer(TradeOffer t) {
        tradeOfferList.removeOffer(t);

        for (Resident r : residentList.getAllResidents()) {
            r.getAssignedTradeOffers().remove(t);
            r.getOwnedTradeOffers().remove(t);
        }
    }

    public void editTradeOffer(TradeOffer offer, String newOffer, String newPrice, String newStatus) {
        offer.setTradeOffer(newOffer);
        offer.setPriceOrService(newPrice);
        offer.setStatus(newStatus);
    }

    public void assignTradeOffer(String assignedName, TradeOffer offer) {
        Resident assigned = residentList.findByName(assignedName);
        Resident owner = residentList.findByName(offer.getOwner());

        if (assigned == null || owner == null) return;

        // Point transfer only happens if the trade price is numeric.
        // Service-based trades do not affect points.

        Integer cost = offer.getPointCost();

        if (cost != null && cost > 0) {
            if (assigned.getPersonalPoints() < cost) return;
            assigned.setPersonalPoints(assigned.getPersonalPoints() - cost);
            owner.setPersonalPoints(owner.getPersonalPoints() + cost);
        }

        offer.setStatus("Assigned");

        if (!assigned.getAssignedTradeOffers().contains(offer)) {
            assigned.getAssignedTradeOffers().add(offer);
        }
    }


    // COMMUNAL TASKS

    public List<CommunalTask> getAllTasks() {
        return taskList.getAllTasks();
    }

    public CommunalTask createTask(String name, String desc, int points) {
        CommunalTask t = new CommunalTask(name, desc, points);
        taskList.addTask(t);
        return t;
    }

    public void deleteTask(CommunalTask t) {
        taskList.removeTask(t);
    }

    public void editTask(CommunalTask task, String newName, String newDescription, int newPoints) {
        task.setName(newName);
        task.setDescription(newDescription);
        task.setPersonalPoints(newPoints);
    }

    public TaskLogEntry completeTask(String residentName, String taskName) {
        Resident r = residentList.findByName(residentName);
        CommunalTask t = taskList.findByName(taskName);

        if (r == null || t == null) return null;

        int base = t.getPersonalPoints();
        int boost = r.getParticipationBoostPercent();

        // Apply participation boost as a percentage bonus
        int bonus = (int) Math.round(base * (boost / 100.0));

        int total = base + bonus;
        r.setPersonalPoints(r.getPersonalPoints() + total);
        addToCommunityPool(total);

        TaskLogEntry entry = new TaskLogEntry(r, t);
        TaskLog.log.add(entry);
        return entry;
    }


    // GREEN ACTIONS

    public GreenAction createGreenAction(String name, int points) {
        GreenAction a = new GreenAction(name, points);
        greenActionList.addAction(a);
        return a;
    }

    public List<GreenAction> getAllGreenActions() {
        return greenActionList.getAllActions();
    }

    public void performGreenAction(String residentName, String actionName) {
        Resident r = residentList.findByName(residentName);
        GreenAction a = greenActionList.findByName(actionName);

        if (r != null && a != null) {
            r.setGreenPoints(r.getGreenPoints() + a.getGreenPoints());
        }
    }


    // RESET POINTS

    // Reset personal points without affecting the community pool

    public void resetAllPersonalPoints() {
        for (Resident r : residentList.getAllResidents()) {
            int points = r.getPersonalPoints();
            if (points > 0) {

                r.setPersonalPoints(0);
            }
        }
    }


    // IMPORT DATA

    // Import uses the same JSON structure as export to keep
    // Java app and website data in sync
    public void importAllData(String filename) {
        try {
            Path importPath = getDataFile(filename);

            if (!Files.exists(importPath)) {
                System.out.println("No JSON file → starting fresh.");
                return;
            }

            String json = Files.readString(importPath);
            Gson gson = new Gson();
            Map<String, Object> root = gson.fromJson(json, Map.class);

            // Clear all in-memory data before import to avoid
            // mixing old state with imported state
            residentList.clear();
            tradeOfferList.clear();
            taskList.clear();
            greenActionList.clear();
            TaskLog.log.clear();


            // Community Pool

            this.communityPool =
                    ((Double) root.getOrDefault("communityPool", 0.0)).intValue();


            // Residents

            List<Map<String, Object>> residentsJson =
                    (List<Map<String, Object>>) root.get("residents");

            if (residentsJson != null) {
                for (Map<String, Object> map : residentsJson) {
                    // Use Double casting because Gson parses numbers as Double by default
                    Resident r = new Resident(
                            (String) map.get("name"),
                            ((Double) map.get("personalPoints")).intValue(),
                            ((Double) map.get("greenPoints")).intValue()
                    );

                    // Restore stable public ID (fallback for older files)
                    Object publicIdObj = map.get("publicId");
                    if (publicIdObj != null) {
                        r.setPublicId(((Double) publicIdObj).intValue());
                    }

                    r.setParticipationBoostPercent(
                            ((Double) map.getOrDefault(
                                    "participationBoostPercent", 0.0)).intValue()
                    );

                    residentList.addResident(r);
                }
            }


// Trade Offers

            List<Map<String, Object>> offersJson =
                    (List<Map<String, Object>>) root.get("tradeOffers");

            if (offersJson != null) {
                for (Map<String, Object> map : offersJson) {

                    TradeOffer o = new TradeOffer(
                            (String) map.get("owner"),
                            (String) map.get("tradeOffer"),
                            (String) map.get("priceOrService"),
                            (String) map.get("status"),
                            map.get("pointCost") == null
                                    ? null
                                    : ((Double) map.get("pointCost")).intValue()
                    );

                    tradeOfferList.addOffer(o);

                    // Attach imported trade offers back to their real owner
                    // so internal relations remain correct

                    Resident owner = residentList.findByName(o.getOwner());
                    if (owner != null) {
                        owner.getOwnedTradeOffers().add(o);
                    }
                }
            }



            // Tasks

            List<Map<String, Object>> tasksJson =
                    (List<Map<String, Object>>) root.get("tasks");

            if (tasksJson != null) {
                for (Map<String, Object> map : tasksJson) {
                    CommunalTask t = new CommunalTask(
                            (String) map.get("name"),
                            (String) map.get("description"),
                            ((Double) map.get("personalPoints")).intValue()
                    );
                    taskList.addTask(t);
                }
            }


            // Green Actions

            List<Map<String, Object>> greenJson =
                    (List<Map<String, Object>>) root.get("greenActions");

            if (greenJson != null) {
                for (Map<String, Object> map : greenJson) {
                    GreenAction g = new GreenAction(
                            (String) map.get("name"),
                            ((Double) map.get("greenPoints")).intValue()
                    );
                    greenActionList.addAction(g);
                }
            }


            // Task Log

            List<Map<String, Object>> logJson =
                    (List<Map<String, Object>>) root.get("taskLog");

            if (logJson != null) {
                for (Map<String, Object> map : logJson) {
                    Resident r =
                            residentList.findByName((String) map.get("residentName"));
                    CommunalTask t =
                            taskList.findByName((String) map.get("taskName"));

                    if (r != null && t != null) {
                        TaskLog.log.add(new TaskLogEntry(r, t));
                    }
                }
            }

            System.out.println("Import successful → " + importPath.toAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // EXPORT DATA

    public void exportAllData(String filename) {
        try {
            Path exportPath = getDataFile(filename);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Map<String, Object> root = new HashMap<>();


            // Community Pool

            root.put("communityPool", communityPool);


            // Residents

            List<Map<String, Object>> residentsJson = new ArrayList<>();
            for (Resident r : residentList.getAllResidents()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", r.getName());                 // internal
                map.put("publicId", r.getPublicId());         // website
                map.put("personalPoints", r.getPersonalPoints());
                map.put("greenPoints", r.getGreenPoints());
                map.put("participationBoostPercent", r.getParticipationBoostPercent());
                residentsJson.add(map);
            }
            root.put("residents", residentsJson);


            List<Map<String, Object>> offersJson = new ArrayList<>();
            for (TradeOffer o : tradeOfferList.getAllOffers()) {
                Map<String, Object> map = new HashMap<>();

                // Export real owner names for clarity on the website
                map.put("owner", o.getOwner());
                map.put("tradeOffer", o.getTradeOffer());
                map.put("priceOrService", o.getPriceOrService());
                map.put("status", o.getStatus());
                map.put("pointCost", o.getPointCost());

                offersJson.add(map);
            }
            root.put("tradeOffers", offersJson);



            // Tasks

            List<Map<String, Object>> tasksJson = new ArrayList<>();
            for (CommunalTask t : taskList.getAllTasks()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", t.getName());
                map.put("description", t.getDescription());
                map.put("personalPoints", t.getPersonalPoints());
                tasksJson.add(map);
            }
            root.put("tasks", tasksJson);


            // Green Actions

            List<Map<String, Object>> greenJson = new ArrayList<>();
            for (GreenAction g : greenActionList.getAllActions()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", g.getName());
                map.put("greenPoints", g.getGreenPoints());
                greenJson.add(map);
            }
            root.put("greenActions", greenJson);


            // Task Log

            List<Map<String, Object>> logJson = new ArrayList<>();
            for (TaskLogEntry entry : TaskLog.log) {
                Map<String, Object> map = new HashMap<>();
                map.put("residentPublic",
                        "Resident " + entry.getResident().getPublicId());
                map.put("taskName", entry.getTask().getName());
                logJson.add(map);
            }
            root.put("taskLog", logJson);

            try (Writer writer = Files.newBufferedWriter(exportPath)) {
                gson.toJson(root, writer);
            }

            System.out.println("Export successful → " + exportPath.toAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
