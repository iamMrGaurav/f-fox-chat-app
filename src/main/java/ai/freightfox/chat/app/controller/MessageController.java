package ai.freightfox.chat.app.controller;

import ai.freightfox.chat.app.dto.request.SendMessageRequest;
import ai.freightfox.chat.app.dto.response.ApiResponse;
import ai.freightfox.chat.app.model.MessageModel;
import ai.freightfox.chat.app.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

@RestController
@RequestMapping("/api/chatapp/chatrooms/{roomId}/messages")
@Validated
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Operation(summary = "Send Message to Chat Room")
    @PostMapping
    public ResponseEntity<ApiResponse> sendMessage(@PathVariable String roomId, @RequestBody SendMessageRequest messageRequest) {
        messageService.saveMessage(roomId, messageRequest.getParticipant(), messageRequest.getMessage());

        ApiResponse response = new ApiResponse();
        response.setStatus("Success");
        response.setMessage("Message sent successfully");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Chat History")
    @GetMapping
    public ResponseEntity<List<MessageModel>> getMessages(
            @PathVariable String roomId,
            @RequestParam(required = false) @Min(value = 1, message = "Limit must be at least 1") @Max(value = 100, message = "Limit cannot exceed 100") Integer limit,
            @RequestParam(required = false, defaultValue = "0") @Min(value = 0, message = "Offset must be non-negative") Integer offset) {
        List<MessageModel> messageModels = messageService.getMessages(roomId, limit, offset);
        return ResponseEntity.ok(messageModels);
    }
}
