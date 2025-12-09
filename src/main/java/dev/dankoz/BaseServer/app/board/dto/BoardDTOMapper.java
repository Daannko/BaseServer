package dev.dankoz.BaseServer.app.board.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import dev.dankoz.BaseServer.app.board.model.Board;

@Component
public class BoardDTOMapper {

    public GetBoardInfoReponse toGetAllBoardsInfoResponse(Board board){
        return new GetBoardInfoReponse(
            board.getId(),
            board.getName(),
            board.getDescription()
        );
    }
}
