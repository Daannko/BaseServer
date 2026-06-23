package dev.dankoz.BaseServer.app.board.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.dankoz.BaseServer.app.board.dto.UpdateBoardRequestDTO;
import dev.dankoz.BaseServer.app.board.model.Board;
import dev.dankoz.BaseServer.app.board.repository.BoardRepository;
import dev.dankoz.BaseServer.app.board.service.BoardService;
import dev.dankoz.BaseServer.config.exceptions.UnauthorizedException;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

    public BoardServiceImpl(BoardRepository boardRepository){
        this.boardRepository = boardRepository;
    }

    @Override
    public Board addBoard(String name, String desc, Integer width, Integer height, Integer userId) {
        Board board = Board.builder()
            .name(name)
            .description(desc)
            .width(width)
            .height(height)
            .authorId(userId)
            .topics(new HashSet<>())
            .build();

        boardRepository.save(board);
        return board;
    }

    @Override
    public void updateBoard(String boardId, UpdateBoardRequestDTO request, Integer userId) {
        Board board = getBoard(boardId, userId);
        if (request.name() != null && !request.name().trim().isEmpty()) {
            board.setName(request.name());
        }
        if (request.description() != null && !request.description().trim().isEmpty()) {
            board.setDescription(request.description());
        }
        if (request.width() != null) {
            board.setWidth(request.width());
        }
        if (request.height() != null) {
            board.setHeight(request.height());
        }
        if (request.camera() != null) {
            board.setCamera(request.camera());
        }
        boardRepository.save(board);
    }

    @Override
    public void removeBoard(String boardId, Integer userId) {
        boardRepository.deleteById(getBoard(boardId, userId).getId());
    }

    @Override
    public List<Board> getAllBoards(Integer authorId) {
        return boardRepository.findAllByAuthorId(authorId);
    }

    @Override
    public Board getBoard(String boardId, Integer userId) {
        Optional<Board> optionalBoard = boardRepository.findById(boardId);
        if(optionalBoard.isEmpty() || !Objects.equals(optionalBoard.get().getAuthorId(), userId)){
            throw new UnauthorizedException("The board does not exist or you do not have access to it");
        }
        return optionalBoard.get();
    }

    @Override
    public Board saveBoard(Board board) {
        boardRepository.save(board);
        return board;
    }
}
