package dev.dankoz.BaseServer.app.board.service;

import java.util.List;

import dev.dankoz.BaseServer.app.board.dto.UpdateBoardRequestDTO;
import dev.dankoz.BaseServer.app.board.model.Board;

public interface BoardService {

    Board addBoard(String name, String desc, Integer width, Integer height, Integer authorId);
    List<Board> getAllBoards(Integer authorId);
    void updateBoard(String boardId, UpdateBoardRequestDTO request, Integer userId);
    void removeBoard(String boardId, Integer userId);
    Board getBoard(String boardId, Integer userId);
    Board saveBoard(Board board);

}
