package main.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import main.dao.ScheduleDao;
import main.model.CollectionInfo;
import main.model.Schedule;
import main.store.DataChangeBus;
import main.store.DataTopics;

public class ScheduleService {

    private final ScheduleDao dao = new ScheduleDao();

    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = dao.findAll();
        return schedules == null ? Collections.<Schedule>emptyList() : schedules;
    }

    public Schedule getById(int id) {
        return getAllSchedules().stream()
                .filter(schedule -> schedule.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Schedule> getByStatus(String status) {
        return getAllSchedules().stream()
                .filter(schedule -> equalsIgnoreCase(schedule.getStatus(), status))
                .collect(Collectors.toList());
    }

    public List<Schedule> getByBarangay(String barangayName) {
        String query = barangayName == null ? "" : barangayName.trim().toLowerCase(Locale.ENGLISH);
        return getAllSchedules().stream()
                .filter(schedule -> schedule.getBarangayName() != null)
                .filter(schedule -> schedule.getBarangayName().toLowerCase(Locale.ENGLISH).contains(query))
                .collect(Collectors.toList());
    }

    public boolean isScheduleValid(Schedule schedule) {
        return schedule != null
                && !isBlank(schedule.getBarangayName())
                && schedule.getDate() != null
                && schedule.getTime() != null
                && !isBlank(schedule.getStatus());
    }

    public List<Schedule> sortByDateAsc() {
        return getAllSchedules().stream()
                .sorted(Comparator.comparing(Schedule::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Schedule::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public List<Schedule> sortByDateDesc() {
        return getAllSchedules().stream()
                .sorted(Comparator.comparing(Schedule::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Schedule::getTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public int countByStatus(String status) {
        return (int) getAllSchedules().stream()
                .filter(schedule -> equalsIgnoreCase(schedule.getStatus(), status))
                .count();
    }

    public int countByBarangay(String barangayName) {
        return getByBarangay(barangayName).size();
    }

    public List<String> getBarangaysScheduledForDay(LocalDate weekStart, LocalDate weekEnd, String dayLabel) {
        return getAllSchedules().stream()
                .filter(schedule -> schedule.getDate() != null)
                .filter(schedule -> !schedule.getDate().isBefore(weekStart))
                .filter(schedule -> !schedule.getDate().isAfter(weekEnd))
                .filter(schedule -> schedule.getDate().getDayOfWeek().name().equalsIgnoreCase(dayLabel))
                .map(Schedule::getBarangayName)
                .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return getAllSchedules().isEmpty();
    }

    public int totalSchedules() {
        return getAllSchedules().size();
    }

    public boolean saveSchedule(Schedule schedule) {
        boolean success = isScheduleValid(schedule) && dao.saveOrUpdate(schedule);
        publish(success);
        return success;
    }

    public boolean updateSchedule(Schedule schedule) {
        boolean success = isScheduleValid(schedule) && dao.saveOrUpdate(schedule);
        publish(success);
        return success;
    }

    public boolean deleteSchedule(int scheduleId) {
        boolean success = dao.deleteById(scheduleId);
        publish(success);
        return success;
    }

    public CollectionInfo getCollectionInfo(String barangayName) {
        return dao.findCurrentCollectionInfo(barangayName);
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void publish(boolean success) {
        if (success) {
            DataChangeBus.publish(DataTopics.SCHEDULES, DataTopics.BARANGAYS, DataTopics.COLLECTION_INFO, DataTopics.DASHBOARD);
        }
    }
}
