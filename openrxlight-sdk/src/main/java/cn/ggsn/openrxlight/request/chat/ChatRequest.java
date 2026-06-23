package cn.ggsn.openrxlight.request.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Lists;

import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@EqualsAndHashCode(of = { "messageId", "userId", "scenario" })
public class ChatRequest implements Validate, Cloneable {
    private String query;
    private List<Message> messages;
    @Required
    private String userId;
    @Required
    private String messageId;
    private String contextId;
    @Required
    private Integer scenario; // ONLY FOR internal use, DO NOT USE THIS FIELD IN YOUR REQUEST!!!
    @Required
    private String appId;
    @Required
    private String messageType;
    private List<Integer> specialists; // ONLY FOR internal use, DO NOT USE THIS FIELD IN YOUR REQUEST!!!
    private String location; // latitude and longitude in "longitude,latitude" format (WGS84), e.g.,
                             // "37.7749,-122.4194", required if your app needs location info;
    private String i18n; // when you set i18n (ISO 639-1), the locale will be forcely configured same as
                         // this fields
    private Set<Callback> callbacks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Message implements Validate, Cloneable {
        @Required
        private String role;
        @Required
        private String content;

        @Override
        public Message clone() {
            return new Message(this.role, this.content);
        }
    }

    @Override
    public void validate() {
        Validate.super.validate();
        if (UserMessageType.fromName(StringUtils.trim(this.messageType)) == null) {
            var valideTypes = Lists.newArrayList();
            for (UserMessageType type : UserMessageType.values()) {
                valideTypes.add(type.getName());
            }
            throw new IllegalArgumentException(
                    String.format("messageType must not be one of: [%s]",
                            StringUtils.join(valideTypes, ",")));
        }

        if (UserMessageType.CALLBACK.getName().equals(this.messageType)
                && (this.callbacks == null || this.callbacks.isEmpty())) {
            throw new IllegalArgumentException("callbacks are required for CALLBACK message type");
        } else if (!UserMessageType.CALLBACK.getName().equals(this.messageType) && StringUtils.isBlank(this.query)) {
            throw new IllegalArgumentException("query is required");
        }

        if (this.callbacks != null) {
            this.callbacks.stream().forEach(Callback::validate);
        }

        if (this.messages != null) {
            for (Message message : this.messages) {
                message.validate();
            }
        }

        if (this.specialists != null && !this.specialists.isEmpty()) {
            throw new IllegalArgumentException(
                    "specialists field is for internal use only and must not be set in the request");
        }

        if (StringUtils.isNotBlank(this.i18n)) {
            String lang = this.i18n.trim().toLowerCase();
            if (lang.length() == 2) {
                // ISO 639-1: must be a recognized 2-letter code
                if (!isISO639_1(lang)) {
                    throw new IllegalArgumentException(
                            "i18n is not a valid ISO 639-1 language code: " + this.i18n);
                }
                this.i18n = lang;
            } else if (lang.length() == 3) {
                // ISO 639-2: convert to ISO 639-1 if possible
                String iso2 = toISO639_1(lang);
                if (iso2 == null) {
                    throw new IllegalArgumentException(
                            "i18n is an ISO 639-2 code with no ISO 639-1 equivalent: " + this.i18n);
                }
                this.i18n = iso2;
            } else {
                throw new IllegalArgumentException(
                        "i18n must be an ISO 639-1 (2-letter) or ISO 639-2 (3-letter) language code, got: "
                                + this.i18n);
            }
        }

        if (StringUtils.isNotBlank(this.location)) {
            if (StringUtils.isBlank(StringUtils.trim(this.location))) {
                throw new IllegalArgumentException("location is required for scenario 1");
            }

            String[] parts = StringUtils.split(this.location, ",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("location must be in format 'longitude,latitude'");
            }

            try {
                var longitude = Double.parseDouble(parts[0]);
                var latitude = Double.parseDouble(parts[1]);

                if (longitude < -180 || longitude > 180) {
                    throw new IllegalArgumentException(
                            "longitude must be between -180 and 180, got: " + longitude);
                }
                if (latitude < -90 || latitude > 90) {
                    throw new IllegalArgumentException(
                            "latitude must be between -90 and 90, got: " + latitude);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("longitude and latitude must be valid numbers");
            }
        }
    }

    private static final Set<String> ISO_639_1_CODES = new HashSet<>(Arrays.asList(Locale.getISOLanguages()));

    private static boolean isISO639_1(String code) {
        return ISO_639_1_CODES.contains(code);
    }

    private static String toISO639_1(String iso3) {
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                if (iso3.equals(locale.getISO3Language())) {
                    String lang = locale.getLanguage();
                    if (!lang.isEmpty() && isISO639_1(lang)) {
                        return lang;
                    }
                }
            } catch (MissingResourceException e) {
                // some locales have no ISO 3-letter code; skip them
            }
        }
        return null;
    }

    @Override
    public ChatRequest clone() {
        ChatRequest cloned = new ChatRequest();
        cloned.query = this.query;
        cloned.messages = this.messages != null
                ? this.messages.stream().map(Message::clone).collect(Collectors.toList())
                : null;
        cloned.userId = this.userId;
        cloned.messageId = this.messageId;
        cloned.contextId = this.contextId;
        cloned.scenario = this.scenario;
        cloned.appId = this.appId;
        cloned.messageType = this.messageType;
        cloned.specialists = this.specialists != null ? new ArrayList<>(this.specialists) : null;
        cloned.location = this.location;
        cloned.i18n = this.i18n;
        cloned.callbacks = this.callbacks != null
                ? this.callbacks.stream().map(Callback::clone).collect(Collectors.toSet())
                : null;
        return cloned;
    }
}
