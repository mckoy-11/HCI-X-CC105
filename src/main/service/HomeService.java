package main.service;

import main.dao.HomeDao;
import main.model.HomeCardData;

public class HomeService {
    
    private final HomeDao homeDao;
    
    public HomeService() {
        this.homeDao = new HomeDao();
    }
    
    public HomeCardData getHomeCardData() {
        return homeDao.getHomeCardData();
    }
}