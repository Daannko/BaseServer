package dev.dankoz.BaseServer.app.board.service;

import java.util.List;
import java.util.Optional;

import dev.dankoz.BaseServer.app.board.controller.dto.GetBoardInfoReponse;
import dev.dankoz.BaseServer.app.board.model.Board;

public interface BoardService {

    public Board addBoard(String name,String desc, Integer userId);
    public List<Board> getAllBoards(Integer authorId);
    public void updateBoard(Integer boardId, String name, String desc, Integer userId);
    public void removeBoard(Integer boardId, Integer userId);
    public Board getBoard(Integer boardId, Integer userId);

}
