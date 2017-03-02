package ru.splat.tm.util;

import ru.splat.messages.conventions.ServicesEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Дмитрий on 25.02.2017.
 */
public class RequestTopicMapper {
    private static Map<ServicesEnum, String> SERVICE_TO_TOPIC_MAP;  //TODO: поменять на BiMap? перенести в отдельный класс helper
    private static Map<String, ServicesEnum> TOPIC_TO_SERVICE_MAP;
    static {
        SERVICE_TO_TOPIC_MAP = new HashMap<>();
        SERVICE_TO_TOPIC_MAP.put(ServicesEnum.BetService, "BetReq");
        SERVICE_TO_TOPIC_MAP.put(ServicesEnum.EventService, "EventReq");
        SERVICE_TO_TOPIC_MAP.put(ServicesEnum.BillingService, "BillingReq");
        SERVICE_TO_TOPIC_MAP.put(ServicesEnum.PunterService, "PunterReq");
        TOPIC_TO_SERVICE_MAP = new HashMap<>();
        TOPIC_TO_SERVICE_MAP.put("BetReq", ServicesEnum.BetService);
        TOPIC_TO_SERVICE_MAP.put("EventReq", ServicesEnum.EventService);
        TOPIC_TO_SERVICE_MAP.put("BillingReq", ServicesEnum.BillingService);
        TOPIC_TO_SERVICE_MAP.put("PunterReq", ServicesEnum.PunterService);
    }

    public static String getTopic(ServicesEnum servicesEnum) {
        return SERVICE_TO_TOPIC_MAP.get(servicesEnum);
    }

    public static ServicesEnum getService(String topic) {
        return TOPIC_TO_SERVICE_MAP.get(topic);
    }
}
