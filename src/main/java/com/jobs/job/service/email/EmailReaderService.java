package com.jobs.job.service.email;

import com.jobs.job.entity.EmailCheckpoint;
import com.jobs.job.repository.EmailCheckpointRepository;
import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.*;
import jakarta.mail.search.FromStringTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailReaderService {

    private final EmailCheckpointRepository checkpointRepository;

    @Value("${imap.host}")
    private String host;

    @Value("${imap.username}")
    private String username;

    @Value("${imap.password}")
    private String password;

    public List<String> readLinkedInJobEmails() {

        log.debug("Polling inbox for LinkedIn emails");

        List<String> emailBodies = new ArrayList<>();

        IMAPFolder inbox = null;
        Store store = null;

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");

            props.put("mail.imaps.connectiontimeout", "15000");
            props.put("mail.imaps.timeout", "15000");
            props.put("mail.imaps.writetimeout", "15000");

            props.put("mail.imaps.keepalive", "true");
            props.put("mail.imaps.ssl.enable", "true");
            props.put("mail.imaps.auth", "true");

            Session session = Session.getInstance(props);
            store = session.getStore("imaps");
            store.connect(host, username, password);

            inbox = (IMAPFolder) store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // BASELINE LOGIC
            var optionalCheckpoint = checkpointRepository.findById("LINKEDIN");

            if (optionalCheckpoint.isEmpty()) {

                long baselineUid = inbox.getUIDNext() - 1;

                EmailCheckpoint checkpoint = new EmailCheckpoint();
                checkpoint.setMailbox("LINKEDIN");
                checkpoint.setLastProcessedUid(baselineUid);
                checkpointRepository.save(checkpoint);

                log.info(
                        "Baseline UID set to {}. Old LinkedIn emails will be ignored.",
                        baselineUid
                );

                return List.of();
            }

            long lastProcessedUid = optionalCheckpoint.get().getLastProcessedUid();

            // LIMIT INBOX SCAN (LAST 20 MAILS ONLY)
            int totalMessages = inbox.getMessageCount();
            int start = Math.max(1, totalMessages - 20);

            Message[] messages = inbox.getMessages(start, totalMessages);

            long maxUidSeen = lastProcessedUid;

            for (Message message : messages) {

                // Filter sender manually (FASTER than inbox.search)
                Address[] froms = message.getFrom();
                if (froms == null ||
                        !froms[0].toString().contains("jobalerts-noreply@linkedin.com")) {
                    continue;
                }

                long uid = inbox.getUID(message);

                if (uid <= lastProcessedUid) {
                    continue;
                }

                maxUidSeen = Math.max(maxUidSeen, uid);

                log.info("New LinkedIn Mail Found (UID={})", uid);
                log.info("From   : {}", froms[0]);
                log.info("Subject: {}", message.getSubject());

                String body = extractText(message);
                if (body != null && !body.isBlank()) {
                    emailBodies.add(body);
                }
            }

            if (maxUidSeen == lastProcessedUid) {
                log.info("No new LinkedIn emails found");
            }

            if (maxUidSeen > lastProcessedUid) {
                EmailCheckpoint checkpoint = optionalCheckpoint.get();
                checkpoint.setLastProcessedUid(maxUidSeen);
                checkpointRepository.save(checkpoint);
            }

        } catch (Exception e) {
            log.warn("IMAP timeout (expected), will retry next cycle");

        } finally {
            try {
                if (inbox != null && inbox.isOpen()) inbox.close(false);
                if (store != null) store.close();
            } catch (Exception ignored) {}
        }

        log.debug("Finished polling inbox");

        return emailBodies;
    }

    private String extractText(Part part) throws Exception {

        if (part.isMimeType("text/plain")) {
            return part.getContent().toString();
        }

        if (part.isMimeType("text/html")) {
            return part.getContent().toString();
        }

        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                String result = extractText(multipart.getBodyPart(i));
                if (result != null) return result;
            }
        }

        return null;
    }
}
