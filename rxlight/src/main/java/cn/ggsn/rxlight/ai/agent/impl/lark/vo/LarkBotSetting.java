package cn.ggsn.rxlight.ai.agent.impl.lark.vo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LarkBotSetting {
    private Boolean accountSynced;

    public static LarkBotSetting load(Path rootPath) throws FileNotFoundException {
        return JsonUtils.fromJson(new FileInputStream(rootPath.resolve("settings.json").toFile()),
                LarkBotSetting.class);
    }

    public void write(Path rootPath) throws FileNotFoundException {
        try (FileOutputStream fos = new FileOutputStream(rootPath.resolve("settings.json").toFile())) {
            fos.write(JsonUtils.toJson(this).getBytes());
        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
