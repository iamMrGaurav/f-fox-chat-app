package ai.freightfox.chat.app.controller;


import ai.freightfox.chat.app.dto.response.ApiResponse;
import ai.freightfox.chat.app.dto.response.ChatRoomCreateResponse;
import ai.freightfox.chat.app.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@RestController
@RequestMapping("/api/chatapp/chatrooms/")
@Validated
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoomCreateResponse> createChatRoom(@RequestBody Map<String, String> roomRequest){
        String roomName = roomRequest.get("roomName");

        chatRoomService.createValidatedChatRoom(roomName);

        ChatRoomCreateResponse response = new ChatRoomCreateResponse(roomName);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "{roomId}/join")
    public ResponseEntity<ApiResponse> joinChatRoom(@RequestBody Map<String , String> joinRoomRequest, @PathVariable String roomId){
        String participantName = joinRoomRequest.get("participant");

        chatRoomService.joinChatRoom(roomId, participantName);

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setMessage("User " + participantName + " Join " + roomId +" Room Successfully");
        apiResponse.setStatus("Success");

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping(value = "{roomId}")
    public ResponseEntity<ApiResponse> removeChatRoom(
            @PathVariable @NotBlank(message = "Room ID cannot be empty") String roomId){

        chatRoomService.removeRoom(roomId);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Room '" + roomId + "' deleted successfully");
        apiResponse.setStatus("Success");

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteWithoutRoomId(){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Room ID is required for deletion");
        apiResponse.setStatus("Error");

        return ResponseEntity.badRequest().body(apiResponse);
    }
}
