package fr.xebia.devoxx2016.kubernetes.todomvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;
import spark.Spark;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class Application {

    // https://sparktutorials.github.io/2015/04/14/getting-started-with-spark-and-docker.html

    // https://sparktutorials.github.io/2015/04/29/spark-and-sql2o.html


    /*

        docker run -d -p 8080:8080 -p 28015:28015 -p 29015:29015 rethinkdb

     */

    public static RethinkDB r = RethinkDB.r;

    public static void main(String[] args) {

        Spark.staticFileLocation("/static");

        get("/resetdb", (req, res) -> {
            Connection conn = r.connection().hostname(System.getenv("RETHINKDB_SERVICE_HOST")).port(28015).connect();
            try {
                r.dbDrop("tododb").run(getConnection());
            } catch (Exception e) {}

            r.dbCreate("tododb").run(getConnection());
            return "ok";
        });

        // ---

        get("/api", (req, res) -> {
            return "{}";
        });


        post("/api/todos", (request, response) -> {
            Todo todo = jsonToObject(request.body(), Todo.class);
            HashMap<String, Object> result = r.table("todo").insert(
                    r.hashMap("title", todo.title)
                            .with("completed", todo.completed)
            ).run(getConnection());
            todo.id = ((List<String>)result.get("generated_keys")).get(0);
            return objectToJson(todo);
        });

        get("/api/todos", (req, res) -> {
            Cursor<HashMap> result = r.db("tododb").table("todo").run(getConnection());
            return result.toList().stream()
                    .map(hashMap -> objectToJson(hashMap))
                    .collect(Collectors.toList());
        });

        get("/api/todos/:id", (req, res) -> {
            String id = req.params(":id");
            Object todo = r.db("tododb").table("todo").get(id).run(getConnection());
            return objectToJson(todo);
        });

        delete("/api/todos/:id", (req, res) -> {
            String id = req.params(":id");
            Object todo = r
                    .db("tododb")
                    .table("todo")
                    .get(id)
                    .delete()
                    .run(getConnection());
            return objectToJson(todo);
        });

        delete("/api/todos", (req, res) -> {
            Object todo = r
                    .db("tododb")
                    .table("todo")
                    .filter(r.hashMap("completed", true))
                    .delete()
                    .run(getConnection());
            return objectToJson(todo);
        });

        put("/api/todos/:id", (req, res) -> {
            String id = req.params(":id");
            Todo todo = jsonToObject(req.body(), Todo.class);
            r.db("tododb")
                    .table("todo")
                    .get(id)
                    .update(
                            r.hashMap("title", todo.title)
                            .with("completed", todo.completed)
                    )
                    .run(getConnection());
            return objectToJson(todo);
        });
    }

    private static Connection getConnection() {
        return r.connection().hostname(System.getenv("RETHINKDB_SERVICE_HOST")).port(28015).db("tododb").timeout(3).connect();
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String objectToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(data);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
