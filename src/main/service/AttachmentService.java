package main.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import main.dao.AttachmentDao;
import main.model.EntryAttachment;

public class AttachmentService {

    private final AttachmentDao dao = new AttachmentDao();

    public List<byte[]> getImageContents(String entryType, int entryId) {
        try {
            List<EntryAttachment> attachments = dao.findByEntry(entryType, entryId);
            List<byte[]> contents = new ArrayList<byte[]>();
            for (EntryAttachment attachment : attachments) {
                contents.add(attachment.getContent());
            }
            return contents;
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public boolean replaceImages(String entryType, int entryId, List<byte[]> images) {
        try {
            dao.replaceForEntry(entryType, entryId, images);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
