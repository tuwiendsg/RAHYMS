package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.adapters.email.MailUtils;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.search.SubjectTerm;
import java.io.IOException;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class EmailInputAdapter implements InputPullAdapter {
    private static final Logger log = LoggerFactory.getLogger(EmailInputAdapter.class);

    private final String subject;
    private final String host;
    private final String username;
    private final String password;
    private final int port;
    private final boolean authentication;
    private final String type;
    private final String subtype;
    private final boolean deleteMessage;

    public EmailInputAdapter(String subject, String host, String username, String password, int port, boolean authentication, String type, String subtype, boolean deleteMessage) {
        this.subject = subject;
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
        this.authentication = authentication;
        this.type = type;
        this.subtype = subtype;
        this.deleteMessage = deleteMessage;
    }

    @Override
    public Message pull() throws AdapterException {
        Folder folder = null;
        try {
            folder = MailUtils.getMail(subject, host, username, password, port, authentication);

            javax.mail.Message mail = null;

            javax.mail.Message[] search = folder.search(new SubjectTerm(subject));
            if (search.length > 0) {
                mail = search[0];
            } else {
                return null;
            }

            if (deleteMessage) {
                mail.setFlag(Flags.Flag.DELETED, true);
            }

            String conversationId = mail.getSubject();
            if (conversationId.toLowerCase().startsWith("re: ")) {
                conversationId = conversationId.substring(4);
            }

            String content = null;
            try {
                content = getText(mail);
            } catch (MessagingException | IOException ignored) {}

            String trimmedSubject = conversationId.replaceFirst("Task - ", "");
            String[] split = trimmedSubject.split(" ");

            Message.MessageBuilder builder = new Message.MessageBuilder()
                    .setConversationId(split[0])
                    .setType(type)
                    .setSubtype(subtype)
                    .setSenderId(Identifier.peer(split[2]));

            if (subtype.isEmpty() && content != null) {
                split = content.replaceAll("<[a-zA-Z0-9 -:;@=\"\r\n]*>", " ").replaceAll("\n\r", " ").replaceAll("\n", " ").replaceAll("\r", " ").trim().split(" ");
                if (split.length > 0 && split[0].length() < 10) {
                    builder.setSubtype(split[0]);
                }
            }

            return builder.create();
        } catch (Exception e) {
            throw new AdapterException(e);
        } finally {
            if (folder != null) {
                try {
                    MailUtils.close(folder);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Return the primary text content of the message.
     */
    private static String getText(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            return (String)p.getContent();
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }
}
