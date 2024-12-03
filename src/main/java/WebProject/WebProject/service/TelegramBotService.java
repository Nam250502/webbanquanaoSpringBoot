package WebProject.WebProject.service;

import WebProject.WebProject.entity.Order;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramBotService {

    void onUpdateReceived(Update update);

    void sendMessage(long chatId, String text);

    void sendAllInfoGroup(Long chatId);

    void sendNewOrderBshipTour(String text, Integer idOrder);

    void sendNewBshipTourToGroup(Order order);

    void sendUpdateBshipTourToGroup(Order order, long chatId, CallbackQuery callbackQuery);
}
