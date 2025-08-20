package ai.freightfox.chat.app.service;

import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.BadRequestException;
import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.ChatRoomNotFoundException;
import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.ParticipantNotFoundException;
import ai.freightfox.chat.app.model.MessageModel;
import ai.freightfox.chat.app.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessagePublisherService messagePublisher;

    @InjectMocks
    private MessageService messageService;

    private MessageModel sampleMessage;

    @BeforeEach
    void setUp() {
        sampleMessage = new MessageModel("john", "Hello everyone!");
    }

    // Test successful message saving with valid parameters
    @Test
    void saveMessage_WithValidParameters_SavesSuccessfully() throws JsonProcessingException {
        String roomName = "general";
        String expectedJson = "{\"participant\":\"john\",\"message\":\"Hello everyone!\"}";

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(chatRoomService.isParticipantInRoom(roomName, "john")).thenReturn(true);
        when(objectMapper.writeValueAsString(any(MessageModel.class))).thenReturn(expectedJson);

        assertDoesNotThrow(() -> messageService.saveMessage(roomName, sampleMessage));

        verify(chatRoomService, times(1)).isRoomExists(roomName);
        verify(chatRoomService, times(1)).isParticipantInRoom(roomName, "john");
        verify(objectMapper, times(1)).writeValueAsString(any(MessageModel.class));
        verify(messageRepository, times(1)).saveChat(any(), eq(expectedJson));
        verify(messagePublisher, times(1)).publishMessage(eq(roomName), any(MessageModel.class));
    }

    // Test message saving sets timestamp when null
    @Test
    void saveMessage_WithNullTimestamp_SetsCurrentTimestamp() throws JsonProcessingException {
        String roomName = "general";
        MessageModel messageWithoutTimestamp = new MessageModel("john", "Hello!");
        messageWithoutTimestamp.setTimestamp(null);

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(chatRoomService.isParticipantInRoom(roomName, "john")).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        messageService.saveMessage(roomName, messageWithoutTimestamp);

        assertNotNull(messageWithoutTimestamp.getTimestamp());
        verify(messageRepository, times(1)).saveChat(any(), any());
    }

    // Test message saving fails when room does not exist
    @Test
    void saveMessage_WithNonExistingRoom_ThrowsChatRoomNotFoundException() {
        String roomName = "non-existing";

        when(chatRoomService.isRoomExists(roomName)).thenReturn(false);

        ChatRoomNotFoundException exception = assertThrows(ChatRoomNotFoundException.class,
            () -> messageService.saveMessage(roomName, sampleMessage));

        assertEquals("Chat room 'non-existing' does not exist", exception.getMessage());
        verify(chatRoomService, times(1)).isRoomExists(roomName);
        verify(messageRepository, never()).saveChat(any(), any());
        verify(messagePublisher, never()).publishMessage(any(), any());
    }

    // Test message saving fails with null message
    @Test
    void saveMessage_WithNullMessage_ThrowsBadRequestException() {
        String roomName = "general";

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> messageService.saveMessage(roomName, null));

        assertEquals("Message cannot be null", exception.getMessage());
        verify(messageRepository, never()).saveChat(any(), any());
    }

    // Test message saving fails with empty participant name
    @Test
    void saveMessage_WithEmptyParticipant_ThrowsBadRequestException() {
        String roomName = "general";
        MessageModel messageWithEmptyParticipant = new MessageModel("", "Hello!");

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> messageService.saveMessage(roomName, messageWithEmptyParticipant));

        assertEquals("Participant name cannot be empty", exception.getMessage());
        verify(messageRepository, never()).saveChat(any(), any());
    }

    // Test message saving fails with empty message content
    @Test
    void saveMessage_WithEmptyMessageContent_ThrowsBadRequestException() {
        String roomName = "general";
        MessageModel messageWithEmptyContent = new MessageModel("john", "");

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> messageService.saveMessage(roomName, messageWithEmptyContent));

        assertEquals("Message content cannot be empty", exception.getMessage());
        verify(messageRepository, never()).saveChat(any(), any());
    }

    // Test message saving fails when participant not in room
    @Test
    void saveMessage_WithParticipantNotInRoom_ThrowsParticipantNotFoundException() {
        String roomName = "general";

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(chatRoomService.isParticipantInRoom(roomName, "john")).thenReturn(false);

        ParticipantNotFoundException exception = assertThrows(ParticipantNotFoundException.class,
            () -> messageService.saveMessage(roomName, sampleMessage));

        assertEquals("Participant 'john' is not a member of room 'general'", exception.getMessage());
        verify(messageRepository, never()).saveChat(any(), any());
    }

    // Test message saving handles JSON processing exception
    @Test
    void saveMessage_WithJsonProcessingError_ThrowsRuntimeException() throws JsonProcessingException {
        String roomName = "general";

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(chatRoomService.isParticipantInRoom(roomName, "john")).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> messageService.saveMessage(roomName, sampleMessage));

        assertTrue(exception.getMessage().contains("Failed to save message"));
        verify(messageRepository, never()).saveChat(any(), any());
        verify(messagePublisher, never()).publishMessage(any(), any());
    }

    // Test saving message with string parameters
    @Test
    void saveMessage_WithStringParameters_SavesSuccessfully() throws JsonProcessingException {
        String roomName = "general";
        String participant = "jane";
        String messageText = "Hello world!";

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(chatRoomService.isParticipantInRoom(roomName, participant)).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        assertDoesNotThrow(() -> messageService.saveMessage(roomName, participant, messageText));

        verify(messageRepository, times(1)).saveChat(any(), any());
        verify(messagePublisher, times(1)).publishMessage(eq(roomName), any(MessageModel.class));
    }

    // Test getting last N messages from existing room
    @Test
    void getLastNMessages_WithValidParameters_ReturnsMessages() throws Exception {
        String roomName = "general";
        int limit = 5;
        List<Object> mockJsonList = Arrays.asList(
            "{\"participant\":\"john\",\"message\":\"Hello\"}",
            "{\"participant\":\"jane\",\"message\":\"Hi there\"}"
        );

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(messageRepository.getLastNMessages(any(), eq(limit))).thenReturn(mockJsonList);
        when(objectMapper.readValue(anyString(), eq(MessageModel.class)))
            .thenReturn(new MessageModel("john", "Hello"))
            .thenReturn(new MessageModel("jane", "Hi there"));

        List<MessageModel> result = messageService.getLastNMessages(roomName, limit);

        assertEquals(2, result.size());
        assertEquals("john", result.get(0).getParticipant());
        assertEquals("Hello", result.get(0).getMessage());
        verify(chatRoomService, times(1)).isRoomExists(roomName);
        verify(messageRepository, times(1)).getLastNMessages(any(), eq(limit));
    }

    // Test getting last N messages from non-existing room
    @Test
    void getLastNMessages_WithNonExistingRoom_ThrowsChatRoomNotFoundException() {
        String roomName = "non-existing";
        int limit = 5;

        when(chatRoomService.isRoomExists(roomName)).thenReturn(false);

        ChatRoomNotFoundException exception = assertThrows(ChatRoomNotFoundException.class,
            () -> messageService.getLastNMessages(roomName, limit));

        assertEquals("Chat room 'non-existing' does not exist", exception.getMessage());
        verify(messageRepository, never()).getLastNMessages(any(), anyInt());
    }

    // Test getting last N messages with invalid limit
    @Test
    void getLastNMessages_WithInvalidLimit_ThrowsBadRequestException() {
        String roomName = "general";
        int limit = 0;

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> messageService.getLastNMessages(roomName, limit));

        assertEquals("Limit must be greater than 0", exception.getMessage());
        verify(messageRepository, never()).getLastNMessages(any(), anyInt());
    }

    // Test getting all messages from existing room
    @Test
    void getAllMessages_WithExistingRoom_ReturnsAllMessages() throws Exception {
        String roomName = "general";
        List<Object> mockJsonList = Arrays.asList(
            "{\"participant\":\"john\",\"message\":\"Hello\"}",
            "{\"participant\":\"jane\",\"message\":\"Hi\"}"
        );

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(messageRepository.getAllMessages(any())).thenReturn(mockJsonList);
        when(objectMapper.readValue(anyString(), eq(MessageModel.class)))
            .thenReturn(new MessageModel("john", "Hello"))
            .thenReturn(new MessageModel("jane", "Hi"));

        List<MessageModel> result = messageService.getAllMessages(roomName);

        assertEquals(2, result.size());
        verify(chatRoomService, times(1)).isRoomExists(roomName);
        verify(messageRepository, times(1)).getAllMessages(any());
    }

    // Test getting messages with limit parameter
    @Test
    void getMessages_WithLimit_ReturnsLimitedMessages() throws Exception {
        String roomName = "general";
        Integer limit = 3;
        List<Object> mockJsonList = Arrays.asList("{\"participant\":\"john\",\"message\":\"Hello\"}");

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(messageRepository.getLastNMessages(any(), eq(limit))).thenReturn(mockJsonList);
        when(objectMapper.readValue(anyString(), eq(MessageModel.class)))
            .thenReturn(new MessageModel("john", "Hello"));

        List<MessageModel> result = messageService.getMessages(roomName, limit);

        assertEquals(1, result.size());
        verify(messageRepository, times(1)).getLastNMessages(any(), eq(limit));
        verify(messageRepository, never()).getAllMessages(any());
    }

    // Test getting messages without limit returns all messages
    @Test
    void getMessages_WithoutLimit_ReturnsAllMessages() throws Exception {
        String roomName = "general";
        List<Object> mockJsonList = Arrays.asList("{\"participant\":\"john\",\"message\":\"Hello\"}");

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(messageRepository.getAllMessages(any())).thenReturn(mockJsonList);
        when(objectMapper.readValue(anyString(), eq(MessageModel.class)))
            .thenReturn(new MessageModel("john", "Hello"));

        List<MessageModel> result = messageService.getMessages(roomName, null);

        assertEquals(1, result.size());
        verify(messageRepository, times(1)).getAllMessages(any());
        verify(messageRepository, never()).getLastNMessages(any(), anyInt());
    }

    // Test getting messages with pagination
    @Test
    void getMessages_WithLimitAndOffset_ReturnsMessages() throws Exception {
        String roomName = "general";
        Integer limit = 2;
        Integer offset = 1;
        List<Object> mockJsonList = Arrays.asList("{\"participant\":\"john\",\"message\":\"Hello\"}");

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(messageRepository.getMessagesWithPagination(any(), eq(limit), eq(offset)))
            .thenReturn(mockJsonList);
        when(objectMapper.readValue(anyString(), eq(MessageModel.class)))
            .thenReturn(new MessageModel("john", "Hello"));

        List<MessageModel> result = messageService.getMessages(roomName, limit, offset);

        assertEquals(1, result.size());
        verify(messageRepository, times(1)).getMessagesWithPagination(any(), eq(limit), eq(offset));
    }

    // Test getting messages with pagination using direct method
    @Test
    void getMessagesWithPagination_ReturnsCorrectMessages() throws Exception {
        String roomName = "general";
        int limit = 2;
        int offset = 1;
        List<Object> mockJsonList = Arrays.asList("{\"participant\":\"john\",\"message\":\"Hello\"}");

        when(messageRepository.getMessagesWithPagination(any(), eq(limit), eq(offset)))
            .thenReturn(mockJsonList);
        when(objectMapper.readValue(anyString(), eq(MessageModel.class)))
            .thenReturn(new MessageModel("john", "Hello"));

        List<MessageModel> result = messageService.getMessagesWithPagination(roomName, limit, offset);

        assertEquals(1, result.size());
        verify(messageRepository, times(1)).getMessagesWithPagination(any(), eq(limit), eq(offset));
    }

    // Test getting total message count from existing room
    @Test
    void getTotalMessageCount_WithExistingRoom_ReturnsCount() {
        String roomName = "general";
        long expectedCount = 25L;

        when(chatRoomService.isRoomExists(roomName)).thenReturn(true);
        when(messageRepository.getMessageCount(any())).thenReturn(expectedCount);

        long result = messageService.getTotalMessageCount(roomName);

        assertEquals(expectedCount, result);
        verify(chatRoomService, times(1)).isRoomExists(roomName);
        verify(messageRepository, times(1)).getMessageCount(any());
    }

    // Test getting total message count from non-existing room
    @Test
    void getTotalMessageCount_WithNonExistingRoom_ThrowsChatRoomNotFoundException() {
        String roomName = "non-existing";

        when(chatRoomService.isRoomExists(roomName)).thenReturn(false);

        ChatRoomNotFoundException exception = assertThrows(ChatRoomNotFoundException.class,
            () -> messageService.getTotalMessageCount(roomName));

        assertEquals("Chat room 'non-existing' does not exist", exception.getMessage());
        verify(messageRepository, never()).getMessageCount(any());
    }

    // Test JSON conversion handles malformed messages gracefully
    @Test
    void convertJsonListToMessages_WithMalformedJson_SkipsInvalidMessages() throws Exception {
        List<Object> mockJsonList = Arrays.asList(
            "{\"participant\":\"john\",\"message\":\"Hello\"}",
            "invalid-json",
            "{\"participant\":\"jane\",\"message\":\"Hi\"}"
        );

        when(objectMapper.readValue(eq("{\"participant\":\"john\",\"message\":\"Hello\"}"), eq(MessageModel.class)))
            .thenReturn(new MessageModel("john", "Hello"));
        when(objectMapper.readValue(eq("invalid-json"), eq(MessageModel.class)))
            .thenThrow(new RuntimeException("Invalid JSON"));
        when(objectMapper.readValue(eq("{\"participant\":\"jane\",\"message\":\"Hi\"}"), eq(MessageModel.class)))
            .thenReturn(new MessageModel("jane", "Hi"));

        when(chatRoomService.isRoomExists("test")).thenReturn(true);
        when(messageRepository.getAllMessages(any())).thenReturn(mockJsonList);

        List<MessageModel> result = messageService.getAllMessages("test");

        assertEquals(2, result.size());
        assertEquals("john", result.get(0).getParticipant());
        assertEquals("jane", result.get(1).getParticipant());
    }

    // Test empty message list returns empty result
    @Test
    void convertJsonListToMessages_WithEmptyList_ReturnsEmptyList() {
        when(chatRoomService.isRoomExists("test")).thenReturn(true);
        when(messageRepository.getAllMessages(any())).thenReturn(List.of());

        List<MessageModel> result = messageService.getAllMessages("test");

        assertTrue(result.isEmpty());
    }

    // Test null message list returns empty result
    @Test
    void convertJsonListToMessages_WithNullList_ReturnsEmptyList() {
        when(chatRoomService.isRoomExists("test")).thenReturn(true);
        when(messageRepository.getAllMessages(any())).thenReturn(null);

        List<MessageModel> result = messageService.getAllMessages("test");

        assertTrue(result.isEmpty());
    }
}