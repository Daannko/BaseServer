package dev.dankoz.BaseServer.app.board.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.dankoz.BaseServer.app.board.model.Board;
import dev.dankoz.BaseServer.app.board.repository.BoardRepository;
import dev.dankoz.BaseServer.app.board.service.BoardService;
import dev.dankoz.BaseServer.config.exceptions.UnauthorizedException;

@Service
public class BoardServiceImpl implements BoardService {

    private BoardRepository boardRepository;

    public BoardServiceImpl(BoardRepository boardRepository){
        this.boardRepository = boardRepository;
    }


    @Override
    public Board addBoard(String name, String desc, Integer userId) {
        Board board = Board.builder()
        .name(name)
        .description(desc)
        .authorId(userId)
        .topics(new HashSet<>())
        .build();

        boardRepository.save(board);
        return board;
    }

    @Override
    public void updateBoard(Integer boardId, String name, String desc, Integer userId) {
        Optional<Board> optionalBoard = boardRepository.findByAuthorIdAndId(userId,boardId);
        if(optionalBoard.isEmpty() || optionalBoard.get().getAuthorId() != userId){
            throw new UnauthorizedException("The board does not exist or you do not have access to it");
        }
        Board board = optionalBoard.get();
        if(name!= null && name.trim().length() != 0){
            board.setName(name);
        }
        if(desc != null && desc.trim().length() != 0){
            board.setDescription(desc);
        }
    }

    @Override
    public void removeBoard(Integer boardId, Integer userId) {
        Optional<Board> optionalBoard = boardRepository.findById(boardId);
        if(optionalBoard.isEmpty() || optionalBoard.get().getAuthorId() != userId){
            throw new UnauthorizedException("The board does not exist or you do not have access to it");
        }
        boardRepository.delete(optionalBoard.get());
    }

    @Override
    public List<Board> getAllBoards(Integer authorId) {
        return boardRepository.findAllByAuthorId(authorId);
    }

    

    


}
