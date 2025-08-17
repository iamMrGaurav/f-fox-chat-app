package ai.freightfox.chat.app.controller;

import ai.freightfox.chat.app.dto.response.ApiResponse;
import ai.freightfox.chat.app.model.Message;
import ai.freightfox.chat.app.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatapp/chatrooms/{roomName}/messages")
@Validated
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse> sendMessage(@PathVariable String roomName, @RequestBody Map<String, String> messageRequest) {
        String participant = messageRequest.get("participant");
        String messageText = messageRequest.get("message");

        messageService.saveMessage(roomName, participant, messageText);

        ApiResponse response = new ApiResponse();
        response.setStatus("Success");
        response.setMessage("Message sent successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String roomName, 
            @RequestParam(required = false) @Min(value = 1, message = "Limit must be at least 1") @Max(value = 100, message = "Limit cannot exceed 100") Integer limit,
            @RequestParam(required = false, defaultValue = "0") @Min(value = 0, message = "Offset must be non-negative") Integer offset) {
        List<Message> messages = messageService.getMessages(roomName, limit, offset);
        return ResponseEntity.ok(messages);
    }
}
