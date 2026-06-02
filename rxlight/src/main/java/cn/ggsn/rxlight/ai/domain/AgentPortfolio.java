package cn.ggsn.rxlight.ai.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;


/**
 * The agent portfolio table
 */
@Entity
@Table(name = "agent_portfolio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class AgentPortfolio extends PanacheEntityBase {

    /**
     * The model of the agent
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Model {
        private Short vendor;
        private String modelName;
    }

    /**
     * The RAG configuration for the agent
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RagConfig {
        private Boolean enable;
        private Integer recallTopK;
        private Integer rerankTopK;
        private Float recallThreshold;
        private Float rerankThreshold;
    }

    /**
     * The deep think configuration for the agent
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeepThink {
        private Boolean enable;
        private Integer budget;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The unique name of the agent
     */
    @Column(name = "unique_name", nullable = false, length = 50)
    private String uniqueName;

    /**
     * The system prompt for the agent
     */
    @Column(name = "system_prompt", nullable = false, columnDefinition = "TEXT")
    private String systemPrompt;

    /**
     * The static tools for the agent
     */
    @Column(name = "static_tools", columnDefinition = "TEXT")
    private String staticTools;

    /**
     * The temperature for the agent
     */
    @Column(name = "temperature", nullable = false)
    private Float temperature;

    /**
     * The max tokens for the agent
     */
    @Column(name = "max_tokens", nullable = false)
    private Integer maxTokens;

    /**
     * The tools for the agent
     */
    @Column(name = "tools", columnDefinition = "TEXT[]")
    private String[] tools;

    /**
     * supported scenarios for the agent
     */
    @Column(name = "scenario", nullable = false)
    private Short scenario;

    /**
     * The sorry message for the agent
     */
    @Column(name = "sorry_message", columnDefinition = "TEXT")
    private String sorryMessage;

    /**
     * The parent id of the agent
     */
    @Column(name = "parent_id")
    private Integer parentId;

    /**
     * The model of the agent
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "vendor", column = @Column(name = "agent_model_vendor")),
            @AttributeOverride(name = "modelName", column = @Column(name = "agent_model_name", length = 50))
    })
    private Model agentModel;

    /**
     * The RAG configuration for the agent
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "enable", column = @Column(name = "rag_enable")),
            @AttributeOverride(name = "recallTopK", column = @Column(name = "rag_recall_top_k")),
            @AttributeOverride(name = "rerankTopK", column = @Column(name = "rag_rerank_top_k")),
            @AttributeOverride(name = "recallThreshold", column = @Column(name = "rag_recall_threshold")),
            @AttributeOverride(name = "rerankThreshold", column = @Column(name = "rag_rerank_threshold"))
    })
    private RagConfig rag;

    /**
     * The deep think configuration for the agent
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "enable", column = @Column(name = "deepthink_enable")),
            @AttributeOverride(name = "budget", column = @Column(name = "deepthink_budget"))
    })
    private DeepThink deepThink;

    /**
     * The streaming output switch for the agent, if true, streaming output is
     * enabled
     */
    @Column(name = "streaming_out")
    private Boolean streamingOut;

    /**
     * The creation date of the agent
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * The last update date of the agent
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Optional<AgentPortfolio> getByUniqueName(String string) {
        return Optional.ofNullable(find("uniqueName", string).firstResult());
    }
}
