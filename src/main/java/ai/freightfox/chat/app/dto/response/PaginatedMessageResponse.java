package ai.freightfox.chat.app.dto.response;

import ai.freightfox.chat.app.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedMessageResponse {
    private List<Message> messages;
    private PaginationInfo pagination;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int currentPage;
        private int pageSize;
        private long totalMessages;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
        private Integer nextOffset;
        private Integer previousOffset;
    }
}