package dev.dankoz.BaseServer.todo;


import dev.dankoz.BaseServer.general.model.User;
import dev.dankoz.BaseServer.general.service.UserService;
import dev.dankoz.BaseServer.todo.dto.AddToDoRequest;
import dev.dankoz.BaseServer.todo.dto.RemoveToDoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InstanceNotFoundException;
import java.util.*;

@Service
public class ToDoService {

    private final UserService userService;
    private final ToDoRepository toDoRepository;

    protected ToDoService(UserService userService, ToDoRepository toDoRepository) {
        this.userService = userService;
        this.toDoRepository = toDoRepository;
    }

    protected ResponseEntity<?> addToDo(AddToDoRequest request){
        User user = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

        ToDo newToDo = ToDo.builder()
                .author(user)
                .expires(request.expires())
                .text(request.text())
                .priority(request.priority())
                .created(new Date())
                .build();

        toDoRepository.save(newToDo);
        return ResponseEntity.ok("Added task!");
    }

    protected ResponseEntity<?> getMyToDos(){
        User user = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        return ResponseEntity.ok(toDoRepository.getAllByAuthor(user));
    }

    protected ResponseEntity<?> removeToDo(RemoveToDoRequest request) throws InstanceNotFoundException {
        Optional<ToDo> optionalToDo = toDoRepository.findById(request.id());
        ToDo removeToDo = optionalToDo.orElseThrow(InstanceNotFoundException::new);
        toDoRepository.delete(removeToDo);
        return ResponseEntity.ok().body("Removed the todo!");
    }

}
