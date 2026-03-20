package com.vibify.notification.client;

import java.util.Map;

public interface PushClient {

    void send(String token, Map<String, Object> payload);

}
