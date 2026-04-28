package main.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import main.dao.PurokChecklistDao;
import main.model.PurokChecklistItem;
import main.store.DataChangeBus;
import main.store.DataTopics;

public class PurokChecklistService {

    private final PurokChecklistDao dao = new PurokChecklistDao();

    public List<PurokChecklistItem> getChecklist(int barangayId, String barangayName) {
        try {
            return dao.findByBarangay(barangayId, barangayName);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public boolean updateCollected(int checklistId, boolean collected) {
        try {
            boolean success = dao.updateCollected(checklistId, collected);
            if (success) {
                DataChangeBus.publish(DataTopics.CHECKLIST, DataTopics.DASHBOARD);
            }
            return success;
        } catch (SQLException e) {
            return false;
        }
    }
}
