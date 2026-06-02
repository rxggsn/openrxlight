package cn.ggsn.openrxlight.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = { "method", "path" })
public class Uri {
    private String method;
    private String path;

    public boolean match(Uri other) {
        try {
            Pattern pattern = Pattern.compile(this.path);
            return this.method.equals(other.method) && pattern.matcher(other.path).find();
        } catch (PatternSyntaxException e) {
            return this.method.equals(other.method) && this.path.equals(other.path);
        }
    }

    public void addPath(String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        if (StringUtils.isBlank(this.path)) {
            if (!value.startsWith("/")) {
                this.path = "/" + value;
            } else {
                this.path = value;
            }
            return;
        }
        if (this.path.endsWith("/") && value.startsWith("/")) {
            this.path = this.path + value.substring(1);
        } else if (!this.path.endsWith("/") && !value.startsWith("/")) {
            this.path = this.path + "/" + value;
        } else {
            this.path = this.path + value;
        }
    }
}
