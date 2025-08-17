package ai.freightfox.chat.app.controller;


import ai.freightfox.chat.app.dto.response.ApiResponse;
import ai.freightfox.chat.app.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chatapp/chatrooms/")
public class ChatRoomController {


    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ApiResponse> createChatRoom(@RequestBody Map<String, String> roomRequest){
        String roomName = roomRequest.get("roomName");

        chatRoomService.createValidatedChatRoom(roomName);

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setMessage("Room Created Successfully");
        apiResponse.setStatus("Success");

        return ResponseEntity.ok(apiResponse);
    }

}
