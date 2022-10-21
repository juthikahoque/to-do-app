package services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime


class DatabaseTest {

    @Test
    fun testDatabase() {
        val db = Database()
        val conn = db.connect("test")

        val stmt = conn.createStatement()
        val dropTable = "DROP TABLE IF EXISTS test"

        val sql = """
            ${dropTable};
            CREATE TABLE IF NOT EXISTS test (name VARCHAR(1000));
            INSERT INTO test VALUES ('Juthika Hoque');
        """.trimMargin()
        stmt.executeUpdate(sql)

        val query = "select * from test"
        val results = stmt.executeQuery(query)

        val test: MutableSet<String> = mutableSetOf()
        while (results.next()) {
            val name = results.getString("name")
            test.add(name)
        }
        assertEquals(1, test.size)
        assertEquals("Juthika Hoque", test.elementAt(0))

        stmt.executeUpdate(dropTable)
    }

    @Test
    fun createUserTable() {
        val db = Database()
        val conn = db.connect("test")

        val stmt = conn.createStatement()
        val dropTable = "DROP TABLE IF EXISTS testUsers"

        val createTable = """
            ${dropTable};
            CREATE TABLE IF NOT EXISTS testUsers (
                id INT NOT NULL PRIMARY KEY,
                name VARCHAR(1000));
            INSERT INTO testUsers (id, name) VALUES (1, 'Juthika Hoque')
        """.trimIndent()
        stmt.executeUpdate(createTable)

        val query = "select * from testUsers"
        val results = stmt.executeQuery(query)

        val users: MutableSet<String> = mutableSetOf()
        while (results.next()) {
            val name = results.getString("name")
            users.add(name)
        }
        assertEquals(1, users.size)
        assertEquals("Juthika Hoque", users.elementAt(0))

        stmt.executeUpdate(dropTable)
    }

    @Test
    fun createBoardTable() {
        val db = Database()
        val conn = db.connect("test")

        val stmt = conn.createStatement()
        val dropTable = "DROP TABLE IF EXISTS testBoards"

        val sql = """
            ${dropTable};
            CREATE TABLE IF NOT EXISTS testBoards (
                id INT NOT NULL PRIMARY KEY,
                name VARCHAR(1000),
                userId INT,
                FOREIGN KEY(userId) REFERENCES testUsers(id));
            INSERT INTO testBoards (id, name, userId) VALUES (1, 'Mine', 1);
        """
        stmt.executeUpdate(sql)

        val query = "select * from testBoards"
        val results = stmt.executeQuery(query)

        val boards: MutableSet<String> = mutableSetOf()
        while (results.next()) {
            val name = results.getString("name")
            boards.add(name)
        }
        assertEquals(1, boards.size)
        assertEquals("Mine", boards.elementAt(0))

        stmt.executeUpdate(dropTable)
    }

    @Test
    fun createItemTable() {
        val db = Database()
        val conn = db.connect("test")

        val stmt = conn.createStatement()
        val dropTable = "DROP TABLE IF EXISTS testItems"

        val sql = """
            ${dropTable};
            CREATE TABLE IF NOT EXISTS testItems (
                id INT NOT NULL PRIMARY KEY,
                text VARCHAR(1000),
                dueDate DATETIME,
                priority INT,
                done BOOLEAN,
                boardId INT,
                FOREIGN KEY(boardId) REFERENCES testBoards(id));
            INSERT INTO testItems (id, text, dueDate, priority, done, boardId) VALUES (
            1, 'to do item', '${LocalDateTime.now()}', 1, 1, 1);
        """
        stmt.executeUpdate(sql)

        val query = "select * from testItems"
        val results = stmt.executeQuery(query)

        val items: MutableSet<String> = mutableSetOf()
        while (results.next()) {
            val name = results.getString("text")
            items.add(name)
        }
        assertEquals(1, items.size)
        assertEquals("to do item", items.elementAt(0))

        stmt.executeUpdate(dropTable)
    }

    @Test
    fun createLabelTable() {
        val db = Database()
        val conn = db.connect("test")

        val stmt = conn.createStatement()
        val dropTable = "DROP TABLE IF EXISTS testLabels"

        val sql = """
            ${dropTable};
            CREATE TABLE IF NOT EXISTS testLabels (
                id INT PRIMARY KEY,
                value VARCHAR(1000),
                itemId INT,
                boardId INT,
                FOREIGN KEY(itemId) REFERENCES testItems(id),
                FOREIGN KEY(boardId) REFERENCES testBoards(id));
             INSERT INTO testLabels (id, value, itemId, boardId) VALUES (1, 'CS 346', 1, 1);
        """.trimMargin()
        stmt.executeUpdate(sql)

        val query = "select * from testLabels"
        val results = stmt.executeQuery(query)

        val labels: MutableSet<String> = mutableSetOf()
        while (results.next()) {
            val name = results.getString("value")
            labels.add(name)
        }
        assertEquals(1, labels.size)
        assertEquals("CS 346", labels.elementAt(0))

        stmt.executeUpdate(dropTable)
    }
}