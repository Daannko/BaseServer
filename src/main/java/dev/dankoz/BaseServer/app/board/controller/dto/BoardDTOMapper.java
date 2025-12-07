package dev.dankoz.BaseServer.app.board.controller.dto;

import java.util.List;

import dev.dankoz.BaseServer.app.board.model.Board;

public class BoardDTOMapper {

    public GetBoardInfoReponse toGetAllBoardsInfoResponse(Board board){
        return new GetBoardInfoReponse(
            board.getId(),
            board.getName(),
            board.getDescription()
        );
    }
}
