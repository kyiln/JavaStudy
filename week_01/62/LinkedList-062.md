LinkedList基于jdk1.8源码分析
我觉得主要就是在节点上使用双向队列的方式，添加nexe，prev实现提高添加，以及删除的效率，是以空间换时间的方式。在redis中是以调表的方式，
对节点上下的引用都添加进来，实现快速查找，是以空间换时间的更加具体实现
private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }