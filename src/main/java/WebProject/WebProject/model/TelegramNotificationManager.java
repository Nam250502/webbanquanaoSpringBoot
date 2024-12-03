package WebProject.WebProject.model;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class TelegramNotificationManager {

    private HashMap<Long, TelegramGroup> chatIdMap = new HashMap<>();

    public void addChatId(Long chatId,String name, boolean shouldNotify) {
        TelegramGroup telegramGroup = new TelegramGroup(chatId, name, shouldNotify);
        chatIdMap.put(chatId, telegramGroup);
    }


    public TelegramGroup getByChatId(Long chatId) {
       return chatIdMap.get(chatId);
    }

    public void updateNotificationStatus(Long chatId, boolean shouldNotify) {
        TelegramGroup telegramGroup = chatIdMap.get(chatId);
        if (telegramGroup != null) {
            telegramGroup.setShouldNotify(shouldNotify);
        }
    }

    public List<Long> getChatIdsWithTrueStatus() {
        List<Long> result = new ArrayList<>();
        for (TelegramGroup group : chatIdMap.values()) {
            if (group.isShouldNotify()) {
                result.add(group.getChatId());
            }
        }
        return result;
    }

    public List<TelegramGroup> getAllInfoGroup() {
        List<TelegramGroup> result = new ArrayList<>();
        for (TelegramGroup group : chatIdMap.values()) {
            result.add(group);
        }
        return result;
    }



}
