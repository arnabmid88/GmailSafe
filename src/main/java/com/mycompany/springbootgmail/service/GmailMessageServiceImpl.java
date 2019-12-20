package com.mycompany.springbootgmail.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GmailMessageServiceImpl implements GmailMessageService {

    private static final String USER_ID = "me";

    private final Gmail gmail;

    public GmailMessageServiceImpl(Gmail gmail) {
        this.gmail = gmail;
    }

    @Override
    public Message getMessage(String messageId) throws IOException {
        return gmail.users().messages().get(USER_ID, messageId).execute();
    }

    @Override
    public List<Message> getMessages(String query, List<String> labelIds) throws IOException {
        ListMessagesResponse response = gmail.users().messages().list(USER_ID)
                .setQ(query).setLabelIds(labelIds).setMaxResults(30L).execute();

        List<Message> messages = new ArrayList<>();
        if (response.getMessages() != null) {
            messages.addAll(response.getMessages());
        }

        return messages;
    }

    public static void main(String[] args) {
		System.out.println(File.pathSeparator);
	}

}
