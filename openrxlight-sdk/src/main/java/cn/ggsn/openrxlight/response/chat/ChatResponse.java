package cn.ggsn.openrxlight.response.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private String id;
    private String object;
    private Long created;
    private String contextId;
    private List<Choice> choices;
    private List<Attachment> attachments;
    private Integer scenario;
    private Usage usage;
    private Set<Callback> callbacks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Choice {
        private Integer index;
        private Delta delta;
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Delta {
        private String role;
        private JsonNode content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Usage {
        @JsonIgnore
        private Integer promptTokens; // ONLY FOR INTERNAL CALCULATION
        @JsonIgnore
        private Integer totalTokens; // ONLY FOR INTERNAL CALCULATION
        private BigDecimal totalCredit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Slf4j
    public static class Attachment {
        private String fileId;
        private String filename;
        @JsonIgnore
        private File file;

        public File downloadToFs(Path fs) {
            // if file already exists, return it directly
            if (this.file != null) {
                return this.file;
            }
            try {
                var stream = download();
                var file = fs.resolve(filename).toFile();
                file.setWritable(true);
                try (OutputStream os = new FileOutputStream(file)) {
                    stream.transferTo(os);
                    this.file = file;
                    return this.file;
                } catch (IOException e) {
                    log.error("Failed to save attachment to file system for file: {}", filename,
                            e);
                    throw new RuntimeException(
                            String.format("Failed to save attachment to file system for file: %s",
                                    filename),
                            e);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to download file from " + fileId, e);
            }
        }

        private InputStream download() {
            try (okhttp3.Response response = new okhttp3.OkHttpClient().newCall(
                    new okhttp3.Request.Builder()
                            .url(fileId)
                            .get()
                            .build())
                    .execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Failed to download file: " + response.message());
                }

                return response.body().byteStream();
            } catch (Exception e) {
                throw new RuntimeException("Failed to download file from " + fileId, e);
            }
        }
    }

    public void merge(ChatResponse other) {
        if (other == null) {
            return;
        }

        this.id = other.id != null ? other.id : this.id;
        this.object = other.object != null ? other.object : this.object;
        this.created = other.created != null ? other.created : this.created;
        this.contextId = other.contextId != null ? other.contextId : this.contextId;
        this.scenario = other.scenario != null ? other.scenario : this.scenario;
        if (other.usage != null) {
            if (this.usage == null) {
                this.usage = other.usage;
            } else {
                this.usage.setTotalCredit(other.usage.getTotalCredit() != null
                        ? other.usage.getTotalCredit().add(this.usage.getTotalCredit())
                        : this.usage.getTotalCredit());
            }
        }
        if (other.getChoices() != null && !other.getChoices().isEmpty()) {
            if (this.choices == null) {
                this.choices = other.getChoices();
            } else {
                this.choices.sort((c1, c2) -> c1.getIndex().compareTo(c2.getIndex()));
                Integer lastIndex = this.choices.get(this.choices.size() - 1).getIndex();
                for (Choice choice : other.getChoices()) {
                    lastIndex++;
                    choice.setIndex(lastIndex);
                }
                this.choices.addAll(other.getChoices());
            }
        }
        if (other.getAttachments() != null && !other.getAttachments().isEmpty()) {
            if (this.attachments == null) {
                this.attachments = other.getAttachments();
            } else {
                this.attachments.addAll(other.getAttachments());
            }
        }

        // if (other.getCallback() != null) {
        // if (this.callback == null) {
        // this.callback = other.getCallback();
        // } else {
        // this.callback.merge(other.getCallback());
        // }
        // }

        if (other.getCallbacks() != null) {
            if (this.callbacks == null) {
                this.callbacks = other.getCallbacks();
            } else {
                var mapping = other.callbacks.stream()
                        .collect(Collectors.toMap(
                                callback -> StringUtils
                                        .join(new String[] { callback.getCallbackId(), callback.getType() }, ":"),
                                callback -> callback));
                this.callbacks.forEach(callback -> {
                    var key = StringUtils
                            .join(new String[] { callback.getCallbackId(), callback.getType() }, ":");
                    var mappedCallback = mapping.remove(key);
                    callback.merge(mappedCallback);
                });

                mapping.values().forEach(callback -> this.callbacks.add(callback));
            }
        }
    }

    public boolean isEmpty() {
        return (this.choices == null || this.choices.isEmpty())
                && (this.attachments == null || this.attachments.isEmpty())
                && (this.callbacks == null || this.callbacks.isEmpty());
    }

    @JsonIgnore
    public JsonNode getContent() {
        if (this.choices == null || this.choices.isEmpty()) {
            return null;
        }

        this.choices.sort((c1, c2) -> c1.getIndex().compareTo(c2.getIndex()));

        return this.choices.stream()
                .filter(choice -> choice.getDelta() != null && choice.getDelta().getContent() != null)
                .map(choice -> choice.getDelta().getContent())
                .reduce(JsonNodeFactory.instance.nullNode(), (origin, newCome) -> {
                    return JsonUtils.mergeInto(origin, newCome);
                });

    }

    public boolean isStopped() {
        if ((this.choices == null || this.choices.isEmpty())
                && (this.callbacks == null || this.callbacks.isEmpty())) {
            return true;
        }

        return this.choices.stream().anyMatch(choice -> "stop".equals(choice.getFinishReason()));
    }
}
