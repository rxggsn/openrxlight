package cn.ggsn.rxlight.ai.agent;

import java.util.Optional;

import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.llm.openai.OpenAI;
import cn.ggsn.rxlight.ai.domain.AgentPortfolio;
import cn.ggsn.rxlight.ai.error.AiError;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class ChatBoxFactory {

    @ConfigProperty(name = "rxlight.llm.api-key")
    private String apiKey;

    @ConfigProperty(name = "rxlight.llm.base-url")
    private String baseUrl;

    @Produces
    public Chatbox chatbox() {
        return AgentPortfolio.getByUniqueName("chat_bot")
                .map(portfolio -> {
                    return new Chatbox(portfolio.getSystemPrompt(), portfolio.getMaxTokens(),
                            portfolio.getTemperature().doubleValue(),
                            0.7,
                            portfolio.getStreamingOut(),
                            Optional.ofNullable(portfolio.getDeepThink()).map(AgentPortfolio.DeepThink::getEnable)
                                    .orElse(false),
                            new OpenAI(apiKey, baseUrl), portfolio.getDeepThink().getBudget(),
                            false, portfolio.getAgentModel().getModelName());
                })
                .orElseThrow(() -> new BizException(AiError.AgentPortfolioNotFound));
    }
}
