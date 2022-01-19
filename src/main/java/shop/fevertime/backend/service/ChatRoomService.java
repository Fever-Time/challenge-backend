package shop.fevertime.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.fevertime.backend.domain.ChatRoom;
import shop.fevertime.backend.domain.User;
import shop.fevertime.backend.dto.ChatMessageDto;
import shop.fevertime.backend.dto.response.ChatRoomResponseDto;
import shop.fevertime.backend.dto.request.ChatRoomRequestDto;
import shop.fevertime.backend.exception.ApiRequestException;
import shop.fevertime.backend.repository.ChatRoomRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    @Transactional
    public List<ChatRoomResponseDto> getAllRooms() {
        return chatRoomRepository.findAll()
                .stream()
                .map(ChatRoomResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createRoom(ChatRoomRequestDto chatRoomDto, User user) {
        chatRoomRepository.save(new ChatRoom(chatRoomDto.getName(), user, chatRoomDto.getUserCount()));
    }

    @Transactional
    public ChatRoomResponseDto getRoom(Long roomId) {
        ChatRoom byId = chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ApiRequestException("해당 채팅방이 없습니다.")
        );
        return new ChatRoomResponseDto(byId);
    }


    @Transactional
    public void deleteRoom(Long roomId, User user) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndUser(roomId, user);
        chatRoomRepository.delete(chatRoom);
    }

    @Transactional
    public void enterRoom(ChatMessageDto messageDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getRoomId()).orElseThrow(
                () -> new ApiRequestException("해당 채팅방이 없습니다.")
        );
        //인원수 증가
        chatRoom.addUserCount();
        //메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageDto.getRoomId(), messageDto);
    }

    @Transactional
    public void sendMsg(ChatMessageDto messageDto) {
        //메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageDto.getRoomId(), messageDto);
    }

    @Transactional
    public void exitRoom(ChatMessageDto messageDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getRoomId()).orElseThrow(
                () -> new ApiRequestException("해당 채팅방이 없습니다.")
        );
        //인원수 감소
        chatRoom.minusUserCount();
        //메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageDto.getRoomId(), messageDto);
    }
}
