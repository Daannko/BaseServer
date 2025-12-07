package dev.dankoz.BaseServer.app.board.controller;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dev.dankoz.BaseServer.app.board.controller.dto.AddBoardRequestDTO;
import dev.dankoz.BaseServer.app.board.controller.dto.BoardDTOMapper;
import dev.dankoz.BaseServer.app.board.controller.dto.GetBoardInfoReponse;
import dev.dankoz.BaseServer.app.board.controller.dto.UpdateBoardRequestDTO;
import dev.dankoz.BaseServer.app.board.model.Board;
import dev.dankoz.BaseServer.app.board.service.BoardService;
import dev.dankoz.BaseServer.general.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;
    private final BoardDTOMapper boardDTOMapper;

    public BoardController(BoardService boardService, UserService userService, BoardDTOMapper boardDTOMapper){
        this.userService = userService;
        this.boardService = boardService;
        this.boardDTOMapper = boardDTOMapper;
    }

    @PostMapping
    public Board addBoard(@Valid @RequestBody AddBoardRequestDTO requestDTO){
       return boardService.addBoard(requestDTO.name(), requestDTO.description(), getUserId());
    }

    @DeleteMapping("/{id}")
    public void removeBoard(@PathVariable Integer id){
        boardService.removeBoard(id, getUserId());
    }

    @PatchMapping
    public void updateBoard(@Valid @RequestBody UpdateBoardRequestDTO requestDTO){
        boardService.updateBoard(requestDTO.boardId(), requestDTO.name(), requestDTO.description(), getUserId());
    }

    @GetMapping("/all")
    public List<GetBoardInfoReponse> getAllBoards() {
        return boardService.getAllBoards(getUserId())
        .stream()
        .map(o -> boardDTOMapper.toGetAllBoardsInfoResponse(o))
        .toList();
    }

    @GetMapping("/{id}")
    public String getBoard(@RequestParam String param) {
        return new String();
    }
    
    
    


    private Integer getUserId(){
        return userService.getCurrentUser().getId();
    }

}
