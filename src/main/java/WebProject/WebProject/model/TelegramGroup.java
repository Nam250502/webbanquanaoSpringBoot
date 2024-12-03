package WebProject.WebProject.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TelegramGroup {
    private long chatId;
    private String groupName;
    private boolean shouldNotify;
}
