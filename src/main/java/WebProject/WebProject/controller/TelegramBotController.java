package WebProject.WebProject.controller;

import WebProject.WebProject.service.TelegramBotService;
import WebProject.WebProject.service.impl.TelegramBotServicelmpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Controller
public class TelegramBotController {

    @Autowired
    TelegramBotServicelmpl telegramBotServicelmpl;

    @Autowired
    TelegramBotService telegramBotService;

    @Autowired(required = false)
    TelegramBotsApi telegramBotsApi;

    // đăng kí sự kiện tin nhắn với telegram
    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        if (telegramBotsApi == null) {
            return;
        }
        telegramBotsApi.registerBot(telegramBotServicelmpl);
    }
}
