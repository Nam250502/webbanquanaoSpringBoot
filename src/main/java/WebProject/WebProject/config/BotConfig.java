package WebProject.WebProject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    @Value("${bot.name}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotToken() {
        return botToken;
    }


    //  cấu hiình khởi tạo bot
    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        if (botUsername == null || botToken == null || botUsername.isEmpty()|| botToken.isEmpty()) {
            return null;
        }
        return new TelegramBotsApi(DefaultBotSession.class);
    }
}
