package dev.dankoz.BaseServer.todo;

import dev.dankoz.BaseServer.general.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface ToDoRepository extends CrudRepository<ToDo,Integer> {



    @Query("select t from ToDo t where t.author.id = :#{#user.id}")
    List<ToDo> getAllByAuthor(@Param("user") User user);


}
