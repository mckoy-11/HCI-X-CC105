package main.service;

import main.dao.HomeDao;
import main.model.HomeCardData;

public class HomeService {
    
    private final HomeDao homeDao;
    
    public HomeService() {
        this.homeDao = new HomeDao();
    }
    
    public HomeCardData getHomeCardData() {
        try {
            HomeCardData data = homeDao.getHomeCardData();
            if (data == null) {
                System.err.println("HomeDao returned null data, using defaults");
                return new HomeCardData();
            }
            return data;
        } catch (Exception e) {
            System.err.println("Error fetching home card data: " + e.getMessage());
            e.printStackTrace();
            // Return empty data instead of crashing
            return new HomeCardData();
        }
    }
}