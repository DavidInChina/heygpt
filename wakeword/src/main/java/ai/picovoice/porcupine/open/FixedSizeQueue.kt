package ai.picovoice.porcupine.open

import java.util.LinkedList


class FixedSizeQueue<T>(private val maxSize: Int) {
    private val queue = LinkedList<T>()

    fun add(element: T) {
        if (queue.size >= maxSize) {
            queue.removeFirst()
        }
        queue.addLast(element)
    }

    fun clear() {
        queue.clear()
    }

    fun getAll(): List<T> {
        return queue.toList()
    }

    fun getSize(): Int {
        return queue.size
    }
}