package ai.freightfox.chat.app.service;

import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.BadRequestException;
import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.ChatRoomNotFoundException;
import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.ResourceAlreadyExistException;
import ai.freightfox.chat.app.model.ChatRoomModel;
import ai.freightfox.chat.app.repository.ChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    // Test successful chat room creation with valid room name
    @Test
    void createChatRoom_WithValidRoomName_CreatesRoom() {
        String roomName = "general";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(false);

        assertDoesNotThrow(() -> chatRoomService.createChatRoom(roomName));

        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, times(1)).saveChatRoom(eq(roomName), any(), any());
    }

    // Test chat room creation fails when room already exists
    @Test
    void createChatRoom_WithExistingRoom_ThrowsResourceAlreadyExistException() {
        String roomName = "existing-room";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(true);

        ResourceAlreadyExistException exception = assertThrows(ResourceAlreadyExistException.class,
            () -> chatRoomService.createChatRoom(roomName));

        assertEquals("Chat room 'existing-room' already exists", exception.getMessage());
        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, never()).saveChatRoom(any(), any(), any());
    }

    // Test saving chat room model with proper Redis key generation
    @Test
    void saveChatRoom_WithValidModel_SavesSuccessfully() {
        ChatRoomModel chatRoom = new ChatRoomModel("test-room");

        assertDoesNotThrow(() -> chatRoomService.saveChatRoom(chatRoom));

        verify(chatRoomRepository, times(1))
            .saveChatRoom(eq("test-room"), eq(chatRoom.getCreatedAt()), any());
    }

    // Test room existence check returns true for existing room
    @Test
    void isRoomExists_WithExistingRoom_ReturnsTrue() {
        String roomName = "existing-room";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(true);

        boolean result = chatRoomService.isRoomExists(roomName);

        assertTrue(result);
        verify(chatRoomRepository, times(1)).isRoomExist(any());
    }

    // Test room existence check returns false for non-existing room
    @Test
    void isRoomExists_WithNonExistingRoom_ReturnsFalse() {
        String roomName = "non-existing-room";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(false);

        boolean result = chatRoomService.isRoomExists(roomName);

        assertFalse(result);
        verify(chatRoomRepository, times(1)).isRoomExist(any());
    }

    // Test successful participant join to existing room
    @Test
    void joinChatRoom_WithValidParameters_JoinsSuccessfully() {
        String roomName = "test-room";
        String participantName = "john";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(true);

        assertDoesNotThrow(() -> chatRoomService.joinChatRoom(roomName, participantName));

        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, times(1))
            .joinChatRoom(eq(roomName), eq(participantName), any(), any());
    }

    // Test join fails when room does not exist
    @Test
    void joinChatRoom_WithNonExistingRoom_ThrowsChatRoomNotFoundException() {
        String roomName = "non-existing";
        String participantName = "john";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(false);

        ChatRoomNotFoundException exception = assertThrows(ChatRoomNotFoundException.class,
            () -> chatRoomService.joinChatRoom(roomName, participantName));

        assertEquals("Chat room 'non-existing' does not exist", exception.getMessage());
        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, never()).joinChatRoom(any(), any(), any(), any());
    }

    // Test join fails with empty participant name
    @Test
    void joinChatRoom_WithEmptyParticipantName_ThrowsBadRequestException() {
        String roomName = "test-room";
        String participantName = "";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> chatRoomService.joinChatRoom(roomName, participantName));

        assertEquals("Participant name cannot be empty", exception.getMessage());
        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, never()).joinChatRoom(any(), any(), any(), any());
    }

    // Test join fails with null participant name
    @Test
    void joinChatRoom_WithNullParticipantName_ThrowsBadRequestException() {
        String roomName = "test-room";
        String participantName = null;

        when(chatRoomRepository.isRoomExist(any())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> chatRoomService.joinChatRoom(roomName, participantName));

        assertEquals("Participant name cannot be empty", exception.getMessage());
        verify(chatRoomRepository, never()).joinChatRoom(any(), any(), any(), any());
    }

    // Test successful participant removal from room
    @Test
    void removeParticipant_WithValidParameters_RemovesSuccessfully() {
        String roomName = "test-room";
        String participantName = "john";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(true);
        when(chatRoomRepository.removeParticipant(eq(participantName), eq(roomName), any(), any()))
            .thenReturn(true);

        boolean result = chatRoomService.removeParticipant(roomName, participantName);

        assertTrue(result);
        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, times(1))
            .removeParticipant(eq(participantName), eq(roomName), any(), any());
    }

    // Test participant removal from non-existing room
    @Test
    void removeParticipant_WithNonExistingRoom_ThrowsChatRoomNotFoundException() {
        String roomName = "non-existing";
        String participantName = "john";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(false);

        ChatRoomNotFoundException exception = assertThrows(ChatRoomNotFoundException.class,
            () -> chatRoomService.removeParticipant(roomName, participantName));

        assertEquals("Chat room 'non-existing' does not exist", exception.getMessage());
        verify(chatRoomRepository, never()).removeParticipant(any(), any(), any(), any());
    }

    // Test getting participants from existing room
    @Test
    void getParticipants_WithExistingRoom_ReturnsParticipants() {
        String roomName = "test-room";
        Set<Object> expectedParticipants = Set.of("john", "jane", "bob");

        when(chatRoomRepository.isRoomExist(any())).thenReturn(true);
        when(chatRoomRepository.getParticipants(eq(roomName), any()))
            .thenReturn(expectedParticipants);

        Set<Object> result = chatRoomService.getParticipants(roomName);

        assertEquals(expectedParticipants, result);
        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, times(1)).getParticipants(eq(roomName), any());
    }

    // Test getting participants from non-existing room
    @Test
    void getParticipants_WithNonExistingRoom_ThrowsChatRoomNotFoundException() {
        String roomName = "non-existing";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(false);

        ChatRoomNotFoundException exception = assertThrows(ChatRoomNotFoundException.class,
            () -> chatRoomService.getParticipants(roomName));

        assertEquals("Chat room 'non-existing' does not exist", exception.getMessage());
        verify(chatRoomRepository, never()).getParticipants(any(), any());
    }

    // Test checking if participant is in existing room
    @Test
    void isParticipantInRoom_WithExistingRoomAndParticipant_ReturnsTrue() {
        String roomName = "test-room";
        String participantName = "john";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(true);
        when(chatRoomRepository.isParticipantInRoom(any(), eq(participantName)))
            .thenReturn(true);

        boolean result = chatRoomService.isParticipantInRoom(roomName, participantName);

        assertTrue(result);
        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, times(1)).isParticipantInRoom(any(), eq(participantName));
    }

    // Test checking participant in non-existing room returns false
    @Test
    void isParticipantInRoom_WithNonExistingRoom_ReturnsFalse() {
        String roomName = "non-existing";
        String participantName = "john";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(false);

        boolean result = chatRoomService.isParticipantInRoom(roomName, participantName);

        assertFalse(result);
        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, never()).isParticipantInRoom(any(), any());
    }

    // Test checking participant with empty name returns false
    @Test
    void isParticipantInRoom_WithEmptyParticipantName_ReturnsFalse() {
        String roomName = "test-room";
        String participantName = "";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(true);

        boolean result = chatRoomService.isParticipantInRoom(roomName, participantName);

        assertFalse(result);
        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, never()).isParticipantInRoom(any(), any());
    }

    // Test getting participant count from room
    @Test
    void getParticipantCount_ReturnsCorrectCount() {
        String roomName = "test-room";
        long expectedCount = 5L;

        when(chatRoomRepository.getParticipantCount(any())).thenReturn(expectedCount);

        long result = chatRoomService.getParticipantCount(roomName);

        assertEquals(expectedCount, result);
        verify(chatRoomRepository, times(1)).getParticipantCount(any());
    }

    // Test room name validation with valid names
    @Test
    void isValidRoomName_WithValidNames_ReturnsTrue() {
        assertTrue(chatRoomService.isValidRoomName("general"));
        assertTrue(chatRoomService.isValidRoomName("test_room"));
        assertTrue(chatRoomService.isValidRoomName("room-123"));
        assertTrue(chatRoomService.isValidRoomName("ABC123"));
        assertTrue(chatRoomService.isValidRoomName("a".repeat(50)));
    }

    // Test room name validation with invalid names
    @Test
    void isValidRoomName_WithInvalidNames_ReturnsFalse() {
        assertFalse(chatRoomService.isValidRoomName(null));
        assertFalse(chatRoomService.isValidRoomName(""));
        assertFalse(chatRoomService.isValidRoomName("ab"));
        assertFalse(chatRoomService.isValidRoomName("a".repeat(51)));
        assertFalse(chatRoomService.isValidRoomName("room with spaces"));
        assertFalse(chatRoomService.isValidRoomName("room@special"));
        assertFalse(chatRoomService.isValidRoomName("room!"));
    }

    // Test creating validated chat room with valid name
    @Test
    void createValidatedChatRoom_WithValidName_CreatesRoom() {
        String roomName = "valid-room";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(false);

        assertDoesNotThrow(() -> chatRoomService.createValidatedChatRoom(roomName));

        verify(chatRoomRepository, times(1)).isRoomExist(any());
        verify(chatRoomRepository, times(1)).saveChatRoom(eq(roomName), any(), any());
    }

    // Test creating validated chat room with invalid name
    @Test
    void createValidatedChatRoom_WithInvalidName_ThrowsBadRequestException() {
        String roomName = "ab";

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> chatRoomService.createValidatedChatRoom(roomName));

        assertEquals("Invalid room name, Must be 3-50 characters, alphanumeric, underscore, or hyphen only.", 
                     exception.getMessage());
        verify(chatRoomRepository, never()).isRoomExist(any());
        verify(chatRoomRepository, never()).saveChatRoom(any(), any(), any());
    }

    // Test successful room removal with all keys existing
    @Test
    void removeRoom_WithExistingRoom_RemovesSuccessfully() {
        String roomName = "test-room";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(true);

        assertDoesNotThrow(() -> chatRoomService.removeRoom(roomName));

        verify(chatRoomRepository, times(3)).isRoomExist(any());
        verify(chatRoomRepository, times(1)).removeRemoveRoomData(any(), any(), any());
    }

    // Test room removal when room does not exist
    @Test
    void removeRoom_WithNonExistingRoom_ThrowsChatRoomNotFoundException() {
        String roomName = "non-existing";

        when(chatRoomRepository.isRoomExist(any())).thenReturn(false);

        ChatRoomNotFoundException exception = assertThrows(ChatRoomNotFoundException.class,
            () -> chatRoomService.removeRoom(roomName));

        assertEquals("Room 'non-existing' does not exist", exception.getMessage());
        verify(chatRoomRepository, times(3)).isRoomExist(any()); // Called 3 times for each key type
        verify(chatRoomRepository, never()).removeRemoveRoomData(any(), any(), any());
    }

    // Test room removal with partial key existence
    @Test
    void removeRoom_WithPartialKeyExistence_RemovesWithWarning() {
        String roomName = "test-room";

        when(chatRoomRepository.isRoomExist(any()))
            .thenReturn(true)   // room exists
            .thenReturn(false)  // participants key missing
            .thenReturn(true);  // messages exist

        assertDoesNotThrow(() -> chatRoomService.removeRoom(roomName));

        verify(chatRoomRepository, times(3)).isRoomExist(any());
        verify(chatRoomRepository, times(1)).removeRemoveRoomData(any(), any(), any());
    }
}