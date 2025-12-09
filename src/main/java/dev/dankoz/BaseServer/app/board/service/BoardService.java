package dev.dankoz.BaseServer.app.board.service;

import java.util.List;
import java.util.Optional;

import dev.dankoz.BaseServer.app.board.dto.GetBoardInfoReponse;
import dev.dankoz.BaseServer.app.board.model.Board;
import dev.dankoz.BaseServer.app.board.model.Topic;

public interface BoardService {

    public Board addBoard(String name,String desc, Integer authorId);
    public List<Board> getAllBoards(Integer authorId);
    public void updateBoard(String boardId, String name, String desc, Integer userId);
    public void removeBoard(String boardId, Integer userId);
    public Board getBoard(String boardId, Integer userId);
    public Board saveBoard(Board board);

}
