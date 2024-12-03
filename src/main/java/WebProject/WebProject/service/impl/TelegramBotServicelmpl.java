package WebProject.WebProject.service.impl;


import WebProject.WebProject.config.BotConfig;
import WebProject.WebProject.entity.Order;
import WebProject.WebProject.model.TelegramGroup;
import WebProject.WebProject.model.TelegramNotificationManager;
import WebProject.WebProject.service.OrderService;
import WebProject.WebProject.service.TelegramBotService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Service
public class TelegramBotServicelmpl extends TelegramLongPollingBot implements TelegramBotService {
    private final BotConfig botConfig;
    private final TelegramNotificationManager notificationManager;
    private final OrderService orderService;
    private HashMap<Long, Boolean> adminMap = new HashMap<>();

    public TelegramBotServicelmpl(BotConfig botConfig, TelegramNotificationManager notificationManager, OrderService orderService) {
        this.botConfig = botConfig;
        this.notificationManager = notificationManager;
        this.orderService = orderService;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    // xử lí khi nhận được tin nhắn từ telegram
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            Message message = update.getMessage();
            Chat chat = message.getChat();

            switch (messageText) {
                // group
                case "/add":
                    notificationManager.addChatId(chatId, chat.getTitle(), true);
                    sendMessage(chatId, "Đã thêm vào danh sách thông báo");
                    break;

                // admin
                case "/show":
                        sendAllInfoGroup(chatId);
                    break;
                case "/logout":
                    if (adminMap.containsKey(chatId) && chat.isUserChat()) {
                        adminMap.remove(chatId);
                        sendMessage(chatId, "Đã đăng xuất quyền admin");
                    }
                    break;

            }
            // xử lí khi người dùng ấn thay đổi trạng thái
        } else if (update.hasCallbackQuery()) {
            // Xử lý khi người dùng nhấn vào nút
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData(); // "processed" hoặc "not_processed"
            long chatId = callbackQuery.getMessage().getChatId();
            String name = callbackQuery.getFrom().getFirstName() + " " + callbackQuery.getFrom().getLastName(); // Lấy username của người dùng

            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setChatId(String.valueOf(chatId));
            editMessageReplyMarkup.setMessageId(callbackQuery.getMessage().getMessageId());
            editMessageReplyMarkup.setReplyMarkup(null);

            try {
                execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            // Trả lời dựa trên nút được nhấn
            if (callbackData.startsWith("processed")) {
                String idOrder = callbackData.split("_")[1];
                Order order = orderService.findById(Integer.parseInt(idOrder));
                order.setStatus("success");
                orderService.saveOrder(order);
                // chỉnh sửa tin nhắn
                sendUpdateBshipTourToGroup(order,chatId,callbackQuery);
            }
            if (callbackData.startsWith("toggle_")) {
                String chatIdStr = callbackData.split("_")[1];
                TelegramGroup telegramGroup = notificationManager.getByChatId(Long.parseLong(chatIdStr));
                notificationManager.updateNotificationStatus(Long.parseLong(chatIdStr), !telegramGroup.isShouldNotify());
                sendMessage(chatId, telegramGroup.getGroupName() + " đã thay đổi trạng thái thông báo");
            }
        }
    }

    @Override
    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.enableHtml(true);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendAllInfoGroup(Long chatId) {
        List<TelegramGroup> telegramGroups = notificationManager.getAllInfoGroup();
        for (TelegramGroup telegramGroup : telegramGroups) {
            sendAllInfoGroupWithButtons(chatId, telegramGroup);
        }
    }

    @Override
    public void sendNewOrderBshipTour(String text, Integer idOrder) {
        List<Long> chatIds = notificationManager.getChatIdsWithTrueStatus();
        for (Long chatId : chatIds) {
            sendMessageWithButtons(chatId, text, idOrder);
        }
    }

    public void sendMessageWithButtons(long chatId, String text, Integer idOrder) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.enableHtml(true);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton btn1 = new InlineKeyboardButton();
        btn1.setCallbackData("processed_" + idOrder);
        btn1.setText("Đã tiếp nhận");
        row1.add(btn1);

        keyboard.add(row1);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendAllInfoGroupWithButtons(long chatId, TelegramGroup telegramGroup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(buildMessageInfoGroup(telegramGroup));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton btn1 = new InlineKeyboardButton();
        btn1.setCallbackData("toggle_" + telegramGroup.getChatId());
        btn1.setText(telegramGroup.isShouldNotify() ? "Tắt thông báo" : "Bật thông báo");
        row1.add(btn1);

        keyboard.add(row1);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getInfoNotification(String chatId, boolean status) {
        GetChat getChat = new GetChat();
        getChat.setChatId(String.valueOf(chatId));

        try {
            Chat chat = execute(getChat);
            return chat.toString();
        } catch (TelegramApiException e) {
            return null;
        }
    }

    @Override
    public void sendNewBshipTourToGroup(Order order) {
        String message = buildMessage(order);
        sendNewOrderBshipTour(message, order.getId());
    }

    @Override
    public void sendUpdateBshipTourToGroup(Order order, long chatId, CallbackQuery callbackQuery) {
        String message = buildMessage(order);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageText.setText(buildMessage(order));
        editMessageText.enableHtml(true);
        try {
            execute(editMessageText); // Sửa tin nhắn
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    public String buildMessage(Order order) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Mã đơn: <b>").append(order.getId()).append("</b>\n")
                .append("Tên Khách Hàng: ").append(order.getFullname()).append("\n")
                .append("Số điện thoại: ").append(order.getPhone()).append("\n")
                .append("Địa chỉ: ").append(order.getAddress()).append("\n")
                .append("Số Tiền: ").append(formatToVND(order.getTotal())).append("\n")
                .append("Thời gian: ").append(order.getBooking_Date()).append("\n")
                .append("Trạng thái: <b>").append(order.getStatus()).append("</b>\n");
        String message = messageBuilder.toString();
        return message;
    }

    public String buildMessageInfoGroup(TelegramGroup telegramGroup) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Mã nhóm: ").append(telegramGroup.getChatId()).append("\n")
                .append("Tên nhóm: ").append(telegramGroup.getGroupName()).append("\n")
                .append("Trạng thái thông báo: ").append(telegramGroup.isShouldNotify() ? "đang bật" : "đang tắt").append("\n");
        String message = messageBuilder.toString();
        return message;
    }

    public static String formatToVND(int amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + " VNĐ";
    }

    public static String formatTime(String timeString) {
        LocalDateTime dateTime = LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
    }

}
