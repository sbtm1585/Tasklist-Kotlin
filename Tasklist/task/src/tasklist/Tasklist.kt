package tasklist

import kotlinx.datetime.*
import kotlin.system.exitProcess
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class Tasklist {
    private var taskList = emptyList<Task>().toMutableList()
    private var index = 0
    private val jsonFile = File("tasklist.json")
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    
    init {
        if (jsonFile.exists()) fromJSON(jsonFile)
    }

    fun menu(): Boolean {
        println("Input an action (add, print, edit, delete, end):")
        val action = readln().trim().lowercase()
        when (action) {
            "add" -> addTask()
            "print" -> printTasks()
            "edit" -> edit()
            "delete" -> delete()
            "end" -> {
                println("Tasklist exiting!")
                toJSON(ListOfTasks(taskList))
                exitProcess(0)
            }
            else -> println("The input action is invalid")
        }
        return true
    }

    private fun addTask() {
        val prio = taskPriority()
        val date = getDateInput()
        val time = getTimeInput()
        val due = checkDate(date)
        println("Input a new task (enter a blank line to end):")
        val tasks =
            generateSequence { readln().trim() }.takeWhile { it.isNotBlank() }.toMutableList()
        val preparedTasks = prepareTaskList(tasks)
        if (preparedTasks.isEmpty()) {
            println("The task is blank")
        } else {
            taskList.add(Task(++index, date, time, prio, preparedTasks, due))
        }
    }

    private fun prepareTaskList(tasks: MutableList<String>): MutableList<String> {
        return buildList {
            tasks.forEach { s ->
                var length = s.length
                if (length > MAX_CHARS) {
                    add(s.substring(0, MAX_CHARS).trim())
                    length -= MAX_CHARS
                    while (s.length - length > MAX_CHARS) {
                        add(
                            s.substring(s.length - length, s.length - length + MAX_CHARS).trim()
                        )
                        length -= MAX_CHARS
                    }
                    if (length in 1..MAX_CHARS) add(
                        s.substring(s.length - length, s.length).trim()
                    )
                } else {
                    add(s)
                }
            }
        }.toMutableList()
    }

    private fun editTask(): MutableList<String> {
        println("Input a new task (enter a blank line to end):")
        val tasks =
            generateSequence { readln().trim() }.takeWhile { it.isNotBlank() }.toMutableList()
        val preparedTasks = prepareTaskList(tasks)
        if (preparedTasks.isEmpty()) {
            println("The task is blank")
            return editTask()
        } else {
            return preparedTasks
        }
    }

    private fun rebuildTaskList() {
        var index = 0
        taskList.forEach { it.index = ++index }
    }

    private fun taskPriority(): String {
        println("Input the task priority (C, H, N, L):")
        return when (readln().lowercase()) {
            "c" -> Priority.C.color
            "h" -> Priority.H.color
            "n" -> Priority.N.color
            "l" -> Priority.L.color
            else -> taskPriority()
        }
    }

    private fun getDateInput(): String {
        println("Input the date (yyyy-mm-dd):")
        try {
            val dt = readln().split("-").toMutableList().map { it.toInt() }
            val d = LocalDate(dt[0], dt[1], dt[2])
            return d.toString()
        } catch (e: Exception) {
            println("The input date is invalid")
            return getDateInput()
        }
    }

    private fun getTimeInput(): String {
        println("Input the time (hh:mm):")
        try {
            val time = readln().split(":").toMutableList().map { it.toInt() }
            val t = LocalTime(time[0], time[1]).toJavaLocalTime()
            return t.toString()
        } catch (e: Exception) {
            println("The input time is invalid")
            return getTimeInput()
        }
    }

    private fun delete() {
        if (!printTasks()) menu()
        while (true) {
            println("Input the task number (1-${taskList.size}):")
            try {
                val taskNum = readln().toInt()
                checkIfNumIsValid(taskNum)
                taskList.removeAt(taskNum - 1)
                rebuildTaskList()
                println("The task is deleted")
                break
            } catch (e: Exception) {
                println("Invalid task number")
            }
        }
    }

    private fun edit() {
        if (!printTasks()) menu()
        loop@ while (true) {
            println("Input the task number (1-${taskList.size}):")
            try {
                val taskNum = readln().toInt()
                checkIfNumIsValid(taskNum)
                while (true) {
                    println("Input a field to edit (priority, date, time, task):")
                    when (val field = readln().lowercase()) {
                        "priority", "date", "time", "task" -> {
                            field.change(taskNum)
                            println("The task is changed")
                            break@loop
                        }
                        else -> {
                            println("Invalid field")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Invalid task number")
                continue@loop
            }
        }
    }

    private fun String.change(tn: Int) {
        val i = tn - 1
        when (this) {
            "priority" -> taskList[i].prio = taskPriority()
            "date" -> taskList[i].apply {
                date = getDateInput()
                due = checkDate(this.date)
            }
            "time" -> taskList[i].time = getTimeInput()
            "task" -> taskList[i].tasks = editTask()
            else -> taskList
        }
    }

    private fun printTasks(): Boolean {
        if (taskList.isEmpty()) {
            println("No tasks have been input")
            menu()
            return false
        } else {
            buildString {
                append(HEADER.trimIndent())
                taskList.forEach { (i, dt, t, p, task, d) ->
                    val iterator = task.iterator()
                    append(
                        "\n| $i  | $dt | $t | $p | $d |${
                            iterator.next().padEnd(MAX_CHARS, ' ')
                        }|\n"
                    )
                    while (iterator.hasNext()) {
                        append("$AUX_LINE${iterator.next().padEnd(MAX_CHARS, ' ')}|\n")
                    }
                    append(LINE_BREAK)
                }
            }.also(::println)
            return true
        }
    }

    private fun checkDate(date: String): String {
        val currentDate =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val taskDate = Instant.parse("${date}T00:00:00Z")
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        val due = currentDate.daysUntil(taskDate)
        return when {
            due < 0 -> DueTag.O.color
            due > 0 -> DueTag.I.color
            else -> DueTag.T.color
        }
    }

    private fun checkIfNumIsValid(num: Int) {
        if (num > taskList.size || num < 1) throw Exception()
    }

    private fun toJSON(tasks: ListOfTasks) {
        val jsonStr = arrayListOf<Task>().apply { tasks.taskList?.forEach { this.add(it) } }
        val taskAdapter = moshi.adapter(Array::class.java)
        val json = taskAdapter.toJson(jsonStr.toArray())
        jsonFile.writeText(json)
    }

    private fun fromJSON(file: File) {
        val jsonStr = file.readText()
        val listType = Types.newParameterizedType(List::class.java, Task::class.java)
        val taskAdapter: JsonAdapter<List<Task>> = moshi.adapter(listType)
        val newTaskList = taskAdapter.fromJson(jsonStr)
        newTaskList?.forEach { taskList.add(it) }
    }
}