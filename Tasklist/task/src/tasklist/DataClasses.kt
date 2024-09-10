package tasklist

data class Task(
    var index: Int, var date: String, var time: String,
    var prio: String, var tasks: MutableList<String>, var due: String)

data class ListOfTasks(var taskList: MutableList<Task>?)