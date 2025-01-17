package dev.dankoz.BaseServer.todo;

import dev.dankoz.BaseServer.todo.dto.AddToDoRequest;
import dev.dankoz.BaseServer.todo.dto.RemoveToDoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.InstanceNotFoundException;

@RestController
@RequestMapping("/todo")
public class ToDoController {

    private final ToDoService toDoService;

    public ToDoController(ToDoService toDoService) {
        this.toDoService = toDoService;
    }

    @GetMapping
    public ResponseEntity<?> getToDos(){
        return toDoService.getMyToDos();
    }

    @PostMapping
    public ResponseEntity<?> addToDo(@RequestBody AddToDoRequest request){
        return toDoService.addToDo(request);
    }

    @DeleteMapping
    public ResponseEntity<?> removeToDo(@RequestBody RemoveToDoRequest request) throws InstanceNotFoundException {
        return toDoService.removeToDo(request);
    }

}
